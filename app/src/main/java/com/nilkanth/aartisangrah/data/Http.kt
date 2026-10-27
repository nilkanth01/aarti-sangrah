package com.nilkanth.aartisangrah.data

import com.nilkanth.aartisangrah.BuildConfig
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

internal object Http {
    fun get(url: String, accept: String = "application/json"): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 15_000
            conn.setRequestProperty("Accept", accept)
            conn.setRequestProperty("User-Agent", "AartiSangrah/${BuildConfig.VERSION_NAME} (Android)")
            val code = conn.responseCode
            if (code !in 200..299) error("HTTP $code")
            return conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    /** Downloads to [target] via a temporary file, so a cut-off download never looks complete. */
    fun download(url: String, target: File) {
        val conn = URL(url).openConnection() as HttpURLConnection
        val tmp = File(target.parentFile, target.name + ".part")
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 30_000
            conn.setRequestProperty("User-Agent", "AartiSangrah/${BuildConfig.VERSION_NAME} (Android)")
            val code = conn.responseCode
            if (code !in 200..299) error("HTTP $code")
            conn.inputStream.use { input -> tmp.outputStream().use { input.copyTo(it) } }
            if (!tmp.renameTo(target)) {
                target.delete()
                if (!tmp.renameTo(target)) error("could not save ${target.name}")
            }
        } finally {
            tmp.delete()
            conn.disconnect()
        }
    }
}
