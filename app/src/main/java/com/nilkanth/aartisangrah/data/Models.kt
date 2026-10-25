package com.nilkanth.aartisangrah.data

import org.json.JSONArray
import org.json.JSONObject

data class Category(
    val id: String,
    val name: String,
    val nameEn: String,
    val description: String,
    val descriptionEn: String,
)

data class Deity(
    val id: String,
    val name: String,
    val nameEn: String,
    val letter: String,
    val color: Long,
    /** Extra English search words, e.g. "Ganesh Vinayak" for Ganpati. */
    val keywords: String,
)

/** An ordered set of items read one after another, e.g. the Ganeshotsav aarti order. */
data class Playlist(
    val id: String,
    val name: String,
    val nameEn: String,
    val description: String,
    val descriptionEn: String,
    val itemIds: List<String>,
)

data class Item(
    val id: String,
    val title: String,
    val titleEn: String,
    val category: String,
    val deity: String?,
    val author: String?,
    val language: String?,
    val about: String?,
    val aboutEn: String?,
    /** The chorus (ध्रुपद) sung after every verse. */
    val refrain: String?,
    /** Show the refrain once before the first verse as well. */
    val refrainFirst: Boolean,
    val verses: List<String>,
    val keywords: String,
) {
    /** A line for list previews that doesn't just repeat the title. */
    val preview: String by lazy {
        val lines = (listOfNotNull(refrain.takeIf { refrainFirst }) + verses)
            .flatMap { it.lines() }
            .map { line -> line.trim { it in "।॥ ०१२३४५६७८९" } }
            .filter { it.isNotBlank() }
        val key = title.take(4)
        lines.firstOrNull { !it.startsWith(key) } ?: lines.first()
    }
}

class Library(
    val version: Int,
    val minAppVersion: Int,
    val updated: String,
    val categories: List<Category>,
    val deities: List<Deity>,
    val playlists: List<Playlist>,
    val items: List<Item>,
) {
    private val itemsById = items.associateBy { it.id }
    private val deitiesById = deities.associateBy { it.id }
    private val categoriesById = categories.associateBy { it.id }

    /** Lower-cased text of everything searchable in an item, built once. */
    private val searchText: Map<String, String> by lazy {
        items.associate { item ->
            item.id to buildString {
                append(item.title).append(' ').append(item.titleEn).append(' ').append(item.keywords).append(' ')
                item.author?.let { append(it).append(' ') }
                deity(item.deity)?.let {
                    append(it.name).append(' ').append(it.nameEn).append(' ').append(it.keywords).append(' ')
                }
                category(item.category)?.let { append(it.name).append(' ').append(it.nameEn).append(' ') }
                item.refrain?.let { append(it).append(' ') }
                item.verses.forEach { append(it).append(' ') }
            }.lowercase()
        }
    }

    fun item(id: String): Item? = itemsById[id]
    fun deity(id: String?): Deity? = id?.let { deitiesById[it] }
    fun category(id: String?): Category? = id?.let { categoriesById[it] }
    fun playlist(id: String): Playlist? = playlists.firstOrNull { it.id == id }
    fun itemsInCategory(id: String) = items.filter { it.category == id }
    fun itemsOfDeity(id: String) = items.filter { it.deity == id }
    fun itemsOf(playlist: Playlist) = playlist.itemIds.mapNotNull { item(it) }

    fun search(query: String, categoryId: String? = null): List<Item> {
        val tokens = query.lowercase().split(' ', ',', '.').map { it.trim() }.filter { it.isNotEmpty() }
        return items.filter { item ->
            (categoryId == null || item.category == categoryId) &&
                tokens.all { token -> searchText.getValue(item.id).contains(token) }
        }
    }
}

/** Parses and validates content.json. Throws if the file is unusable, so a broken
 *  remote update can never replace good content on the phone. */
object LibraryParser {
    fun parse(json: String): Library {
        val root = JSONObject(json)
        val categories = root.getJSONArray("categories").objects().map {
            Category(
                id = it.getString("id"),
                name = it.getString("name"),
                nameEn = it.optString("nameEn"),
                description = it.optString("description"),
                descriptionEn = it.optString("descriptionEn"),
            )
        }
        val deities = root.getJSONArray("deities").objects().map {
            Deity(
                id = it.getString("id"),
                name = it.getString("name"),
                nameEn = it.optString("nameEn"),
                letter = it.optString("letter").ifBlank { it.getString("name").take(1) },
                color = parseColor(it.optString("color", "#8C5A00")),
                keywords = it.optString("keywords"),
            )
        }
        val items = root.getJSONArray("items").objects().map {
            Item(
                id = it.getString("id"),
                title = it.getString("title"),
                titleEn = it.optString("titleEn"),
                category = it.getString("category"),
                deity = it.optStringOrNull("deity"),
                author = it.optStringOrNull("author"),
                language = it.optStringOrNull("language"),
                about = it.optStringOrNull("about"),
                aboutEn = it.optStringOrNull("aboutEn"),
                refrain = it.optStringOrNull("refrain"),
                refrainFirst = it.optBoolean("refrainFirst", false),
                verses = it.getJSONArray("verses").strings().filter { v -> v.isNotBlank() },
                keywords = it.optString("keywords"),
            )
        }
        require(items.isNotEmpty()) { "content has no items" }
        val ids = HashSet<String>()
        items.forEach { item ->
            require(ids.add(item.id)) { "duplicate item id ${item.id}" }
            require(item.verses.isNotEmpty()) { "item ${item.id} has no verses" }
        }
        val known = ids
        val playlists = root.optJSONArray("playlists")?.objects()?.map {
            Playlist(
                id = it.getString("id"),
                name = it.getString("name"),
                nameEn = it.optString("nameEn"),
                description = it.optString("description"),
                descriptionEn = it.optString("descriptionEn"),
                itemIds = it.getJSONArray("items").strings().filter { id -> id in known },
            )
        }.orEmpty().filter { it.itemIds.isNotEmpty() }

        return Library(
            version = root.getInt("version"),
            minAppVersion = root.optInt("minAppVersion", 1),
            updated = root.optString("updated"),
            categories = categories,
            deities = deities,
            playlists = playlists,
            items = items,
        )
    }

    private fun parseColor(hex: String): Long {
        val clean = hex.removePrefix("#")
        val argb = if (clean.length == 6) "FF$clean" else clean
        return argb.toLong(16)
    }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
    private fun JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }
    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
}
