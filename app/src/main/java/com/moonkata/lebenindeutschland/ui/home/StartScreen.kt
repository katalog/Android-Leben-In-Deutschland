package com.moonkata.lebenindeutschland.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moonkata.lebenindeutschland.data.QuestionRepository
import com.moonkata.lebenindeutschland.data.Topics
import com.moonkata.lebenindeutschland.data.UserPrefs
import com.moonkata.lebenindeutschland.ui.theme.Accent
import com.moonkata.lebenindeutschland.ui.theme.Accent700
import com.moonkata.lebenindeutschland.ui.theme.LidSpace
import com.moonkata.lebenindeutschland.ui.theme.LidType
import com.moonkata.lebenindeutschland.ui.theme.Neutral300
import com.moonkata.lebenindeutschland.ui.theme.Neutral600
import com.moonkata.lebenindeutschland.ui.theme.Rule

@Composable
fun StartScreen(
    repository: QuestionRepository,
    onPracticeTopic: (topicId: String) -> Unit,
    onPracticeBundesland: (code: String) -> Unit,
    onStartExam: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { UserPrefs(context) }
    val bundesland = prefs.bundesland

    var overallCorrect by remember { mutableIntStateOf(0) }
    var overallTotal by remember { mutableIntStateOf(1) }
    var topicProgress by remember { mutableStateOf<Map<String, Pair<Int, Int>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        overallTotal = repository.overallTotal()
        overallCorrect = repository.overallCorrect()
        topicProgress = Topics.generalNames.keys.associateWith { topicId ->
            repository.topicCorrect(topicId) to repository.topicTotal(topicId)
        }
    }

    Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Text("Guten Tag", style = LidType.display, modifier = Modifier.padding(LidSpace.gutter))

            Rule()
            Column(modifier = Modifier.padding(vertical = LidSpace.x4, horizontal = LidSpace.gutter)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GELERNT", style = LidType.label, color = Neutral600)
                    Text("$overallCorrect / $overallTotal", style = LidType.rowTitle)
                }
                Spacer(modifier = Modifier.height(LidSpace.x2))
                val fraction = overallCorrect / overallTotal.toFloat()
                Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Neutral300)) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).background(Accent))
                }
            }
            Rule()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Accent)
                    .clickable(onClick = onStartExam)
                    .padding(top = 22.dp, start = LidSpace.gutter, end = LidSpace.gutter, bottom = 24.dp),
            ) {
                Text("Prüfung\nsimulieren", style = LidType.poster, color = Color.White)
                Spacer(modifier = Modifier.height(LidSpace.x2))
                Text("33 FRAGEN · 60 MIN · 17 ZUM BESTEHEN", style = LidType.label, color = Color.White)
            }
            Rule()

            Text(
                "THEMEN ÜBEN",
                style = LidType.label,
                color = Neutral600,
                modifier = Modifier.padding(top = LidSpace.x4, start = LidSpace.gutter, bottom = LidSpace.x2),
            )
            Topics.generalNames.forEach { (topicId, name) ->
                val (correct, total) = topicProgress[topicId] ?: (0 to 0)
                TopicRow(name, correct, total) { onPracticeTopic(topicId) }
            }

            if (bundesland != null) {
                Rule()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPracticeBundesland(bundesland) }
                        .padding(vertical = LidSpace.x3, horizontal = LidSpace.gutter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("BUNDESLAND", style = LidType.label, color = Neutral600)
                        Text(
                            Topics.bundeslandNames[bundesland] ?: bundesland,
                            style = LidType.display.copy(fontSize = 20.sp, lineHeight = 22.sp),
                        )
                    }
                    Text("+10 FRAGEN", style = LidType.label, color = Accent700)
                }
            }
            Rule()
        }
    }
}

@Composable
private fun TopicRow(name: String, correct: Int, total: Int, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Rule()
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = LidSpace.gutter),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(name, style = LidType.rowTitle)
            Text("$correct/$total", style = LidType.rowTitle, color = Neutral600)
        }
    }
}
