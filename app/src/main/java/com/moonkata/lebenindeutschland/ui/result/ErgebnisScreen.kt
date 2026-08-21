package com.moonkata.lebenindeutschland.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moonkata.lebenindeutschland.data.AttemptMode
import com.moonkata.lebenindeutschland.data.Topics
import com.moonkata.lebenindeutschland.ui.quiz.QuizViewModel
import com.moonkata.lebenindeutschland.ui.theme.Accent
import com.moonkata.lebenindeutschland.ui.theme.LidButton
import com.moonkata.lebenindeutschland.ui.theme.LidSpace
import com.moonkata.lebenindeutschland.ui.theme.LidType
import com.moonkata.lebenindeutschland.ui.theme.Neutral300
import com.moonkata.lebenindeutschland.ui.theme.Neutral700
import com.moonkata.lebenindeutschland.ui.theme.Neutral800
import com.moonkata.lebenindeutschland.ui.theme.Rule

private data class TopicTally(val topicId: String, val correct: Int, val total: Int)

@Composable
fun ErgebnisScreen(viewModel: QuizViewModel, onStart: () -> Unit, onReviewWrong: (List<Int>) -> Unit) {
    val correct = viewModel.correctCount
    val total = viewModel.total
    val wrongIds = viewModel.sessionResults.filter { !it.second }.map { it.first }.distinct()

    val byTopic = viewModel.questions.groupBy { it.topicId }
    val tallies = byTopic.map { (topicId, questionsInTopic) ->
        val idsInTopic = questionsInTopic.map { it.id }.toSet()
        val correctInTopic = viewModel.sessionResults.count { (qid, ok) -> ok && qid in idsInTopic }
        TopicTally(topicId, correctInTopic, questionsInTopic.size)
    }

    Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                ScorePoster(mode = viewModel.mode, correct = correct, total = total, passed = viewModel.passed)
                Rule()

                Text(
                    "NACH THEMA",
                    style = LidType.label,
                    color = Neutral700,
                    modifier = Modifier.padding(top = LidSpace.x4, start = LidSpace.gutter, bottom = LidSpace.x2),
                )
                tallies.forEach { tally ->
                    TopicRow(tally)
                }
            }

            Rule()
            Row(modifier = Modifier.padding(vertical = LidSpace.x4, horizontal = LidSpace.gutter)) {
                LidButton(
                    onClick = onStart,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                ) { Text("Start") }
                if (wrongIds.isNotEmpty()) {
                    Spacer(modifier = Modifier.padding(start = LidSpace.x2))
                    LidButton(onClick = { onReviewWrong(wrongIds) }, modifier = Modifier.weight(2f)) {
                        Text("${wrongIds.size} falsche Fragen üben")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScorePoster(mode: AttemptMode, correct: Int, total: Int, passed: Boolean?) {
    Column(modifier = Modifier.fillMaxWidth().background(Accent).padding(top = 24.dp, start = LidSpace.gutter, end = LidSpace.gutter, bottom = 26.dp)) {
        Text(
            if (mode == AttemptMode.EXAM) "MODELLTEST" else "ÜBUNG",
            style = LidType.label,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(LidSpace.x2))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$correct", style = LidType.scoreNumeral, color = Color.White)
            Text(" / $total", style = LidType.poster, color = Color.White)
        }
        if (passed != null) {
            Spacer(modifier = Modifier.height(LidSpace.x1))
            Text(if (passed) "BESTANDEN" else "NICHT BESTANDEN", style = LidType.poster, color = Color.White)
        }
    }
}

@Composable
private fun TopicRow(tally: TopicTally) {
    Column(modifier = Modifier.padding(top = LidSpace.x1, start = LidSpace.gutter, end = LidSpace.gutter, bottom = LidSpace.x3)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
            Text(Topics.displayName(tally.topicId), style = LidType.rowTitle)
            Text("${tally.correct}/${tally.total}", style = LidType.label)
        }
        Spacer(modifier = Modifier.height(LidSpace.x1))
        val fraction = if (tally.total == 0) 0f else tally.correct / tally.total.toFloat()
        val barColor = if (fraction < 0.75f) Accent else Neutral800
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Neutral300)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).background(barColor))
        }
    }
}
