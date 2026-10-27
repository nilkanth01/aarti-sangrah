package com.nilkanth.aartisangrah.data

import java.security.MessageDigest

/** One line to be read aloud, and where it sits on the reader page (so it can be highlighted). */
data class SpokenLine(
    /** "rt" = refrain shown at the top, "v3" = verse 3, "r3" = refrain after verse 3. */
    val block: String,
    /** Line number inside that block. */
    val line: Int,
    /** The words to speak, without verse numbers and dandas. */
    val text: String,
)

/** Turns an item into the exact sequence of lines shown on screen, ready for text-to-speech. */
object Recitation {
    private val verseNumber = Regex("॥\\s*[०-९0-9]+\\s*॥")
    private val noise = Regex("[।॥'\"“”‘’()०-९0-9]")
    private val spaces = Regex("\\s+")

    fun clean(line: String): String =
        line.replace(verseNumber, " ").replace(noise, " ").replace("—", " ").replace(spaces, " ").trim()

    fun script(item: Item): List<SpokenLine> {
        val out = mutableListOf<SpokenLine>()
        fun add(block: String, text: String) =
            text.lines().forEachIndexed { i, raw ->
                val spoken = clean(raw)
                if (spoken.isNotEmpty()) out += SpokenLine(block, i, spoken)
            }
        val refrain = item.refrain
        if (refrain != null && item.refrainFirst) add("rt", refrain)
        item.verses.forEachIndexed { i, verse ->
            add("v$i", verse)
            if (refrain != null) add("r$i", refrain)
        }
        return out
    }

    /**
     * Fingerprint of what is read aloud. The natural-voice audio stores the same SHA-1, so audio made
     * for an older wording of a text is never played over the new one (scripts/make_voice.py).
     */
    fun hash(lines: List<SpokenLine>): String =
        MessageDigest.getInstance("SHA-1")
            .digest(lines.joinToString("\n") { it.text }.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    /** Hindi and Awadhi texts sound right with a Hindi voice; everything else uses Marathi. */
    fun languageTag(item: Item): String = when (item.language) {
        "हिंदी", "अवधी" -> "hi-IN"
        else -> "mr-IN"
    }
}
