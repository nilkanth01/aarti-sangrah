@file:OptIn(ExperimentalMaterial3Api::class)

package com.nilkanth.aartisangrah.ui.screens

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilkanth.aartisangrah.data.Item
import com.nilkanth.aartisangrah.data.Library
import com.nilkanth.aartisangrah.data.Marathi
import com.nilkanth.aartisangrah.data.Prefs
import com.nilkanth.aartisangrah.ui.components.DeityAvatar
import com.nilkanth.aartisangrah.ui.components.FavoriteButton
import com.nilkanth.aartisangrah.ui.components.Ornament
import com.nilkanth.aartisangrah.ui.components.deityTextColor
import com.nilkanth.aartisangrah.ui.theme.verseStyle
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

    LaunchedEffect(current.id) { onMarkRead(current.id) }
    KeepScreenOn(keepScreenOn)

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
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "मागे") }
                },
                actions = {
                    IconButton(onClick = { showFontSheet = true }) {
                        Icon(Icons.Rounded.FormatSize, contentDescription = "अक्षरांचा आकार")
                    }
                    FavoriteButton(current.id in favorites, { onToggleFavorite(current.id) }, MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = { onShare(current) }) {
                        Icon(Icons.Rounded.Share, contentDescription = "शेअर करा")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            if (items.size > 1) {
                PagerBar(
                    page = pager.currentPage,
                    count = items.size,
                    onPrev = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
                    onNext = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                )
            }
        },
    ) { padding ->
        HorizontalPager(
            state = pager,
            modifier = Modifier.padding(padding).fillMaxSize(),
            beyondViewportPageCount = 1,
            key = { items[it].id },
        ) { page ->
            ItemPage(items[page], library, fontSize)
        }
    }

    if (showFontSheet) {
        FontSizeSheet(fontSize, onFontSize) { showFontSheet = false }
    }
}

@Composable
private fun ItemPage(item: Item, library: Library, fontSize: Float) {
    val deity = library.deity(item.deity)
    val category = library.category(item.category)
    val style = verseStyle(fontSize)
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
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
                Text(
                    listOfNotNull(deity?.name, category?.name, item.language).joinToString(" • "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                item.author?.let {
                    Text("रचना: $it", style = MaterialTheme.typography.titleSmall, color = deityTextColor(deity), textAlign = TextAlign.Center)
                }
                Ornament(Modifier.padding(top = 14.dp))
            }
        }
        item.about?.let { about ->
            item(key = "about") {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                ) {
                    Row(Modifier.padding(14.dp)) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(10.dp))
                        Text(about, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
        val refrain = item.refrain
        if (refrain != null && item.refrainFirst) item(key = "refrain-top") { RefrainBlock(refrain, style) }
        itemsIndexed(item.verses, key = { i, _ -> "verse-$i" }) { _, verse ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    verse,
                    style = style,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                )
                if (refrain != null) RefrainBlock(refrain, style)
            }
        }
        item(key = "end") { Ornament(Modifier.padding(top = 22.dp)) }
    }
}

@Composable
private fun RefrainBlock(text: String, style: TextStyle) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Text(
            text,
            style = style.copy(fontSize = style.fontSize * 0.92f, lineHeight = style.lineHeight * 0.92f),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun PagerBar(page: Int, count: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            Modifier.navigationBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onPrev, enabled = page > 0) {
                Icon(Icons.AutoMirrored.Rounded.NavigateBefore, contentDescription = null)
                Text("मागील")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${Marathi.num(page + 1)} / ${Marathi.num(count)}", style = MaterialTheme.typography.titleSmall)
                LinearProgressIndicator(
                    progress = { (page + 1f) / count },
                    modifier = Modifier.width(88.dp).padding(top = 4.dp),
                )
            }
            TextButton(onClick = onNext, enabled = page < count - 1) {
                Text("पुढील")
                Icon(Icons.AutoMirrored.Rounded.NavigateNext, contentDescription = null)
            }
        }
    }
}

@Composable
private fun FontSizeSheet(size: Float, onChange: (Float) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
            Text("अक्षरांचा आकार", style = MaterialTheme.typography.titleLarge)
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
