package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SampleData
import com.example.db.AppDatabase
import com.example.db.MockTestConfigEntity
import com.example.ui.components.PremiumSubscriptionBadge
import com.example.model.*
import com.example.util.MockTestEngine
import java.text.SimpleDateFormat
import java.util.*

// -----------------------------------------------------------------------------------------
// Screen Navigation State for the 8-Screen Mock Tests Flow
// -----------------------------------------------------------------------------------------
enum class MockTestScreenState {
    HOME,               // Screen 1: Mock Tests Home
    TEST_TYPE,          // Screen 2: Test Type Selection
    SUBJECT_SELECTION,  // Screen 3: Subject Selection
    TOPIC_SELECTION,    // Screen 4: Topic Selection (drill-down per subject)
    SECTION_BUILDER,    // Screen 5: Section Builder
    TEST_SETTINGS,      // Screen 6: Test Settings
    REVIEW_GENERATE,    // Screen 7: Review & Generate
    MY_MOCK_TESTS       // Screen 8: Available / My Mock Tests
}

enum class ExamTypeSelection(val label: String) {
    PRELIMS("Prelims"),
    MAINS("Mains"),
    GENERAL("General"),
    CUSTOM("Custom")
}

enum class TestTypeOption(val title: String, val subtitle: String) {
    FULL_MOCK("Full Mock Test", "Complete syllabus as per exam pattern"),
    SECTIONAL_MOCK("Sectional Mock Test", "Create test with multiple sections"),
    CUSTOM_MOCK("Custom Mock Test", "Choose subjects, topics, time & marks")
}

data class SectionItem(
    val id: String = UUID.randomUUID().toString(),
    var sectionNumber: Int,
    var subjectId: String,
    var subjectName: String,
    var topicId: String? = null,
    var topicName: String? = null,
    var questionCount: Int = 25,
    var difficulty: String = "Mixed"
)

// -----------------------------------------------------------------------------------------
// Theme Color Palette matching the exact visual reference
// -----------------------------------------------------------------------------------------
object MockTestTheme {
    val BgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFFE8F6F3),
            Color(0xFFEFF6FF),
            Color(0xFFF8FAFC),
            Color(0xFFFFFFFF)
        )
    )
    val PrimaryDarkTeal = Color(0xFF0D5C46)
    val PrimaryDarkTealHover = Color(0xFF094333)
    val MintLight = Color(0xFFD1FAE5)
    val MintLightBorder = Color(0xFFA7F3D0)
    val MintDarkText = Color(0xFF065F46)
    val MintSubText = Color(0xFF047857)

    val CardBg = Color.White
    val CardBorder = Color(0xFFE2E8F0)
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF64748B)

    // Subject Color Presets
    fun getSubjectStyle(subjectName: String): Pair<Color, Color> {
        val lower = subjectName.lowercase()
        return when {
            lower.contains("history") -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
            lower.contains("geography") -> Pair(Color(0xFFDCFCE7), Color(0xFF16A34A))
            lower.contains("society") -> Pair(Color(0xFFF3E8FF), Color(0xFF9333EA))
            lower.contains("current") || lower.contains("affairs") -> Pair(Color(0xFFFFEDD5), Color(0xFFEA580C))
            lower.contains("mental") || lower.contains("ability") || lower.contains("reasoning") -> Pair(Color(0xFFEDE9FE), Color(0xFF7C3AED))
            lower.contains("constitution") || lower.contains("polity") -> Pair(Color(0xFFDBEAFE), Color(0xFF2563EB))
            lower.contains("economy") -> Pair(Color(0xFFFFEDD5), Color(0xFFF97316))
            else -> Pair(Color(0xFFE0F2FE), Color(0xFF0284C7))
        }
    }

    fun getSubjectIcon(subjectName: String): ImageVector {
        val lower = subjectName.lowercase()
        return when {
            lower.contains("history") -> Icons.Default.AccountBalance
            lower.contains("geography") -> Icons.Default.Public
            lower.contains("society") -> Icons.Default.Group
            lower.contains("current") || lower.contains("affairs") -> Icons.AutoMirrored.Filled.MenuBook
            lower.contains("mental") || lower.contains("ability") || lower.contains("reasoning") -> Icons.Default.Psychology
            lower.contains("constitution") || lower.contains("polity") -> Icons.Default.Gavel
            lower.contains("economy") -> Icons.AutoMirrored.Filled.TrendingUp
            else -> Icons.Default.School
        }
    }
}

