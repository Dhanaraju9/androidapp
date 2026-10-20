package com.example

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import com.example.data.SampleData
import com.example.model.GeneratedMockTest
import com.example.model.MockTestAttemptRecord
import com.example.model.Question
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import com.example.ui.screens.*
import com.example.ui.theme.SyllabusProTheme
import java.util.UUID

sealed class AppScreen {
    abstract val routeKey: String

    data object Splash : AppScreen() {
        override val routeKey: String = "splash"
    }

    data object Home : AppScreen() {
        override val routeKey: String = "home"
    }

    /**
     * Level 1 -> Level 2: Full-screen Units page for the selected Subject.
     */
    data class SyllabusExplorer(
        val subjectId: String,
        val fallbackSubject: Subject? = null
    ) : AppScreen() {
        constructor(subject: Subject, initialUnit: UnitModel? = null) : this(
            subjectId = subject.id,
            fallbackSubject = subject
        )

        override val routeKey: String = "units_$subjectId"
    }

    /**
     * Level 2 -> Level 3: Full-screen Topics page for the selected Unit.
     */
    data class UnitTopics(
        val subjectId: String,
        val unitId: String,
        val fallbackSubject: Subject? = null,
        val fallbackUnit: UnitModel? = null
    ) : AppScreen() {
        constructor(subject: Subject, unit: UnitModel) : this(
            subjectId = subject.id,
            unitId = unit.id,
            fallbackSubject = subject,
            fallbackUnit = unit
        )

        override val routeKey: String = "topics_${subjectId}_$unitId"
    }

    /**
     * Level 3 -> Level 4: Full-screen Sets page for the selected Topic.
     */
    data class TopicSets(
        val subjectId: String,
        val unitId: String,
        val topicId: String,
        val fallbackSubject: Subject? = null,
        val fallbackUnit: UnitModel? = null,
        val fallbackTopic: TopicModel? = null
    ) : AppScreen() {
        constructor(subject: Subject, unit: UnitModel, topic: TopicModel) : this(
            subjectId = subject.id,
            unitId = unit.id,
            topicId = topic.id,
            fallbackSubject = subject,
            fallbackUnit = unit,
            fallbackTopic = topic
        )

        override val routeKey: String = "sets_${subjectId}_${unitId}_$topicId"
    }

    /**
     * Level 4 -> Level 5: Full-screen Subtopics page for the selected Set.
     */
    data class SetSubtopics(
        val subjectId: String,
        val unitId: String,
        val topicId: String,
        val setId: String,
        val setNumber: Int = 1,
        val setTitle: String = "",
        val fallbackSubject: Subject? = null,
        val fallbackUnit: UnitModel? = null,
        val fallbackTopic: TopicModel? = null
    ) : AppScreen() {
        override val routeKey: String = "subtopics_${subjectId}_${unitId}_${topicId}_$setId"
    }

    /**
     * Level 5 -> Dedicated Content/Detail Screen: Full-screen Practice Arena for the selected Subtopic or Set.
     */
    data class PracticeArena(
        val subjectId: String,
        val unitId: String,
        val topicId: String,
        val setName: String,
        val setId: String = "",
        val subtopicId: String = "",
        val subtopicTitle: String = "",
        val preloadedQuestions: List<Question> = emptyList()
    ) : AppScreen() {
        override val routeKey: String =
            "practice_${subjectId}_${unitId}_${topicId}_${setId}_${subtopicId}_$setName"
    }

    data class Result(
        val score: Int,
        val total: Int,
        val correct: Int,
        val incorrect: Int,
        val timeSpent: Long
    ) : AppScreen() {
        override val routeKey: String = "result_${score}_${total}_$correct"
    }

    data object Subscription : AppScreen() {
        override val routeKey: String = "subscription"
    }

    data object Login : AppScreen() {
        override val routeKey: String = "login"
    }

    data object AdminDashboard : AppScreen() {
        override val routeKey: String = "admin_dashboard"
    }

    data object Profile : AppScreen() {
        override val routeKey: String = "profile"
    }

    data object SavedQuestions : AppScreen() {
        override val routeKey: String = "saved_questions"
    }

    data object Notifications : AppScreen() {
        override val routeKey: String = "notifications"
    }

    data object CurrentAffairs : AppScreen() {
        override val routeKey: String = "current_affairs"
    }

    data object MockTestGenerator : AppScreen() {
        override val routeKey: String = "mock_test_generator"
    }

    data object AvailableMockTests : AppScreen() {
        override val routeKey: String = "available_mock_tests"
    }

