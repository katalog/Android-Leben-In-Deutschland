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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import androidx.work.WorkManager
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

@Composable
fun MehrScreen(
    repository: QuestionRepository,
    onChangeLanguage: () -> Unit,
    onChangeBundesland: () -> Unit,
    onSelectTab: (BottomTab) -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { UserPrefs(context) }
    val language = Languages.all.find { it.code == prefs.selectedLanguage }
    val bundesland = prefs.bundesland?.let { Topics.bundeslandNames[it] }

    Surface(modifier = Modifier.fillMaxSize().statusBarsPadding(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Text("Mehr", style = LidType.display, modifier = Modifier.padding(LidSpace.gutter))
                Rule()

                InfoRow("Übersetzungssprache", language?.native ?: "—", onClick = onChangeLanguage)
                InfoRow("Bundesland", bundesland ?: "—", onClick = onChangeBundesland)

                Text(
                    "SPRACHMODELLE",
                    style = LidType.label,
                    color = Neutral600,
                    modifier = Modifier.padding(top = LidSpace.x4, start = LidSpace.gutter, bottom = LidSpace.x2),
                )
                Languages.all.forEach { lang ->
                    LanguageModelRow(languageCode = lang.code, native = lang.native, repository = repository)
                }
            }
            LidBottomBar(active = BottomTab.MEHR, onSelect = onSelectTab)
        }
    }
}

/** Owns its own live state: initial DB count, then whatever PreTranslateWorker reports while running. */
@Composable
private fun LanguageModelRow(languageCode: String, native: String, repository: QuestionRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var translatedCount by remember(languageCode) { mutableIntStateOf(0) }
    var isRunning by remember(languageCode) { mutableStateOf(false) }

    LaunchedEffect(languageCode) {
        translatedCount = repository.translationCoverage(languageCode)
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(PreTranslateWorker.workName(languageCode))
            .collect { infos ->
                val info = infos.firstOrNull() ?: return@collect
                isRunning = info.state == WorkInfo.State.RUNNING || info.state == WorkInfo.State.ENQUEUED
                val progressDone = info.progress.getInt(PreTranslateWorker.KEY_DONE, -1)
                if (progressDone >= 0) translatedCount = progressDone
                if (info.state == WorkInfo.State.SUCCEEDED) {
                    translatedCount = repository.translationCoverage(languageCode)
                    isRunning = false
                }
            }
    }

    val downloaded = translatedCount > 0
    val percent = (translatedCount * 100 / PreTranslateWorker.TOTAL_FIELDS).coerceAtMost(100)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = LidSpace.gutter),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(native, style = LidType.rowTitle)
            when {
                isRunning -> Text("$percent %", style = LidType.label, color = Accent700)
                downloaded -> Text(
                    if (percent >= 100) "LÖSCHEN" else "$percent % · LÖSCHEN",
                    style = LidType.label,
                    color = Accent700,
                    modifier = Modifier.clickable {
                        scope.launch {
                            TranslationEngine.deleteModel(languageCode)
                            repository.clearTranslationCache(languageCode)
                            translatedCount = 0
                        }
                    },
                )
                else -> Text(
                    "LADEN",
                    style = LidType.label,
                    color = Accent700,
                    modifier = Modifier.clickable { PreTranslateWorker.enqueue(context, languageCode) },
                )
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