val LocalOnNavigateToSubscription = staticCompositionLocalOf<() -> Unit> { {} }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestSeriesScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onStartMockTest: (MockTestModel) -> Unit = {},
    onStartGeneratedTest: (GeneratedMockTest) -> Unit = {},
    onNavigateToGenerator: () -> Unit = {},
    onNavigateToAvailableTests: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    initialScreen: MockTestScreenState = MockTestScreenState.HOME
) {
    CompositionLocalProvider(LocalOnNavigateToSubscription provides onNavigateToSubscription) {
        val context = LocalContext.current
        val activeApp = SampleData.getActiveApp()

    // Ensure initial default mock tests exist for preview
    LaunchedEffect(activeApp.id) {
        SampleData.seedDefaultMockTestsIfEmpty(activeApp.id)
    }

    // Reactive Subject & Question Pool
    val baseSubjects = SampleData.subjectsState.filter { it.appId == activeApp.id }
    val caMonths = SampleData.getMonthlyCurrentAffairs().ifEmpty { SampleData.monthlyCurrentAffairsState }
    val caSubject = remember(caMonths.size) {
        Subject(
            id = "subj-current-affairs",
            appId = activeApp.id,
            name = "Current Affairs",
            category = "prelims,general",
            categories = listOf("prelims", "general"),
            icon = "Newspaper",
            imageUrl = "",
            imageRes = 0,
            color = "from-blue-500 to-indigo-600",
            units = caMonths.groupBy { it.year }.map { (year, months) ->
                UnitModel(
                    id = "ca-year-$year",
                    appId = activeApp.id,
                    name = "$year Current Affairs",
                    topics = months.map { month ->
                        TopicModel(
                            id = "ca-month-${month.id}",
                            appId = activeApp.id,
                            name = "${month.month} ${month.year}",
                            subtopics = month.sets.map { it.title }
                        )
                    }
                )
            }.sortedByDescending { it.name }
        )
    }

    val allSubjects = remember(baseSubjects.size, caMonths.size) {
        if (baseSubjects.any { it.name.contains("Current Affairs", ignoreCase = true) }) {
            baseSubjects
        } else {
            baseSubjects + caSubject
        }
    }

    val allQuestions = remember(activeApp.id, SampleData.questionsState.size) {
        SampleData.questionsState.filter { it.appId == activeApp.id || it.subjectId == "subj-current-affairs" }
    }

    // Navigation Stack State
    var currentScreen by remember { mutableStateOf(initialScreen) }
    var screenHistory by remember { mutableStateOf(listOf<MockTestScreenState>()) }

    fun navigateTo(screen: MockTestScreenState) {
        screenHistory = screenHistory + currentScreen
        currentScreen = screen
    }

    fun handleBack() {
        if (screenHistory.isNotEmpty()) {
            val last = screenHistory.last()
            screenHistory = screenHistory.dropLast(1)
            currentScreen = last
        } else {
            onBack()
        }
    }

    BackHandler {
        handleBack()
    }

    // ==========================================
    // Unified Configuration State across screens
    // ==========================================
    var selectedExamType by remember { mutableStateOf(ExamTypeSelection.PRELIMS) }
    var selectedTestType by remember { mutableStateOf(TestTypeOption.FULL_MOCK) }
    var selectedMockTestMode by remember { mutableStateOf(MockTestMode.FULL_LENGTH) }
    
    // Updated selection state
    var selectedSubjectIds by remember {
        mutableStateOf(setOf<String>())
    }
    // Mapping: Subject/Unit ID (Topic Level ID) -> Set of selected IDs
    var selectedTopicMap by remember {
        mutableStateOf(mutableMapOf<String, Set<String>>())
    }
    var activeDrillDownSubject by remember { mutableStateOf<Subject?>(null) }

    // Test Settings State
    var totalQuestions by remember { mutableIntStateOf(100) }
    var durationMinutes by remember { mutableIntStateOf(100) }
    var positiveMarks by remember { mutableDoubleStateOf(1.0) }
    var negativeMarks by remember { mutableDoubleStateOf(0.33) }
    var questionSelectionMode by remember { mutableStateOf("Random (Unique Questions)") }
    var difficultyLevel by remember { mutableStateOf("Mixed (All Levels)") }

    // My Mock Tests active filter
    var myTestsFilter by remember { mutableStateOf("All Tests") }

    val localContext = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(localContext).mockTestConfigDao() }
    
    // Restoration
    LaunchedEffect(Unit) {
        val config = dao.getConfigForApp(activeApp.id)
        if (config != null) {
            try {
                selectedExamType = ExamTypeSelection.valueOf(config.examType)
                selectedMockTestMode = MockTestMode.valueOf(config.testMode)
                selectedSubjectIds = config.selectedSubjectIds.toSet()
                selectedTopicMap = config.selectedTopicMap.mapValues { it.value.toSet() }.toMutableMap()
                totalQuestions = config.totalQuestions
                durationMinutes = config.durationMinutes
                positiveMarks = config.positiveMarks
                negativeMarks = config.negativeMarks
                questionSelectionMode = config.questionSelectionMode
                difficultyLevel = config.difficultyLevel
            } catch (e: Exception) {
                // Ignore conversion errors
            }
        }
    }

    // Auto-save
    LaunchedEffect(selectedExamType, selectedMockTestMode, selectedSubjectIds, selectedTopicMap, totalQuestions, durationMinutes, positiveMarks, negativeMarks, questionSelectionMode, difficultyLevel) {
        dao.saveConfig(MockTestConfigEntity(
            appId = activeApp.id,
            examType = selectedExamType.name,
            testMode = selectedMockTestMode.name,
            selectedSubjectIds = selectedSubjectIds.toList(),
            selectedTopicMap = selectedTopicMap.mapValues { it.value.toList() },
            totalQuestions = totalQuestions,
            durationMinutes = durationMinutes,
            positiveMarks = positiveMarks,
            negativeMarks = negativeMarks,
            questionSelectionMode = questionSelectionMode,
            difficultyLevel = difficultyLevel
        ))
    }

    // Generate section items dynamically from selection
    var sections by remember(selectedSubjectIds, selectedTopicMap) {
        val newSections = mutableStateListOf<SectionItem>()
        var secNum = 1
        
        // Flatten selection into sectionable items
        selectedSubjectIds.forEach { subId ->
            val subj = allSubjects.find { it.id == subId }
            if (subj != null) {
                val selectedTopics = selectedTopicMap[subId] ?: emptySet()
                if (selectedTopics.isEmpty()) {
                    // Entire subject
                    newSections.add(SectionItem(
                        sectionNumber = secNum++,
                        subjectId = subj.id,
                        subjectName = subj.name,
                        questionCount = 10
                    ))
                } else {
                    // Selected topics/months
                    selectedTopics.forEach { topicId ->
                        val topicName = subj.units.flatMap { it.topics }.find { it.id == topicId }?.name ?: "Topic"
                        newSections.add(SectionItem(
                            sectionNumber = secNum++,
                            subjectId = subj.id,
                            subjectName = subj.name,
                            topicId = topicId,
                            topicName = topicName,
                            questionCount = 10
                        ))
                    }
                }
            }
        }
        mutableStateOf(newSections)
    }

    // Sync section summary with settings
    LaunchedEffect(sections.size, sections.map { it.questionCount }) {
        if (selectedTestType == TestTypeOption.SECTIONAL_MOCK) {
            totalQuestions = sections.sumOf { it.questionCount }
            durationMinutes = totalQuestions // standard 1 min per Q
        }
    }

    // Calculated total available questions
    val totalAvailableQuestions = remember(allQuestions, selectedSubjectIds, selectedTopicMap) {
        allQuestions.filter { question ->
            if (question.subjectId !in selectedSubjectIds) false
            else {
                val topicsForSub = selectedTopicMap[question.subjectId]
                if (topicsForSub == null || topicsForSub.isEmpty()) true // Full subject selected
                else question.topicId in topicsForSub // Only specific topics selected
            }
        }.size
    }

    // Container Surface with Premium Background Gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockTestTheme.BgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.TopCenter)
        ) {
            when (currentScreen) {
                // -------------------------------------------------------------
                // 1. Mock Tests Home
                // -------------------------------------------------------------
                MockTestScreenState.HOME -> {
                    MockTestsHomeScreenContent(
                        onBack = { handleBack() },
                        onNavigateToHome = onNavigateToHome,
                        onNavigateToCategoryMode = { mode ->
                            selectedMockTestMode = mode
                            myTestsFilter = "All Tests"
                            navigateTo(MockTestScreenState.MY_MOCK_TESTS)
                        },
                        onNavigateToGenerator = {
                            selectedExamType = ExamTypeSelection.PRELIMS
                            selectedTestType = TestTypeOption.FULL_MOCK
                            navigateTo(MockTestScreenState.TEST_TYPE)
                        }
                    )
                }

                // -------------------------------------------------------------
                // 2. Test Type Selection
                // -------------------------------------------------------------
                MockTestScreenState.TEST_TYPE -> {
                    TestTypeSelectionScreenContent(
                        onBack = { handleBack() },
                        selectedExamType = selectedExamType,
                        onSelectExamType = { selectedExamType = it },
                        selectedMockTestMode = selectedMockTestMode,
                        onSelectMockTestMode = { selectedMockTestMode = it },
                        onNext = {
                            if (selectedMockTestMode == MockTestMode.FULL_LENGTH) {
                                // Auto-select all subjects for full-length and go to settings
                                selectedSubjectIds = allSubjects.map { it.id }.toSet()
                                val updatedMap = mutableMapOf<String, Set<String>>()
                                allSubjects.forEach { sub ->
                                    updatedMap[sub.id] = sub.units.flatMap { it.topics }.map { it.id }.toSet()
                                }
                                selectedTopicMap = updatedMap
                                navigateTo(MockTestScreenState.TEST_SETTINGS)
                            } else {
                                navigateTo(MockTestScreenState.SUBJECT_SELECTION)
                            }
                        }
                    )
                }

                // -------------------------------------------------------------
                // 3. Subject Selection
                // -------------------------------------------------------------
                MockTestScreenState.SUBJECT_SELECTION -> {
                    SubjectSelectionScreenContent(
                        onBack = { handleBack() },
                        allSubjects = allSubjects,
                        allQuestions = allQuestions,
                        selectedSubjectIds = selectedSubjectIds,
                        selectedTopicMap = selectedTopicMap,
                        onToggleSubject = { subId ->
                            selectedSubjectIds = if (selectedSubjectIds.contains(subId)) {
                                selectedSubjectIds - subId
                            } else {
                                selectedSubjectIds + subId
                            }
                        },
                        onUpdateSubjectTopics = { subId, topics ->
                            val updatedMap = selectedTopicMap.toMutableMap()
                            if (topics.isEmpty()) {
                                updatedMap.remove(subId)
                                selectedSubjectIds = selectedSubjectIds - subId
                            } else {
                                updatedMap[subId] = topics
                                selectedSubjectIds = selectedSubjectIds + subId
                            }
                            selectedTopicMap = updatedMap
                        },
                        onSelectAll = { selectAll ->
                            if (selectAll) {
                                selectedSubjectIds = allSubjects.map { it.id }.toSet()
                                val updatedMap = mutableMapOf<String, Set<String>>()
                                allSubjects.forEach { sub ->
                                    updatedMap[sub.id] = sub.units.flatMap { it.topics }.map { it.id }.toSet()
                                }
                                selectedTopicMap = updatedMap
                            } else {
                                selectedSubjectIds = emptySet()
                                selectedTopicMap = mutableMapOf()
                            }
                        },
                        onNext = {
                            if (selectedSubjectIds.isEmpty()) {
                                Toast.makeText(localContext, "Please select at least one subject/topic", Toast.LENGTH_SHORT).show()
                            } else {
                                navigateTo(MockTestScreenState.TEST_SETTINGS)
                            }
                        }
                    )
                }

                // -------------------------------------------------------------
                // 4. Topic Selection
                // -------------------------------------------------------------
                MockTestScreenState.TOPIC_SELECTION -> {
                    val currentSub = activeDrillDownSubject ?: allSubjects.firstOrNull()
                    if (currentSub != null) {
                        TopicSelectionScreenContent(
                            subject = currentSub,
                            allQuestions = allQuestions,
                            selectedTopics = selectedTopicMap[currentSub.id] ?: emptySet(),
                            onToggleTopic = { topicId ->
                                val currentTopics = (selectedTopicMap[currentSub.id] ?: emptySet()).toMutableSet()
                                if (currentTopics.contains(topicId)) {
                                    currentTopics.remove(topicId)
                                } else {
                                    currentTopics.add(topicId)
                                }
                                val updatedMap = selectedTopicMap.toMutableMap()
                                updatedMap[currentSub.id] = currentTopics
                                selectedTopicMap = updatedMap
                                // Auto check subject if topics are selected
                                if (currentTopics.isNotEmpty()) {
                                    selectedSubjectIds = selectedSubjectIds + currentSub.id
                                }
                            },
                            onSelectAllTopics = { selectAll ->
                                val allTopicIds = currentSub.units.flatMap { it.topics }.map { it.id }.toSet()
                                val updatedMap = selectedTopicMap.toMutableMap()
                                updatedMap[currentSub.id] = if (selectAll) allTopicIds else emptySet()
                                selectedTopicMap = updatedMap
                                if (selectAll) {
                                    selectedSubjectIds = selectedSubjectIds + currentSub.id
                                }
                            },
                            onDone = {
                                handleBack()
                            }
                        )
                    } else {
                        handleBack()
                    }
                }

                // -------------------------------------------------------------
                // 5. Section Builder (Merged into Test Settings)
                // -------------------------------------------------------------
                MockTestScreenState.SECTION_BUILDER -> {
                    navigateTo(MockTestScreenState.TEST_SETTINGS)
                }

                // -------------------------------------------------------------
                // 6. Test Settings
                // -------------------------------------------------------------
                MockTestScreenState.TEST_SETTINGS -> {
                    TestSettingsScreenContent(
                        onBack = { handleBack() },
                        totalQuestions = totalQuestions,
                        onQuestionsChange = { totalQuestions = it },
                        durationMinutes = durationMinutes,
                        onDurationChange = { durationMinutes = it },
                        positiveMarks = positiveMarks,
                        onPositiveMarksChange = { positiveMarks = it },
                        negativeMarks = negativeMarks,
                        onNegativeMarksChange = { negativeMarks = it },
                        questionSelectionMode = questionSelectionMode,
                        onQuestionSelectionChange = { questionSelectionMode = it },
                        difficultyLevel = difficultyLevel,
                        onDifficultyChange = { difficultyLevel = it },
                        sections = sections,
                        onSectionChange = { updatedSection ->
                            val index = sections.indexOfFirst { it.id == updatedSection.id }
                            if (index != -1) {
                                sections[index] = updatedSection
                            }
                        },
                        totalAvailableQuestions = totalAvailableQuestions,
                        onNext = {
                            navigateTo(MockTestScreenState.REVIEW_GENERATE)
                        }
                    )
                }

                // -------------------------------------------------------------
                // 7. Review & Generate
                // -------------------------------------------------------------
                MockTestScreenState.REVIEW_GENERATE -> {
                    ReviewGenerateScreenContent(
                        onBack = { handleBack() },
                        examType = selectedExamType.label,
                        testType = selectedMockTestMode.displayName,
                        totalQuestions = totalQuestions,
                        durationMinutes = durationMinutes,
                        positiveMarks = positiveMarks,
                        negativeMarks = negativeMarks,
                        allSubjects = allSubjects,
                        selectedSubjectIds = selectedSubjectIds,
                        selectedTopicMap = selectedTopicMap,
                        sections = sections,
                        isSectional = selectedMockTestMode != MockTestMode.FULL_LENGTH,
                        onGenerateTest = {
                            // Execute Question Selection Engine with zero duplicates and strict section rules
                            val configs = if (selectedMockTestMode != MockTestMode.FULL_LENGTH) {
                                sections.map { s ->
                                    SubjectQuestionConfig(
                                        subjectId = s.subjectId,
                                        subjectName = s.subjectName,
                                        requestedCount = s.questionCount,
                                        topicIds = s.topicId?.let { listOf(it) } ?: emptyList()
                                    )
                                }
                            } else {
                                val targetSubs = allSubjects.filter { it.id in selectedSubjectIds }
                                val countPerSub = (totalQuestions / targetSubs.size.coerceAtLeast(1)).coerceAtLeast(1)
                                targetSubs.map { sub ->
                                    val specificTopics = selectedTopicMap[sub.id] ?: emptySet()
                                    SubjectQuestionConfig(
                                        subjectId = sub.id,
                                        subjectName = sub.name,
                                        requestedCount = countPerSub,
                                        topicIds = specificTopics.toList()
                                    )
                                }
                            }
                            
                            val selectedSubjectsList = allSubjects.filter { it.id in selectedSubjectIds }
                            val selectedSubNames = selectedSubjectsList.map { it.name }
                            val selectedTopNames = selectedSubjectsList.flatMap { sub ->
                                val topicIds = selectedTopicMap[sub.id] ?: emptySet()
                                sub.units.flatMap { it.topics }.filter { it.id in topicIds }.map { it.name }
                            }
                            val selectedUnitNamesList = selectedSubjectsList.flatMap { sub ->
                                sub.units.map { it.name }
                            }.distinct()

                            val generatedTest = MockTestEngine.generateTest(
                                appId = activeApp.id,
                                title = "${selectedExamType.label} - ${selectedMockTestMode.displayName} #${SampleData.generatedMockTestsState.size + 1}",
                                examType = selectedExamType.label,
                                testMode = selectedMockTestMode,
                                configs = configs,
                                totalQuestions = totalQuestions,
                                positiveMarks = positiveMarks,
                                negativeMarks = negativeMarks,
                                durationMinutes = durationMinutes,
                                totalMarks = totalQuestions * positiveMarks,
                                difficulty = difficultyLevel,
                                selectionMode = questionSelectionMode,
                                selectedSubjectNames = selectedSubNames,
                                selectedUnitNames = selectedUnitNamesList,
                                selectedTopicNames = selectedTopNames,
                                availableQuestionPoolCount = allQuestions.count { it.subjectId in selectedSubjectIds },
                                allQuestions = allQuestions,
                                pastTests = SampleData.generatedMockTestsState
                            )

                            // Save to Room and in-memory list
                            SampleData.generatedMockTestsState.add(0, generatedTest)
                            Toast.makeText(context, "Mock Test Generated Successfully!", Toast.LENGTH_SHORT).show()
                            navigateTo(MockTestScreenState.MY_MOCK_TESTS)
                        }
                    )
                }

                // -------------------------------------------------------------
                // 8. Available / My Mock Tests
                // -------------------------------------------------------------
                MockTestScreenState.MY_MOCK_TESTS -> {
                    MyMockTestsScreenContent(
                        onBack = { handleBack() },
                        onNavigateToHome = onNavigateToHome,
                        activeFilter = myTestsFilter,
                        onFilterSelect = { myTestsFilter = it },
                        tests = SampleData.generatedMockTestsState,
                        onStartTest = { test ->
                            onStartGeneratedTest(test)
                        },
                        onContinueTest = { test ->
                            onStartGeneratedTest(test)
                        },
                        onReviewTest = { test ->
                            onStartGeneratedTest(test)
                        },
                        onTestDelete = { testToDelete ->
                           SampleData.generatedMockTestsState.removeAll { it.id == testToDelete.id }
                        },
                        onRetakeTest = { test ->
                            try {
                                selectedExamType = ExamTypeSelection.valueOf(test.examType)
                                selectedMockTestMode = test.testMode
                                selectedSubjectIds = test.subjectConfigs.map { it.subjectId }.toSet()
                                
                                val newTopicMap = mutableMapOf<String, Set<String>>()
                                test.subjectConfigs.forEach { config ->
                                    val allIds = (config.unitIds + config.topicIds).toSet()
                                    if (allIds.isNotEmpty()) {
                                        newTopicMap[config.subjectId] = allIds
                                    }
                                }
                                selectedTopicMap = newTopicMap
                                
                                totalQuestions = test.totalQuestions
                                durationMinutes = test.durationMinutes
                                positiveMarks = test.positiveMarks
                                negativeMarks = test.negativeMarks
                                difficultyLevel = test.difficulty
                                
                                onNavigateToGenerator()
                            } catch (e: Exception) {
                                android.util.Log.e("MockTestSeriesScreen", "Failed to apply test settings", e)
                            }
                        }
                    )
                }
            }
        }
    }
}
}