    data class FullLengthMockTest(
        val testId: String,
        val testTitle: String,
        val timeMinutes: Int
    ) : AppScreen() {
        override val routeKey: String = "full_mock_$testId"
    }

    data class MockTestResult(
        val testId: String
    ) : AppScreen() {
        override val routeKey: String = "mock_test_result_$testId"
    }

    data object MockTests : AppScreen() {
        override val routeKey: String = "mock_tests"
    }

    data object Performance : AppScreen() {
        override val routeKey: String = "performance"
    }

    data object ContactUs : AppScreen() {
        override val routeKey: String = "contact_us"
    }

    data class LegalSupport(val initialSection: String? = null) : AppScreen() {
        override val routeKey: String = "legal_support_${initialSection ?: "default"}"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.util.AnimationConfigManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            SyllabusProTheme {
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
    val saveableStateHolder = rememberSaveableStateHolder()
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }
    var screenHistory by remember { mutableStateOf<List<AppScreen>>(emptyList()) }
    var isNavigatingForward by remember { mutableStateOf(true) }
    var lastNavigationTimeMs by remember { mutableLongStateOf(0L) }
    var lastNavigatedRouteKey by remember { mutableStateOf("") }

    fun navigateTo(screen: AppScreen) {
        val now = SystemClock.elapsedRealtime()
        // Prevent duplicate destinations from rapid repeated taps
        if (currentScreen.routeKey == screen.routeKey) return
        if (lastNavigatedRouteKey == screen.routeKey && (now - lastNavigationTimeMs) < 300L) return

        lastNavigationTimeMs = now
        lastNavigatedRouteKey = screen.routeKey
        isNavigatingForward = true
        if (screenHistory.lastOrNull()?.routeKey != currentScreen.routeKey) {
            screenHistory = screenHistory + currentScreen
        }
        currentScreen = screen
    }

    fun goBack() {
        val poppedScreen = currentScreen
        val last = screenHistory.lastOrNull()
        isNavigatingForward = false
        if (last != null) {
            screenHistory = screenHistory.dropLast(1)
            currentScreen = last
            saveableStateHolder.removeState(poppedScreen.routeKey)
        } else if (currentScreen != AppScreen.Home && currentScreen != AppScreen.Splash) {
            currentScreen = AppScreen.Home
            saveableStateHolder.removeState(poppedScreen.routeKey)
        }
    }

    fun navigateToHome() {
        if (currentScreen != AppScreen.Home) {
            isNavigatingForward = false
            screenHistory = emptyList()
            currentScreen = AppScreen.Home
        }
    }

    BackHandler(enabled = currentScreen != AppScreen.Home && currentScreen != AppScreen.Splash) {
        goBack()
    }

    val activeEngineConfig = com.example.util.AnimationConfigManager.getActiveEngineConfig()
    val slowMo by com.example.util.AnimationConfigManager.sandboxSlowMoFactor

