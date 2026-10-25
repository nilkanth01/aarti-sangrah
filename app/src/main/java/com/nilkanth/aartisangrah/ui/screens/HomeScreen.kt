package com.nilkanth.aartisangrah.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nilkanth.aartisangrah.R
import com.nilkanth.aartisangrah.data.AppRelease
import com.nilkanth.aartisangrah.data.Library
import com.nilkanth.aartisangrah.data.Marathi
import com.nilkanth.aartisangrah.ui.components.DeityAvatar
import com.nilkanth.aartisangrah.ui.components.ItemCard
import com.nilkanth.aartisangrah.ui.components.Ornament
import com.nilkanth.aartisangrah.ui.components.SectionTitle
import com.nilkanth.aartisangrah.ui.components.categoryIcon
import com.nilkanth.aartisangrah.ui.components.deityColor
import com.nilkanth.aartisangrah.ui.components.deityTextColor
import com.nilkanth.aartisangrah.ui.theme.Gold
import com.nilkanth.aartisangrah.ui.theme.HeroBrush
import com.nilkanth.aartisangrah.ui.theme.LocalIsDark
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    library: Library,
    favorites: List<String>,
    recents: List<String>,
    appUpdate: AppRelease?,
    onOpenReader: (ids: List<String>, index: Int, title: String) -> Unit,
    onOpenList: (type: String, id: String) -> Unit,
    onOpenSearch: () -> Unit,
    onDownloadUpdate: (AppRelease) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val recentItems = recents.mapNotNull { library.item(it) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Hero(onOpenSearch) }
        if (appUpdate != null) item { UpdateBanner(appUpdate) { onDownloadUpdate(appUpdate) } }
        item { TodayCard(library, onOpenReader, onOpenList) }
        if (library.playlists.isNotEmpty()) {
            item { SectionTitle("पूजा क्रम") }
            item { PlaylistRow(library, onOpenReader) }
        }
        item { SectionTitle("प्रकार") }
        item { CategoryGrid(library, onOpenList) }
        item { SectionTitle("देवता") }
        item { DeityRow(library, onOpenList) }
        if (recentItems.isNotEmpty()) {
            item { SectionTitle("अलीकडे वाचलेले") }
            items(recentItems, key = { "recent-" + it.id }) { item ->
                ItemCard(
                    item = item,
                    library = library,
                    isFavorite = item.id in favorites,
                    onClick = { onOpenReader(recentItems.map { it.id }, recentItems.indexOf(item), "अलीकडे वाचलेले") },
                    onToggleFavorite = { onToggleFavorite(item.id) },
                )
            }
        }
        item { Footer() }
    }
}

@Composable
private fun Hero(onOpenSearch: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(HeroBrush),
    ) {
        MandalaBackdrop(Modifier.matchParentSize())
        Column(
            Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = 22.dp),
        ) {
            Text(
                "॥ श्री गणेशाय नमः ॥",
                color = Gold,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("आरती संग्रह", style = MaterialTheme.typography.displaySmall, color = Color.White)
                    Text(
                        "आरत्या • श्लोक • स्तोत्रे • अभंग",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f),
                    )
                }
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.16f))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.WbSunny, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(Marathi.date(), style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
            Spacer(Modifier.height(18.dp))
            Surface(
                onClick = onOpenSearch,
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "आरती, श्लोक किंवा देव शोधा…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Soft concentric rings and petals drawn behind the hero text. */
@Composable
private fun MandalaBackdrop(modifier: Modifier) {
    Canvas(modifier) {
        val center = Offset(size.width * 0.92f, size.height * 0.12f)
        val ring = Color.White.copy(alpha = 0.09f)
        for (i in 1..7) drawCircle(ring, radius = i * 34.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
        val petalRadius = 4 * 34.dp.toPx()
        for (k in 0 until 24) {
            val a = Math.toRadians(k * 15.0)
            drawCircle(
                Color.White.copy(alpha = 0.10f),
                radius = 5.dp.toPx(),
                center = Offset(center.x + (petalRadius * cos(a)).toFloat(), center.y + (petalRadius * sin(a)).toFloat()),
            )
        }
        val c2 = Offset(size.width * 0.02f, size.height * 1.02f)
        for (i in 1..4) drawCircle(ring, radius = i * 30.dp.toPx(), center = c2, style = Stroke(1.dp.toPx()))
    }
}

@Composable
private fun UpdateBanner(release: AppRelease, onDownload: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("नवीन आवृत्ती ${release.versionName} उपलब्ध", style = MaterialTheme.typography.titleSmall)
                Text("नवीन सुविधा व सुधारणा", style = MaterialTheme.typography.bodySmall)
            }
            FilledTonalButton(onClick = onDownload) { Text("डाउनलोड") }
        }
    }
}

