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
    val appName = t("देवघर", "Devghar")
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
    val shareFooter = t("— देवघर ॲप", "— Devghar app")
    fun composedBy(author: String) = t("रचना: $author", "By $author")

    // Read aloud
    val listen = t("ऐका", "Listen")
    val pause = t("थांबवा", "Pause")
    val resume = t("पुढे ऐका", "Resume")
    val stopListening = t("बंद करा", "Stop")
    val slow = t("हळू", "Slow")
    val normalSpeed = t("सामान्य", "Normal")
    val music = t("तंबोरा", "Tanpura")
    val preparingVoice = t("आवाज तयार होत आहे…", "Preparing the voice…")
    fun lineOf(i: Int, n: Int) = t("ओळ ${num(i)} / ${num(n)}", "Line $i of $n")
    val hindiVoiceNote = t("मराठी आवाज नाही — हिंदी आवाज वापरत आहे", "No Marathi voice — using the Hindi voice")
    val noVoiceTitle = t("मराठी आवाज उपलब्ध नाही", "Marathi voice not available")
    val noVoiceMessage = t(
        "फोनच्या सेटिंग्जमध्ये Text-to-speech उघडा, Google चे इंजिन निवडा आणि मराठी आवाज डाउनलोड करा. मग पुन्हा “ऐका” दाबा.",
        "Open Text-to-speech in your phone's settings, choose Google's engine and download the Marathi voice. Then tap “Listen” again.",
    )
    val openVoiceSettings = t("आवाज सेटिंग्ज उघडा", "Open voice settings")
    val cancel = t("रद्द", "Cancel")
    val downloadingVoice = t("आवाज डाउनलोड होत आहे…", "Downloading the voice…")
    val naturalVoiceLabel = t("नैसर्गिक आवाज", "Natural voice")
    val phoneVoiceLabel = t("फोनचा आवाज", "Phone voice")
    val naturalVoiceSetting = t("नैसर्गिक (AI) आवाज वापरा", "Use the natural (AI) voice")
    val naturalVoiceNote = t(
        "AI4Bharat च्या Indic Parler-TTS ने तयार केलेला स्पष्ट मराठी/हिंदी आवाज. प्रत्येक रचना पहिल्यांदा ऐकताना डाउनलोड होते आणि नंतर इंटरनेटशिवायही चालते. बंद केल्यास फोनचा आवाज वापरला जाईल.",
        "A clear Marathi/Hindi voice made with AI4Bharat's Indic Parler-TTS. Each text downloads the first time you play it, then works offline. Turn off to use the phone's voice.",
    )
    fun voiceOnPhone(done: Long, total: Long) = t(
        "फोनवर: ${mb(done)} / ${mb(total)} MB",
        "On this phone: ${mb(done)} of ${mb(total)} MB",
    )
    fun downloadAllVoices(total: Long) = t("सर्व आवाज डाउनलोड करा (${mb(total)} MB)", "Download all voices (${mb(total)} MB)")
    val allVoicesDownloaded = t("सर्व आवाज फोनवर आहेत ✓", "All voices are on this phone ✓")
    val voiceDownloadFailed = t("डाउनलोड पूर्ण झाले नाही — इंटरनेट तपासा", "Download didn't finish — check your internet")
    private fun mb(bytes: Long): String {
        val tenths = ((bytes + 50_000) / 100_000).toInt()
        val text = "${tenths / 10}.${tenths % 10}"
        return if (english) text else text.map { c -> if (c in '0'..'9') "०१२३४५६७८९"[c - '0'] else c }.joinToString("")
    }
    val listenGroup = t("ऐका", "Listen")
    val speechSpeed = t("वाचण्याचा वेग", "Reading speed")
    val musicSetting = t("सोबत तंबोरा वाजवा", "Play tanpura in the background")
    val listenNote = t(
        "“ऐका” दाबल्यावर आरती ओळ-ओळ वाचली जाते आणि चालू ओळ रंगीत दिसते — उच्चार शिकण्यासाठी. आवाज फोनच्या Text-to-speech मधून येतो (हिंदी रचनांसाठी हिंदी आवाज).",
        "“Listen” reads the text line by line and highlights the current line, to help with pronunciation. The voice is your phone's text-to-speech voice (Hindi texts use the Hindi voice).",
    )

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
    val collection = t("आरत्या व श्लोक", "Aartis & shlokas")
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
            else t("आरत्या व श्लोक अद्ययावत झाले 🌼", "Aartis & shlokas updated 🌼")
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
