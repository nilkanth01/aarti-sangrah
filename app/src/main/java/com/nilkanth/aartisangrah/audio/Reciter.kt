package com.nilkanth.aartisangrah.audio

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.nilkanth.aartisangrah.R
import com.nilkanth.aartisangrah.data.Item
import com.nilkanth.aartisangrah.data.Recitation
import com.nilkanth.aartisangrah.data.SpokenLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

/**
 * Reads an aarti aloud line by line with a soft tanpura drone underneath.
 *
 * It prefers the natural AI4Bharat voice recorded by scripts/make_voice.py ([VoicePack]): the item's
 * MP3 is downloaded once and the highlight follows its line timings. When there is no recording for
 * the item's current wording, or it can't be downloaded, it uses the phone's own text-to-speech
 * voice (Marathi, or Hindi for Hindi texts).
 */
class Reciter(context: Context, private val voicePack: VoicePack) {
    enum class Status { IDLE, PREPARING, PLAYING, PAUSED }

    data class State(
        val status: Status = Status.IDLE,
        val itemId: String? = null,
        /** Index into [Recitation.script] of the line being spoken. */
        val index: Int = -1,
        val total: Int = 0,
        /** True when no Marathi voice is installed and the Hindi one is used instead. */
        val usingHindiVoice: Boolean = false,
        /** True while the natural recording is playing (rather than the phone's voice). */
        val natural: Boolean = false,
        /** True while the natural recording is being downloaded for the first time. */
        val downloading: Boolean = false,
    )

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val _finished = MutableSharedFlow<String>(extraBufferCapacity = 4)
    /** Emits an item id when it has been read to the end. */
    val finished: SharedFlow<String> = _finished

    private val _noVoice = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** Emits when the phone has no usable Marathi or Hindi voice (and no recording is available). */
    val noVoice: SharedFlow<Unit> = _noVoice

    var slow = false
        private set
    var musicOn = true
        private set
    var naturalOn = true
        private set

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pending: (() -> Unit)? = null
    private var item: Item? = null
    private var script: List<SpokenLine> = emptyList()
    private var music: MediaPlayer? = null
    private var voicePlayer: MediaPlayer? = null
    private var entry: VoicePack.Entry? = null
    private var playJob: Job? = null
    private var ticker: Job? = null

    /** Bumped whenever playback is replaced, so callbacks from an old run are ignored. */
    @Volatile private var generation = 0

    fun play(item: Item, from: Int = 0) {
        val lines = Recitation.script(item)
        if (lines.isEmpty()) return
        val start = from.coerceIn(0, lines.lastIndex)
        stopOutputs()
        val gen = ++generation
        this.item = item
        script = lines
        _state.update { State(Status.PREPARING, item.id, start, lines.size, it.usingHindiVoice) }
        playJob = scope.launch {
            val recording = if (naturalOn) recordingFor(item) else null
            if (gen != generation) return@launch
            if (recording != null && playRecording(recording.first, recording.second, start)) return@launch
            withTts { speakFrom(start) }
        }
    }

    fun pause() {
        if (_state.value.status != Status.PLAYING) return
        val player = voicePlayer
        if (player != null) {
            runCatching { player.pause() }
            ticker?.cancel()
        } else {
            generation++
            tts?.stop()
        }
        music?.pause()
        _state.update { it.copy(status = Status.PAUSED) }
    }

    fun resume() {
        val player = voicePlayer
        if (player != null) {
            applySpeed(player)
            player.start()
            startTicker()
            startMusic()
            _state.update { it.copy(status = Status.PLAYING) }
            return
        }
        val current = item ?: return
        play(current, _state.value.index.coerceAtLeast(0))
    }

    fun stop() {
        stopOutputs()
        generation++
        item = null
        script = emptyList()
        _state.update { State(usingHindiVoice = it.usingHindiVoice) }
    }

    fun setSlow(on: Boolean) {
        slow = on
        if (_state.value.status != Status.PLAYING) return
        val player = voicePlayer
        if (player != null) applySpeed(player) else speakFrom(_state.value.index.coerceAtLeast(0))
    }

    fun setMusic(on: Boolean) {
        musicOn = on
        if (_state.value.status != Status.PLAYING) return
        if (on) startMusic() else music?.pause()
    }

    /** Off = always use the phone's voice. Takes effect from the next "Listen". */
    fun setNatural(on: Boolean) {
        naturalOn = on
    }

    fun release() {
        stop()
        scope.cancel()
        tts?.shutdown()
        tts = null
        music?.release()
        music = null
    }

    // ---- Natural voice ----

    private suspend fun recordingFor(item: Item): Pair<VoicePack.Entry, File>? {
        if (voicePack.isEmpty) voicePack.refresh() else scope.launch { voicePack.refresh() }
        val found = voicePack.entryFor(item) ?: (if (voicePack.refresh(force = true)) voicePack.entryFor(item) else null)
            ?: return null
        if (!voicePack.isCached(found)) _state.update { it.copy(downloading = true) }
        val file = runCatching { voicePack.file(found) }.getOrNull()
        _state.update { it.copy(downloading = false) }
        return file?.let { found to it }
    }

    private fun playRecording(found: VoicePack.Entry, file: File, from: Int): Boolean {
        val player = runCatching {
            MediaPlayer().apply {
                setDataSource(file.path)
                prepare()
            }
        }.getOrNull() ?: return false
        val gen = generation
        voicePlayer = player
        entry = found
        player.setOnCompletionListener {
            if (gen != generation) return@setOnCompletionListener
            val doneId = item?.id
            stop()
            doneId?.let { _finished.tryEmit(it) }
        }
        player.seekTo(found.lines.getOrNull(from)?.first?.toInt() ?: 0)
        applySpeed(player)
        player.start()
        _state.update { it.copy(status = Status.PLAYING, index = from, natural = true, usingHindiVoice = false) }
        startMusic()
        startTicker()
        return true
    }

    /** Moves the highlight to the line whose time range contains the playback position. */
    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val player = voicePlayer ?: break
                val lines = entry?.lines ?: break
                val position = runCatching { player.currentPosition.toLong() }.getOrDefault(0L)
                val index = lines.indexOfLast { it.first <= position + 80 }.coerceAtLeast(0)
                if (index != _state.value.index) _state.update { it.copy(index = index) }
                delay(80)
            }
        }
    }

    private fun applySpeed(player: MediaPlayer) {
        runCatching { player.playbackParams = player.playbackParams.setSpeed(if (slow) NATURAL_SLOW else 1f) }
    }

    /** Stops whatever is sounding (recording or phone voice) without resetting the state. */
    private fun stopOutputs() {
        playJob?.cancel()
        ticker?.cancel()
        voicePlayer?.let { p -> runCatching { p.stop() }; p.release() }
        voicePlayer = null
        entry = null
        tts?.stop()
        music?.takeIf { it.isPlaying }?.pause()
    }

    // ---- Phone text-to-speech ----

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
        _state.update {
            it.copy(status = Status.PLAYING, index = from, natural = false, usingHindiVoice = wanted == "mr-IN" && used == "hi-IN")
        }
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
            scope.launch {
                stop()
                doneId?.let { _finished.tryEmit(it) }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String) {
            val (gen, _) = parse(utteranceId) ?: return
            if (gen == generation) scope.launch { stop() }
        }
    }

    companion object {
        const val NORMAL_RATE = 0.85f
        const val SLOW_RATE = 0.6f
        const val NATURAL_SLOW = 0.8f
        const val MUSIC_VOLUME = 0.18f
    }
}
