@file:OptIn(ExperimentalMaterial3Api::class)

package com.nilkanth.aartisangrah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nilkanth.aartisangrah.data.Item
import com.nilkanth.aartisangrah.data.Library
import com.nilkanth.aartisangrah.ui.LocalStrings
import com.nilkanth.aartisangrah.ui.components.DeityAvatar
import com.nilkanth.aartisangrah.ui.components.ItemCard
import com.nilkanth.aartisangrah.ui.components.categoryIcon
import com.nilkanth.aartisangrah.ui.components.deityColor
import com.nilkanth.aartisangrah.ui.theme.HeroBrush

/** All items of one category or one deity. */
@Composable
fun ListScreen(
    library: Library,
    type: String,
    id: String,
    favorites: List<String>,
    onBack: () -> Unit,
    onOpenReader: (ids: List<String>, index: Int, title: String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val strings = LocalStrings.current
    val deity = if (type == "deity") library.deity(id) else null
    val category = if (type == "category") library.category(id) else null
    val title = deity?.let { strings.deityName(it) } ?: category?.let { strings.categoryName(it) } ?: ""
    val items: List<Item> = when {
        deity != null -> library.itemsOfDeity(id)
        category != null -> library.itemsInCategory(id)
        else -> emptyList()
    }
    val ids = items.map { it.id }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(top = padding.calculateTopPadding()).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                val brush = deity?.let { Brush.linearGradient(listOf(deityColor(it).copy(alpha = 0.85f), deityColor(it))) } ?: HeroBrush
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(brush)
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (deity != null) {
                        DeityAvatar(deity, 64.dp)
                    } else if (category != null) {
                        Box(
                            Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(categoryIcon(category.id), contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text(
                            category?.let { strings.categoryDescription(it) }?.takeIf { it.isNotBlank() }
                                ?: strings.texts(items.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.88f),
                        )
                    }
                }
            }
            itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                ItemCard(
                    item = item,
                    library = library,
                    isFavorite = item.id in favorites,
                    onClick = { onOpenReader(ids, index, title) },
                    onToggleFavorite = { onToggleFavorite(item.id) },
                )
            }
            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}
