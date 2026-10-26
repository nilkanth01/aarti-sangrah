@file:OptIn(ExperimentalMaterial3Api::class)

package com.nilkanth.aartisangrah.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nilkanth.aartisangrah.BuildConfig
import com.nilkanth.aartisangrah.data.AppLanguage
import com.nilkanth.aartisangrah.data.AppRelease
import com.nilkanth.aartisangrah.data.Library
import com.nilkanth.aartisangrah.data.Prefs
import com.nilkanth.aartisangrah.data.ThemeMode
import com.nilkanth.aartisangrah.ui.LocalStrings
import com.nilkanth.aartisangrah.ui.components.Ornament
import com.nilkanth.aartisangrah.ui.theme.verseStyle

@Composable
fun SettingsScreen(
    library: Library,
    language: AppLanguage,
    theme: ThemeMode,
    fontSize: Float,
    keepScreenOn: Boolean,
    appUpdate: AppRelease?,
    checkingContent: Boolean,
    checkingApp: Boolean,
    onLanguage: (AppLanguage) -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onFontSize: (Float) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit,
    onCheckContent: () -> Unit,
    onCheckApp: () -> Unit,
    onDownloadUpdate: (AppRelease) -> Unit,
    onReportIssue: () -> Unit,
    onOpenSource: () -> Unit,
    speechSlow: Boolean = false,
    musicOn: Boolean = true,
    onSpeechSlow: (Boolean) -> Unit = {},
    onMusic: (Boolean) -> Unit = {},
    onVoiceSettings: () -> Unit = {},
) {
    val strings = LocalStrings.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 4.dp)) {
                Text(strings.tabSettings, style = MaterialTheme.typography.headlineMedium)
                Text(
                    strings.settingsSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            // Each option is labelled in its own language so it can always be found.
            Group(if (strings.english) "Language • भाषा" else "भाषा • Language") {
                Text(strings.appLanguage, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                val options = listOf(AppLanguage.MARATHI to "मराठी", AppLanguage.ENGLISH to "English")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    options.forEachIndexed { i, (option, label) ->
                        SegmentedButton(
                            selected = language == option,
                            onClick = { onLanguage(option) },
                            shape = SegmentedButtonDefaults.itemShape(i, options.size),
                        ) { Text(label) }
                    }
                }
                Text(
                    strings.languageNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        item {
            Group(strings.listenGroup) {
                Text(strings.listenNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(14.dp))
                Text(strings.speechSpeed, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                val speeds = listOf(true to strings.slow, false to strings.normalSpeed)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    speeds.forEachIndexed { i, (isSlow, label) ->
                        SegmentedButton(
                            selected = speechSlow == isSlow,
                            onClick = { onSpeechSlow(isSlow) },
                            shape = SegmentedButtonDefaults.itemShape(i, speeds.size),
                        ) { Text(label) }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.musicSetting, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Switch(checked = musicOn, onCheckedChange = onMusic)
                }
                Spacer(Modifier.height(4.dp))
                LinkRow(Icons.Rounded.RecordVoiceOver, strings.openVoiceSettings, onVoiceSettings)
            }
        }

        item {
            Group(strings.appearance) {
                Text(strings.theme, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                val options = listOf(
                    Triple(ThemeMode.SYSTEM, strings.themeAuto, Icons.Rounded.BrightnessAuto),
                    Triple(ThemeMode.LIGHT, strings.themeDay, Icons.Rounded.LightMode),
                    Triple(ThemeMode.DARK, strings.themeNight, Icons.Rounded.DarkMode),
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    options.forEachIndexed { i, (mode, label, icon) ->
                        SegmentedButton(
                            selected = theme == mode,
                            onClick = { onTheme(mode) },
                            shape = SegmentedButtonDefaults.itemShape(i, options.size),
                            icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        ) { Text(label) }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.textSize, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text(strings.num(fontSize.toInt()), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = fontSize, onValueChange = onFontSize, valueRange = Prefs.MIN_FONT_SIZE..Prefs.MAX_FONT_SIZE)
                Text(
                    "॥ श्री गणेशाय नमः ॥",
                    style = verseStyle(fontSize),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                HorizontalDivider(Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(strings.keepScreenOn, style = MaterialTheme.typography.titleSmall)
                        Text(
                            strings.keepScreenOnHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = keepScreenOn, onCheckedChange = onKeepScreenOn)
                }
            }
        }

        item {
            Group(strings.updates) {
                UpdateRow(
                    icon = Icons.AutoMirrored.Rounded.LibraryBooks,
                    title = strings.collection,
                    subtitle = strings.collectionInfo(library.version, library.items.size),
                ) {
                    if (checkingContent) CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
                    else OutlinedButton(onClick = onCheckContent) { Text(strings.checkNew) }
                }
                Text(
                    strings.otaNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 40.dp, top = 2.dp),
                )
                HorizontalDivider(Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant)
                UpdateRow(
                    icon = Icons.Rounded.SystemUpdate,
                    title = strings.appVersion,
                    subtitle = BuildConfig.VERSION_NAME,
                ) {
                    when {
                        appUpdate != null -> Button(onClick = { onDownloadUpdate(appUpdate) }) { Text(strings.getVersion(appUpdate.versionName)) }
                        checkingApp -> CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
                        else -> OutlinedButton(onClick = onCheckApp) { Text(strings.check) }
                    }
                }
            }
        }

        item {
            Group(strings.about) {
                Text(strings.aboutText, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                LinkRow(Icons.Rounded.BugReport, strings.reportIssue, onReportIssue)
                LinkRow(Icons.Rounded.Code, strings.sourceCode, onOpenSource)
            }
        }

        item {
            Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Ornament()
                Text(
                    "॥ सर्वे भवन्तु सुखिनः ॥",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun Group(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp),
    )
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable
private fun UpdateRow(icon: ImageVector, title: String, subtitle: String, action: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        action()
    }
}

@Composable
private fun LinkRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
