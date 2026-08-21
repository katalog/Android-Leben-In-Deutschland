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
import com.moonkata.lebenindeutschland.data.translation.PreTranslateWorker
import com.moonkata.lebenindeutschland.ui.home.BundeslandPickerScreen
import com.moonkata.lebenindeutschland.ui.home.StartScreen
import com.moonkata.lebenindeutschland.ui.language.SpracheScreen
import com.moonkata.lebenindeutschland.ui.more.MehrScreen
import com.moonkata.lebenindeutschland.ui.quiz.FrageScreen
import com.moonkata.lebenindeutschland.ui.quiz.QuizViewModel
import com.moonkata.lebenindeutschland.ui.result.ErgebnisScreen
import com.moonkata.lebenindeutschland.ui.stats.StatistikScreen
import com.moonkata.lebenindeutschland.ui.theme.BottomTab
import kotlinx.coroutines.launch

private object Routes {
    const val SPRACHE = "sprache"
    const val START = "start"
    const val FRAGE = "frage"
    const val ERGEBNIS = "ergebnis"
    const val BUNDESLAND_PICKER = "bundesland_picker"
    const val BUNDESLAND_ONBOARDING = "bundesland_onboarding"
    const val STATISTIK = "statistik"
    const val MEHR = "mehr"
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

    fun goToTab(tab: BottomTab) {
        when (tab) {
            BottomTab.UEBEN -> navController.popBackStack(Routes.START, inclusive = false)
            BottomTab.PRUEFUNG ->
                scope.launch { startQuiz(AttemptMode.EXAM, prefs.bundesland, repository.examQuestions(prefs.bundesland)) }
            BottomTab.FORTSCHRITT ->
                navController.navigate(Routes.STATISTIK) { popUpTo(Routes.START); launchSingleTop = true }
            BottomTab.MEHR ->
                navController.navigate(Routes.MEHR) { popUpTo(Routes.START); launchSingleTop = true }
        }
    }

    val startDestination = when {
        prefs.selectedLanguage == null -> Routes.SPRACHE
        prefs.bundesland == null -> Routes.BUNDESLAND_ONBOARDING
        else -> Routes.START
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SPRACHE) {
            SpracheScreen(onLanguageChosen = { code ->
                prefs.selectedLanguage = code
                PreTranslateWorker.enqueue(context, code)
                if (prefs.bundesland == null) {
                    navController.navigate(Routes.BUNDESLAND_ONBOARDING)
                } else {
                    // popUpTo(graph.id) clears the ENTIRE back stack, unlike
                    // popUpTo(graph.startDestinationId): that id is fixed to whatever destination
                    // the graph first started at, and popUpTo only pops up to the nearest matching
                    // entry — so when this is reached a second time (Mehr's "change language"),
                    // the original onboarding entries are long gone and it would only pop the
                    // freshly-pushed Sprache entry, leaving stale Start/Mehr entries underneath.
                    navController.navigate(Routes.START) { popUpTo(navController.graph.id) { inclusive = true } }
                }
            })
        }

        composable(Routes.BUNDESLAND_ONBOARDING) {
            BundeslandPickerScreen(isOnboardingStep = true, onChosen = { code ->
                prefs.bundesland = code
                // Works whether this screen was reached via Sprache (fresh install) or was
                // itself the start destination (language already set, Bundesland wasn't).
                navController.navigate(Routes.START) { popUpTo(navController.graph.startDestinationId) { inclusive = true } }
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
                onPickBundesland = { navController.navigate(Routes.BUNDESLAND_PICKER) },
                onStartExam = {
                    scope.launch { startQuiz(AttemptMode.EXAM, prefs.bundesland, repository.examQuestions(prefs.bundesland)) }
                },
                onReviewWrong = {
                    scope.launch { startQuiz(AttemptMode.REVIEW, null, repository.globalWrongQuestions()) }
                },
                onSelectTab = ::goToTab,
            )
        }

        composable(Routes.BUNDESLAND_PICKER) {
            BundeslandPickerScreen(onChosen = { code ->
                prefs.bundesland = code
                navController.popBackStack()
            })
        }

        composable(Routes.STATISTIK) {
            StatistikScreen(repository = repository, onSelectTab = ::goToTab)
        }

        composable(Routes.MEHR) {
            MehrScreen(
                repository = repository,
                onChangeLanguage = { navController.navigate(Routes.SPRACHE) },
                onChangeBundesland = { navController.navigate(Routes.BUNDESLAND_PICKER) },
                onSelectTab = ::goToTab,
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
                repository = repository,
                languageCode = prefs.selectedLanguage,
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
