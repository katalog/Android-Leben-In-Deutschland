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
import kotlinx.coroutines.launch

/**
 * Drives the Frage screen for practice, exam, and review — they differ only in header/exit
 * behavior and which questions were selected before this ViewModel was created (see
 * QuestionRepository.examQuestions for the 30+3 exam composition). The 60-minute exam timer
 * is the one piece still missing (Phase 3).
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

    /** (questionId, wasCorrect) for every question answered this session, in order — feeds Ergebnis's per-topic breakdown. */
    val sessionResults = mutableStateListOf<Pair<Int, Boolean>>()

    private var attempt: Attempt? = null

    val total: Int get() = questions.size
    val currentQuestion: Question get() = questions[currentIndex]
    val isLastQuestion: Boolean get() = currentIndex == total - 1
    val passed: Boolean? get() = if (mode == AttemptMode.EXAM) correctCount >= EXAM_PASS_THRESHOLD else null

    init {
        viewModelScope.launch {
            attempt = repository.startAttempt(mode, bundesland, total)
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
            finished = true
            viewModelScope.launch {
                attempt?.let { repository.finishAttempt(it, correctCount, passed) }
            }
            return
        }
        currentIndex++
        pickedAnswerIndex = null
    }

    companion object {
        const val EXAM_PASS_THRESHOLD = 17
    }
}
