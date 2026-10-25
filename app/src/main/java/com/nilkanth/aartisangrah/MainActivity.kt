package com.nilkanth.aartisangrah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nilkanth.aartisangrah.ui.AartiApp
import com.nilkanth.aartisangrah.ui.AppViewModel
import com.nilkanth.aartisangrah.ui.LocalStrings
import com.nilkanth.aartisangrah.ui.Strings
import com.nilkanth.aartisangrah.ui.theme.AartiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppViewModel = viewModel()
            val theme by vm.prefs.theme.collectAsStateWithLifecycle()
            val language by vm.prefs.language.collectAsStateWithLifecycle()
            AartiTheme(theme) {
                CompositionLocalProvider(LocalStrings provides Strings.of(language)) { AartiApp(vm) }
            }
        }
    }
}
