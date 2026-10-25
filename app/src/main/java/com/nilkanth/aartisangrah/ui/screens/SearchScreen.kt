package com.nilkanth.aartisangrah.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.nilkanth.aartisangrah.data.Library
import com.nilkanth.aartisangrah.data.Marathi
import com.nilkanth.aartisangrah.ui.components.EmptyState
import com.nilkanth.aartisangrah.ui.components.ItemCard

@Composable
fun SearchScreen(
    library: Library,
    favorites: List<String>,
    autoFocus: Boolean,
    onOpenReader: (ids: List<String>, index: Int, title: String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableStateOf<String?>(null) }
    val results = remember(query, categoryId, library) { library.search(query, categoryId) }
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(autoFocus) { if (autoFocus) runCatching { focus.requestFocus() } }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 18.dp)) {
            Text("शोधा", style = MaterialTheme.typography.headlineMedium)
            Text(
                "मराठी किंवा इंग्रजीत शोधा — उदा. सुखकर्ता, ganpati, ramraksha",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).focusRequester(focus),
            placeholder = { Text("आरती, श्लोक, देव किंवा ओळ…") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Clear, contentDescription = "पुसा") }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        )
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(selected = categoryId == null, onClick = { categoryId = null }, label = { Text("सर्व") })
            }
            items(library.categories, key = { it.id }) { c ->
                FilterChip(
                    selected = categoryId == c.id,
                    onClick = { categoryId = if (categoryId == c.id) null else c.id },
                    label = { Text(c.name) },
                )
            }
        }
        Text(
            "${Marathi.num(results.size)} रचना",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        if (results.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.SearchOff,
                title = "काही सापडले नाही",
                message = "दुसरा शब्द वापरून पहा. इंग्रजी अक्षरांतही शोधता येते — उदा. aarti, vitthal, datta.",
            )
        } else {
            val ids = results.map { it.id }
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                itemsIndexed(results, key = { _, it -> it.id }) { index, item ->
                    ItemCard(
                        item = item,
                        library = library,
                        isFavorite = item.id in favorites,
                        onClick = { onOpenReader(ids, index, "शोध परिणाम") },
                        onToggleFavorite = { onToggleFavorite(item.id) },
                    )
                }
            }
        }
    }
}
