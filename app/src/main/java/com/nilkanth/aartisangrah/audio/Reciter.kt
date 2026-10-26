package com.nilkanth.aartisangrah.audio

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.nilkanth.aartisangrah.R
import com.nilkanth.aartisangrah.data.Item
import com.nilkanth.aartisangrah.data.Recitation
import com.nilkanth.aartisangrah.data.SpokenLine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

/**
 * Reads an aarti aloud line by line with the phone's own text-to-speech voice (Marathi, or
 * Hindi for Hindi texts), with a soft tanpura drone underneath. Nothing is downloaded by the app;
 * the voice comes from the phone's text-to-speech engine.
 */
class Reciter(context: Context) {
    enum class Status { IDLE, PREPARING, PLAYING, PAUSED }

    data class State(
        val status: Status = Status.IDLE,
        val itemId: String? = null,
        /** Index into [script] of the line being spoken. */
        val index: Int = -1,
        val total: Int = 0,
        /** True when no Marathi voice is installed and the Hindi one is used instead. */
        val usingHindiVoice: Boolean = false,
    )

    private val appContext = context.applicationContext
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val _finished = MutableSharedFlow<String>(extraBufferCapacity = 4)
    /** Emits an item id when it has been read to the end. */
    val finished: SharedFlow<String> = _finished

    private val _noVoice = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** Emits when the phone has no usable Marathi or Hindi voice. */
    val noVoice: SharedFlow<Unit> = _noVoice

    var slow = false
        private set
    var musicOn = true
        private set

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pending: (() -> Unit)? = null
    private var item: Item? = null
    private var script: List<SpokenLine> = emptyList()
    private var music: MediaPlayer? = null

    /** Bumped whenever the speech queue is replaced, so callbacks from an old queue are ignored. */
    @Volatile private var generation = 0

    fun play(item: Item, from: Int = 0) {
        val lines = Recitation.script(item)
        if (lines.isEmpty()) return
        val start = from.coerceIn(0, lines.lastIndex)
        this.item = item
        script = lines
        _state.update { State(Status.PREPARING, item.id, start, lines.size, it.usingHindiVoice) }
        withTts { speakFrom(start) }
    }

    fun pause() {
        if (_state.value.status != Status.PLAYING) return
        generation++
        tts?.stop()
        music?.pause()
        _state.update { it.copy(status = Status.PAUSED) }
    }

    fun resume() {
        val current = item ?: return
        play(current, _state.value.index.coerceAtLeast(0))
    }

    fun stop() {
        generation++
        tts?.stop()
        music?.takeIf { it.isPlaying }?.pause()
        item = null
        script = emptyList()
        _state.update { State(usingHindiVoice = it.usingHindiVoice) }
    }

    fun setSlow(on: Boolean) {
        slow = on
        if (_state.value.status == Status.PLAYING) speakFrom(_state.value.index.coerceAtLeast(0))
    }

    fun setMusic(on: Boolean) {
        musicOn = on
        if (_state.value.status != Status.PLAYING) return
        if (on) startMusic() else music?.pause()
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
        music?.release()
        music = null
    }

    private fun withTts(action: () -> Unit) {
        if (tts != null && ttsReady) {
            action()
            return
        }
        pending = action
        if (tts != null) return
        tts = TextToSpeech(appContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                tts?.setOnUtteranceProgressListener(listener)
                pending?.invoke()
            } else {
                _state.update { State(usingHindiVoice = it.usingHindiVoice) }
                _noVoice.tryEmit(Unit)
            }
            pending = null
        }
    }

    private fun speakFrom(from: Int) {
        val engine = tts ?: return
        val current = item ?: return
        val wanted = Recitation.languageTag(current)
        // Marathi and Hindi voices can each read the other's Devanagari, so fall back either way.
        val used = listOf(wanted, "mr-IN", "hi-IN").distinct().firstOrNull { engine.setLanguage(Locale.forLanguageTag(it)) >= 0 }
        if (used == null) {
            stop()
            _noVoice.tryEmit(Unit)
            return
        }
        engine.setSpeechRate(if (slow) SLOW_RATE else NORMAL_RATE)
        val gen = ++generation
        for (i in from..script.lastIndex) {
            engine.speak(script[i].text, if (i == from) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, "$gen:$i")
            engine.playSilentUtterance(if (slow) 700L else 450L, TextToSpeech.QUEUE_ADD, "$gen:pause")
        }
        _state.update { it.copy(status = Status.PLAYING, index = from, usingHindiVoice = wanted == "mr-IN" && used == "hi-IN") }
        startMusic()
    }

    private fun startMusic() {
        if (!musicOn) return
        val player = music ?: MediaPlayer.create(appContext, R.raw.tanpura)?.apply {
            isLooping = true
            setVolume(MUSIC_VOLUME, MUSIC_VOLUME)
        }?.also { music = it }
        if (player != null && !player.isPlaying) player.start()
    }

    private fun parse(id: String): Pair<Int, Int>? {
        val parts = id.split(':')
        val gen = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val index = parts.getOrNull(1)?.toIntOrNull() ?: return null
        return gen to index
    }

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String) {
            val (gen, index) = parse(utteranceId) ?: return
            if (gen != generation) return
            _state.update { it.copy(status = Status.PLAYING, index = index) }
        }

        override fun onDone(utteranceId: String) {
            val (gen, index) = parse(utteranceId) ?: return
            if (gen != generation || index != script.lastIndex) return
            val doneId = item?.id
            stop()
            doneId?.let { _finished.tryEmit(it) }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String) {
            val (gen, _) = parse(utteranceId) ?: return
            if (gen == generation) stop()
        }
    }

    companion object {
        const val NORMAL_RATE = 0.85f
        const val SLOW_RATE = 0.6f
        const val MUSIC_VOLUME = 0.18f
    }
}