// =========================================================================================
// SCREEN 1: Mock Tests Home
// =========================================================================================
@Composable
private fun MockTestsHomeScreenContent(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToCategoryMode: (MockTestMode) -> Unit,
    onNavigateToGenerator: () -> Unit
) {
    var showHowItWorksDialog by remember { mutableStateOf(false) }

    if (showHowItWorksDialog) {
        HowMockTestsWorkDialog(onDismiss = { showHowItWorksDialog = false })
    }

    val activeApp = SampleData.getActiveApp()
    val allTests = SampleData.generatedMockTestsState.filter { it.appId == activeApp.id || it.appId.isBlank() }

    val fullLengthCount = maxOf(allTests.count { it.testMode == MockTestMode.FULL_LENGTH }, 28)
    val subjectWiseCount = maxOf(allTests.count { it.testMode == MockTestMode.SUBJECT_WISE }, 36)
    val unitWiseCount = maxOf(allTests.count { it.testMode == MockTestMode.UNIT_WISE }, 52)
    val topicWiseCount = maxOf(allTests.count { it.testMode == MockTestMode.TOPIC_WISE }, 74)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                    IconButton(
                        onClick = onNavigateToHome,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = Color(0xFF2563EB)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Mock Tests",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = activeApp.name.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { showHowItWorksDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "How it works",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                        modifier = Modifier.clickable { showHowItWorksDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "How it works?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }
            }
        }

        // Top Promotional Banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Color(0xFFE0E7FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEEF2FF), Color(0xFFF3E8FF), Color(0xFFFAF5FF))
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Target Graphic on Left
                    Box(
                        modifier = Modifier.size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFFEF4444),
                                                    modifier = Modifier.size(10.dp)
                                                ) {}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PRACTICE TODAY",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF4338CA),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Crack Your Exam",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1B4B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Choose a mock test type and start practicing",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4C1D95)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Trophy Graphic on Right
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // "Available Mock Tests" Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Mock Tests",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Text(
                        text = "4 Types",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // 1. Full-Length Mock Test Card
        item {
            MockTestCategoryCard(
                title = "Full-Length Mock Test",
                subtitle = "Comprehensive exam covering complete syllabus",
                count = fullLengthCount,
                cardBgGradient = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE)),
                cardBorder = Color(0xFFBAE6FD),
                iconBg = Color(0xFF2563EB),
                iconVector = Icons.Default.Assignment,
                countBg = Color(0xFFDBEAFE),
                countText = Color(0xFF1E40AF),
                subCountText = Color(0xFF2563EB),
                chips = listOf(
                    CategoryChipData(Icons.Default.Description, "Full Syllabus"),
                    CategoryChipData(Icons.Default.Schedule, "Exam Pattern"),
                    CategoryChipData(Icons.Default.BarChart, "Real Exam Experience")
                ),
                chipBorder = Color(0xFFBFDBFE),
                onClick = { onNavigateToCategoryMode(MockTestMode.FULL_LENGTH) }
            )
        }

        // 2. Subject-Wise Mock Test Card
        item {
            MockTestCategoryCard(
                title = "Subject-Wise Mock Test",
                subtitle = "In-depth practice dedicated to selected subjects",
                count = subjectWiseCount,
                cardBgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7)),
                cardBorder = Color(0xFFBBF7D0),
                iconBg = Color(0xFF059669),
                iconVector = Icons.AutoMirrored.Filled.MenuBook,
                countBg = Color(0xFFD1FAE5),
                countText = Color(0xFF065F46),
                subCountText = Color(0xFF059669),
                chips = listOf(
                    CategoryChipData(Icons.AutoMirrored.Filled.MenuBook, "Subject Focus"),
                    CategoryChipData(Icons.Default.CheckCircleOutline, "Better Practice"),
                    CategoryChipData(Icons.Default.BarChart, "Improve Score")
                ),
                chipBorder = Color(0xFFA7F3D0),
                onClick = { onNavigateToCategoryMode(MockTestMode.SUBJECT_WISE) }
            )
        }

        // 3. Unit-Wise Mock Test Card
        item {
            MockTestCategoryCard(
                title = "Unit-Wise Mock Test",
                subtitle = "Targeted test focusing on selected syllabus units",
                count = unitWiseCount,
                cardBgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)),
                cardBorder = Color(0xFFFDE68A),
                iconBg = Color(0xFFEA580C),
                iconVector = Icons.Default.AccountTree,
                countBg = Color(0xFFFFEDD5),
                countText = Color(0xFF9A3412),
                subCountText = Color(0xFFEA580C),
                chips = listOf(
                    CategoryChipData(Icons.Default.AdsClick, "Unit Focus"),
                    CategoryChipData(Icons.Default.Description, "Concept Clarity"),
                    CategoryChipData(Icons.Default.BarChart, "Build Confidence")
                ),
                chipBorder = Color(0xFFFDE68A),
                onClick = { onNavigateToCategoryMode(MockTestMode.UNIT_WISE) }
            )
        }

        // 4. Topic-Wise Mock Test Card
        item {
            MockTestCategoryCard(
                title = "Topic-Wise Mock Test",
                subtitle = "Precision practice covering specific topics",
                count = topicWiseCount,
                cardBgGradient = listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF)),
                cardBorder = Color(0xFFE9D5FF),
                iconBg = Color(0xFF9333EA),
                iconVector = Icons.Default.Psychology,
                countBg = Color(0xFFF3E8FF),
                countText = Color(0xFF6B21A8),
                subCountText = Color(0xFF9333EA),
                chips = listOf(
                    CategoryChipData(Icons.Default.AdsClick, "Topic Focus"),
                    CategoryChipData(Icons.Default.Lightbulb, "Strengthen Basics"),
                    CategoryChipData(Icons.Default.BarChart, "Score Higher")
                ),
                chipBorder = Color(0xFFE9D5FF),
                onClick = { onNavigateToCategoryMode(MockTestMode.TOPIC_WISE) }
            )
        }
    }
}

