package com.nilkanth.aartisangrah.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.nilkanth.aartisangrah.data.AppLanguage
import com.nilkanth.aartisangrah.data.Category
import com.nilkanth.aartisangrah.data.Deity
import com.nilkanth.aartisangrah.data.Item
import com.nilkanth.aartisangrah.data.Marathi
import com.nilkanth.aartisangrah.data.Playlist
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Every menu and label of the app in Marathi and English. The aartis, shlokas and their
 * titles are content, not UI, so they always stay in Devanagari whichever language is chosen.
 */
class Strings private constructor(val language: AppLanguage) {
    val english = language == AppLanguage.ENGLISH
    private fun t(mr: String, en: String) = if (english) en else mr

    fun num(n: Int): String = if (english) n.toString() else Marathi.num(n)

    // Navigation
    val tabHome = t("मुख्य", "Home")
    val tabSearch = t("शोधा", "Search")
    val tabFavorites = t("आवडते", "Favourites")
    val tabSettings = t("सेटिंग्ज", "Settings")
    val back = t("मागे", "Back")

    // Home
    val appName = t("आरती संग्रह", "Aarti Sangrah")
    val appTagline = t("आरत्या • श्लोक • स्तोत्रे • अभंग", "Aartis • Shlokas • Stotras • Abhangs")
    val searchHint = t("आरती, श्लोक किंवा देव शोधा…", "Search aartis, shlokas or deities…")
    /** Label of the quick language switch: always names the *other* language. */
    val otherLanguage = t("English", "मराठी")
    val poojaSequences = t("पूजा क्रम", "Pooja sequences")
    val categories = t("प्रकार", "Categories")
    val deities = t("देवता", "Deities")
    val recentlyRead = t("अलीकडे वाचलेले", "Recently read")
    val start = t("सुरू करा", "Start")
    val all = t("सर्व", "All")
    val footer = t("खाते नाही • जाहिराती नाहीत • इंटरनेटशिवायही चालते", "No account • No ads • Works offline")
    val newFeatures = t("नवीन सुविधा व सुधारणा", "New features and fixes")
    val download = t("डाउनलोड", "Download")
    fun newVersionAvailable(version: String) = t("नवीन आवृत्ती $version उपलब्ध", "Version $version is available")
    fun today(date: LocalDate = LocalDate.now()) = t("आज ${Marathi.day(date)}", "Today is ${englishDay(date)}")
    fun deityOfDay(name: String) = t("आजचे दैवत: $name", "Deity of the day: $name")
    fun date(date: LocalDate = LocalDate.now()) =
        if (english) "${englishDay(date)}, ${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}"
        else Marathi.date(date)
    fun parts(n: Int) = t("${num(n)} भाग", if (n == 1) "1 part" else "$n parts")
    fun texts(n: Int) = t("${num(n)} रचना", if (n == 1) "1 text" else "$n texts")

    // Reader
    val textSize = t("अक्षरांचा आकार", "Text size")
    val share = t("शेअर करा", "Share")
    val previous = t("मागील", "Previous")
    val next = t("पुढील", "Next")
    val addFavorite = t("आवडत्यांत जोडा", "Add to favourites")
    val removeFavorite = t("आवडत्यांतून काढा", "Remove from favourites")
    val shareFooter = t("— आरती संग्रह ॲप", "— Aarti Sangrah app")
    fun composedBy(author: String) = t("रचना: $author", "By $author")

    // Search
    val searchSubtitle = t(
        "मराठी किंवा इंग्रजीत शोधा — उदा. सुखकर्ता, ganpati, ramraksha",
        "Type in English or Marathi — e.g. ganpati, ramraksha, सुखकर्ता",
    )
    val searchPlaceholder = t("आरती, श्लोक, देव किंवा ओळ…", "Aarti, shloka, deity or a line…")
    val clear = t("पुसा", "Clear")
    val searchResults = t("शोध परिणाम", "Search results")
    val noResults = t("काही सापडले नाही", "Nothing found")
    val noResultsHint = t(
        "दुसरा शब्द वापरून पहा. इंग्रजी अक्षरांतही शोधता येते — उदा. aarti, vitthal, datta.",
        "Try another word. English spellings work too — e.g. aarti, vitthal, datta.",
    )

    // Favourites
    val favoritesEmptySubtitle = t("तुमच्या आवडत्या आरत्या एका ठिकाणी", "Your favourite aartis in one place")
    fun favoritesCount(n: Int) = t("${num(n)} आवडत्या रचना", if (n == 1) "1 favourite" else "$n favourites")
    val favoritesEmptyTitle = t("अजून काही आवडते नाही", "No favourites yet")
    val favoritesEmptyMessage = t(
        "कोणतीही आरती उघडा आणि ♡ दाबा — ती येथे लगेच मिळेल.",
        "Open any aarti and tap ♡ — it will show up here.",
    )

