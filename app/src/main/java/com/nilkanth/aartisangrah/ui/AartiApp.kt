package com.nilkanth.aartisangrah.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nilkanth.aartisangrah.data.AppUpdates
import com.nilkanth.aartisangrah.data.Item
import com.nilkanth.aartisangrah.ui.screens.FavoritesScreen
import com.nilkanth.aartisangrah.ui.screens.HomeScreen
import com.nilkanth.aartisangrah.ui.screens.ListScreen
import com.nilkanth.aartisangrah.ui.screens.ReaderScreen
import com.nilkanth.aartisangrah.ui.screens.SearchScreen
import com.nilkanth.aartisangrah.ui.screens.SettingsScreen
import com.nilkanth.aartisangrah.ui.screens.openVoiceSettings
import com.nilkanth.aartisangrah.ui.theme.LocalIsDark

private enum class Tab(val route: String, val icon: ImageVector) {
    Home("home", Icons.Rounded.Home),
    Search("search", Icons.Rounded.Search),
    Favorites("favorites", Icons.Rounded.Favorite),
    Settings("settings", Icons.Rounded.Settings);

    fun label(strings: Strings) = when (this) {
        Home -> strings.tabHome
        Search -> strings.tabSearch
        Favorites -> strings.tabFavorites
        Settings -> strings.tabSettings
    }
}

private fun readerRoute(ids: List<String>, index: Int, title: String) =
    "reader?ids=${Uri.encode(ids.joinToString(","))}&index=$index&title=${Uri.encode(title)}"

