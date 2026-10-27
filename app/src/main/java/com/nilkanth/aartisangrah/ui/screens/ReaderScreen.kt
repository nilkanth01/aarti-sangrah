@file:OptIn(ExperimentalMaterial3Api::class)

package com.nilkanth.aartisangrah.ui.screens

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilkanth.aartisangrah.audio.Reciter
import com.nilkanth.aartisangrah.data.Item
import com.nilkanth.aartisangrah.data.Library
import com.nilkanth.aartisangrah.data.Prefs
import com.nilkanth.aartisangrah.data.Recitation
import com.nilkanth.aartisangrah.data.SpokenLine
import com.nilkanth.aartisangrah.ui.LocalStrings
import com.nilkanth.aartisangrah.ui.components.DeityAvatar
import com.nilkanth.aartisangrah.ui.components.FavoriteButton
import com.nilkanth.aartisangrah.ui.components.Ornament
import com.nilkanth.aartisangrah.ui.components.deityTextColor
import com.nilkanth.aartisangrah.ui.theme.verseStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@Composable
fun ReaderScreen(
    library: Library,
    ids: List<String>,
    startIndex: Int,
    sourceTitle: String,
    fontSize: Float,
    keepScreenOn: Boolean,
    favorites: List<String>,
    onBack: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onFontSize: (Float) -> Unit,
    onMarkRead: (String) -> Unit,
    onShare: (Item) -> Unit,
    recite: Reciter.State = Reciter.State(),
    speechSlow: Boolean = false,
    musicOn: Boolean = true,
    reciteFinished: Flow<String> = emptyFlow(),
    noVoice: Flow<Unit> = emptyFlow(),
    onListen: (Item) -> Unit = {},
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onStopListening: () -> Unit = {},
    onSpeechSlow: (Boolean) -> Unit = {},
    onMusic: (Boolean) -> Unit = {},
) {
    val items = remember(ids, library) { ids.mapNotNull { library.item(it) } }
    if (items.isEmpty()) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val pager = rememberPagerState(initialPage = startIndex.coerceIn(0, items.lastIndex)) { items.size }
    val current = items[pager.currentPage.coerceIn(0, items.lastIndex)]
    val scope = rememberCoroutineScope()
    var showFontSheet by remember { mutableStateOf(false) }
    var showNoVoice by remember { mutableStateOf(false) }
    val strings = LocalStrings.current
    val latestRecite by rememberUpdatedState(recite)
    val active = recite.itemId == current.id && recite.status != Reciter.Status.IDLE

    LaunchedEffect(current.id) { onMarkRead(current.id) }
    KeepScreenOn(keepScreenOn)

    // Swiping to another aarti while listening carries on with that one; while paused, it stops.
    LaunchedEffect(current.id) {
        val r = latestRecite
        if (r.itemId != null && r.itemId != current.id) {
            if (r.status == Reciter.Status.PLAYING || r.status == Reciter.Status.PREPARING) onListen(current) else onStopListening()
        }
    }
    // In a pooja sequence, move on to the next aarti when one finishes.
    LaunchedEffect(Unit) {
        reciteFinished.collect { id ->
            val page = pager.currentPage
            if (items.getOrNull(page)?.id == id && page < items.lastIndex) {
                pager.animateScrollToPage(page + 1)
                onListen(items[page + 1])
            }
        }
    }
    LaunchedEffect(Unit) { noVoice.collect { showNoVoice = true } }
    DisposableEffect(Unit) { onDispose { onStopListening() } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(current.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (items.size > 1) {
                            Text(
                                sourceTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back) }
                },
                actions = {
                    IconButton(onClick = { showFontSheet = true }) {
                        Icon(Icons.Rounded.FormatSize, contentDescription = strings.textSize)
                    }
                    FavoriteButton(current.id in favorites, { onToggleFavorite(current.id) }, MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = { onShare(current) }) {
                        Icon(Icons.Rounded.Share, contentDescription = strings.share)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            if (!active) {
                ExtendedFloatingActionButton(
                    onClick = { onListen(current) },
                    icon = { Icon(Icons.Rounded.Headphones, contentDescription = null) },
                    text = { Text(strings.listen) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        bottomBar = {
            Column {
                if (active) {
                    PlayerBar(
                        recite = recite,
                        slow = speechSlow,
                        musicOn = musicOn,
                        onPlayPause = { if (recite.status == Reciter.Status.PAUSED) onResume() else onPause() },
                        onSlow = { onSpeechSlow(!speechSlow) },
                        onMusic = { onMusic(!musicOn) },
                        onStop = onStopListening,
                        withNavPadding = items.size <= 1,
                    )
                }
                if (items.size > 1) {
                    PagerBar(
                        page = pager.currentPage,
                        count = items.size,
                        onPrev = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
                        onNext = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    )
                }
            }
        },
    ) { padding ->
        HorizontalPager(
            state = pager,
            modifier = Modifier.padding(padding).fillMaxSize(),
            beyondViewportPageCount = 1,
            key = { items[it].id },
        ) { page ->
            val item = items[page]
            val script = remember(item) { Recitation.script(item) }
            val highlight = if (recite.itemId == item.id && recite.status != Reciter.Status.IDLE) script.getOrNull(recite.index) else null
            ItemPage(item, library, fontSize, highlight)
        }
    }

    if (showFontSheet) {
        FontSizeSheet(fontSize, onFontSize) { showFontSheet = false }
    }
    if (showNoVoice) NoVoiceDialog { showNoVoice = false }
}

@Composable
private fun ItemPage(item: Item, library: Library, fontSize: Float, highlight: SpokenLine?) {
    val strings = LocalStrings.current
    val deity = library.deity(item.deity)
    val category = library.category(item.category)
    val style = verseStyle(fontSize)
    val about = strings.about(item)
    val refrain = item.refrain
    val listState = rememberLazyListState()

    // Keep the line being read in view.
    val firstVerseIndex = 1 + (if (about != null) 1 else 0) + (if (refrain != null && item.refrainFirst) 1 else 0)
    LaunchedEffect(highlight?.block) {
        val block = highlight?.block ?: return@LaunchedEffect
        val index = if (block == "rt") firstVerseIndex - 1 else firstVerseIndex + block.drop(1).toInt()
        listState.animateScrollToItem(index)
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item(key = "header") {
            Column(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                DeityAvatar(deity, 60.dp)
                Spacer(Modifier.height(12.dp))
                Text(
                    item.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                if (strings.english && item.titleEn.isNotBlank()) {
                    Text(
                        item.titleEn,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    listOfNotNull(
                        deity?.let { strings.deityName(it) },
                        category?.let { strings.categoryName(it) },
                        strings.languageName(item.language),
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                item.author?.let {
                    Text(
                        strings.composedBy(it),
                        style = MaterialTheme.typography.titleSmall,
                        color = deityTextColor(deity),
                        textAlign = TextAlign.Center,
                    )
                }
                Ornament(Modifier.padding(top = 14.dp))
            }
        }
        about?.let {
            item(key = "about") {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                ) {
                    Row(Modifier.padding(14.dp)) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(10.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
        if (refrain != null && item.refrainFirst) item(key = "refrain-top") { RefrainBlock(refrain, style, "rt", highlight) }
        itemsIndexed(item.verses, key = { i, _ -> "verse-$i" }) { i, verse ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinesText(
                    text = verse,
                    block = "v$i",
                    highlight = highlight,
                    style = style,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                )
                if (refrain != null) RefrainBlock(refrain, style, "r$i", highlight)
            }
        }
        item(key = "end") { Ornament(Modifier.padding(top = 22.dp)) }
    }
}

/** Shows a block of text and colours the line that is being read aloud. */
@Composable
private fun LinesText(text: String, block: String, highlight: SpokenLine?, style: TextStyle, color: Color, modifier: Modifier) {
    val activeColor = MaterialTheme.colorScheme.primary
    val activeBackground = MaterialTheme.colorScheme.secondaryContainer
    val activeLine = if (highlight?.block == block) highlight.line else -1
    val annotated = remember(text, activeLine, activeColor, activeBackground) {
        buildAnnotatedString {
            text.lines().forEachIndexed { i, line ->
                if (i > 0) append('\n')
                if (i == activeLine) withStyle(SpanStyle(color = activeColor, background = activeBackground)) { append(line) }
                else append(line)
            }
        }
    }
    Text(annotated, style = style, color = color, textAlign = TextAlign.Center, modifier = modifier)
}

@Composable
private fun RefrainBlock(text: String, style: TextStyle, block: String, highlight: SpokenLine?) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        LinesText(
            text = text,
            block = block,
            highlight = highlight,
            style = style.copy(fontSize = style.fontSize * 0.92f, lineHeight = style.lineHeight * 0.92f),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun PlayerBar(
    recite: Reciter.State,
    slow: Boolean,
    musicOn: Boolean,
    onPlayPause: () -> Unit,
    onSlow: () -> Unit,
    onMusic: () -> Unit,
    onStop: () -> Unit,
    withNavPadding: Boolean,
) {
    val strings = LocalStrings.current
    val playing = recite.status == Reciter.Status.PLAYING || recite.status == Reciter.Status.PREPARING
    Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
        Column(if (withNavPadding) Modifier.navigationBarsPadding() else Modifier) {
            LinearProgressIndicator(
                progress = { if (recite.total == 0) 0f else (recite.index + 1f) / recite.total },
                modifier = Modifier.fillMaxWidth().height(3.dp),
            )
            Row(
                Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilledIconButton(onClick = onPlayPause) {
                    Icon(
                        if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playing) strings.pause else strings.resume,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        when {
                            recite.downloading -> strings.downloadingVoice
                            recite.status == Reciter.Status.PREPARING -> strings.preparingVoice
                            else -> strings.lineOf(recite.index + 1, recite.total)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    if (recite.status != Reciter.Status.PREPARING) {
                        Text(
                            if (recite.usingHindiVoice) strings.hindiVoiceNote
                            else if (recite.natural) strings.naturalVoiceLabel else strings.phoneVoiceLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                FilterChip(selected = slow, onClick = onSlow, label = { Text(strings.slow) })
                IconToggleButton(checked = musicOn, onCheckedChange = { onMusic() }) {
                    Icon(
                        if (musicOn) Icons.Rounded.MusicNote else Icons.Rounded.MusicOff,
                        contentDescription = strings.music,
                        tint = if (musicOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    )
                }
                IconButton(onClick = onStop) { Icon(Icons.Rounded.Close, contentDescription = strings.stopListening) }
            }
        }
    }
}

@Composable
private fun NoVoiceDialog(onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.noVoiceTitle) },
        text = { Text(strings.noVoiceMessage) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                openVoiceSettings(context)
            }) { Text(strings.openVoiceSettings) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
    )
}

/** Opens the phone's text-to-speech settings, where the Marathi voice can be downloaded. */
fun openVoiceSettings(context: android.content.Context) {
    val intents = listOf(
        Intent("com.android.settings.TTS_SETTINGS"),
        Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA),
    )
    for (intent in intents) {
        if (runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess) return
    }
}

@Composable
private fun PagerBar(page: Int, count: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    val strings = LocalStrings.current
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            Modifier.navigationBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onPrev, enabled = page > 0) {
                Icon(Icons.AutoMirrored.Rounded.NavigateBefore, contentDescription = null)
                Text(strings.previous)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${strings.num(page + 1)} / ${strings.num(count)}", style = MaterialTheme.typography.titleSmall)
                LinearProgressIndicator(
                    progress = { (page + 1f) / count },
                    modifier = Modifier.width(88.dp).padding(top = 4.dp),
                )
            }
            TextButton(onClick = onNext, enabled = page < count - 1) {
                Text(strings.next)
                Icon(Icons.AutoMirrored.Rounded.NavigateNext, contentDescription = null)
            }
        }
    }
}

@Composable
private fun FontSizeSheet(size: Float, onChange: (Float) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
            Text(LocalStrings.current.textSize, style = MaterialTheme.typography.titleLarge)
            Text(
                "सुखकर्ता दुःखहर्ता",
                style = verseStyle(size),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = { onChange(size - 2) }) { Text("अ", fontSize = 14.sp) }
                Slider(
                    value = size,
                    onValueChange = onChange,
                    valueRange = Prefs.MIN_FONT_SIZE..Prefs.MAX_FONT_SIZE,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                )
                FilledTonalIconButton(onClick = { onChange(size + 2) }) { Text("अ", fontSize = 22.sp) }
            }
        }
    }
}

/** Stops the screen from sleeping while an aarti is open (during the actual aarti!). */
@Composable
private fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}
