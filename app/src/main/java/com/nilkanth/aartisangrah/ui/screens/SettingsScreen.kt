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
import com.nilkanth.aartisangrah.data.AppRelease
import com.nilkanth.aartisangrah.data.Library
import com.nilkanth.aartisangrah.data.Marathi
import com.nilkanth.aartisangrah.data.Prefs
import com.nilkanth.aartisangrah.data.ThemeMode
import com.nilkanth.aartisangrah.ui.components.Ornament
import com.nilkanth.aartisangrah.ui.theme.verseStyle

@Composable
fun SettingsScreen(
    library: Library,
    theme: ThemeMode,
    fontSize: Float,
    keepScreenOn: Boolean,
    appUpdate: AppRelease?,
    checkingContent: Boolean,
    checkingApp: Boolean,
    onTheme: (ThemeMode) -> Unit,
    onFontSize: (Float) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit,
    onCheckContent: () -> Unit,
    onCheckApp: () -> Unit,
    onDownloadUpdate: (AppRelease) -> Unit,
    onReportIssue: () -> Unit,
    onOpenSource: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 4.dp)) {
                Text("सेटिंग्ज", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "तुमच्या सोयीनुसार ॲप बदला",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Group("दिसणे") {
                Text("रंगसंगती", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                val options = listOf(
                    Triple(ThemeMode.SYSTEM, "आपोआप", Icons.Rounded.BrightnessAuto),
                    Triple(ThemeMode.LIGHT, "दिवस", Icons.Rounded.LightMode),
                    Triple(ThemeMode.DARK, "रात्र", Icons.Rounded.DarkMode),
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
                    Text("अक्षरांचा आकार", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text(Marathi.num(fontSize.toInt()), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
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
                        Text("वाचताना स्क्रीन चालू ठेवा", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "आरती म्हणताना स्क्रीन बंद होणार नाही",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = keepScreenOn, onCheckedChange = onKeepScreenOn)
                }
            }
        }

        item {
            Group("अपडेट्स") {
                UpdateRow(
                    icon = Icons.AutoMirrored.Rounded.LibraryBooks,
                    title = "आरती संग्रह",
                    subtitle = "आवृत्ती ${Marathi.num(library.version)} • ${Marathi.num(library.items.size)} रचना",
                ) {
                    if (checkingContent) CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
                    else OutlinedButton(onClick = onCheckContent) { Text("नवीन तपासा") }
                }
                Text(
                    "नवीन आरत्या इंटरनेटवरून आपोआप येतात — ॲप पुन्हा इंस्टॉल करायची गरज नाही.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 40.dp, top = 2.dp),
                )
                HorizontalDivider(Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant)
                UpdateRow(
                    icon = Icons.Rounded.SystemUpdate,
                    title = "ॲप आवृत्ती",
                    subtitle = BuildConfig.VERSION_NAME,
                ) {
                    when {
                        appUpdate != null -> Button(onClick = { onDownloadUpdate(appUpdate) }) { Text("v${appUpdate.versionName} घ्या") }
                        checkingApp -> CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
                        else -> OutlinedButton(onClick = onCheckApp) { Text("तपासा") }
                    }
                }
            }
        }

        item {
            Group("माहिती") {
                Text(
                    "हे ॲप पूर्णपणे मोफत आहे — खाते नाही, जाहिराती नाहीत, तुमची कोणतीही माहिती गोळा केली जात नाही. " +
                        "सर्व आरत्या फोनमध्येच साठवलेल्या आहेत, त्यामुळे इंटरनेटशिवायही ॲप चालते.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                LinkRow(Icons.Rounded.BugReport, "चूक कळवा / नवीन आरती सुचवा", onReportIssue)
                LinkRow(Icons.Rounded.Code, "स्रोत कोड (GitHub)", onOpenSource)
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