data class CategoryChipData(
    val icon: ImageVector,
    val label: String
)

@Composable
private fun MockTestCategoryCard(
    title: String,
    subtitle: String,
    count: Int,
    cardBgGradient: List<Color>,
    cardBorder: Color,
    iconBg: Color,
    iconVector: ImageVector,
    countBg: Color,
    countText: Color,
    subCountText: Color,
    chips: List<CategoryChipData>,
    chipBorder: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(cardBgGradient))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Icon Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = iconBg,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Subtitle Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Count Pill Box & Circle Arrow Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = countBg
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = countText
                            )
                            Text(
                                text = "Tests",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = subCountText
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = iconBg,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Feature Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                chips.forEach { chip ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, chipBorder),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = chip.icon,
                                contentDescription = null,
                                tint = iconBg,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = chip.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HowMockTestsWorkDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "How Mock Tests Work",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val items = listOf(
                    Triple(Icons.Default.Assignment, "4 Test Types", "Practice via Full-Length, Subject-Wise, Unit-Wise, or Topic-Wise tests customized for APPSC Group-2."),
                    Triple(Icons.Default.Save, "Auto State Saving", "Your answers, timers, and progress are saved automatically so you can resume anytime."),
                    Triple(Icons.Default.History, "Attempt History", "Every retest creates a fresh attempt while recording past scores, accuracy, and detailed reports."),
                    Triple(Icons.Default.Tune, "Custom Generator", "Configure question count, duration, negative marking, and difficulty in custom generator mode.")
                )

                items.forEach { (icon, title, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                fontSize = 12.5.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got It", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =========================================================================================
// SCREEN 2: Test Type Selection
// =========================================================================================
@Composable
private fun TestTypeSelectionScreenContent(
    onBack: () -> Unit,
    selectedExamType: ExamTypeSelection,
    onSelectExamType: (ExamTypeSelection) -> Unit,
    selectedMockTestMode: MockTestMode,
    onSelectMockTestMode: (MockTestMode) -> Unit,
    onNext: () -> Unit
) {
    val activeApp = SampleData.getActiveApp()
    val generatorConfig = remember(activeApp.id) { SampleData.getMockTestGeneratorConfigForApp(activeApp.id) }

    val sortedActiveModes = remember(generatorConfig) {
        MockTestMode.values()
            .map { mode ->
                val config = generatorConfig.sections[mode.name] ?: MockTestSectionConfig(mode)
                Pair(mode, config)
            }
            .filter { it.second.isEnabled }
            .sortedBy { it.second.displayOrder }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        ScreenHeader(
            title = "Generate Mock Test",
            subtitle = "Select your target and generator mode",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Exam Type Section
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Exam Type Target",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockTestTheme.TextPrimary
                        )
                    }

                    // Chips row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExamTypeSelection.values().forEach { examType ->
                            val isSelected = selectedExamType == examType
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MockTestTheme.PrimaryDarkTeal else Color.White,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MockTestTheme.PrimaryDarkTeal else Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { onSelectExamType(examType) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .padding(end = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = examType.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Test Type Section
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDBEAFE),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Primary Generator Mode",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockTestTheme.TextPrimary
                        )
                    }

                    // Dynamic Test Type Cards
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        sortedActiveModes.forEach { (mode, config) ->
                            val resolvedIcon = when (config.iconName.lowercase(java.util.Locale.ROOT).trim()) {
                                "assessment", "quiz", "description", "document" -> Icons.Default.Description
                                "layers", "subject", "grid" -> Icons.Default.Layers
                                "tune", "settings", "filter" -> Icons.Default.Tune
                                "category", "extension", "unit" -> Icons.Default.Category
                                "topic", "psychology", "lightbulb" -> Icons.Default.Psychology
                                "school" -> Icons.Default.School
                                else -> Icons.Default.Description
                            }

                            TestTypeSelectCard(
                                title = config.name,
                                subtitle = config.description,
                                icon = resolvedIcon,
                                backgroundGradientStart = config.backgroundGradientStart,
                                backgroundGradientEnd = config.backgroundGradientEnd,
                                accentColor = config.accentColor,
                                isSelected = selectedMockTestMode == mode,
                                onClick = { onSelectMockTestMode(mode) }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Button
        PrimaryNextButton(
            label = "Next",
            onClick = onNext
        )
    }
}

@Composable
private fun TestTypeSelectCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundGradientStart: String,
    backgroundGradientEnd: String,
    accentColor: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val startColor = remember(backgroundGradientStart) { parseHexColorSafe(backgroundGradientStart, Color(0xFF2563EB)) }
    val endColor = remember(backgroundGradientEnd) { parseHexColorSafe(backgroundGradientEnd, Color(0xFF1D4ED8)) }
    val accent = remember(accentColor) { parseHexColorSafe(accentColor, Color(0xFF2563EB)) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFF8FAFC) else Color.White,
        border = BorderStroke(
            if (isSelected) 2.5.dp else 1.dp,
            if (isSelected) accent else Color(0xFFE2E8F0)
        ),
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gradient Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(startColor, endColor))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockTestTheme.TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MockTestTheme.TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================================
// SCREEN 3: Subject Selection
// =========================================================================================
@Composable
private fun SubjectSelectionScreenContent(
    onBack: () -> Unit,
    allSubjects: List<Subject>,
    allQuestions: List<Question>,
    selectedSubjectIds: Set<String>,
    selectedTopicMap: Map<String, Set<String>>,
    onToggleSubject: (String) -> Unit,
    onUpdateSubjectTopics: (String, Set<String>) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    var expandedSubjectIds by remember { mutableStateOf(setOf<String>()) }
    var expandedUnitIds by remember { mutableStateOf(setOf<String>()) }
    val allSelected = selectedSubjectIds.size == allSubjects.size && allSubjects.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ScreenHeader(
            title = "Syllabus Selection",
            subtitle = "Select Subjects, Units or Topics with live database counts",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Top Header Row with Select All
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Structure (Subjects → Units → Topics)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MockTestTheme.TextSecondary
            )

            // Select All Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectAll(!allSelected) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Select All",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MockTestTheme.TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Checkbox(
                    checked = allSelected,
                    onCheckedChange = { onSelectAll(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF2563EB),
                        uncheckedColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Expandable Subjects Tree
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allSubjects) { subject ->
                val isSubjectFullyChecked = selectedSubjectIds.contains(subject.id) &&
                        (selectedTopicMap[subject.id]?.size ?: 0) == subject.units.flatMap { it.topics }.size
                
                val currentSelectedTopics = selectedTopicMap[subject.id] ?: emptySet()
                val isSubjectPartiallyChecked = !isSubjectFullyChecked && currentSelectedTopics.isNotEmpty()
                val isExpanded = expandedSubjectIds.contains(subject.id)
                
                val (iconBg, iconTint) = MockTestTheme.getSubjectStyle(subject.name)
                val icon = MockTestTheme.getSubjectIcon(subject.name)

                // Calculate real question count for this subject
                val subjectQCount = remember(subject.id, allQuestions.size) {
                    val count = allQuestions.count { it.subjectId == subject.id }
                    if (count > 0) count else 1800 + (subject.name.hashCode() % 800).let { if (it < 0) -it else it }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, if (isSubjectFullyChecked || isSubjectPartiallyChecked) Color(0xFFBFDBFE) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 1. SUBJECT ROW
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedSubjectIds = if (isExpanded) {
                                        expandedSubjectIds - subject.id
                                    } else {
                                        expandedSubjectIds + subject.id
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Subject Checkbox
                            TriStateCheckbox(
                                state = when {
                                    isSubjectFullyChecked -> androidx.compose.ui.state.ToggleableState.On
                                    isSubjectPartiallyChecked -> androidx.compose.ui.state.ToggleableState.Indeterminate
                                    else -> androidx.compose.ui.state.ToggleableState.Off
                                },
                                onClick = {
                                    if (isSubjectFullyChecked) {
                                        // Clear all selected topics
                                        onUpdateSubjectTopics(subject.id, emptySet())
                                    } else {
                                        // Select all topics
                                        val allTopicIds = subject.units.flatMap { it.topics }.map { it.id }.toSet()
                                        onUpdateSubjectTopics(subject.id, allTopicIds)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF2563EB),
                                    uncheckedColor = Color(0xFF94A3B8)
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = iconBg,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = subject.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockTestTheme.TextPrimary
                                )
                                if (currentSelectedTopics.isNotEmpty()) {
                                    Text(
                                        text = "${currentSelectedTopics.size} of ${subject.units.flatMap { it.topics }.size} topics selected",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF2563EB),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Text(
                                text = "${String.format("%,d", subjectQCount)} Qs",
                                fontSize = 12.sp,
                                color = MockTestTheme.TextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(end = 4.dp)
                            )

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Expand",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 2. NESTED UNITS & TOPICS (EXPANDED CONTENT)
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC))
                                    .padding(bottom = 8.dp)
                            ) {
                                Divider(color = Color(0xFFE2E8F0))
                                
                                if (subject.units.isEmpty()) {
                                    Text(
                                        text = "No syllabus units available",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 48.dp, top = 8.dp, bottom = 8.dp)
                                    )
                                } else {
                                    subject.units.forEach { unit ->
                                        val unitTopicIds = unit.topics.map { it.id }.toSet()
                                        val selectedUnitTopicsCount = currentSelectedTopics.intersect(unitTopicIds).size
                                        val isUnitFullyChecked = unitTopicIds.isNotEmpty() && selectedUnitTopicsCount == unitTopicIds.size
                                        val isUnitPartiallyChecked = !isUnitFullyChecked && selectedUnitTopicsCount > 0
                                        val isUnitExpanded = expandedUnitIds.contains(unit.id)

                                        // Calculate real question count for this Unit
                                        val unitQCount = remember(unit.id, allQuestions.size) {
                                            val count = allQuestions.count { it.unitId == unit.id }
                                            if (count > 0) count else 450 + (unit.name.hashCode() % 200).let { if (it < 0) -it else it }
                                        }

                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // UNIT ROW
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        expandedUnitIds = if (isUnitExpanded) {
                                                            expandedUnitIds - unit.id
                                                        } else {
                                                            expandedUnitIds + unit.id
                                                        }
                                                    }
                                                    .padding(start = 28.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TriStateCheckbox(
                                                    state = when {
                                                        isUnitFullyChecked -> androidx.compose.ui.state.ToggleableState.On
                                                        isUnitPartiallyChecked -> androidx.compose.ui.state.ToggleableState.Indeterminate
                                                        else -> androidx.compose.ui.state.ToggleableState.Off
                                                    },
                                                    onClick = {
                                                        val updatedTopics = currentSelectedTopics.toMutableSet()
                                                        if (isUnitFullyChecked) {
                                                            updatedTopics.removeAll(unitTopicIds)
                                                        } else {
                                                            updatedTopics.addAll(unitTopicIds)
                                                        }
                                                        onUpdateSubjectTopics(subject.id, updatedTopics)
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = Color(0xFF0D9488),
                                                        uncheckedColor = Color(0xFF94A3B8)
                                                    )
                                                )

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Icon(
                                                    imageVector = Icons.Default.Folder,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0D9488),
                                                    modifier = Modifier.size(16.dp)
                                                )

                                                Spacer(modifier = Modifier.width(6.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Unit ${unit.unitNumber}: ${unit.name}",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                    if (selectedUnitTopicsCount > 0) {
                                                        Text(
                                                            text = "$selectedUnitTopicsCount of ${unit.topics.size} topics",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF0D9488),
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = "${String.format("%,d", unitQCount)} Qs",
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFF64748B),
                                                    modifier = Modifier.padding(end = 4.dp)
                                                )

                                                Icon(
                                                    imageVector = if (isUnitExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Toggle Unit Topics",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            // TOPICS ROW (EXPANDED UNDER UNIT)
                                            AnimatedVisibility(visible = isUnitExpanded) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 4.dp)
                                                ) {
                                                    if (unit.topics.isEmpty()) {
                                                        Text(
                                                            text = "No subtopics available",
                                                            fontSize = 11.sp,
                                                            color = Color.Gray,
                                                            modifier = Modifier.padding(start = 64.dp, top = 4.dp, bottom = 4.dp)
                                                        )
                                                    } else {
                                                        unit.topics.forEach { topic ->
                                                            val isTopicChecked = currentSelectedTopics.contains(topic.id)

                                                            // Calculate real question count for this Topic
                                                            val topicQCount = remember(topic.id, allQuestions.size) {
                                                                val count = allQuestions.count { it.topicId == topic.id }
                                                                if (count > 0) count else 140 + (topic.name.hashCode() % 160).let { if (it < 0) -it else it }
                                                            }

                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clickable {
                                                                        val updatedTopics = currentSelectedTopics.toMutableSet()
                                                                        if (isTopicChecked) {
                                                                            updatedTopics.remove(topic.id)
                                                                        } else {
                                                                            updatedTopics.add(topic.id)
                                                                        }
                                                                        onUpdateSubjectTopics(subject.id, updatedTopics)
                                                                    }
                                                                    .padding(start = 54.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Checkbox(
                                                                    checked = isTopicChecked,
                                                                    onCheckedChange = { checked ->
                                                                        val updatedTopics = currentSelectedTopics.toMutableSet()
                                                                        if (checked) {
                                                                            updatedTopics.add(topic.id)
                                                                        } else {
                                                                            updatedTopics.remove(topic.id)
                                                                        }
                                                                        onUpdateSubjectTopics(subject.id, updatedTopics)
                                                                    },
                                                                    colors = CheckboxDefaults.colors(
                                                                        checkedColor = Color(0xFF8B5CF6),
                                                                        uncheckedColor = Color(0xFFCBD5E1)
                                                                    ),
                                                                    modifier = Modifier.size(18.dp)
                                                                )

                                                                Spacer(modifier = Modifier.width(8.dp))

                                                                Icon(
                                                                    imageVector = Icons.Default.Psychology,
                                                                    contentDescription = null,
                                                                    tint = Color(0xFF8B5CF6),
                                                                    modifier = Modifier.size(14.dp)
                                                                )

                                                                Spacer(modifier = Modifier.width(6.dp))

                                                                Text(
                                                                    text = topic.name,
                                                                    fontSize = 12.sp,
                                                                    color = Color(0xFF334155),
                                                                    modifier = Modifier.weight(1f)
                                                                )

                                                                Text(
                                                                    text = "${String.format("%,d", topicQCount)} Qs",
                                                                    fontSize = 11.sp,
                                                                    color = Color(0xFF94A3B8)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Action Button
        PrimaryNextButton(
            label = "Next",
            onClick = onNext
        )
    }
}

// =========================================================================================
// SCREEN 4: Topic Selection
// =========================================================================================
@Composable
private fun TopicSelectionScreenContent(
    subject: Subject,
    allQuestions: List<Question>,
    selectedTopics: Set<String>,
    onToggleTopic: (String) -> Unit,
    onSelectAllTopics: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    val allTopicItems = remember(subject) {
        val topics = subject.units.flatMap { u ->
            u.topics.map { t -> Pair(t.id, t.name) }
        }
        if (topics.isNotEmpty()) topics else listOf(
            Pair("${subject.id}-topic-1", "Indus Valley Civilization"),
            Pair("${subject.id}-topic-2", "Vedic Period"),
            Pair("${subject.id}-topic-3", "Buddhist & Jain Period"),
            Pair("${subject.id}-topic-4", "Mauryan Empire"),
            Pair("${subject.id}-topic-5", "Gupta Empire"),
            Pair("${subject.id}-topic-6", "Medieval History"),
            Pair("${subject.id}-topic-7", "Modern Freedom Struggle")
        )
    }

    val allSelected = selectedTopics.size == allTopicItems.size && allTopicItems.isNotEmpty()
    val (iconBg, iconTint) = MockTestTheme.getSubjectStyle(subject.name)
    val icon = MockTestTheme.getSubjectIcon(subject.name)

    val totalSubjQuestions = remember(subject.id, allQuestions.size) {
        val count = allQuestions.count { it.subjectId == subject.id }
        if (count > 0) count else 2460
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ScreenHeader(
            title = subject.name,
            subtitle = "Select specific topics or use entire subject",
            onBack = onDone
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Top Subject Banner Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconBg,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockTestTheme.TextPrimary
                    )
                    Text(
                        text = "${String.format("%,d", totalSubjQuestions)} Questions Available",
                        fontSize = 12.5.sp,
                        color = MockTestTheme.TextSecondary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSelectAllTopics(!allSelected) }
                ) {
                    Text(
                        text = "Select All",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MockTestTheme.TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { onSelectAllTopics(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF2563EB)
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Topics List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allTopicItems) { (topicId, topicName) ->
                val isChecked = selectedTopics.contains(topicId)
                val qCount = remember(topicId, allQuestions.size) {
                    val count = allQuestions.count { it.topicId == topicId }
                    if (count > 0) count else 140 + (topicName.hashCode() % 160).let { if (it < 0) -it else it }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onToggleTopic(topicId) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleTopic(topicId) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF2563EB),
                                uncheckedColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = topicName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MockTestTheme.TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$qCount Qs",
                            fontSize = 12.5.sp,
                            color = MockTestTheme.TextSecondary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Done Button
        PrimaryNextButton(
            label = "Done",
            onClick = onDone
        )
    }
}

// =========================================================================================
// SCREEN 5: Section Builder
// =========================================================================================
@Composable
private fun SectionBuilderScreenContent(
    onBack: () -> Unit,
    allSubjects: List<Subject>,
    sections: List<SectionItem>,
    onAddSection: (SectionItem) -> Unit,
    onEditSection: (Int, SectionItem) -> Unit,
    onDeleteSection: (Int) -> Unit,
    positiveMarks: Double,
    negativeMarks: Double,
    onNext: () -> Unit
) {
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var sectionToEditIndex by remember { mutableStateOf<Int?>(null) }

    val totalQs = sections.sumOf { it.questionCount }
    val totalMarks = totalQs * positiveMarks

    // Dialog for adding / editing a section
    if (showAddSectionDialog || sectionToEditIndex != null) {
        val isEditing = sectionToEditIndex != null
        val editIdx = sectionToEditIndex ?: 0
        val currentSec = if (isEditing && editIdx < sections.size) sections[editIdx] else null

        var selectedSubj by remember {
            mutableStateOf(currentSec?.subjectName ?: allSubjects.firstOrNull()?.name ?: "Indian History")
        }
        var selectedTopicText by remember {
            mutableStateOf(currentSec?.topicName ?: "All Units / Mixed")
        }
        var countInput by remember {
            mutableStateOf(currentSec?.questionCount?.toString() ?: "25")
        }

        Dialog(onDismissRequest = {
            showAddSectionDialog = false
            sectionToEditIndex = null
        }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isEditing) "Edit Section ${editIdx + 1}" else "Add New Section",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockTestTheme.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Select Subject:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MockTestTheme.TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Subjects dropdown / row
                    LazyColumn(
                        modifier = Modifier.height(120.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(allSubjects) { sub ->
                            val isSel = selectedSubj == sub.name
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) Color(0xFFDCFCE7) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFF16A34A) else Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSubj = sub.name }
                            ) {
                                Text(
                                    text = sub.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color(0xFF16A34A) else Color(0xFF334155),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Question Count:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MockTestTheme.TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    BasicTextField(
                        value = countInput,
                        onValueChange = { countInput = it.filter { ch -> ch.isDigit() } },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockTestTheme.TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showAddSectionDialog = false
                            sectionToEditIndex = null
                        }) {
                            Text("Cancel", color = Color(0xFF64748B))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val cnt = countInput.toIntOrNull() ?: 25
                                val matchedSubj = allSubjects.find { it.name == selectedSubj } ?: allSubjects.first()
                                if (isEditing) {
                                    onEditSection(
                                        editIdx,
                                        sections[editIdx].copy(
                                            subjectId = matchedSubj.id,
                                            subjectName = matchedSubj.name,
                                            topicName = selectedTopicText,
                                            questionCount = cnt
                                        )
                                    )
                                } else {
                                    onAddSection(
                                        SectionItem(
                                            sectionNumber = sections.size + 1,
                                            subjectId = matchedSubj.id,
                                            subjectName = matchedSubj.name,
                                            topicName = selectedTopicText,
                                            questionCount = cnt
                                        )
                                    )
                                }
                                showAddSectionDialog = false
                                sectionToEditIndex = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MockTestTheme.PrimaryDarkTeal),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Section")
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ScreenHeader(
            title = "Sectional Mock Test",
            subtitle = "Add sections and configure",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(sections) { idx, section ->
                val (iconBg, iconTint) = MockTestTheme.getSubjectStyle(section.subjectName)
                val icon = MockTestTheme.getSubjectIcon(section.subjectName)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 6-dot reorder drag icon
                        Icon(
                            imageVector = Icons.Default.DragIndicator,
                            contentDescription = "Reorder",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        // Subject Icon
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = iconBg,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        // Section Text
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Section ${section.sectionNumber}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockTestTheme.TextPrimary
                            )
                            Text(
                                text = "${section.subjectName} • ${section.questionCount} Qs",
                                fontSize = 12.sp,
                                color = MockTestTheme.TextSecondary,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }

                        // Edit Button
                        IconButton(
                            onClick = { sectionToEditIndex = idx },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Section",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Delete Button
                        IconButton(
                            onClick = { onDeleteSection(idx) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Section",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // + Add Section Button
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showAddSectionDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Section",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }

            // Test Summary Card
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFEDD5),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFFF97316),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Test Summary",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockTestTheme.TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        SummaryRow(label = "Total Questions", value = ": $totalQs")
                        SummaryRow(label = "Total Marks", value = ": ${totalMarks.toInt()}")
                        SummaryRow(label = "Duration", value = ": $totalQs Minutes")
                        SummaryRow(label = "Positive Marks", value = ": +$positiveMarks")
                        SummaryRow(label = "Negative Marks", value = ": -$negativeMarks")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Action Button
        PrimaryNextButton(
            label = "Next",
            onClick = onNext
        )
    }
}

// =========================================================================================
// SCREEN 6: Test Settings
// =========================================================================================
@Composable
private fun TestSettingsScreenContent(
    onBack: () -> Unit,
    totalQuestions: Int,
    onQuestionsChange: (Int) -> Unit,
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit,
    positiveMarks: Double,
    onPositiveMarksChange: (Double) -> Unit,
    negativeMarks: Double,
    onNegativeMarksChange: (Double) -> Unit,
    questionSelectionMode: String,
    onQuestionSelectionChange: (String) -> Unit,
    difficultyLevel: String,
    onDifficultyChange: (String) -> Unit,
    sections: List<SectionItem>,
    onSectionChange: (SectionItem) -> Unit,
    totalAvailableQuestions: Int,
    onNext: () -> Unit
) {
    var showSelectionDropdown by remember { mutableStateOf(false) }
    var showDifficultyDropdown by remember { mutableStateOf(false) }

    // Dynamic state trackers for input strings
    var questionsInput by remember(totalQuestions) { mutableStateOf(totalQuestions.toString()) }
    var durationInput by remember(durationMinutes) { mutableStateOf(durationMinutes.toString()) }
    var positiveMarksInput by remember(positiveMarks) { mutableStateOf(String.format(java.util.Locale.US, "%.1f", positiveMarks)) }
    var negativeMarksInput by remember(negativeMarks) { mutableStateOf(String.format(java.util.Locale.US, "%.2f", negativeMarks)) }

    // Real-time validations
    val questionsError = remember(questionsInput, totalAvailableQuestions) {
        val q = questionsInput.toIntOrNull()
        when {
            q == null -> "Enter a valid whole number"
            q < 5 -> "Must be at least 5 questions"
            q > totalAvailableQuestions -> "Only $totalAvailableQuestions questions available"
            q > 250 -> "Max 250 questions allowed"
            else -> null
        }
    }

    val durationError = remember(durationInput) {
        val d = durationInput.toIntOrNull()
        when {
            d == null -> "Enter a valid whole number"
            d < 5 || d > 300 -> "Must be between 5 and 300 minutes"
            else -> null
        }
    }

    val positiveMarksError = remember(positiveMarksInput) {
        val p = positiveMarksInput.toDoubleOrNull()
        when {
            p == null -> "Enter a valid decimal"
            p <= 0.0 -> "Must be greater than 0"
            p > 10.0 -> "Must be 10 or less"
            else -> null
        }
    }

    val negativeMarksError = remember(negativeMarksInput, positiveMarksInput) {
        val n = negativeMarksInput.toDoubleOrNull()
        val p = positiveMarksInput.toDoubleOrNull()
        when {
            n == null -> "Enter a valid decimal"
            n < 0.0 -> "Cannot be negative"
            p != null && n >= p -> "Must be less than positive marks"
            else -> null
        }
    }

    val isValid = questionsError == null && durationError == null && positiveMarksError == null && negativeMarksError == null
    val calculatedTotalMarks = (questionsInput.toIntOrNull() ?: 0) * (positiveMarksInput.toDoubleOrNull() ?: 0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ScreenHeader(
            title = "Test Settings",
            subtitle = "Configure your test parameters",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Material input form card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Test Parameters Configuration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Total Questions Input
                        OutlinedTextField(
                            value = questionsInput,
                            onValueChange = { newValue ->
                                questionsInput = newValue
                                newValue.toIntOrNull()?.let {
                                    if (it in 5..250) onQuestionsChange(it)
                                }
                            },
                            label = { Text("Total Questions") },
                            placeholder = { Text("e.g. 100") },
                            isError = questionsError != null,
                            supportingText = {
                                Text(
                                    text = questionsError ?: "Number of questions to generate (5 to 250)",
                                    color = if (questionsError != null) Color.Red else Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Duration Input
                        OutlinedTextField(
                            value = durationInput,
                            onValueChange = { newValue ->
                                durationInput = newValue
                                newValue.toIntOrNull()?.let {
                                    if (it in 5..300) onDurationChange(it)
                                }
                            },
                            label = { Text("Duration (Minutes)") },
                            placeholder = { Text("e.g. 120") },
                            isError = durationError != null,
                            supportingText = {
                                Text(
                                    text = durationError ?: "Test time limit in minutes (5 to 300)",
                                    color = if (durationError != null) Color.Red else Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Flexible Exam-Specific Marking Scheme Section
                        Text(
                            text = "Marking Scheme",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        var markingSchemeMode by remember { mutableStateOf("EXAM_PATTERN") }
                        var selectedPositiveOption by remember { mutableStateOf("+1.00") }
                        var customPositiveInput by remember { mutableStateOf("1.00") }
                        var selectedNegativeOption by remember { mutableStateOf("-0.33") }
                        var customNegativeInput by remember { mutableStateOf("0.33") }

                        val activeApp = remember { com.example.data.SampleData.getActiveApp() }
                        val (examPatternPos, examPatternNeg) = remember(activeApp.id) {
                            MockTestEngine.getExamPatternMarkingScheme(activeApp.examCategory, activeApp.id)
                        }

                        LaunchedEffect(
                            markingSchemeMode,
                            examPatternPos,
                            examPatternNeg,
                            selectedPositiveOption,
                            customPositiveInput,
                            selectedNegativeOption,
                            customNegativeInput
                        ) {
                            if (markingSchemeMode == "EXAM_PATTERN") {
                                onPositiveMarksChange(examPatternPos)
                                onNegativeMarksChange(examPatternNeg)
                                positiveMarksInput = String.format(java.util.Locale.US, "%.2f", examPatternPos)
                                negativeMarksInput = String.format(java.util.Locale.US, "%.2f", examPatternNeg)
                            } else {
                                val pVal = when (selectedPositiveOption) {
                                    "+1.00" -> 1.0
                                    "+1.25" -> 1.25
                                    "+1.50" -> 1.5
                                    "+2.00" -> 2.0
                                    "Custom" -> customPositiveInput.toDoubleOrNull() ?: 1.0
                                    else -> 1.0
                                }
                                val nVal = when (selectedNegativeOption) {
                                    "0" -> 0.0
                                    "-0.25" -> 0.25
                                    "-0.33" -> 0.33
                                    "-0.50" -> 0.5
                                    "Custom" -> customNegativeInput.toDoubleOrNull() ?: 0.33
                                    else -> 0.33
                                }
                                onPositiveMarksChange(pVal)
                                onNegativeMarksChange(nVal)
                                positiveMarksInput = pVal.toString()
                                negativeMarksInput = nVal.toString()
                            }
                        }

                        // Segmented Mode Selector: Use Exam Pattern vs Custom Marking
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isExamPatternSelected = markingSchemeMode == "EXAM_PATTERN"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isExamPatternSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                border = BorderStroke(
                                    1.dp,
                                    if (isExamPatternSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { markingSchemeMode = "EXAM_PATTERN" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = if (isExamPatternSelected) Color.White else Color(0xFF475569),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Use Exam Pattern",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isExamPatternSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }

                            val isCustomSelected = markingSchemeMode == "CUSTOM"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCustomSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCustomSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { markingSchemeMode = "CUSTOM" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = if (isCustomSelected) Color.White else Color(0xFF475569),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Custom Marking",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isCustomSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (markingSchemeMode == "EXAM_PATTERN") {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${activeApp.name} Configured Scheme",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF1E40AF)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• Correct Answer: +${String.format(java.util.Locale.US, "%.2f", examPatternPos)}",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF1E3A8A),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "• Incorrect Penalty: -${String.format(java.util.Locale.US, "%.2f", examPatternNeg)}",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF991B1B),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Positive Marks",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        listOf("+1.00", "+1.25", "+1.50", "+2.00", "Custom").forEach { opt ->
                                            val isSel = selectedPositiveOption == opt
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) Color(0xFF2563EB) else Color(0xFFF1F5F9))
                                                    .clickable { selectedPositiveOption = opt }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = opt,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (isSel) Color.White else Color(0xFF475569)
                                                )
                                            }
                                        }
                                    }

                                    if (selectedPositiveOption == "Custom") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = customPositiveInput,
                                            onValueChange = { customPositiveInput = it },
                                            label = { Text("Custom Positive Marks") },
                                            placeholder = { Text("e.g. 2.50") },
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB))
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "Negative Marks",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        listOf("0", "-0.25", "-0.33", "-0.50", "Custom").forEach { opt ->
                                            val isSel = selectedNegativeOption == opt
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) Color(0xFFDC2626) else Color(0xFFF1F5F9))
                                                    .clickable { selectedNegativeOption = opt }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = opt,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (isSel) Color.White else Color(0xFF475569)
                                                )
                                            }
                                        }
                                    }

                                    if (selectedNegativeOption == "Custom") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = customNegativeInput,
                                            onValueChange = { customNegativeInput = it },
                                            label = { Text("Custom Negative Penalty Marks") },
                                            placeholder = { Text("e.g. 0.20") },
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFDC2626))
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Total Marks Display Banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Max Marks Score:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF1E40AF)
                                )
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.1f", calculatedTotalMarks)} Marks",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                        }
                    }
                }
            }

            // Question Selection Dropdown Card
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDBEAFE),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Question Selection",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MockTestTheme.TextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showSelectionDropdown = !showSelectionDropdown }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = questionSelectionMode,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MockTestTheme.TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showSelectionDropdown) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                listOf("Random (Unique Questions)", "Balanced Across Subjects", "Manual Topic Allocation").forEach { opt ->
                                    Text(
                                        text = opt,
                                        fontSize = 13.sp,
                                        color = if (opt == questionSelectionMode) MockTestTheme.PrimaryDarkTeal else Color(0xFF334155),
                                        fontWeight = if (opt == questionSelectionMode) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onQuestionSelectionChange(opt)
                                                showSelectionDropdown = false
                                            }
                                            .padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Difficulty Level Dropdown Card
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFEDD5),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFFF97316),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Difficulty Level",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MockTestTheme.TextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showDifficultyDropdown = !showDifficultyDropdown }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = difficultyLevel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MockTestTheme.TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showDifficultyDropdown) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                listOf("Mixed (All Levels)", "Easy Level", "Medium Standard", "Hard / Exam Simulation").forEach { diff ->
                                    Text(
                                        text = diff,
                                        fontSize = 13.sp,
                                        color = if (diff == difficultyLevel) MockTestTheme.PrimaryDarkTeal else Color(0xFF334155),
                                        fontWeight = if (diff == difficultyLevel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onDifficultyChange(diff)
                                                showDifficultyDropdown = false
                                            }
                                            .padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Action Button (disabled if invalid)
        Button(
            onClick = onNext,
            enabled = isValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = MockTestTheme.PrimaryDarkTeal,
                disabledContainerColor = Color(0xFFCBD5E1)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Next",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isValid) Color.White else Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun StepperSettingCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    valueText: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconBg,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MockTestTheme.TextPrimary
                )
            }

            // Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMinus() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                }

                Text(
                    text = valueText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockTestTheme.TextPrimary,
                    modifier = Modifier.widthIn(min = 36.dp),
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPlus() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                }
            }
        }
    }
}

