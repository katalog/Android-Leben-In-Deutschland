package com.moonkata.lebenindeutschland.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moonkata.lebenindeutschland.data.AttemptMode
import com.moonkata.lebenindeutschland.data.LidDatabase
import com.moonkata.lebenindeutschland.data.Question
import com.moonkata.lebenindeutschland.data.QuestionRepository
import com.moonkata.lebenindeutschland.data.UserPrefs
import com.moonkata.lebenindeutschland.ui.home.StartScreen
import com.moonkata.lebenindeutschland.ui.language.SpracheScreen
import com.moonkata.lebenindeutschland.ui.quiz.FrageScreen
import com.moonkata.lebenindeutschland.ui.quiz.QuizViewModel
import com.moonkata.lebenindeutschland.ui.result.ErgebnisScreen
import kotlinx.coroutines.launch

private object Routes {
    const val SPRACHE = "sprache"
    const val START = "start"
    const val FRAGE = "frage"
    const val ERGEBNIS = "ergebnis"
}

@Composable
fun AppNavHost() {
    val context = LocalContext.current
    val prefs = remember { UserPrefs(context) }
    val repository = remember { QuestionRepository(LidDatabase.get(context)) }
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    var pendingQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var pendingMode by remember { mutableStateOf(AttemptMode.PRACTICE) }
    var pendingBundesland by remember { mutableStateOf<String?>(null) }
    var finishedViewModel by remember { mutableStateOf<QuizViewModel?>(null) }
    var seeded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        LidDatabase.ensureSeeded(context)
        seeded = true
    }

    if (!seeded) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {}
        }
        return
    }

    fun startQuiz(mode: AttemptMode, bundesland: String?, questions: List<Question>) {
        if (questions.isEmpty()) return
        pendingMode = mode
        pendingBundesland = bundesland
        pendingQuestions = questions
        navController.navigate(Routes.FRAGE)
    }

    val startDestination = if (prefs.selectedLanguage == null) Routes.SPRACHE else Routes.START

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SPRACHE) {
            SpracheScreen(onLanguageChosen = { code ->
                prefs.selectedLanguage = code
                navController.navigate(Routes.START) { popUpTo(Routes.SPRACHE) { inclusive = true } }
            })
        }

        composable(Routes.START) {
            StartScreen(
                repository = repository,
                onPracticeTopic = { topicId ->
                    scope.launch { startQuiz(AttemptMode.PRACTICE, null, repository.randomByTopic(topicId)) }
                },
                onPracticeBundesland = { code ->
                    scope.launch { startQuiz(AttemptMode.PRACTICE, code, repository.randomBundesland(code, 10)) }
                },
                onStartExam = {
                    scope.launch { startQuiz(AttemptMode.EXAM, prefs.bundesland, repository.examQuestions(prefs.bundesland)) }
                },
            )
        }

        composable(Routes.FRAGE) {
            val factory = remember(pendingQuestions) {
                viewModelFactory {
                    initializer { QuizViewModel(repository, pendingMode, pendingBundesland, pendingQuestions) }
                }
            }
            val quizViewModel: QuizViewModel = viewModel(factory = factory)
            FrageScreen(
                viewModel = quizViewModel,
                onExit = { navController.popBackStack() },
                onFinished = { vm ->
                    finishedViewModel = vm
                    navController.navigate(Routes.ERGEBNIS) { popUpTo(Routes.FRAGE) { inclusive = true } }
                },
            )
        }

        composable(Routes.ERGEBNIS) {
            val vm = finishedViewModel
            if (vm == null) {
                // Reaching Ergebnis without a finished attempt (e.g. process death); bounce home.
                navController.popBackStack(Routes.START, inclusive = false)
            } else {
                ErgebnisScreen(
                    viewModel = vm,
                    onStart = { navController.popBackStack(Routes.START, inclusive = false) },
                    onReviewWrong = { wrongIds ->
                        scope.launch {
                            val questions = repository.byIds(wrongIds)
                            pendingMode = AttemptMode.REVIEW
                            pendingBundesland = null
                            pendingQuestions = questions
                            navController.navigate(Routes.FRAGE) { popUpTo(Routes.ERGEBNIS) { inclusive = true } }
                        }
                    },
                )
            }
        }
    }
}
