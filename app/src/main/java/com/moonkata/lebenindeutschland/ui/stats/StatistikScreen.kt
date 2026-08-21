package com.moonkata.lebenindeutschland.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moonkata.lebenindeutschland.data.Attempt
import com.moonkata.lebenindeutschland.data.AttemptMode
import com.moonkata.lebenindeutschland.data.QuestionRepository
import com.moonkata.lebenindeutschland.data.Topics
import com.moonkata.lebenindeutschland.ui.theme.Accent
import com.moonkata.lebenindeutschland.ui.theme.Accent700
import com.moonkata.lebenindeutschland.ui.theme.BottomTab
import com.moonkata.lebenindeutschland.ui.theme.LidBottomBar
import com.moonkata.lebenindeutschland.ui.theme.LidSpace
import com.moonkata.lebenindeutschland.ui.theme.LidType
import com.moonkata.lebenindeutschland.ui.theme.Neutral300
import com.moonkata.lebenindeutschland.ui.theme.Neutral600
import com.moonkata.lebenindeutschland.ui.theme.Neutral800
import com.moonkata.lebenindeutschland.ui.theme.Rule
import java.text.DateFormat
import java.util.Date

@Composable
fun StatistikScreen(repository: QuestionRepository, onSelectTab: (BottomTab) -> Unit) {
    var attempts by remember { mutableStateOf<List<Attempt>>(emptyList()) }
    var topicProgress by remember { mutableStateOf<Map<String, Pair<Int, Int>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        repository.attemptHistory().collect { attempts = it }
    }
    LaunchedEffect(Unit) {
        topicProgress = (Topics.generalNames.keys + Topics.bundeslandNames.keys.map { "bundesland_${it.lowercase()}" })
            .associateWith { topicId -> repository.topicCorrect(topicId) to repository.topicTotal(topicId) }
    }

    val examAttempts = attempts.filter { it.mode == AttemptMode.EXAM }

    Surface(modifier = Modifier.fillMaxSize().statusBarsPadding(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Text("Fortschritt", style = LidType.display, modifier = Modifier.padding(LidSpace.gutter))
                Rule()

                if (examAttempts.isNotEmpty()) {
                    Text(
                        "PRÜFUNGEN",
                        style = LidType.label,
                        color = Neutral600,
                        modifier = Modifier.padding(top = LidSpace.x4, start = LidSpace.gutter, bottom = LidSpace.x2),
                    )
                    ScoreTrend(examAttempts.reversed())
                    Rule()
                }

                Text(
                    "VERLAUF",
                    style = LidType.label,
                    color = Neutral600,
                    modifier = Modifier.padding(top = LidSpace.x4, start = LidSpace.gutter, bottom = LidSpace.x2),
                )
                if (attempts.isEmpty()) {
                    Text(
                        "Noch keine Übungen oder Prüfungen.",
                        style = LidType.explanation,
                        modifier = Modifier.padding(horizontal = LidSpace.gutter, vertical = LidSpace.x2),
                    )
                } else {
                    attempts.forEach { attempt -> AttemptRow(attempt) }
                }

                Rule()
                Text(
                    "NACH THEMA",
                    style = LidType.label,
                    color = Neutral600,
                    modifier = Modifier.padding(top = LidSpace.x4, start = LidSpace.gutter, bottom = LidSpace.x2),
                )
                (Topics.generalNames + Topics.bundeslandNames.mapKeys { "bundesland_${it.key.lowercase()}" }).forEach { (topicId, name) ->
                    val (correct, total) = topicProgress[topicId] ?: (0 to 0)
                    if (total > 0) TopicBar(name, correct, total)
                }
            }
            LidBottomBar(active = BottomTab.FORTSCHRITT, onSelect = onSelectTab)
        }
    }
}

@Composable
private fun ScoreTrend(examAttempts: List<Attempt>) {
    Row(
        modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = LidSpace.gutter),
        horizontalArrangement = Arrangement.spacedBy(LidSpace.x1),
        verticalAlignment = Alignment.Bottom,
    ) {
        examAttempts.takeLast(12).forEach { attempt ->
            val fraction = attempt.correctCount / attempt.totalQuestions.toFloat()
            val barColor = if (attempt.passed == true) Neutral800 else Accent
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(fraction.coerceIn(0.04f, 1f))
                    .background(barColor),
            )
        }
    }
}

@Composable
private fun AttemptRow(attempt: Attempt) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = LidSpace.x2, horizontal = LidSpace.gutter)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(modeLabel(attempt), style = LidType.rowTitle)
            Text("${attempt.correctCount}/${attempt.totalQuestions}", style = LidType.rowTitle, color = Neutral600)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatDate(attempt.startedAt), style = LidType.explanation)
            if (attempt.passed != null) {
                Text(
                    if (attempt.passed) "BESTANDEN" else "NICHT BESTANDEN",
                    style = LidType.label,
                    color = if (attempt.passed) Neutral800 else Accent700,
                )
            }
        }
    }
    Rule()
}

@Composable
private fun TopicBar(name: String, correct: Int, total: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = LidSpace.x1, start = LidSpace.gutter, end = LidSpace.gutter, bottom = LidSpace.x3)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = LidType.rowTitle)
            Text("$correct/$total", style = LidType.label)
        }
        Spacer(modifier = Modifier.height(LidSpace.x1))
        val fraction = correct / total.toFloat()
        val barColor = if (fraction < 0.75f) Accent else Neutral800
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Neutral300)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).background(barColor))
        }
    }
}

private fun modeLabel(attempt: Attempt): String = when (attempt.mode) {
    AttemptMode.EXAM -> "Prüfung"
    AttemptMode.PRACTICE -> "Übung"
    AttemptMode.REVIEW -> "Wiederholung"
}

private fun formatDate(millis: Long): String = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(millis))
