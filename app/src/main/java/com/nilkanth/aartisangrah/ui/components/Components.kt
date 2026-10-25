package com.nilkanth.aartisangrah.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilkanth.aartisangrah.data.Deity
import com.nilkanth.aartisangrah.data.Item
import com.nilkanth.aartisangrah.data.Library
import com.nilkanth.aartisangrah.ui.LocalStrings
import com.nilkanth.aartisangrah.ui.theme.LocalIsDark
import com.nilkanth.aartisangrah.ui.theme.Mukta

fun categoryIcon(id: String): ImageVector = when (id) {
    "aarti" -> Icons.Rounded.LocalFireDepartment
    "shlok" -> Icons.Rounded.SelfImprovement
    "stotra" -> Icons.Rounded.AutoStories
    "abhang" -> Icons.Rounded.MusicNote
    else -> Icons.AutoMirrored.Rounded.MenuBook
}

fun deityColor(deity: Deity?): Color = deity?.let { Color(it.color) } ?: Color(0xFF8C5A00)

/** A round badge with the deity's initial on a gradient of its colour. */
@Composable
fun DeityAvatar(deity: Deity?, size: Dp, modifier: Modifier = Modifier) {
    val base = deityColor(deity)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(base.copy(alpha = 0.78f), base)))
            .border(width = (size.value / 28f).dp, color = Color(0x66FFE3A3), shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = deity?.letter ?: "ॐ",
            color = Color.White,
            fontFamily = Mukta,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.4f).sp,
            lineHeight = (size.value * 0.5f).sp,
            maxLines = 1,
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: @Composable (() -> Unit)? = null) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 24.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(width = 4.dp, height = 20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFF39A3A), Color(0xFFB4441E)))),
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

/** Thin line — lotus-diamond — thin line. Used between verses and sections. */
@Composable
fun Ornament(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Canvas(modifier.fillMaxWidth().height(18.dp)) {
        val cy = size.height / 2
        val cx = size.width / 2
        val reach = size.width * 0.28f
        drawLine(
            brush = Brush.horizontalGradient(listOf(Color.Transparent, color.copy(alpha = 0.6f)), cx - reach, cx - 14.dp.toPx()),
            start = Offset(cx - reach, cy), end = Offset(cx - 14.dp.toPx(), cy), strokeWidth = 1.2.dp.toPx(),
        )
        drawLine(
            brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.6f), Color.Transparent), cx + 14.dp.toPx(), cx + reach),
            start = Offset(cx + 14.dp.toPx(), cy), end = Offset(cx + reach, cy), strokeWidth = 1.2.dp.toPx(),
        )
        val d = 6.dp.toPx()
        val diamond = Path().apply {
            moveTo(cx, cy - d); lineTo(cx + d, cy); lineTo(cx, cy + d); lineTo(cx - d, cy); close()
        }
        drawPath(diamond, color)
        drawCircle(color.copy(alpha = 0.7f), radius = 2.dp.toPx(), center = Offset(cx - 10.dp.toPx(), cy))
        drawCircle(color.copy(alpha = 0.7f), radius = 2.dp.toPx(), center = Offset(cx + 10.dp.toPx(), cy))
    }
}

@Composable
fun ItemCard(
    item: Item,
    library: Library,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val deity = library.deity(item.deity)
    val category = library.category(item.category)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
    ) {
        Row(Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            DeityAvatar(deity, 48.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                // In English the transliterated title helps; in Marathi the next line of the text does.
                val second = if (strings.english) item.titleEn.ifBlank { item.preview } else item.preview
                val meta = listOfNotNull(
                    category?.let { strings.categoryName(it) },
                    item.author ?: deity?.let { strings.deityName(it) },
                ).joinToString(" • ")
                Text(
                    meta,
                    style = MaterialTheme.typography.labelMedium,
                    color = deityTextColor(deity),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    second,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            FavoriteButton(isFavorite, onToggleFavorite)
        }
    }
}

@Composable
fun FavoriteButton(isFavorite: Boolean, onToggle: () -> Unit, tint: Color = MaterialTheme.colorScheme.outline) {
    val strings = LocalStrings.current
    val color by animateColorAsState(if (isFavorite) Color(0xFFE0245E) else tint, label = "fav")
    IconButton(onClick = onToggle) {
        Icon(
            if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = if (isFavorite) strings.removeFavorite else strings.addFavorite,
            tint = color,
        )
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** The deity colour, adjusted so it stays readable as text in both themes. */
@Composable
fun deityTextColor(deity: Deity?): Color {
    val c = deityColor(deity)
    return if (LocalIsDark.current) lerp(c, Color.White, 0.45f) else lerp(c, Color.Black, 0.12f)
}
