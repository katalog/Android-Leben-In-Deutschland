package com.moonkata.lebenindeutschland.ui.quiz

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.moonkata.lebenindeutschland.data.AttemptMode
import com.moonkata.lebenindeutschland.ui.theme.Accent
import com.moonkata.lebenindeutschland.ui.theme.Accent700
import com.moonkata.lebenindeutschland.ui.theme.LidButton
import com.moonkata.lebenindeutschland.ui.theme.LidSpace
import com.moonkata.lebenindeutschland.ui.theme.LidType
import com.moonkata.lebenindeutschland.ui.theme.Neutral300
import com.moonkata.lebenindeutschland.ui.theme.Rule

@Composable
fun FrageScreen(viewModel: QuizViewModel, onExit: () -> Unit, onFinished: (QuizViewModel) -> Unit) {
    if (viewModel.finished) {
        onFinished(viewModel)
        return
    }

    val question = viewModel.currentQuestion
    Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            FrageHeader(
                mode = viewModel.mode,
                position = viewModel.currentIndex + 1,
                total = viewModel.total,
                correctCount = viewModel.correctCount,
                examSecondsRemaining = viewModel.examSecondsRemaining,
                onCancel = onExit,
            )
            QuizProgressBar(fraction = (viewModel.currentIndex + 1) / viewModel.total.toFloat())

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Rule()
                Column(modifier = Modifier.padding(LidSpace.gutter)) {
                    question.imageAsset?.let { asset ->
                        AssetImage(asset, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(LidSpace.x3))
                    }
                    Text(question.textDe, style = LidType.question)
                }
                Rule()

                val picked = viewModel.pickedAnswerIndex
                AnswerRow('A', question.answerA, rowState(picked, 0, question.correctAnswerIndex)) { viewModel.pick(0) }
                AnswerRow('B', question.answerB, rowState(picked, 1, question.correctAnswerIndex)) { viewModel.pick(1) }
                AnswerRow('C', question.answerC, rowState(picked, 2, question.correctAnswerIndex)) { viewModel.pick(2) }
                AnswerRow('D', question.answerD, rowState(picked, 3, question.correctAnswerIndex)) { viewModel.pick(3) }

                if (picked != null && question.explanationDe != null) {
                    Column(modifier = Modifier.padding(top = LidSpace.x4, start = LidSpace.gutter, end = LidSpace.gutter, bottom = LidSpace.x4)) {
                        Text("Erklärung. " + question.explanationDe, style = LidType.explanation)
                    }
                    Rule()
                }
            }

            Rule()
            Row(modifier = Modifier.padding(vertical = LidSpace.x4, horizontal = LidSpace.gutter)) {
                LidButton(
                    onClick = viewModel::next,
                    enabled = viewModel.pickedAnswerIndex != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (viewModel.pickedAnswerIndex != null) "Weiter" else "Antwort wählen")
                }
            }
        }
    }
}

private fun rowState(picked: Int?, index: Int, correctIndex: Int): AnswerRowState = when {
    picked == null -> AnswerRowState.UNANSWERED
    index == correctIndex -> AnswerRowState.CORRECT
    index == picked -> AnswerRowState.WRONG_PICKED
    else -> AnswerRowState.WRONG_UNPICKED
}

@Composable
private fun FrageHeader(
    mode: AttemptMode,
    position: Int,
    total: Int,
    correctCount: Int,
    examSecondsRemaining: Int,
    onCancel: () -> Unit,
) {
    val modeLabel = if (mode == AttemptMode.EXAM) "PRÜFUNG" else "ÜBEN"
    Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = LidSpace.gutter, end = LidSpace.gutter, bottom = 10.dp)) {
        Text(
            "✕ ABBRECHEN",
            style = LidType.label,
            modifier = Modifier.align(Alignment.CenterStart).clickable(onClick = onCancel),
        )
        Text("$modeLabel $position / $total", style = LidType.label, modifier = Modifier.align(Alignment.Center))
        when (mode) {
            AttemptMode.PRACTICE ->
                Text("$correctCount RICHTIG", style = LidType.label, color = Accent700, modifier = Modifier.align(Alignment.CenterEnd))
            AttemptMode.EXAM -> {
                val minutes = examSecondsRemaining / 60
                Text("$minutes MIN", style = LidType.label, color = Accent700, modifier = Modifier.align(Alignment.CenterEnd))
            }
            AttemptMode.REVIEW -> Unit
        }
    }
}

@Composable
private fun QuizProgressBar(fraction: Float) {
    Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Neutral300)) {
        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).background(Accent))
    }
}

@Composable
private fun AssetImage(assetName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, assetName) {
        value = try {
            context.assets.open("images/$assetName").use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    bitmapState.value?.let { bitmap ->
        Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
    }
}
