package com.moonkata.lebenindeutschland.ui.quiz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moonkata.lebenindeutschland.data.Attempt
import com.moonkata.lebenindeutschland.data.AttemptMode
import com.moonkata.lebenindeutschland.data.Question
import com.moonkata.lebenindeutschland.data.QuestionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Drives the Frage screen for practice, exam, and review — they differ only in header/exit
 * behavior and which questions were selected before this ViewModel was created (see
 * QuestionRepository.examQuestions for the 30+3 exam composition).
 *
 * The exam timer is timed from this ViewModel's creation (in-memory), so it survives rotation
 * (ViewModels do) but not the OS killing the whole app in the background mid-exam — resuming an
 * in-progress exam after process death isn't implemented yet.
 */
class QuizViewModel(
    private val repository: QuestionRepository,
    val mode: AttemptMode,
    private val bundesland: String?,
    val questions: List<Question>,
) : ViewModel() {

    var currentIndex by mutableStateOf(0)
        private set
    var pickedAnswerIndex by mutableStateOf<Int?>(null)
        private set
    var correctCount by mutableStateOf(0)
        private set
    var finished by mutableStateOf(false)
        private set

    /** Counts down from [EXAM_DURATION_SECONDS] in EXAM mode only; unused otherwise. */
    var examSecondsRemaining by mutableStateOf(EXAM_DURATION_SECONDS)
        private set

    /** (questionId, wasCorrect) for every question answered this session, in order — feeds Ergebnis's per-topic breakdown. */
    val sessionResults = mutableStateListOf<Pair<Int, Boolean>>()

    private var attempt: Attempt? = null
    private val examStartedAtMillis = System.currentTimeMillis()

    val total: Int get() = questions.size
    val currentQuestion: Question get() = questions[currentIndex]
    val isLastQuestion: Boolean get() = currentIndex == total - 1
    val passed: Boolean? get() = if (mode == AttemptMode.EXAM) correctCount >= EXAM_PASS_THRESHOLD else null

    init {
        viewModelScope.launch {
            attempt = repository.startAttempt(mode, bundesland, total)
        }
        if (mode == AttemptMode.EXAM) {
            viewModelScope.launch {
                while (!finished) {
                    val elapsed = (System.currentTimeMillis() - examStartedAtMillis) / 1000
                    examSecondsRemaining = (EXAM_DURATION_SECONDS - elapsed).toInt().coerceAtLeast(0)
                    if (examSecondsRemaining <= 0) {
                        finishNow()
                        break
                    }
                    delay(1000)
                }
            }
        }
    }

    fun pick(answerIndex: Int) {
        if (pickedAnswerIndex != null) return
        pickedAnswerIndex = answerIndex
        val isCorrect = answerIndex == currentQuestion.correctAnswerIndex
        if (isCorrect) correctCount++
        val question = currentQuestion
        sessionResults.add(question.id to isCorrect)
        viewModelScope.launch {
            attempt?.let { repository.recordAnswer(it.id, question.id, answerIndex, isCorrect) }
        }
    }

    fun next() {
        if (isLastQuestion) {
            finishNow()
            return
        }
        currentIndex++
        pickedAnswerIndex = null
    }

    /** Ends the attempt now, whether because the last question was answered or the exam clock hit zero. */
    private fun finishNow() {
        if (finished) return
        finished = true
        viewModelScope.launch {
            attempt?.let { repository.finishAttempt(it, correctCount, passed) }
        }
    }

    companion object {
        const val EXAM_PASS_THRESHOLD = 17
        const val EXAM_DURATION_SECONDS = 60 * 60
    }
}
