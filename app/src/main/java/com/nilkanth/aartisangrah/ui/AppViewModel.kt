package com.nilkanth.aartisangrah.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nilkanth.aartisangrah.data.AppRelease
import com.nilkanth.aartisangrah.data.AppUpdates
import com.nilkanth.aartisangrah.data.ContentRepository
import com.nilkanth.aartisangrah.data.ContentUpdate
import com.nilkanth.aartisangrah.data.Library
import com.nilkanth.aartisangrah.data.Prefs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Short notices shown as snackbars; the UI turns them into text in the chosen language. */
sealed interface UiMessage {
    data class ContentUpdated(val newItems: Int) : UiMessage
    data object ContentUpToDate : UiMessage
    data object NeedsAppUpdate : UiMessage
    data object Offline : UiMessage
    data object AppUpToDate : UiMessage
    data object AppUpdateAvailable : UiMessage
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ContentRepository(app)
    val prefs = Prefs(app)
    val library: StateFlow<Library?> = repo.library

    private val _appUpdate = MutableStateFlow<AppRelease?>(null)
    val appUpdate: StateFlow<AppRelease?> = _appUpdate

    private val _checkingContent = MutableStateFlow(false)
    val checkingContent: StateFlow<Boolean> = _checkingContent

    private val _checkingApp = MutableStateFlow(false)
    val checkingApp: StateFlow<Boolean> = _checkingApp

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 4)
    val messages: SharedFlow<UiMessage> = _messages

    init {
        viewModelScope.launch {
            repo.load()
            // Quietly look for new aartis and a newer APK at most every 6 hours.
            if (System.currentTimeMillis() - prefs.lastUpdateCheck > 6 * 60 * 60 * 1000L) {
                prefs.lastUpdateCheck = System.currentTimeMillis()
                val result = repo.checkForUpdate()
                if (result is ContentUpdate.Updated) _messages.emit(UiMessage.ContentUpdated(result.newItems))
                refreshAppUpdate()
            }
        }
    }

    fun checkContentNow() {
        if (_checkingContent.value) return
        viewModelScope.launch {
            _checkingContent.value = true
            val msg = when (val r = repo.checkForUpdate()) {
                is ContentUpdate.Updated -> UiMessage.ContentUpdated(r.newItems)
                is ContentUpdate.UpToDate -> UiMessage.ContentUpToDate
                ContentUpdate.NeedsAppUpdate -> UiMessage.NeedsAppUpdate
                is ContentUpdate.Failed -> UiMessage.Offline
            }
            _checkingContent.value = false
            _messages.emit(msg)
        }
    }

    fun checkAppNow() {
        if (_checkingApp.value) return
        viewModelScope.launch {
            _checkingApp.value = true
            val found = refreshAppUpdate()
            _checkingApp.value = false
            _messages.emit(
                when (found) {
                    null -> UiMessage.Offline
                    false -> UiMessage.AppUpToDate
                    true -> UiMessage.AppUpdateAvailable
                },
            )
        }
    }

    /** @return true if a newer APK exists, false if not, null if GitHub couldn't be reached. */
    private suspend fun refreshAppUpdate(): Boolean? {
        val latest = AppUpdates.latest() ?: return null
        val newer = AppUpdates.isNewer(latest.versionName)
        _appUpdate.value = latest.takeIf { newer }
        return newer
    }
}
