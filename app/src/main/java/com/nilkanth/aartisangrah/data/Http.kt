package com.nilkanth.aartisangrah.data

import com.nilkanth.aartisangrah.BuildConfig
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
}