@Composable
fun AartiApp(vm: AppViewModel) {
    val library by vm.library.collectAsStateWithLifecycle()
    val lib = library
    if (lib == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val favorites by vm.prefs.favorites.collectAsStateWithLifecycle()
    val recents by vm.prefs.recents.collectAsStateWithLifecycle()
    val theme by vm.prefs.theme.collectAsStateWithLifecycle()
    val language by vm.prefs.language.collectAsStateWithLifecycle()
    val fontSize by vm.prefs.fontSize.collectAsStateWithLifecycle()
    val keepScreenOn by vm.prefs.keepScreenOn.collectAsStateWithLifecycle()
    val appUpdate by vm.appUpdate.collectAsStateWithLifecycle()
    val checkingContent by vm.checkingContent.collectAsStateWithLifecycle()
    val checkingApp by vm.checkingApp.collectAsStateWithLifecycle()
    val recite by vm.reciter.state.collectAsStateWithLifecycle()
    val speechSlow by vm.prefs.speechSlow.collectAsStateWithLifecycle()
    val musicOn by vm.prefs.music.collectAsStateWithLifecycle()

    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val strings = LocalStrings.current
    val currentStrings by rememberUpdatedState(strings)
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(currentStrings.message(it)) } }

    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val currentTab = Tab.entries.firstOrNull { route?.startsWith(it.route) == true }
    SystemBarsAppearance(lightIcons = LocalIsDark.current || currentTab == Tab.Home)

    val openReader: (List<String>, Int, String) -> Unit = { ids, index, title -> nav.navigate(readerRoute(ids, index, title)) }
    val openUrl: (String) -> Unit = { url ->
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (currentTab != null) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = tab == currentTab,
                            onClick = { nav.navigateTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label(strings)) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Tab.Home.route,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) {
            composable(Tab.Home.route) {
                HomeScreen(
                    library = lib,
                    favorites = favorites,
                    recents = recents,
                    appUpdate = appUpdate,
                    onOpenReader = openReader,
                    onOpenList = { type, id -> nav.navigate("list/$type/$id") },
                    onOpenSearch = { nav.navigateTab("search?focus=true") },
                    onDownloadUpdate = { openUrl(it.apkUrl ?: it.pageUrl) },
                    onToggleFavorite = vm.prefs::toggleFavorite,
                    onToggleLanguage = vm.prefs::toggleLanguage,
                )
            }
            composable(
                "search?focus={focus}",
                arguments = listOf(navArgument("focus") { type = NavType.BoolType; defaultValue = false }),
            ) { entry ->
                SearchScreen(
                    library = lib,
                    favorites = favorites,
                    autoFocus = entry.arguments?.getBoolean("focus") == true,
                    onOpenReader = openReader,
                    onToggleFavorite = vm.prefs::toggleFavorite,
                )
            }
            composable(Tab.Favorites.route) {
                FavoritesScreen(lib, favorites, openReader, vm.prefs::toggleFavorite)
            }
            composable(Tab.Settings.route) {
                SettingsScreen(
                    library = lib,
                    language = language,
                    theme = theme,
                    fontSize = fontSize,
                    keepScreenOn = keepScreenOn,
                    appUpdate = appUpdate,
                    checkingContent = checkingContent,
                    checkingApp = checkingApp,
                    onLanguage = vm.prefs::setLanguage,
                    speechSlow = speechSlow,
                    musicOn = musicOn,
                    onSpeechSlow = vm::setSpeechSlow,
                    onMusic = vm::setMusic,
                    onVoiceSettings = { openVoiceSettings(context) },
                    onTheme = vm.prefs::setTheme,
                    onFontSize = vm.prefs::setFontSize,
                    onKeepScreenOn = vm.prefs::setKeepScreenOn,
                    onCheckContent = vm::checkContentNow,
                    onCheckApp = vm::checkAppNow,
                    onDownloadUpdate = { openUrl(it.apkUrl ?: it.pageUrl) },
                    onReportIssue = { openUrl(AppUpdates.issuesUrl) },
                    onOpenSource = { openUrl(AppUpdates.repoUrl) },
                )
            }
            composable(
                "list/{type}/{id}",
                enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
                popExitTransition = { slideOutHorizontally { it / 4 } + fadeOut() },
            ) { entry ->
                ListScreen(
                    library = lib,
                    type = entry.arguments?.getString("type").orEmpty(),
                    id = entry.arguments?.getString("id").orEmpty(),
                    favorites = favorites,
                    onBack = { nav.popBackStack() },
                    onOpenReader = openReader,
                    onToggleFavorite = vm.prefs::toggleFavorite,
                )
            }
            composable(
                "reader?ids={ids}&index={index}&title={title}",
                arguments = listOf(
                    navArgument("ids") { type = NavType.StringType; defaultValue = "" },
                    navArgument("index") { type = NavType.IntType; defaultValue = 0 },
                    navArgument("title") { type = NavType.StringType; defaultValue = "" },
                ),
                enterTransition = { slideInHorizontally { it / 3 } + fadeIn() },
                popExitTransition = { slideOutHorizontally { it / 3 } + fadeOut() },
            ) { entry ->
                ReaderScreen(
                    library = lib,
                    ids = entry.arguments?.getString("ids").orEmpty().split(',').filter { it.isNotBlank() },
                    startIndex = entry.arguments?.getInt("index") ?: 0,
                    sourceTitle = entry.arguments?.getString("title").orEmpty(),
                    fontSize = fontSize,
                    keepScreenOn = keepScreenOn,
                    favorites = favorites,
                    onBack = { nav.popBackStack() },
                    onToggleFavorite = vm.prefs::toggleFavorite,
                    onFontSize = vm.prefs::setFontSize,
                    onMarkRead = vm.prefs::markRead,
                    onShare = { context.shareItem(it, strings) },
                    recite = recite,
                    speechSlow = speechSlow,
                    musicOn = musicOn,
                    reciteFinished = vm.reciter.finished,
                    noVoice = vm.reciter.noVoice,
                    onListen = { vm.reciter.play(it) },
                    onPause = vm.reciter::pause,
                    onResume = vm.reciter::resume,
                    onStopListening = vm.reciter::stop,
                    onSpeechSlow = vm::setSpeechSlow,
                    onMusic = vm::setMusic,
                )
            }
        }
    }
}

private fun NavHostController.navigateTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

private fun Context.shareItem(item: Item, strings: Strings) {
    val text = buildString {
        append("॥ ").append(item.title).append(" ॥\n")
        item.author?.let { append(strings.composedBy(it)).append('\n') }
        append('\n')
        if (item.refrainFirst && item.refrain != null) append(item.refrain).append("\n\n")
        item.verses.forEach { verse ->
            append(verse).append("\n\n")
            item.refrain?.let { append(it).append("\n\n") }
        }
        append(strings.shareFooter)
    }
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    runCatching { startActivity(Intent.createChooser(send, item.title)) }
}

/** Light status-bar icons over the saffron hero and in dark mode; dark icons elsewhere. */
@Composable
private fun SystemBarsAppearance(lightIcons: Boolean) {
    val view = LocalView.current
    val dark = LocalIsDark.current
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !lightIcons
            isAppearanceLightNavigationBars = !dark
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