    // Settings
    val settingsSubtitle = t("तुमच्या सोयीनुसार ॲप बदला", "Make the app your own")
    val languageGroup = t("भाषा", "Language")
    val appLanguage = t("ॲपची भाषा", "App language")
    val languageNote = t(
        "मेनू व माहिती या भाषेत दिसेल. आरत्या, श्लोक व स्तोत्रे नेहमी देवनागरीतच राहतील.",
        "Menus and notes use this language. Aartis, shlokas and stotras always stay in Devanagari.",
    )
    val appearance = t("दिसणे", "Appearance")
    val theme = t("रंगसंगती", "Theme")
    val themeAuto = t("आपोआप", "Auto")
    val themeDay = t("दिवस", "Day")
    val themeNight = t("रात्र", "Night")
    val keepScreenOn = t("वाचताना स्क्रीन चालू ठेवा", "Keep screen on while reading")
    val keepScreenOnHint = t("आरती म्हणताना स्क्रीन बंद होणार नाही", "The screen won't sleep during an aarti")
    val updates = t("अपडेट्स", "Updates")
    val collection = t("आरती संग्रह", "Aarti collection")
    fun collectionInfo(version: Int, count: Int) =
        t("आवृत्ती ${num(version)} • ${num(count)} रचना", "Version $version • $count texts")
    val checkNew = t("नवीन तपासा", "Check for new")
    val otaNote = t(
        "नवीन आरत्या इंटरनेटवरून आपोआप येतात — ॲप पुन्हा इंस्टॉल करायची गरज नाही.",
        "New aartis arrive automatically over the internet — no need to reinstall.",
    )
    val appVersion = t("ॲप आवृत्ती", "App version")
    fun getVersion(version: String) = t("v$version घ्या", "Get v$version")
    val check = t("तपासा", "Check")
    val about = t("माहिती", "About")
    val aboutText = t(
        "हे ॲप पूर्णपणे मोफत आहे — खाते नाही, जाहिराती नाहीत, तुमची कोणतीही माहिती गोळा केली जात नाही. " +
            "सर्व आरत्या फोनमध्येच साठवलेल्या आहेत, त्यामुळे इंटरनेटशिवायही ॲप चालते.",
        "This app is completely free — no account, no ads, and none of your data is collected. " +
            "All aartis are stored on the phone, so the app works without internet too.",
    )
    val reportIssue = t("चूक कळवा / नवीन आरती सुचवा", "Report a mistake / suggest an aarti")
    val sourceCode = t("स्रोत कोड (GitHub)", "Source code (GitHub)")

    fun message(m: UiMessage): String = when (m) {
        is UiMessage.ContentUpdated ->
            if (m.newItems > 0) t("${num(m.newItems)} नवीन आरत्या/श्लोक जोडले गेले 🌼", "${m.newItems} new aartis/shlokas added 🌼")
            else t("आरती संग्रह अद्ययावत झाला 🌼", "Aarti collection updated 🌼")
        UiMessage.ContentUpToDate -> t("सर्व आरत्या अद्ययावत आहेत 🙏", "All aartis are up to date 🙏")
        UiMessage.NeedsAppUpdate -> t("नवीन मजकुरासाठी कृपया ॲप अपडेट करा", "Please update the app to get the new content")
        UiMessage.Offline -> t("इंटरनेट जोडणी तपासा आणि पुन्हा प्रयत्न करा", "Check your internet connection and try again")
        UiMessage.AppUpToDate -> t("तुमच्याकडे नवीनतम आवृत्ती आहे 🙏", "You have the latest version 🙏")
        UiMessage.AppUpdateAvailable -> t("नवीन आवृत्ती उपलब्ध आहे!", "A new version is available!")
    }

    // Names that come from content.json, shown in the chosen language when available.
    fun categoryName(c: Category) = if (english) c.nameEn.ifBlank { c.name } else c.name
    fun categoryDescription(c: Category) = if (english) c.descriptionEn.ifBlank { c.description } else c.description
    fun deityName(d: Deity) = if (english) d.nameEn.ifBlank { d.name } else d.name
    fun deityShortName(d: Deity) = if (english) deityName(d) else d.name.removePrefix("श्री ").removePrefix("सद्गुरु ")
    fun playlistName(p: Playlist) = if (english) p.nameEn.ifBlank { p.name } else p.name
    fun playlistDescription(p: Playlist) = if (english) p.descriptionEn.ifBlank { p.description } else p.description
    fun about(item: Item): String? = if (english) item.aboutEn ?: item.about else item.about
    fun languageName(language: String?): String? = if (!english) language else when (language) {
        "मराठी" -> "Marathi"
        "संस्कृत" -> "Sanskrit"
        "हिंदी" -> "Hindi"
        "अवधी" -> "Awadhi"
        else -> language
    }

    private fun englishDay(date: LocalDate) = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

    companion object {
        private val marathiStrings = Strings(AppLanguage.MARATHI)
        private val englishStrings = Strings(AppLanguage.ENGLISH)
        fun of(language: AppLanguage) = if (language == AppLanguage.ENGLISH) englishStrings else marathiStrings
    }
}

val LocalStrings = staticCompositionLocalOf { Strings.of(AppLanguage.MARATHI) }
