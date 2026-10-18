package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.data.SampleData

sealed class AppScreen {
    data object Splash : AppScreen()
    data object Home : AppScreen()
    data class SyllabusExplorer(val subject: Subject, val initialUnit: UnitModel? = null) : AppScreen()
    data class TopicSets(val subject: Subject, val unit: UnitModel, val topic: TopicModel) : AppScreen()
    data class PracticeArena(val subjectId: String, val unitId: String, val topicId: String, val setName: String) : AppScreen()
    data class Result(val score: Int, val total: Int, val correct: Int, val incorrect: Int, val timeSpent: Long) : AppScreen()
    data object Subscription : AppScreen()
    data object Login : AppScreen()
    data object AdminDashboard : AppScreen()
    data object Profile : AppScreen()
    data object SavedQuestions : AppScreen()
    data object Notifications : AppScreen()
    data object CurrentAffairs : AppScreen()
    data object MockTests : AppScreen()
    data object Performance : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigationHost()
                }
            }
        }
    }
}

@Composable
fun AppNavigationHost() {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }
    var screenHistory by remember { mutableStateOf<List<AppScreen>>(emptyList()) }

    fun navigateTo(screen: AppScreen) {
        if (currentScreen != screen) {
            screenHistory = screenHistory + currentScreen
            currentScreen = screen
        }
    }

    fun goBack() {
        val last = screenHistory.lastOrNull()
        if (last != null) {
            screenHistory = screenHistory.dropLast(1)
            currentScreen = last
        } else if (currentScreen != AppScreen.Home && currentScreen != AppScreen.Splash) {
            currentScreen = AppScreen.Home
        }
    }

    BackHandler(enabled = currentScreen != AppScreen.Home && currentScreen != AppScreen.Splash) {
        goBack()
    }

    when (val screen = currentScreen) {
        is AppScreen.Splash -> {
            AppSplashScreen(
                onLoadingFinished = {
                    currentScreen = AppScreen.Home
                }
            )
        }
        is AppScreen.Home -> {
            HomeScreen(
                onNavigateToSubject = { subject ->
                    navigateTo(AppScreen.SyllabusExplorer(subject))
                },
                onNavigateToCurrentAffairs = {
                    navigateTo(AppScreen.CurrentAffairs)
                },
                onNavigateToPerformance = {
                    navigateTo(AppScreen.Performance)
                },
                onNavigateToSubscription = {
                    navigateTo(AppScreen.Subscription)
                },
                onNavigateToScientificCalculator = {},
                onNavigateToPrivacy = {},
                onNavigateToMockTests = {
                    navigateTo(AppScreen.MockTests)
                },
                onNavigateToDailyQuiz = {
                    navigateTo(AppScreen.MockTests)
                },
                onNavigateToProfile = {
                    navigateTo(AppScreen.Profile)
                },
                onNavigateToLogin = {
                    navigateTo(AppScreen.Login)
                },
                onNavigateToSavedQuestions = {
                    navigateTo(AppScreen.SavedQuestions)
                },
                onNavigateToAdminDashboard = {
                    navigateTo(AppScreen.AdminDashboard)
                },
                onNavigateToTopic = { subject, unit, topic ->
                    navigateTo(AppScreen.TopicSets(subject, unit, topic))
                },
                onNavigateToUnit = { subject, unit ->
                    navigateTo(AppScreen.SyllabusExplorer(subject, unit))
                },
                onNavigateToLeaderboard = {},
                onNavigateToOfflineManager = {},
                onNavigateToNotifications = {
                    navigateTo(AppScreen.Notifications)
                },
                onNavigateToPreviousYearPapers = {}
            )
        }
        is AppScreen.SyllabusExplorer -> {
            SyllabusExplorerScreen(
                subject = screen.subject,
                initialUnit = screen.initialUnit,
                onBack = { goBack() },
                onNavigateToHome = { currentScreen = AppScreen.Home },
                onSelectTopic = { unit, topic ->
                    navigateTo(AppScreen.TopicSets(screen.subject, unit, topic))
                },
                onNavigateToSubscription = {
                    navigateTo(AppScreen.Subscription)
                }
            )
        }
        is AppScreen.TopicSets -> {
            TopicSetsScreen(
                subject = screen.subject,
                unit = screen.unit,
                topic = screen.topic,
                onBack = { goBack() },
                onNavigateToHome = { currentScreen = AppScreen.Home },
                onStartSet = { practiceSet, _ ->
                    navigateTo(AppScreen.PracticeArena(screen.subject.id, screen.unit.id, screen.topic.id, practiceSet.title))
                },
                onNavigateToSubscription = {
                    navigateTo(AppScreen.Subscription)
                }
            )
        }
        is AppScreen.PracticeArena -> {
            val questions = SampleData.getQuestionsForPractice(screen.subjectId, screen.unitId, screen.topicId, screen.setName)
            PracticeArenaScreen(
                practiceSessionId = "session-${screen.setName}",
                questions = questions.ifEmpty { SampleData.questionsState.take(10) },
                title = screen.setName,
                onBack = { goBack() },
                onSubmitTest = { score, total, _, _, correctCount ->
                    val wrongCount = total - correctCount
                    navigateTo(AppScreen.Result(score.toInt(), total, correctCount, wrongCount, 0L))
                }
            )
        }
        is AppScreen.Result -> {
            val questions = SampleData.questionsState.take(screen.total.coerceAtLeast(1))
            ResultScreen(
                questions = questions,
                userAnswers = emptyMap(),
                correctCount = screen.correct,
                wrongCount = screen.incorrect,
                score = screen.score.toDouble(),
                onPracticeAgain = { goBack() },
                onBackToHome = { currentScreen = AppScreen.Home }
            )
        }
        is AppScreen.Subscription -> {
            SubscriptionScreen(
                onBack = { goBack() },
                onNavigateToHome = { currentScreen = AppScreen.Home }
            )
        }
        is AppScreen.Login -> {
            LoginScreen(
                onBack = { goBack() },
                onLoginSuccess = { currentScreen = AppScreen.Home }
            )
        }
        is AppScreen.AdminDashboard -> {
            AdminDashboardScreen(
                onSwitchToUserPage = { currentScreen = AppScreen.Home },
                onLogout = { currentScreen = AppScreen.Home },
                onNavigateToSubscription = { navigateTo(AppScreen.Subscription) }
            )
        }
        is AppScreen.Profile -> {
            UserProfileScreen(
                onBack = { goBack() },
                onNavigateToHome = { currentScreen = AppScreen.Home },
                onNavigateToSubscription = { navigateTo(AppScreen.Subscription) },
                onNavigateToLogin = { navigateTo(AppScreen.Login) },
                onNavigateToSavedQuestions = { navigateTo(AppScreen.SavedQuestions) }
            )
        }
        is AppScreen.SavedQuestions -> {
            SavedQuestionsScreen(
                savedQuestions = SampleData.questionsState.take(10),
                onBack = { goBack() },
                onNavigateToHome = { currentScreen = AppScreen.Home },
                onRemoveBookmark = {},
                onPracticeSaved = { _, _ -> },
                onNavigateToExplore = { currentScreen = AppScreen.Home }
            )
        }
        is AppScreen.Notifications -> {
            NotificationsScreen(
                onBack = { goBack() },
                onNavigateToHome = { currentScreen = AppScreen.Home }
            )
        }
        is AppScreen.CurrentAffairs -> {
            CurrentAffairsScreen(
                onBack = { goBack() },
                onNavigateToHome = { currentScreen = AppScreen.Home },
                onNavigateToPractice = { subId, unitId, topicId, setName ->
                    navigateTo(AppScreen.PracticeArena(subId, unitId, topicId, setName))
                },
                onNavigateToSubscription = { navigateTo(AppScreen.Subscription) }
            )
        }
        is AppScreen.MockTests -> {
            MockTestSeriesScreen(
                onBack = { goBack() },
                onNavigateToHome = { currentScreen = AppScreen.Home },
                onNavigateToSubscription = { navigateTo(AppScreen.Subscription) }
            )
        }
        is AppScreen.Performance -> {
            PerformanceBoardScreen(
                onBack = { goBack() },
                onNavigateToHome = { currentScreen = AppScreen.Home }
            )
        }
    }
}
