package com.nilkanth.aartisangrah.data

import com.nilkanth.aartisangrah.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class AppRelease(
    val versionName: String,
    val apkUrl: String?,
    val pageUrl: String,
    val notes: String,
)

/** Looks at the latest GitHub release to tell the user when a newer APK exists. */
object AppUpdates {
    val repoUrl = "https://github.com/${BuildConfig.GITHUB_REPO}"
    val releasesUrl = "$repoUrl/releases/latest"
    val issuesUrl = "$repoUrl/issues/new"

    suspend fun latest(): AppRelease? = withContext(Dispatchers.IO) {
        runCatching {
            val json = JSONObject(
                Http.get(
                    "https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases/latest",
                    accept = "application/vnd.github+json",
                ),
            )
            val assets = json.optJSONArray("assets")
            val apk = (0 until (assets?.length() ?: 0))
                .map { assets!!.getJSONObject(it) }
                .firstOrNull { it.optString("name").endsWith(".apk") }
            AppRelease(
                versionName = json.getString("tag_name").removePrefix("v"),
                apkUrl = apk?.optString("browser_download_url"),
                pageUrl = json.optString("html_url", releasesUrl),
                notes = json.optString("body"),
            )
        }.getOrNull()
    }

    fun isNewer(remote: String, local: String = BuildConfig.VERSION_NAME): Boolean {
        fun parts(v: String) = v.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        val r = parts(remote)
        val l = parts(local)
        for (i in 0 until maxOf(r.size, l.size)) {
            val a = r.getOrElse(i) { 0 }
            val b = l.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }
}
