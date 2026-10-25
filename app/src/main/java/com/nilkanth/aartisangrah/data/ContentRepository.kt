package com.nilkanth.aartisangrah.data

import android.content.Context
import com.nilkanth.aartisangrah.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File

sealed interface ContentUpdate {
    data class Updated(val version: Int, val newItems: Int) : ContentUpdate
    data class UpToDate(val version: Int) : ContentUpdate
    data object NeedsAppUpdate : ContentUpdate
    data class Failed(val reason: String?) : ContentUpdate
}

/**
 * Keeps the aarti library. Content bundled in the APK is always available offline; a newer
 * content.json downloaded from GitHub is cached in app storage and preferred when present.
 */
class ContentRepository(private val context: Context) {
    private val cacheFile = File(context.filesDir, "content.json")
    private val _library = MutableStateFlow<Library?>(null)
    val library: StateFlow<Library?> = _library

    private val remoteUrl =
        "https://raw.githubusercontent.com/${BuildConfig.GITHUB_REPO}/main/content/content.json"

    suspend fun load() = withContext(Dispatchers.IO) {
        val bundled = LibraryParser.parse(context.assets.open("content.json").bufferedReader().use { it.readText() })
        val cached = runCatching { if (cacheFile.exists()) LibraryParser.parse(cacheFile.readText()) else null }.getOrNull()
        _library.value = listOfNotNull(bundled, cached)
            .filter { it.minAppVersion <= BuildConfig.VERSION_CODE }
            .maxByOrNull { it.version } ?: bundled
    }

    suspend fun checkForUpdate(): ContentUpdate = withContext(Dispatchers.IO) {
        try {
            val text = Http.get("$remoteUrl?t=${System.currentTimeMillis() / 60_000}")
            val remote = LibraryParser.parse(text)
            val current = _library.value
            when {
                current != null && remote.version <= current.version -> ContentUpdate.UpToDate(current.version)
                remote.minAppVersion > BuildConfig.VERSION_CODE -> ContentUpdate.NeedsAppUpdate
                else -> {
                    val tmp = File(context.filesDir, "content.json.tmp")
                    tmp.writeText(text)
                    if (!tmp.renameTo(cacheFile)) {
                        cacheFile.delete()
                        tmp.renameTo(cacheFile)
                    }
                    _library.value = remote
                    ContentUpdate.Updated(remote.version, remote.items.size - (current?.items?.size ?: 0))
                }
            }
        } catch (e: Exception) {
            ContentUpdate.Failed(e.message)
        }
    }
}
