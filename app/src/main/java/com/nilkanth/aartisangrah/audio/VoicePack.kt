package com.nilkanth.aartisangrah.audio

import android.content.Context
import com.nilkanth.aartisangrah.BuildConfig
import com.nilkanth.aartisangrah.data.Http
import com.nilkanth.aartisangrah.data.Item
import com.nilkanth.aartisangrah.data.Recitation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * The natural-voice recordings made by scripts/make_voice.py and published with the web app on
 * GitHub Pages: voice/index.json lists one MP3 per item with the start/end of every line.
 * Files are downloaded the first time an item is played (or all at once from Settings) and kept.
 */
class VoicePack(context: Context) {
    data class Entry(
        val itemId: String,
        val file: String,
        val hash: String,
        val bytes: Long,
        /** Start and end of each spoken line, in milliseconds, in Recitation.script order. */
        val lines: List<LongRange>,
    )

    private val dir = File(context.filesDir, "voice").apply { mkdirs() }
    private val indexFile = File(dir, "index.json")
    private val baseUrl = BuildConfig.GITHUB_REPO.split('/').let { (owner, name) -> "https://$owner.github.io/$name/voice/" }

    @Volatile private var entries: Map<String, Entry> = runCatching { parse(indexFile.readText()) }.getOrDefault(emptyMap())
    @Volatile private var refreshed = false

    val totalBytes: Long get() = entries.values.sumOf { it.bytes }
    val downloadedBytes: Long get() = entries.values.filter { isCached(it) }.sumOf { it.bytes }
    val isEmpty: Boolean get() = entries.isEmpty()

    /** Fetches the latest list (at most once per app run unless [force]). Keeps the old one offline. */
    suspend fun refresh(force: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        if (refreshed && !force) return@withContext true
        runCatching {
            val text = Http.get("${baseUrl}index.json?t=${System.currentTimeMillis() / 60_000}")
            entries = parse(text)
            indexFile.writeText(text)
            refreshed = true
        }.isSuccess
    }

    /** The recording for this item, if there is one for its current wording. */
    fun entryFor(item: Item): Entry? =
        entries[item.id]?.takeIf { it.hash == Recitation.hash(Recitation.script(item)) }

    fun isCached(entry: Entry): Boolean = File(dir, entry.file).let { it.exists() && it.length() == entry.bytes }

    /** Returns the local file, downloading it first if needed. Throws when offline. */
    suspend fun file(entry: Entry): File = withContext(Dispatchers.IO) {
        val target = File(dir, entry.file)
        if (!isCached(entry)) {
            Http.download(baseUrl + entry.file, target)
            // Drop older recordings of the same item.
            dir.listFiles { f -> f.name.startsWith("${entry.itemId}-") && f.name != entry.file }?.forEach { it.delete() }
        }
        target
    }

    /** Downloads every recording not yet on the phone. [progress] gets (bytes done, bytes total). */
    suspend fun downloadAll(progress: (Long, Long) -> Unit) {
        refresh(force = true)
        val all = entries.values.toList()
        val total = all.sumOf { it.bytes }
        var done = all.filter { isCached(it) }.sumOf { it.bytes }
        progress(done, total)
        for (entry in all) {
            if (isCached(entry)) continue
            file(entry)
            done += entry.bytes
            progress(done, total)
        }
    }

    private fun parse(json: String): Map<String, Entry> {
        val items = JSONObject(json).getJSONObject("items")
        return items.keys().asSequence().associateWith { id ->
            val o = items.getJSONObject(id)
            val lines = o.getJSONArray("lines")
            Entry(
                itemId = id,
                file = o.getString("file"),
                hash = o.getString("hash"),
                bytes = o.getLong("bytes"),
                lines = (0 until lines.length()).map { i ->
                    val pair = lines.getJSONArray(i)
                    (pair.getDouble(0) * 1000).toLong()..(pair.getDouble(1) * 1000).toLong()
                },
            )
        }
    }
}