    androidx.compose.animation.AnimatedContent(
        targetState = currentScreen,
        contentKey = { it.routeKey },
        transitionSpec = {
            com.example.ui.components.PremiumAnimationEngine.buildScreenTransition(
                masterConfig = activeEngineConfig.masterConfig,
                isForward = isNavigatingForward,
                slowMoFactor = slowMo
            )
        },
        label = "AppScreenTransition"
    ) { screen ->
        saveableStateHolder.SaveableStateProvider(screen.routeKey) {
            when (screen) {
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
                            navigateTo(AppScreen.SyllabusExplorer(subject.id, subject))
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
                        onNavigateToPrivacy = {
                            navigateTo(AppScreen.ContactUs)
                        },
                        onNavigateToPrivacySection = { section ->
                            if (section == "contact_us") {
                                navigateTo(AppScreen.ContactUs)
                            } else {
                                navigateTo(AppScreen.LegalSupport(section))
                            }
                        },
                        onNavigateToMockTestGenerator = {
                            navigateTo(AppScreen.MockTestGenerator)
                        },
                        onNavigateToAvailableMockTests = {
                            navigateTo(AppScreen.AvailableMockTests)
                        },
                        onNavigateToMockTests = {
                            navigateTo(AppScreen.AvailableMockTests)
                        },
                        onNavigateToDailyQuiz = {
                            navigateTo(AppScreen.AvailableMockTests)
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
                            navigateTo(
                                AppScreen.TopicSets(
                                    subjectId = subject.id,
                                    unitId = unit.id,
                                    topicId = topic.id,
                                    fallbackSubject = subject,
                                    fallbackUnit = unit,
                                    fallbackTopic = topic
                                )
                            )
                        },
                        onNavigateToUnit = { subject, unit ->
                            navigateTo(
                                AppScreen.UnitTopics(
                                    subjectId = subject.id,
                                    unitId = unit.id,
                                    fallbackSubject = subject,
                                    fallbackUnit = unit
                                )
                            )
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
                        subjectId = screen.subjectId,
                        fallbackSubject = screen.fallbackSubject,
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onSelectUnit = { subject, unit ->
                            navigateTo(
                                AppScreen.UnitTopics(
                                    subjectId = subject.id,
                                    unitId = unit.id,
                                    fallbackSubject = subject,
                                    fallbackUnit = unit
                                )
                            )
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.UnitTopics -> {
                    UnitTopicsScreen(
                        subjectId = screen.subjectId,
                        unitId = screen.unitId,
                        fallbackSubject = screen.fallbackSubject,
                        fallbackUnit = screen.fallbackUnit,
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onSelectTopic = { subject, unit, topic ->
                            navigateTo(
                                AppScreen.TopicSets(
                                    subjectId = subject.id,
                                    unitId = unit.id,
                                    topicId = topic.id,
                                    fallbackSubject = subject,
                                    fallbackUnit = unit,
                                    fallbackTopic = topic
                                )
                            )
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.TopicSets -> {
                    TopicSetsScreen(
                        subjectId = screen.subjectId,
                        unitId = screen.unitId,
                        topicId = screen.topicId,
                        fallbackSubject = screen.fallbackSubject,
                        fallbackUnit = screen.fallbackUnit,
                        fallbackTopic = screen.fallbackTopic,
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onSelectSet = { practiceSet, _ ->
                            navigateTo(
                                AppScreen.PracticeArena(
                                    subjectId = screen.subjectId,
                                    unitId = screen.unitId,
                                    topicId = screen.topicId,
                                    setName = practiceSet.title,
                                    setId = practiceSet.id,
                                    subtopicId = "",
                                    subtopicTitle = "",
                                    preloadedQuestions = practiceSet.questions
                                )
                            )
                        },
                        onSelectSubtopic = { subtopicPracticeSet, subtopicTitle ->
                            navigateTo(
                                AppScreen.PracticeArena(
                                    subjectId = screen.subjectId,
                                    unitId = screen.unitId,
                                    topicId = screen.topicId,
                                    setName = subtopicTitle,
                                    setId = "",
                                    subtopicId = subtopicPracticeSet.id,
                                    subtopicTitle = subtopicTitle,
                                    preloadedQuestions = subtopicPracticeSet.questions
                                )
                            )
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.SetSubtopics -> {
                    SetSubtopicsScreen(
                        subjectId = screen.subjectId,
                        unitId = screen.unitId,
                        topicId = screen.topicId,
                        setId = screen.setId,
                        setNumber = screen.setNumber,
                        setTitle = screen.setTitle,
                        fallbackSubject = screen.fallbackSubject,
                        fallbackUnit = screen.fallbackUnit,
                        fallbackTopic = screen.fallbackTopic,
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onSelectSubtopic = { subtopicPracticeSet, subtopicTitle ->
                            navigateTo(
                                AppScreen.PracticeArena(
                                    subjectId = screen.subjectId,
                                    unitId = screen.unitId,
                                    topicId = screen.topicId,
                                    setName = subtopicTitle,
                                    setId = screen.setId,
                                    subtopicId = subtopicPracticeSet.id,
                                    subtopicTitle = subtopicTitle,
                                    preloadedQuestions = subtopicPracticeSet.questions
                                )
                            )
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.PracticeArena -> {
                    val questions = remember(
                        screen.subjectId,
                        screen.unitId,
                        screen.topicId,
                        screen.setName,
                        screen.subtopicTitle,
                        screen.preloadedQuestions
                    ) {
                        when {
                            screen.preloadedQuestions.isNotEmpty() -> screen.preloadedQuestions
                            screen.subtopicTitle.isNotBlank() -> {
                                SampleData.getQuestionsForPractice(
                                    screen.subjectId,
                                    screen.unitId,
                                    screen.topicId,
                                    screen.subtopicTitle
                                ).ifEmpty {
                                    SampleData.getQuestionsForPractice(
                                        screen.subjectId,
                                        screen.unitId,
                                        screen.topicId,
                                        ""
                                    )
                                }
                            }
                            else -> {
                                SampleData.getQuestionsForPractice(
                                    screen.subjectId,
                                    screen.unitId,
                                    screen.topicId,
                                    screen.setName
                                ).ifEmpty {
                                    SampleData.getQuestionsForPractice(
                                        screen.subjectId,
                                        screen.unitId,
                                        screen.topicId,
                                        ""
                                    )
                                }
                            }
                        }.ifEmpty { SampleData.questionsState.take(10) }
                    }
                    val sessionKey = screen.subtopicId.ifBlank { "session-${screen.setName}" }
                    PracticeArenaScreen(
                        practiceSessionId = sessionKey,
                        questions = questions,
                        title = screen.subtopicTitle.ifBlank { screen.setName },
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
                        onBackToHome = { navigateToHome() }
                    )
                }

                is AppScreen.Subscription -> {
                    SubscriptionScreen(
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() }
                    )
                }

                is AppScreen.Login -> {
                    LoginScreen(
                        onBack = { goBack() },
                        onLoginSuccess = { navigateToHome() }
                    )
                }

                is AppScreen.AdminDashboard -> {
                    AdminDashboardScreen(
                        onSwitchToUserPage = { navigateToHome() },
                        onLogout = { navigateToHome() },
                        onNavigateToSubscription = { navigateTo(AppScreen.Subscription) }
                    )
                }

                is AppScreen.Profile -> {
                    UserProfileScreen(
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onNavigateToSubscription = { navigateTo(AppScreen.Subscription) },
                        onNavigateToLogin = { navigateTo(AppScreen.Login) },
                        onNavigateToSavedQuestions = { navigateTo(AppScreen.SavedQuestions) },
                        onNavigateToContact = { navigateTo(AppScreen.ContactUs) },
                        onNavigateToPrivacy = { navigateTo(AppScreen.LegalSupport("privacy_policy")) }
                    )
                }

                is AppScreen.ContactUs -> {
                    ContactUsScreen(
                        onBack = { goBack() },
                        onNavigateToFaq = { navigateTo(AppScreen.LegalSupport("faq")) },
                        onNavigateToHelpCenter = { navigateTo(AppScreen.LegalSupport("help_center")) }
                    )
                }

                is AppScreen.LegalSupport -> {
                    LegalSupportCenterScreen(
                        initialSection = screen.initialSection,
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() }
                    )
                }

                is AppScreen.SavedQuestions -> {
                    SavedQuestionsScreen(
                        savedQuestions = SampleData.questionsState.take(10),
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onRemoveBookmark = {},
                        onPracticeSaved = { _, _ -> },
                        onNavigateToExplore = { navigateToHome() }
                    )
                }

                is AppScreen.Notifications -> {
                    NotificationsScreen(
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() }
                    )
                }

                is AppScreen.CurrentAffairs -> {
                    CurrentAffairsScreen(
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onNavigateToPractice = { subId, unitId, topicId, setName ->
                            navigateTo(AppScreen.PracticeArena(subId, unitId, topicId, setName))
                        },
                        onNavigateToSubscription = { navigateTo(AppScreen.Subscription) }
                    )
                }

                is AppScreen.MockTestGenerator -> {
                    MockTestGeneratorScreen(
                        onNavigateBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onTestGenerated = { newTest ->
                            if (SampleData.generatedMockTestsState.none { it.id == newTest.id }) {
                                SampleData.generatedMockTestsState.add(0, newTest)
                            }
                        },
                        pastTests = SampleData.generatedMockTestsState,
                        onStartGeneratedTest = { test ->
                            if (SampleData.generatedMockTestsState.none { it.id == test.id }) {
                                SampleData.generatedMockTestsState.add(0, test)
                            }
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = test.id,
                                    testTitle = test.title,
                                    timeMinutes = test.durationMinutes
                                )
                            )
                        },
                        onNavigateToAvailableTests = {
                            navigateTo(AppScreen.AvailableMockTests)
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.AvailableMockTests -> {
                    AvailableMockTestsScreen(
                        onNavigateBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onNavigateToGenerator = {
                            navigateTo(AppScreen.MockTestGenerator)
                        },
                        onStartTest = { test ->
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = test.id,
                                    testTitle = test.title,
                                    timeMinutes = test.durationMinutes
                                )
                            )
                        },
                        onResumeTest = { test ->
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = test.id,
                                    testTitle = test.title,
                                    timeMinutes = test.durationMinutes
                                )
                            )
                        },
                        onReattemptTest = { test ->
                            val reattemptTest = test.copy(
                                status = "Not Started",
                                userAnswers = emptyMap(),
                                score = null,
                                correctAnswers = 0,
                                wrongAnswers = 0,
                                unanswered = test.questions.size
                            )
                            val index = SampleData.generatedMockTestsState.indexOfFirst { it.id == test.id }
                            if (index != -1) {
                                SampleData.generatedMockTestsState[index] = reattemptTest
                            }
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = reattemptTest.id,
                                    testTitle = reattemptTest.title,
                                    timeMinutes = reattemptTest.durationMinutes
                                )
                            )
                        },
                        onViewResult = { test ->
                            navigateTo(AppScreen.MockTestResult(test.id))
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.FullLengthMockTest -> {
                    val activeTest = SampleData.generatedMockTestsState.find { it.id == screen.testId }
                    if (activeTest != null) {
                        FullLengthMockTestScreen(
                            test = activeTest,
                            onSubmitTest = { correct, wrong, score, userAnswers, timeTakenSeconds ->
                                val totalQs = activeTest.questions.size
                                val unanswered = (totalQs - (correct + wrong)).coerceAtLeast(0)
                                val totalMarks = if (activeTest.totalMarks > 0) activeTest.totalMarks else (totalQs * activeTest.positiveMarks).coerceAtLeast(1.0)
                                val attempted = correct + wrong
                                val percentage = if (totalMarks > 0) (score / totalMarks * 100.0).coerceIn(0.0, 100.0) else 0.0
                                val accuracy = if (attempted > 0) "${(correct.toDouble() / attempted * 100).toInt()}%" else "0%"
                                val newRecord = MockTestAttemptRecord(
                                    attemptId = UUID.randomUUID().toString(),
                                    attemptNumber = activeTest.attemptsHistory.size + 1,
                                    completedAt = System.currentTimeMillis(),
                                    score = score,
                                    totalMarks = totalMarks,
                                    correctAnswers = correct,
                                    wrongAnswers = wrong,
                                    unanswered = unanswered,
                                    timeTakenSeconds = timeTakenSeconds,
                                    percentage = percentage,
                                    accuracyStr = accuracy,
                                    userAnswers = userAnswers
                                )
                                val updatedHistory = activeTest.attemptsHistory + newRecord
                                val updatedTest = activeTest.copy(
                                    status = "Completed",
                                    score = score,
                                    correctAnswers = correct,
                                    wrongAnswers = wrong,
                                    unanswered = unanswered,
                                    userAnswers = userAnswers,
                                    timeTakenSeconds = timeTakenSeconds,
                                    attemptsHistory = updatedHistory
                                )
                                val idx = SampleData.generatedMockTestsState.indexOfFirst { it.id == activeTest.id }
                                if (idx != -1) {
                                    SampleData.generatedMockTestsState[idx] = updatedTest
                                }
                                navigateTo(AppScreen.MockTestResult(activeTest.id))
                            },
                            onPauseTest = { correct, wrong, unanswered, markedForReview, visited, userAnswers ->
                                val updatedTest = activeTest.copy(
                                    status = if (userAnswers.isNotEmpty()) "In Progress" else activeTest.status,
                                    userAnswers = userAnswers
                                )
                                val idx = SampleData.generatedMockTestsState.indexOfFirst { it.id == activeTest.id }
                                if (idx != -1) {
                                    SampleData.generatedMockTestsState[idx] = updatedTest
                                }
                                goBack()
                            },
                            onNavigateToSubscription = {
                                navigateTo(AppScreen.Subscription)
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            goBack()
                        }
                    }
                }

                is AppScreen.MockTestResult -> {
                    val resultTest = SampleData.generatedMockTestsState.find { it.id == screen.testId }
                    if (resultTest != null) {
                        MockTestResultScreen(
                            test = resultTest,
                            onBack = { goBack() },
                            onNavigateToHome = { navigateToHome() }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            goBack()
                        }
                    }
                }

                is AppScreen.MockTests -> {
                    AvailableMockTestsScreen(
                        onNavigateBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onNavigateToGenerator = {
                            navigateTo(AppScreen.MockTestGenerator)
                        },
                        onStartTest = { test ->
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = test.id,
                                    testTitle = test.title,
                                    timeMinutes = test.durationMinutes
                                )
                            )
                        },
                        onResumeTest = { test ->
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = test.id,
                                    testTitle = test.title,
                                    timeMinutes = test.durationMinutes
                                )
                            )
                        },
                        onReattemptTest = { test ->
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = test.id,
                                    testTitle = test.title,
                                    timeMinutes = test.durationMinutes
                                )
                            )
                        },
                        onViewResult = { test ->
                            navigateTo(AppScreen.MockTestResult(test.id))
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.Performance -> {
                    PerformanceBoardScreen(
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onNavigateToSubject = { subject ->
                            navigateTo(AppScreen.SyllabusExplorer(subject))
                        },
                        onNavigateToTopic = { subject, unit, topic ->
                            navigateTo(AppScreen.TopicSets(subject, unit, topic))
                        },
                        onNavigateToMockTests = {
                            navigateTo(AppScreen.MockTests)
                        }
                    )
                }
            }
        }
    }
}
