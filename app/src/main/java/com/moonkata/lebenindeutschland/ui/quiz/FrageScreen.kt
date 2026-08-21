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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.moonkata.lebenindeutschland.data.AttemptMode
import com.moonkata.lebenindeutschland.data.QuestionRepository
import com.moonkata.lebenindeutschland.data.TranslationCacheEntry
import com.moonkata.lebenindeutschland.data.TranslationContentType
import com.moonkata.lebenindeutschland.data.UserPrefs
import com.moonkata.lebenindeutschland.data.translation.TranslationEngine
import com.moonkata.lebenindeutschland.ui.theme.Accent
import com.moonkata.lebenindeutschland.ui.theme.Accent700
import com.moonkata.lebenindeutschland.ui.theme.LidButton
import com.moonkata.lebenindeutschland.ui.theme.LidSpace
import com.moonkata.lebenindeutschland.ui.theme.LidType
import com.moonkata.lebenindeutschland.ui.theme.Neutral300
import com.moonkata.lebenindeutschland.ui.theme.Rule

@Composable
fun FrageScreen(
    viewModel: QuizViewModel,
    repository: QuestionRepository,
    languageCode: String?,
    onExit: () -> Unit,
    onFinished: (QuizViewModel) -> Unit,
) {
    if (viewModel.finished) {
        onFinished(viewModel)
        return
    }

    val context = LocalContext.current
    val prefs = remember { UserPrefs(context) }
    var translationsOn by remember { mutableStateOf(languageCode != null && prefs.translationsVisible) }

    val question = viewModel.currentQuestion
    var translations by remember { mutableStateOf<Map<TranslationContentType, String>>(emptyMap()) }

    LaunchedEffect(question.id, translationsOn, languageCode) {
        if (!translationsOn || languageCode == null) {
            translations = emptyMap()
            return@LaunchedEffect
        }
        val fields = listOf(
            TranslationContentType.QUESTION to question.textDe,
            TranslationContentType.ANSWER_A to question.answerA,
            TranslationContentType.ANSWER_B to question.answerB,
            TranslationContentType.ANSWER_C to question.answerC,
            TranslationContentType.ANSWER_D to question.answerD,
        )
        val result = mutableMapOf<TranslationContentType, String>()
        val newlyTranslated = mutableListOf<TranslationCacheEntry>()
        for ((type, text) in fields) {
            val cached = repository.cachedTranslation(question.id, type, languageCode)
            val value = cached ?: runCatching { TranslationEngine.translate(text, languageCode) }
                .onSuccess { newlyTranslated.add(TranslationCacheEntry(question.id, type, languageCode, it)) }
                .getOrNull()
            if (value != null) result[type] = value
        }
        repository.cacheTranslations(newlyTranslated)
        translations = result
    }

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
                    translations[TranslationContentType.QUESTION]?.let { translated ->
                        Row(modifier = Modifier.padding(top = LidSpace.x3).height(IntrinsicSize.Min)) {
                            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Accent))
                            Text(translated, style = LidType.translation, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
                Rule()

                val picked = viewModel.pickedAnswerIndex
                AnswerRow('A', question.answerA, rowState(picked, 0, question.correctAnswerIndex), translations[TranslationContentType.ANSWER_A]) { viewModel.pick(0) }
                AnswerRow('B', question.answerB, rowState(picked, 1, question.correctAnswerIndex), translations[TranslationContentType.ANSWER_B]) { viewModel.pick(1) }
                AnswerRow('C', question.answerC, rowState(picked, 2, question.correctAnswerIndex), translations[TranslationContentType.ANSWER_C]) { viewModel.pick(2) }
                AnswerRow('D', question.answerD, rowState(picked, 3, question.correctAnswerIndex), translations[TranslationContentType.ANSWER_D]) { viewModel.pick(3) }

                if (picked != null && question.explanationDe != null) {
                    Column(modifier = Modifier.padding(top = LidSpace.x4, start = LidSpace.gutter, end = LidSpace.gutter, bottom = LidSpace.x4)) {
                        Text("Erklärung. " + question.explanationDe, style = LidType.explanation)
                    }
                    Rule()
                }
            }

            Rule()
            Row(modifier = Modifier.padding(vertical = LidSpace.x4, horizontal = LidSpace.gutter)) {
                if (languageCode != null) {
                    LidButton(
                        onClick = {
                            translationsOn = !translationsOn
                            prefs.translationsVisible = translationsOn
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground),
                    ) {
                        Text(if (translationsOn) languageCode else "DE")
                    }
                    Spacer(modifier = Modifier.width(LidSpace.x2))
                }
                LidButton(
                    onClick = viewModel::next,
                    enabled = viewModel.pickedAnswerIndex != null,
                    modifier = Modifier.weight(1f),
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
