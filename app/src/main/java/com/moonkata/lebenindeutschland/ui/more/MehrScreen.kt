package com.moonkata.lebenindeutschland.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.moonkata.lebenindeutschland.data.Languages
import com.moonkata.lebenindeutschland.data.QuestionRepository
import com.moonkata.lebenindeutschland.data.Topics
import com.moonkata.lebenindeutschland.data.UserPrefs
import com.moonkata.lebenindeutschland.data.translation.PreTranslateWorker
import com.moonkata.lebenindeutschland.data.translation.TranslationEngine
import com.moonkata.lebenindeutschland.ui.theme.Accent700
import com.moonkata.lebenindeutschland.ui.theme.BottomTab
import com.moonkata.lebenindeutschland.ui.theme.LidBottomBar
import com.moonkata.lebenindeutschland.ui.theme.LidSpace
import com.moonkata.lebenindeutschland.ui.theme.LidType
import com.moonkata.lebenindeutschland.ui.theme.Neutral600
import com.moonkata.lebenindeutschland.ui.theme.Rule
import kotlinx.coroutines.launch

private const val TOTAL_TRANSLATABLE_FIELDS = 460 * 5

@Composable
fun MehrScreen(repository: QuestionRepository, onChangeLanguage: () -> Unit, onSelectTab: (BottomTab) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { UserPrefs(context) }
    val scope = rememberCoroutineScope()
    val language = Languages.all.find { it.code == prefs.selectedLanguage }
    val bundesland = prefs.bundesland?.let { Topics.bundeslandNames[it] }

    var coverage by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(Unit) {
        coverage = Languages.all.associate { it.code to repository.translationCoverage(it.code) }
    }

    Surface(modifier = Modifier.fillMaxSize().statusBarsPadding(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Text("Mehr", style = LidType.display, modifier = Modifier.padding(LidSpace.gutter))
                Rule()

                InfoRow("Übersetzungssprache", language?.native ?: "—", onClick = onChangeLanguage)
                InfoRow("Bundesland", bundesland ?: "—")

                Text(
                    "SPRACHMODELLE",
                    style = LidType.label,
                    color = Neutral600,
                    modifier = Modifier.padding(top = LidSpace.x4, start = LidSpace.gutter, bottom = LidSpace.x2),
                )
                Languages.all.forEach { lang ->
                    val count = coverage[lang.code] ?: 0
                    LanguageModelRow(
                        native = lang.native,
                        translatedCount = count,
                        onDownload = {
                            PreTranslateWorker.enqueue(context, lang.code)
                        },
                        onDelete = {
                            scope.launch {
                                TranslationEngine.deleteModel(lang.code)
                                repository.clearTranslationCache(lang.code)
                                coverage = coverage + (lang.code to 0)
                            }
                        },
                    )
                }
            }
            LidBottomBar(active = BottomTab.MEHR, onSelect = onSelectTab)
        }
    }
}

@Composable
private fun LanguageModelRow(native: String, translatedCount: Int, onDownload: () -> Unit, onDelete: () -> Unit) {
    val downloaded = translatedCount > 0
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = LidSpace.gutter),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(native, style = LidType.rowTitle)
            if (downloaded) {
                val percent = (translatedCount * 100 / TOTAL_TRANSLATABLE_FIELDS).coerceAtMost(100)
                Text(
                    if (percent >= 100) "LÖSCHEN" else "$percent % · LÖSCHEN",
                    style = LidType.label,
                    color = Accent700,
                    modifier = Modifier.clickable(onClick = onDelete),
                )
            } else {
                Text("LADEN", style = LidType.label, color = Accent700, modifier = Modifier.clickable(onClick = onDownload))
            }
        }
        Rule()
    }
}

@Composable
private fun InfoRow(label: String, value: String, onClick: (() -> Unit)? = null) {
    Column(modifier = Modifier.fillMaxWidth().let { if (onClick != null) it.clickable(onClick = onClick) else it }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = LidSpace.gutter),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = LidType.rowTitle)
            Text(value, style = LidType.rowTitle, color = Neutral600)
        }
        Rule()
    }
}