@Composable
private fun TodayCard(
    library: Library,
    onOpenReader: (List<String>, Int, String) -> Unit,
    onOpenList: (String, String) -> Unit,
) {
    val deity = library.deity(Marathi.deityOfDay()) ?: return
    val items = library.itemsOfDeity(deity.id)
    if (items.isEmpty()) return
    val ids = items.map { it.id }
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 18.dp),
    ) {
        Column(Modifier.padding(vertical = 16.dp)) {
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                DeityAvatar(deity, 52.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("आज ${Marathi.day()}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text("आजचे दैवत: ${deity.name}", style = MaterialTheme.typography.titleMedium)
                }
                TextButton(onClick = { onOpenList("deity", deity.id) }) { Text("सर्व") }
            }
            Spacer(Modifier.height(12.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                    Surface(
                        onClick = { onOpenReader(ids, index, deity.name) },
                        shape = RoundedCornerShape(18.dp),
                        color = deityColor(deity).copy(alpha = if (LocalIsDark.current) 0.22f else 0.10f),
                        modifier = Modifier.widthIn(max = 230.dp),
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Icon(categoryIcon(item.category), contentDescription = null, tint = deityTextColor(deity), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                library.category(item.category)?.name.orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val playlistBrushes = listOf(
    listOf(Color(0xFFF08C00), Color(0xFFC8471A)),
    listOf(Color(0xFF9C2A4A), Color(0xFF5E1230)),
    listOf(Color(0xFF4263EB), Color(0xFF6741D9)),
    listOf(Color(0xFF2F9E44), Color(0xFF0C8599)),
)

@Composable
private fun PlaylistRow(library: Library, onOpenReader: (List<String>, Int, String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(library.playlists, key = { _, it -> it.id }) { index, playlist ->
            Box(
                Modifier
                    .width(272.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(playlistBrushes[index % playlistBrushes.size]))
                    .clickable { onOpenReader(playlist.itemIds, 0, playlist.name) },
            ) {
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(Color.White.copy(alpha = 0.08f), radius = 70.dp.toPx(), center = Offset(size.width, 0f))
                    drawCircle(Color.White.copy(alpha = 0.06f), radius = 110.dp.toPx(), center = Offset(size.width, 0f))
                }
                Column(Modifier.padding(18.dp)) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, tint = Color.White)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(playlist.name, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        playlist.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2,
                        minLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White)
                                .padding(start = 8.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color(0xFF7A1F3D), modifier = Modifier.size(20.dp))
                            Text("सुरू करा", style = MaterialTheme.typography.labelLarge, color = Color(0xFF7A1F3D))
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${Marathi.num(playlist.itemIds.size)} भाग",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryGrid(library: Library, onOpenList: (String, String) -> Unit) {
    val dark = LocalIsDark.current
    val scheme = MaterialTheme.colorScheme
    val tints = listOf(
        scheme.primaryContainer to scheme.onPrimaryContainer,
        scheme.secondaryContainer to scheme.onSecondaryContainer,
        scheme.tertiaryContainer to scheme.onTertiaryContainer,
        (if (dark) Color(0xFF2B3270) else Color(0xFFE3E7FF)) to (if (dark) Color(0xFFDDE1FF) else Color(0xFF1E2A78)),
    )
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        library.categories.chunked(2).forEachIndexed { row, pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEachIndexed { col, category ->
                    val (bg, fg) = tints[(row * 2 + col) % tints.size]
                    val count = library.itemsInCategory(category.id).size
                    Surface(
                        onClick = { onOpenList("category", category.id) },
                        shape = RoundedCornerShape(24.dp),
                        color = bg,
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Box(
                                Modifier.size(44.dp).clip(CircleShape).background(fg.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(categoryIcon(category.id), contentDescription = null, tint = fg)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(category.name, style = MaterialTheme.typography.titleMedium, color = fg, maxLines = 1)
                            Text(
                                "${Marathi.num(count)} रचना",
                                style = MaterialTheme.typography.labelMedium,
                                color = fg.copy(alpha = 0.75f),
                            )
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DeityRow(library: Library, onOpenList: (String, String) -> Unit) {
    val withItems = library.deities.filter { library.itemsOfDeity(it.id).isNotEmpty() }
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(withItems, key = { it.id }) { deity ->
            Column(
                Modifier
                    .width(74.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenList("deity", deity.id) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DeityAvatar(deity, 62.dp)
                Spacer(Modifier.height(6.dp))
                Text(
                    deity.name.removePrefix("श्री ").removePrefix("सद्गुरु "),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun Footer() {
    Column(Modifier.fillMaxWidth().padding(top = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Ornament()
        Spacer(Modifier.height(8.dp))
        Text("॥ शुभं भवतु ॥", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "खाते नाही • जाहिराती नाहीत • इंटरनेटशिवायही चालते",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
