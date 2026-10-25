package com.nilkanth.aartisangrah.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nilkanth.aartisangrah.data.Library
import com.nilkanth.aartisangrah.data.Marathi
import com.nilkanth.aartisangrah.ui.components.EmptyState
import com.nilkanth.aartisangrah.ui.components.ItemCard

@Composable
fun FavoritesScreen(
    library: Library,
    favorites: List<String>,
    onOpenReader: (ids: List<String>, index: Int, title: String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val items = favorites.mapNotNull { library.item(it) }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)) {
            Text("आवडते", style = MaterialTheme.typography.headlineMedium)
            Text(
                if (items.isEmpty()) "तुमच्या आवडत्या आरत्या एका ठिकाणी" else "${Marathi.num(items.size)} आवडत्या रचना",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (items.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.FavoriteBorder,
                title = "अजून काही आवडते नाही",
                message = "कोणतीही आरती उघडा आणि ♡ दाबा — ती येथे लगेच मिळेल.",
            )
        } else {
            val ids = items.map { it.id }
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                    ItemCard(
                        item = item,
                        library = library,
                        isFavorite = true,
                        onClick = { onOpenReader(ids, index, "आवडते") },
                        onToggleFavorite = { onToggleFavorite(item.id) },
                    )
                }
            }
        }
    }
}
