package com.nilkanth.aartisangrah

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.nilkanth.aartisangrah.audio.Reciter
import com.nilkanth.aartisangrah.data.AppLanguage
import com.nilkanth.aartisangrah.data.Recitation
import com.nilkanth.aartisangrah.data.Library
import com.nilkanth.aartisangrah.data.LibraryParser
import com.nilkanth.aartisangrah.data.ThemeMode
import com.nilkanth.aartisangrah.ui.LocalStrings
import com.nilkanth.aartisangrah.ui.Strings
import com.nilkanth.aartisangrah.ui.screens.HomeScreen
import com.nilkanth.aartisangrah.ui.screens.ListScreen
import com.nilkanth.aartisangrah.ui.screens.ReaderScreen
import com.nilkanth.aartisangrah.ui.screens.SearchScreen
import com.nilkanth.aartisangrah.ui.screens.SettingsScreen
import com.nilkanth.aartisangrah.ui.theme.AartiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Checks the bundled content.json and renders the main screens to PNGs in docs/screenshots
 * (run: gradlew testDebugUnitTest). Uses Robolectric, so no phone or emulator is needed.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h860dp-xxhdpi")
class ContentAndScreensTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val library: Library by lazy {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LibraryParser.parse(context.assets.open("content.json").bufferedReader().use { it.readText() })
    }

    @Test
    fun bundledContentIsValid() {
        assertTrue("expected a full library", library.items.size >= 40)
        library.items.forEach { item ->
            assertTrue("${item.id} has unknown category", library.category(item.category) != null)
            assertTrue("${item.id} has unknown deity", item.deity == null || library.deity(item.deity) != null)
            assertTrue("${item.id} has a note but no English note", item.about == null || item.aboutEn != null)
        }
        library.playlists.forEach {
            assertEquals(it.itemIds.size, library.itemsOf(it).size)
            assertTrue("${it.id} has no English name", it.nameEn.isNotBlank())
        }
        library.deities.forEach { assertTrue("${it.id} has no English name", it.nameEn.isNotBlank()) }
        library.categories.forEach { assertTrue("${it.id} has no English name", it.nameEn.isNotBlank()) }
        assertTrue(library.search("ganpati").isNotEmpty())
        assertTrue(library.search("vinayak").isNotEmpty())
        assertTrue(library.search("सुखकर्ता").first().id == "sukhakarta")
        assertTrue(library.search("ramraksha").isNotEmpty())
    }

    @Test
    fun readAloudScriptMatchesTheScreen() {
        val sukhakarta = library.item("sukhakarta")!!
        val script = Recitation.script(sukhakarta)
        // 3 verses of 4 lines, each followed by the 2-line refrain.
        assertEquals(18, script.size)
        assertEquals("सुखकर्ता दुःखहर्ता वार्ता विघ्नाची", script[0].text)
        assertEquals("कंठी झळके माळ मुक्ताफळांची", script[3].text)
        assertEquals("r0", script[4].block)
        assertEquals("mr-IN", Recitation.languageTag(sukhakarta))
        assertEquals("hi-IN", Recitation.languageTag(library.item("hanuman-chalisa")!!))
        assertTrue(library.item("jay-jayaji-ganpati") != null)
        library.items.forEach { item ->
            val lines = Recitation.script(item)
            assertTrue("${item.id} has nothing to read", lines.isNotEmpty())
            lines.forEach { assertTrue("${item.id}: '${it.text}' still has marks", it.text.none { c -> c in "।॥०१२३४५६७८९" }) }
        }
    }

    /** The app only plays a recording whose fingerprint matches; scripts/make_voice.py must agree. */
    @Test
    fun naturalVoiceFingerprintsMatchTheGenerator() {
        // Reference value printed by scripts/make_voice.py's script_hash() for this aarti.
        assertEquals("fe7fec5e69178d55a50a643023f1d3a4e0a8255f", Recitation.hash(Recitation.script(library.item("sukhakarta")!!)))
        val index = File(System.getProperty("voiceIndex") ?: return)
        if (!index.exists()) return
        val items = org.json.JSONObject(index.readText()).getJSONObject("items")
        items.keys().forEach { id ->
            val item = library.item(id) ?: return@forEach
            val entry = items.getJSONObject(id)
            assertEquals("$id: recording made for different wording", Recitation.hash(Recitation.script(item)), entry.getString("hash"))
            assertEquals("$id: line timings don't match the lines", Recitation.script(item).size, entry.getJSONArray("lines").length())
        }
    }

    @Test
    fun renderScreens() {
        shot("1-home") {
            HomeScreen(library, listOf("sukhakarta"), emptyList(), null, { _, _, _ -> }, { _, _ -> }, {}, {}, {}, {})
        }
        shot("2-reader") {
            ReaderScreen(
                library, library.playlist("ganeshotsav")!!.itemIds, 0, "गणेशोत्सव आरती क्रम",
                22f, false, emptyList(), {}, {}, {}, {}, {},
            )
        }
        shot("3-deity") {
            ListScreen(library, "deity", "vitthal", emptyList(), {}, { _, _, _ -> }, {})
        }
        shot("4-search") {
            SearchScreen(library, emptyList(), false, { _, _, _ -> }, {})
        }
        shot("5-settings") {
            SettingsScreen(
                library, AppLanguage.MARATHI, ThemeMode.SYSTEM, 22f, true, null, false, false,
                {}, {}, {}, {}, {}, {}, {}, {}, {},
            )
        }
        shot("6-reader-dark", ThemeMode.DARK) {
            ReaderScreen(library, listOf("ramraksha"), 0, "", 22f, false, listOf("ramraksha"), {}, {}, {}, {}, {})
        }
        shot("7-home-dark", ThemeMode.DARK) {
            HomeScreen(library, emptyList(), listOf("pasaydan"), null, { _, _, _ -> }, { _, _ -> }, {}, {}, {}, {})
        }
        shot("8-home-english", language = AppLanguage.ENGLISH) {
            HomeScreen(library, emptyList(), emptyList(), null, { _, _, _ -> }, { _, _ -> }, {}, {}, {}, {})
        }
        shot("9-reader-english", language = AppLanguage.ENGLISH) {
            ReaderScreen(
                library, library.playlist("ganeshotsav")!!.itemIds, 0, "Ganeshotsav Aarti Order",
                22f, false, emptyList(), {}, {}, {}, {}, {},
            )
        }
        shot("10-settings-english", language = AppLanguage.ENGLISH) {
            SettingsScreen(
                library, AppLanguage.ENGLISH, ThemeMode.SYSTEM, 22f, true, null, false, false,
                {}, {}, {}, {}, {}, {}, {}, {}, {},
            )
        }
        shot("11-deity-english", language = AppLanguage.ENGLISH) {
            ListScreen(library, "deity", "ganpati", emptyList(), {}, { _, _, _ -> }, {})
        }
        shot("12-reader-listening") {
            ReaderScreen(
                library, listOf("sukhakarta"), 0, "", 22f, false, emptyList(), {}, {}, {}, {}, {},
                recite = Reciter.State(Reciter.Status.PLAYING, "sukhakarta", index = 5, total = 18),
            )
        }
    }

    /** The web app (web/) reuses the launcher icon; iPhones need it as PNG. */
    @Test
    fun renderWebIcons() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dir = File(System.getProperty("webIconDir") ?: "build/web-icons").apply { mkdirs() }
        mapOf("apple-touch-icon.png" to 180, "icon-192.png" to 192, "icon-512.png" to 512).forEach { (name, size) ->
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            listOf(R.drawable.ic_launcher_background, R.drawable.ic_launcher_foreground).forEach { id ->
                context.getDrawable(id)!!.apply { setBounds(0, 0, size, size) }.draw(canvas)
            }
            File(dir, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun shot(
        name: String,
        theme: ThemeMode = ThemeMode.LIGHT,
        language: AppLanguage = AppLanguage.MARATHI,
        content: @Composable () -> Unit,
    ) {
        compose.activity.setContent {
            AartiTheme(theme) {
                CompositionLocalProvider(LocalStrings provides Strings.of(language)) { Surface { content() } }
            }
        }
        compose.waitForIdle()
        val view = compose.activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        val dir = File(System.getProperty("screenshotDir") ?: "build/screenshots").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