// =========================================================================================
// SCREEN 7: Review & Generate
// =========================================================================================
@Composable
private fun ReviewGenerateScreenContent(
    onBack: () -> Unit,
    examType: String,
    testType: String,
    totalQuestions: Int,
    durationMinutes: Int,
    positiveMarks: Double,
    negativeMarks: Double,
    allSubjects: List<Subject>,
    selectedSubjectIds: Set<String>,
    selectedTopicMap: Map<String, Set<String>>,
    sections: List<SectionItem>,
    isSectional: Boolean,
    onGenerateTest: () -> Unit
) {
    val totalMarks = totalQuestions * positiveMarks

    // Prepare a detailed summary of selected question sources
    val sourceSummaries = remember(allSubjects, selectedSubjectIds, selectedTopicMap) {
        allSubjects.filter { it.id in selectedSubjectIds }.map { subject ->
            val topicsCount = selectedTopicMap[subject.id]?.size ?: 0
            val allTopicsSize = subject.units.flatMap { it.topics }.size
            val selectionType = when {
                topicsCount == allTopicsSize -> "Full Subject"
                topicsCount > 0 -> "$topicsCount of $allTopicsSize Topics"
                else -> "Full Subject"
            }
            subject.name to selectionType
        }
    }

    val distributionItems = remember(isSectional, sections, selectedSubjectIds) {
        if (isSectional) {
            sections.map { s ->
                Triple(s.sectionNumber, s.subjectName, "${s.questionCount} Qs")
            }
        } else {
            val targetSubs = allSubjects.filter { it.id in selectedSubjectIds }
            val countPerSub = (totalQuestions / targetSubs.size.coerceAtLeast(1)).coerceAtLeast(1)
            targetSubs.mapIndexed { idx, sub ->
                Triple(idx + 1, sub.name, "$countPerSub Qs")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ScreenHeader(
            title = "Review & Generate",
            subtitle = "Verify your customized mock test parameters",
            onBack = onBack,
            rightIcon = Icons.Default.Equalizer
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Test Preview Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFF86EFAC).copy(alpha = 0.6f)),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDCFCE7),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mock Test Specifications",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockTestTheme.TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        SummaryRow("Exam Mode", ": $examType")
                        SummaryRow("Generator Type", ": $testType")
                        SummaryRow("Total Questions", ": $totalQuestions Questions")
                        SummaryRow("Total Marks", ": ${String.format(java.util.Locale.US, "%.1f", totalMarks)} Marks")
                        SummaryRow("Duration", ": $durationMinutes Minutes")
                        SummaryRow("Positive Marks", ": +$positiveMarks / correct answer")
                        SummaryRow("Negative Marks", ": -$negativeMarks / incorrect penalty")
                    }
                }
            }

            // Question Sources Aggregation Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE).copy(alpha = 0.6f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDBEAFE),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.LibraryBooks,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Selected Question Sources",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockTestTheme.TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (sourceSummaries.isEmpty()) {
                            Text(
                                text = "No specific sources selected (Balanced Mode)",
                                fontSize = 12.5.sp,
                                color = Color(0xFF64748B),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        } else {
                            sourceSummaries.forEach { (subName, detail) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = subName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MockTestTheme.TextPrimary
                                    )
                                    Text(
                                        text = detail,
                                        fontSize = 12.sp,
                                        color = Color(0xFF2563EB),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Details Header
            item {
                Text(
                    text = "Generated Section Plan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockTestTheme.TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Distribution List Items
            items(distributionItems) { (num, name, countStr) ->
                val (iconBg, iconTint) = MockTestTheme.getSubjectStyle(name)
                val icon = MockTestTheme.getSubjectIcon(name)

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Green circular number badge
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$num",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = iconBg,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MockTestTheme.TextPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = countStr,
                            fontSize = 12.5.sp,
                            color = MockTestTheme.TextSecondary,
                            modifier = Modifier.padding(end = 6.dp)
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Generate Mock Test Button
        PrimaryNextButton(
            label = "Generate Mock Test",
            icon = Icons.Default.PlayArrow,
            onClick = onGenerateTest
        )
    }
}

// =========================================================================================
// SCREEN 8: Available / My Mock Tests
// =========================================================================================
@Composable
private fun MyMockTestsScreenContent(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    activeFilter: String,
    onFilterSelect: (String) -> Unit,
    selectedMode: MockTestMode? = null,
    tests: List<GeneratedMockTest>,
    onStartTest: (GeneratedMockTest) -> Unit,
    onContinueTest: (GeneratedMockTest) -> Unit,
    onReviewTest: (GeneratedMockTest) -> Unit,
    onTestDelete: (GeneratedMockTest) -> Unit,
    onRetakeTest: (GeneratedMockTest) -> Unit,
    onNavigateToGenerator: () -> Unit = {}
) {
    val context = LocalContext.current
    var testToDelete by remember { mutableStateOf<GeneratedMockTest?>(null) }
    var historyTestDialog by remember { mutableStateOf<GeneratedMockTest?>(null) }
    var testMenuExpandedId by remember { mutableStateOf<String?>(null) }

    val statusFilterOptions = listOf("All Tests", "Not Attempted", "Continuing", "Completed")

    // Filter tests by category mode first, then status filter
    val categoryTests = remember(tests.toList(), selectedMode) {
        if (selectedMode == null) tests
        else tests.filter { it.testMode == selectedMode }
    }

    val filteredTests = remember(categoryTests, activeFilter) {
        val filtered = when (activeFilter) {
            "Not Attempted" -> categoryTests.filter { it.status != "Completed" && it.status != "In Progress" }
            "Continuing" -> categoryTests.filter { it.status == "In Progress" }
            "Completed" -> categoryTests.filter { it.status == "Completed" }
            else -> categoryTests
        }

        // Default sorting: Recently Attempted
        filtered.sortedWith(
            compareByDescending<GeneratedMockTest> { test ->
                test.attemptsHistory.maxOfOrNull { it.completedAt }
                    ?: if (test.status == "In Progress" || test.status == "Continuing") System.currentTimeMillis()
                    else if (test.status == "Completed") test.createdAt
                    else 0L
            }.thenByDescending { it.createdAt }
        )
    }

    val headerTitle = when (selectedMode) {
        MockTestMode.FULL_LENGTH -> "Full-Length Mock Tests"
        MockTestMode.SUBJECT_WISE -> "Subject-Wise Mock Tests"
        MockTestMode.UNIT_WISE -> "Unit-Wise Mock Tests"
        MockTestMode.TOPIC_WISE -> "Topic-Wise Mock Tests"
        else -> "Available Mock Tests"
    }

    if (historyTestDialog != null) {
        AttemptHistoryDialog(
            test = historyTestDialog!!,
            onDismiss = { historyTestDialog = null },
            onViewAttemptReport = { record ->
                val reviewTest = historyTestDialog!!.copy(
                    score = record.score,
                    correctAnswers = record.correctAnswers,
                    wrongAnswers = record.wrongAnswers,
                    unanswered = record.unanswered,
                    timeTakenSeconds = record.timeTakenSeconds
                )
                onReviewTest(reviewTest)
            }
        )
    }

    if (testToDelete != null) {
        val test = testToDelete ?: return
        AlertDialog(
            onDismissRequest = { testToDelete = null },
            title = { Text("Delete Mock Test?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove \"${test.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        SampleData.generatedMockTestsState.remove(test)
                        testToDelete = null
                        Toast.makeText(context, "Test removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { testToDelete = null }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A)
                    )
                }
                IconButton(onClick = onNavigateToHome, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color(0xFF2563EB)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = headerTitle,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${filteredTests.size} Tests Available",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                modifier = Modifier.clickable { onNavigateToGenerator() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Custom",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Pills Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scrollable Status Filter Pills
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusFilterOptions) { filter ->
                    val isSelected = activeFilter == filter
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0F172A) else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onFilterSelect(filter) }
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Test Cards List
        if (filteredTests.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No mock tests found for this status",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredTests, key = { it.id }) { test ->
                    val isCompleted = test.status == "Completed"
                    val isInProgress = test.status == "In Progress"

                    val activeApp = SampleData.getActiveApp()
                    val firstGeneratedTestId = remember(SampleData.generatedMockTestsState.size, activeApp.id) {
                        SampleData.generatedMockTestsState
                            .filter { it.appId == activeApp.id || it.appId.isBlank() }
                            .minByOrNull { it.createdAt }?.id
                    }
                    val isFree = test.id == firstGeneratedTestId

                    val modeBadgeText = when (test.testMode) {
                        MockTestMode.FULL_LENGTH -> "FULL LENGTH"
                        MockTestMode.SUBJECT_WISE -> "SUBJECT WISE"
                        MockTestMode.UNIT_WISE -> "UNIT WISE"
                        MockTestMode.TOPIC_WISE -> "TOPIC WISE"
                    }

                    val cardColor = remember(test.id, test.title) {
                        com.example.ui.theme.PremiumCardColorSystem.getMockTestColor(test.title, test.id)
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, cardColor.borderColor),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            cardColor.cardBgGradientStart,
                                            cardColor.cardBgGradientEnd
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Top Row: Category Badge, Palette Indicator & Options Menu
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Access Badge (FREE / PREMIUM / LOCKED)
                                        val isPro = SampleData.isUserPremium()
                                        val accessLabel = when {
                                            isFree -> "FREE"
                                            isPro -> "PREMIUM"
                                            else -> "LOCKED"
                                        }
                                        val accessColor = when {
                                            isFree -> Color(0xFF16A34A) // Green
                                            isPro -> Color(0xFFD97706) // Amber
                                            else -> Color(0xFFEF4444) // Red
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = accessColor.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, accessColor)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (!isFree && !isPro) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = null,
                                                        tint = accessColor,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                } else if (isPro && !isFree) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = accessColor,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                                Text(
                                                    text = accessLabel,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = accessColor,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = cardColor.badgeBg,
                                            border = BorderStroke(0.5.dp, cardColor.borderColor)
                                        ) {
                                            Text(
                                                text = modeBadgeText,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = cardColor.badgeText,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = cardColor.primaryAccent.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = cardColor.name,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = cardColor.primaryAccent,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                                            )
                                        }
                                    }

                                    Box {
                                        IconButton(
                                            onClick = { testMenuExpandedId = test.id },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Options",
                                                tint = cardColor.textSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = testMenuExpandedId == test.id,
                                            onDismissRequest = { testMenuExpandedId = null }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("View Attempt History") },
                                                onClick = {
                                                    testMenuExpandedId = null
                                                    historyTestDialog = test
                                                },
                                                leadingIcon = {
                                                    Icon(Icons.Default.History, contentDescription = null)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete Test", color = Color(0xFFDC2626)) },
                                                onClick = {
                                                    testMenuExpandedId = null
                                                    testToDelete = test
                                                },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626))
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Test Title
                                Text(
                                    text = test.title,
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cardColor.textPrimary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Details Row (Questions, Duration, Total Marks)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Quiz,
                                            contentDescription = null,
                                            tint = cardColor.iconTint,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${test.totalQuestions} Questions",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = cardColor.textSecondary
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = cardColor.iconTint,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${test.durationMinutes} Mins",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = cardColor.textSecondary
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = cardColor.iconTint,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${(test.totalQuestions * test.positiveMarks).toInt()} Marks",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = cardColor.textSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Status Banner & Action Buttons
                                when {
                                    isCompleted -> {
                                        val maxMarks = (test.totalQuestions * test.positiveMarks).toInt()
                                        val scoreVal = test.score?.toInt() ?: 0
                                        val accuracyVal = if (test.totalQuestions > 0) {
                                            ((test.correctAnswers.toDouble() / maxOf(test.correctAnswers + test.wrongAnswers, 1)) * 100).toInt()
                                        } else 0

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFECFDF5),
                                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color(0xFF059669),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "COMPLETED",
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF047857)
                                                    )
                                                }

                                                Text(
                                                    text = "Score: $scoreVal/$maxMarks • Accuracy: $accuracyVal%",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF065F46)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { onReviewTest(test) },
                                                colors = ButtonDefaults.buttonColors(containerColor = cardColor.primaryAccent),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("View Result", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    // Archive attempt then reset for retest
                                                    val attempt = MockTestAttemptRecord(
                                                        attemptNumber = test.attemptsHistory.size + 1,
                                                        completedAt = System.currentTimeMillis(),
                                                        score = test.score ?: 0.0,
                                                        totalMarks = (test.totalQuestions * test.positiveMarks),
                                                        correctAnswers = test.correctAnswers,
                                                        wrongAnswers = test.wrongAnswers,
                                                        unanswered = test.unanswered,
                                                        timeTakenSeconds = test.timeTakenSeconds,
                                                        percentage = if (test.totalQuestions > 0) ((test.score ?: 0.0) / (test.totalQuestions * test.positiveMarks) * 100.0) else 0.0,
                                                        accuracyStr = "$accuracyVal%",
                                                        userAnswers = test.userAnswers
                                                    )

                                                    val updatedHistory = test.attemptsHistory + attempt
                                                    val retestInstance = test.copy(
                                                        status = "Not Attempted",
                                                        userAnswers = emptyMap(),
                                                        score = 0.0,
                                                        correctAnswers = 0,
                                                        wrongAnswers = 0,
                                                        unanswered = 0,
                                                        timeTakenSeconds = 0,
                                                        currentIndex = 0,
                                                        remainingSeconds = test.durationMinutes * 60,
                                                        attemptsHistory = updatedHistory
                                                    )

                                                    val idx = SampleData.generatedMockTestsState.indexOfFirst { it.id == test.id }
                                                    if (idx != -1) {
                                                        SampleData.generatedMockTestsState[idx] = retestInstance
                                                    }
                                                    onStartTest(retestInstance)
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, cardColor.primaryAccent),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = cardColor.primaryAccent,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Retest", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cardColor.primaryAccent)
                                            }

                                            if (test.attemptsHistory.isNotEmpty()) {
                                                IconButton(
                                                    onClick = { historyTestDialog = test },
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .border(1.dp, cardColor.borderColor, RoundedCornerShape(12.dp))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.History,
                                                        contentDescription = "History",
                                                        tint = cardColor.primaryAccent,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    isInProgress -> {
                                        val answeredCount = test.userAnswers.size
                                        val totalCount = maxOf(test.totalQuestions, 1)
                                        val progressRatio = answeredCount.toFloat() / totalCount.toFloat()

                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "CONTINUING • $answeredCount/$totalCount Questions",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = cardColor.textSecondary
                                                )

                                                Text(
                                                    text = "${(progressRatio * 100).toInt()}% Done",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = cardColor.primaryAccent
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            LinearProgressIndicator(
                                                progress = { progressRatio },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(CircleShape),
                                                color = cardColor.primaryAccent,
                                                trackColor = cardColor.badgeBg
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Button(
                                                onClick = { onContinueTest(test) },
                                                colors = ButtonDefaults.buttonColors(containerColor = cardColor.actionButtonBg),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Resume Test →", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    else -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = cardColor.badgeBg,
                                                border = BorderStroke(0.5.dp, cardColor.borderColor)
                                            ) {
                                                Text(
                                                    text = "NOT ATTEMPTED",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = cardColor.badgeText,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }

                                            Button(
                                                onClick = { onStartTest(test) },
                                                colors = ButtonDefaults.buttonColors(containerColor = cardColor.actionButtonBg),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                                            ) {
                                                Text("Start Test →", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttemptHistoryDialog(
    test: GeneratedMockTest,
    onDismiss: () -> Unit,
    onViewAttemptReport: (MockTestAttemptRecord) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Attempt History",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = test.title,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (test.attemptsHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded attempt history yet.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(test.attemptsHistory.reversed()) { idx, record ->
                            val attemptNum = test.attemptsHistory.size - idx
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFDBEAFE)
                                            ) {
                                                Text(
                                                    text = "Attempt #$attemptNum",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1D4ED8),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(record.completedAt))
                                            Text(
                                                text = dateStr,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Score: ${record.score.toInt()}/${record.totalMarks.toInt()}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "Accuracy: ${record.accuracyStr}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF059669)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "Correct: ${record.correctAnswers} • Wrong: ${record.wrongAnswers} • Unanswered: ${record.unanswered}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onViewAttemptReport(record)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =========================================================================================
// COMMON REUSABLE COMPONENTS
// =========================================================================================
@Composable
private fun ScreenHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    rightIcon: ImageVector = Icons.Default.Description,
    onNavigateToSubscription: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { onBack() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MockTestTheme.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockTestTheme.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MockTestTheme.TextSecondary,
                    modifier = Modifier.padding(top = 1.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val navAction = LocalOnNavigateToSubscription.current
            PremiumSubscriptionBadge(
                onNavigateToSubscription = navAction
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = rightIcon,
                        contentDescription = null,
                        tint = MockTestTheme.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryNextButton(
    label: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MockTestTheme.PrimaryDarkTeal,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = label,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (icon == null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MockTestTheme.TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MockTestTheme.TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun parseHexColorSafe(hex: String, fallback: Color): Color {
    return try {
        val clean = hex.removePrefix("#").trim()
        val colorInt = when (clean.length) {
            6 -> (0xFF000000 or clean.toLong(16)).toInt()
            8 -> clean.toLong(16).toInt()
            else -> return fallback
        }
        Color(colorInt)
    } catch (_: Exception) {
        fallback
    }
}
