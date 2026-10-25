package com.nilkanth.aartisangrah.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nilkanth.aartisangrah.data.AppRelease
import com.nilkanth.aartisangrah.data.AppUpdates
import com.nilkanth.aartisangrah.data.ContentRepository
import com.nilkanth.aartisangrah.data.ContentUpdate
import com.nilkanth.aartisangrah.data.Library
import com.nilkanth.aartisangrah.data.Marathi
import com.nilkanth.aartisangrah.data.Prefs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages

    init {
        viewModelScope.launch {
            repo.load()
            // Quietly look for new aartis and a newer APK at most every 6 hours.
            if (System.currentTimeMillis() - prefs.lastUpdateCheck > 6 * 60 * 60 * 1000L) {
                prefs.lastUpdateCheck = System.currentTimeMillis()
                val result = repo.checkForUpdate()
                if (result is ContentUpdate.Updated) _messages.emit(updatedMessage(result))
                refreshAppUpdate()
            }
        }
    }

    fun checkContentNow() {
        if (_checkingContent.value) return
        viewModelScope.launch {
            _checkingContent.value = true
            val msg = when (val r = repo.checkForUpdate()) {
                is ContentUpdate.Updated -> updatedMessage(r)
                is ContentUpdate.UpToDate -> "सर्व आरत्या अद्ययावत आहेत 🙏"
                ContentUpdate.NeedsAppUpdate -> "नवीन मजकुरासाठी कृपया ॲप अपडेट करा"
                is ContentUpdate.Failed -> "इंटरनेट जोडणी तपासा आणि पुन्हा प्रयत्न करा"
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
                    null -> "इंटरनेट जोडणी तपासा आणि पुन्हा प्रयत्न करा"
                    false -> "तुमच्याकडे नवीनतम आवृत्ती आहे 🙏"
                    true -> "नवीन आवृत्ती उपलब्ध आहे!"
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

    private fun updatedMessage(r: ContentUpdate.Updated) =
        if (r.newItems > 0) "${Marathi.num(r.newItems)} नवीन आरत्या/श्लोक जोडले गेले 🌼"
        else "आरती संग्रह अद्ययावत झाला 🌼"
}
