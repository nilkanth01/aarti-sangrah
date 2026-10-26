package com.nilkanth.aartisangrah.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Language of the app's menus and labels. Aartis and shlokas always stay in Devanagari. */
enum class AppLanguage { MARATHI, ENGLISH }

/** Small on-device settings store. Nothing here ever leaves the phone. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("aarti_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(
        runCatching { ThemeMode.valueOf(sp.getString("theme", null) ?: "SYSTEM") }.getOrDefault(ThemeMode.SYSTEM),
    )
    val theme: StateFlow<ThemeMode> = _theme

    private val _language = MutableStateFlow(
        runCatching { AppLanguage.valueOf(sp.getString("language", null) ?: "MARATHI") }.getOrDefault(AppLanguage.MARATHI),
    )
    val language: StateFlow<AppLanguage> = _language

    private val _fontSize = MutableStateFlow(sp.getFloat("fontSize", DEFAULT_FONT_SIZE))
    val fontSize: StateFlow<Float> = _fontSize

    private val _keepScreenOn = MutableStateFlow(sp.getBoolean("keepScreenOn", true))
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn

    private val _speechSlow = MutableStateFlow(sp.getBoolean("speechSlow", false))
    val speechSlow: StateFlow<Boolean> = _speechSlow

    private val _music = MutableStateFlow(sp.getBoolean("music", true))
    val music: StateFlow<Boolean> = _music

    private val _favorites = MutableStateFlow(sp.getString("favorites", "").orEmpty().toIdList())
    val favorites: StateFlow<List<String>> = _favorites

    private val _recents = MutableStateFlow(sp.getString("recents", "").orEmpty().toIdList())
    val recents: StateFlow<List<String>> = _recents

    var lastUpdateCheck: Long
        get() = sp.getLong("lastUpdateCheck", 0L)
        set(value) = sp.edit().putLong("lastUpdateCheck", value).apply()

    fun setTheme(mode: ThemeMode) {
        _theme.value = mode
        sp.edit().putString("theme", mode.name).apply()
    }

    fun setLanguage(language: AppLanguage) {
        _language.value = language
        sp.edit().putString("language", language.name).apply()
    }

    fun toggleLanguage() =
        setLanguage(if (_language.value == AppLanguage.MARATHI) AppLanguage.ENGLISH else AppLanguage.MARATHI)

    fun setFontSize(size: Float) {
        val clamped = size.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        _fontSize.value = clamped
        sp.edit().putFloat("fontSize", clamped).apply()
    }

    fun setKeepScreenOn(on: Boolean) {
        _keepScreenOn.value = on
        sp.edit().putBoolean("keepScreenOn", on).apply()
    }

    fun setSpeechSlow(on: Boolean) {
        _speechSlow.value = on
        sp.edit().putBoolean("speechSlow", on).apply()
    }

    fun setMusic(on: Boolean) {
        _music.value = on
        sp.edit().putBoolean("music", on).apply()
    }

    fun toggleFavorite(id: String) {
        val list = _favorites.value
        val updated = if (id in list) list - id else listOf(id) + list
        _favorites.value = updated
        sp.edit().putString("favorites", updated.joinToString(",")).apply()
    }

    fun markRead(id: String) {
        val updated = (listOf(id) + _recents.value.filter { it != id }).take(8)
        if (updated == _recents.value) return
        _recents.value = updated
        sp.edit().putString("recents", updated.joinToString(",")).apply()
    }

    private fun String.toIdList() = split(',').filter { it.isNotBlank() }

    companion object {
        const val DEFAULT_FONT_SIZE = 22f
        const val MIN_FONT_SIZE = 16f
        const val MAX_FONT_SIZE = 40f
    }
}
