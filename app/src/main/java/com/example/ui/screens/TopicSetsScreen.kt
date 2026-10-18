package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.ui.platform.LocalContext
import com.example.util.PracticeSessionManager
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.PracticeSetModel
import com.example.model.Question
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import com.example.ui.components.PracticeAppearancePanel
import com.example.util.PracticeAppearanceManager
import com.example.util.FontSettingsManager
import com.example.repository.AppConfigRepository
import com.example.model.PracticeAppearanceSettings
import android.widget.Toast
import androidx.compose.material.icons.filled.Palette
import com.example.ui.components.HeaderLevel
import com.example.ui.components.PremiumSectionHeader
import com.example.ui.components.PremiumSubscriptionBadge
import com.example.ui.components.StatBadge
import com.example.ui.components.TopicFhdCardImage
import com.example.ui.components.premiumPress
import com.example.ui.theme.AppLuminousBackground
import com.example.ui.theme.PremiumCardColorSystem

// =========================================================================
// 1. PALETTES FOR EXACT RECREATION OF REFERENCE DESIGN
// =========================================================================

data class SetCardTheme(
    val accentColor: Color,
    val cardBgStart: Color,
    val cardBgEnd: Color,
    val cardBorder: Color,
    val buttonBg: Color,
    val chevronTint: Color
)

data class SubtopicCardTheme(
    val accentColor: Color,
    val pillBg: Color,
    val pillBorder: Color,
    val cardBgStart: Color,
    val cardBgEnd: Color,
    val cardBorder: Color
)

private val setCardThemes = listOf(
    // 01: Warm Orange / Amber
    SetCardTheme(
        accentColor = Color(0xFFF59E0B),
        cardBgStart = Color(0xFFFFFBEB),
        cardBgEnd = Color(0xFFFEF3C7),
        cardBorder = Color(0xFFFDE68A),
        buttonBg = Color(0xFFFFFBEB),
        chevronTint = Color(0xFFEA580C)
    ),
    // 02: Mint / Emerald Green
    SetCardTheme(
        accentColor = Color(0xFF059669),
        cardBgStart = Color(0xFFF0FDF4),
        cardBgEnd = Color(0xFFDCFCE7),
        cardBorder = Color(0xFFA7F3D0),
        buttonBg = Color(0xFFF0FDF4),
        chevronTint = Color(0xFF059669)
    ),
    // 03: Coral Pink / Rose
    SetCardTheme(
        accentColor = Color(0xFFE11D48),
        cardBgStart = Color(0xFFFFF1F2),
        cardBgEnd = Color(0xFFFFE4E6),
        cardBorder = Color(0xFFFECDD3),
        buttonBg = Color(0xFFFFF1F2),
        chevronTint = Color(0xFFE11D48)
    ),
    // 04: Violet / Purple
    SetCardTheme(
        accentColor = Color(0xFF7C3AED),
        cardBgStart = Color(0xFFF5F3FF),
        cardBgEnd = Color(0xFFEDE9FE),
        cardBorder = Color(0xFFDDD6FE),
        buttonBg = Color(0xFFF5F3FF),
        chevronTint = Color(0xFF7C3AED)
    ),
    // 05: Sky Blue / Cyan
    SetCardTheme(
        accentColor = Color(0xFF0284C7),
        cardBgStart = Color(0xFFF0F9FF),
        cardBgEnd = Color(0xFFE0F2FE),
        cardBorder = Color(0xFFBAE6FD),
        buttonBg = Color(0xFFF0F9FF),
        chevronTint = Color(0xFF0284C7)
    ),
    // 06: Indigo / Deep Blue
    SetCardTheme(
        accentColor = Color(0xFF4F46E5),
        cardBgStart = Color(0xFFEEF2FF),
        cardBgEnd = Color(0xFFE0E7FF),
        cardBorder = Color(0xFFC7D2FE),
        buttonBg = Color(0xFFEEF2FF),
        chevronTint = Color(0xFF4F46E5)
    ),
    // 07: Teal / Emerald
    SetCardTheme(
        accentColor = Color(0xFF0D9488),
        cardBgStart = Color(0xFFF0FDFA),
        cardBgEnd = Color(0xFFCCFBF1),
        cardBorder = Color(0xFF99F6E4),
        buttonBg = Color(0xFFF0FDFA),
        chevronTint = Color(0xFF0D9488)
    ),
    // 08: Fuchsia / Magenta
    SetCardTheme(
        accentColor = Color(0xFFC026D3),
        cardBgStart = Color(0xFFFDF4FF),
        cardBgEnd = Color(0xFFFAE8FF),
        cardBorder = Color(0xFFF5D0FE),
        buttonBg = Color(0xFFFDF4FF),
        chevronTint = Color(0xFFC026D3)
    )
)

private val subtopicCardThemes = listOf(
    // S01: Sapphire Theme
    SubtopicCardTheme(
        accentColor = Color(0xFF2563EB),
        pillBg = Color(0xFFDBEAFE),
        pillBorder = Color(0xFF93C5FD),
        cardBgStart = Color(0xFFF0F6FF),
        cardBgEnd = Color(0xFFE0EDFF),
        cardBorder = Color(0xFFBFDBFE)
    ),
    // S02: Emerald Theme
    SubtopicCardTheme(
        accentColor = Color(0xFF059669),
        pillBg = Color(0xFFD1FAE5),
        pillBorder = Color(0xFF6EE7B7),
        cardBgStart = Color(0xFFF0FDF4),
        cardBgEnd = Color(0xFFDCFCE7),
        cardBorder = Color(0xFFA7F3D0)
    ),
    // S03: Royal Purple Theme
    SubtopicCardTheme(
        accentColor = Color(0xFF7C3AED),
        pillBg = Color(0xFFEDE9FE),
        pillBorder = Color(0xFFC4B5FD),
        cardBgStart = Color(0xFFFAF5FF),
        cardBgEnd = Color(0xFFF3E8FF),
        cardBorder = Color(0xFFE9D5FF)
    ),
    // S04: Sunset Coral Theme
    SubtopicCardTheme(
        accentColor = Color(0xFFEA580C),
        pillBg = Color(0xFFFFEDD5),
        pillBorder = Color(0xFFFDBA74),
        cardBgStart = Color(0xFFFFF7ED),
        cardBgEnd = Color(0xFFFFEDD5),
        cardBorder = Color(0xFFFED7AA)
    ),
    // S05: Ruby Theme
    SubtopicCardTheme(
        accentColor = Color(0xFFE11D48),
        pillBg = Color(0xFFFFE4E6),
        pillBorder = Color(0xFFFDA4AF),
        cardBgStart = Color(0xFFFFF1F2),
        cardBgEnd = Color(0xFFFFE4E6),
        cardBorder = Color(0xFFFECDD3)
    ),
    // S06: Ocean Cyan Theme
    SubtopicCardTheme(
        accentColor = Color(0xFF0891B2),
        pillBg = Color(0xFFCCFBF1),
        pillBorder = Color(0xFF67E8F9),
        cardBgStart = Color(0xFFECFEFF),
        cardBgEnd = Color(0xFFCFFAFE),
        cardBorder = Color(0xFFA5F3FC)
    )
)

// =========================================================================
// 2. HELPER: CHUNK QUESTIONS INTO EXACT 10-QUESTION SETS
// =========================================================================

fun generateSetsForQuestionsList(
    questions: List<Question>,
    baseId: String = "set"
): List<PracticeSetModel> {
    if (questions.isEmpty()) return emptyList()

    val chunks = questions.chunked(10)
    return chunks.mapIndexed { index, chunkQuestions ->
        val setNumber = index + 1
        val startQ = index * 10 + 1
        val endQ = startQ + chunkQuestions.size - 1
        PracticeSetModel(
            id = "${baseId}-set-$setNumber",
            title = "Set %02d".format(setNumber),
            subtitle = "${chunkQuestions.size} Qs • Q$startQ – Q$endQ",
            setNumber = setNumber,
            questionsCount = chunkQuestions.size,
            startQuestionNumber = startQ,
            endQuestionNumber = endQ,
            questions = chunkQuestions,
            badge = ""
        )
    }
}

// =========================================================================
// 3. MAIN TOPIC SETS SCREEN (DASHBOARD SAME AS UNIT & SUBJECTS DASHBOARD)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicSetsScreen(
    subject: Subject,
    unit: UnitModel?,
    topic: TopicModel?,
    onBack: () -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    onStartSet: (PracticeSetModel, List<PracticeSetModel>) -> Unit,
    testResults: List<com.example.model.TestResult> = emptyList(),
    onNavigateToSubscription: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeTopicVal = remember(subject.id, unit?.id, topic?.id) {
        val found = if (unit != null && topic != null) {
            SampleData.subjectsState
                .find { it.id == subject.id }
                ?.units?.find { it.id == unit.id }
                ?.topics?.find { it.id == topic.id } ?: topic
        } else topic ?: unit?.topics?.firstOrNull()

        found ?: TopicModel(
            id = "default-topic",
            name = unit?.name ?: subject.name,
            estimatedMcqs = "100"
        )
    }

    val topicQuestions = remember(activeTopicVal.id, subject.id, unit?.id, SampleData.questionsState.size) {
        val direct = SampleData.questionsState.filter { 
            (it.topicId == activeTopicVal.id || (topic != null && it.topicId == topic.id)) &&
            (it.subjectId.isBlank() || it.subjectId == subject.id)
        }
        if (direct.isNotEmpty()) {
            direct
        } else {
            val unitMatches = SampleData.questionsState.filter {
                it.subjectId == subject.id && (unit == null || it.unitId == unit.id || it.unitId.isBlank())
            }
            if (unitMatches.isNotEmpty()) unitMatches else {
                SampleData.questionsState.filter { it.subjectId == subject.id }
            }
        }
    }

    // Determine subtopics list: prefer explicit subtopics on topic or questions
    val subtopicsList = remember(activeTopicVal.subtopics, topic?.subtopics, topicQuestions) {
        val explicit = (activeTopicVal.subtopics.ifEmpty { topic?.subtopics ?: emptyList() }).filter { it.isNotBlank() }
        val rawList = if (explicit.isNotEmpty()) {
            explicit
        } else {
            topicQuestions.map { it.subtopic }.filter { it.isNotBlank() }.distinct()
        }
        // ONLY keep subtopics that have at least 1 question in topicQuestions
        rawList.filter { sub ->
            topicQuestions.any { q -> q.subtopic.equals(sub, ignoreCase = true) }
        }
    }

    var activeSection by rememberSaveable { mutableStateOf("SETS") } // "SETS" or "SUBTOPICS"

    // Generate sets chunked to 10 or fewer questions per set directly from all topic questions
    val setsList = remember(topicQuestions) {
        generateSetsForQuestionsList(
            questions = topicQuestions,
            baseId = "set-${subject.id}-${unit?.id ?: ""}-${activeTopicVal.id}-all"
        )
    }

    val subtopicSetsList = remember(subtopicsList, topicQuestions) {
        subtopicsList.mapIndexed { index, subtopicTitle ->
            val subQuestions = topicQuestions.filter { it.subtopic.equals(subtopicTitle, ignoreCase = true) }
            PracticeSetModel(
                id = "subtopic-${activeTopicVal.id}-${subtopicTitle.hashCode()}",
                title = subtopicTitle,
                subtitle = "${subQuestions.size} Questions",
                setNumber = index + 1,
                questionsCount = subQuestions.size,
                startQuestionNumber = 1,
                endQuestionNumber = subQuestions.size,
                questions = subQuestions,
                badge = ""
            )
        }
    }

    val matchedCountsMap = remember(subtopicsList, topicQuestions) {
        subtopicsList.mapIndexed { index, subtopicTitle ->
            val matched = topicQuestions.count { it.subtopic.equals(subtopicTitle, ignoreCase = true) }
            val count = if (matched > 0) {
                matched
            } else {
                if (subtopicsList.size <= 1) {
                    topicQuestions.size
                } else {
                    val baseSize = (topicQuestions.size / subtopicsList.size).coerceAtLeast(1)
                    if (index == subtopicsList.lastIndex) {
                        (topicQuestions.size - (baseSize * (subtopicsList.size - 1))).coerceAtLeast(1)
                    } else {
                        baseSize
                    }
                }
            }
            subtopicTitle to count
        }.toMap()
    }

    val topicHeaderGradient = remember(activeTopicVal.id, activeTopicVal.name, subject.name) {
        val topicIdx = unit?.topics?.indexOfFirst { it.id == activeTopicVal.id }?.takeIf { it >= 0 } ?: 0
        PremiumCardColorSystem.getGradientForTopic(topicIdx, activeTopicVal.name, subject.name)
    }

    // Intercept back button: exit back
    BackHandler {
        onBack()
    }

    val configRepository = remember { AppConfigRepository.getInstance() }
    val currentAppConfig by configRepository.currentAppConfig.collectAsState()
    var showPracticeAppearanceSheet by remember { mutableStateOf(false) }

    AppLuminousBackground(screenName = "Topic Sets Screen") {
        Scaffold(
            topBar = {
                PremiumSectionHeader(
                    title = activeTopicVal.name,
                    level = HeaderLevel.TOPIC,
                    onBack = {
                        onBack()
                    },
                    onNavigateToHome = onNavigateToHome,
                    rightAction = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Practice Appearance Button for Sets and Subtopics
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { showPracticeAppearanceSheet = true }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Practice Appearance Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            PremiumSubscriptionBadge(
                                onNavigateToSubscription = onNavigateToSubscription
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // =========================================================
                // 1. TOPIC DASHBOARD CARD (SAME FHD HERO STYLE AS UNIT & SUBJECTS DASHBOARD)
                // BUT CONTAINS ONLY TOPIC NAME, NUMBER OF SETS, AND NUMBER OF MCQS
                // =========================================================
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.5.dp, topicHeaderGradient.borderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp)
                                .background(Brush.linearGradient(colors = topicHeaderGradient.colors))
                        ) {
                            // Topic assigned FHD Image in background
                            TopicFhdCardImage(
                                topic = activeTopicVal,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Subtle transparent overlay for text readability with clear background image visibility
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.15f),
                                                Color.Black.copy(alpha = 0.35f),
                                                Color.Black.copy(alpha = 0.55f)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = activeTopicVal.name.uppercase(),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!activeTopicVal.subtitle.isNullOrBlank()) {
                                        Text(
                                            text = activeTopicVal.subtitle,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                            color = Color(0xFFFDE68A).copy(alpha = 0.9f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Topic name, number of Sets, MCQs, and Subtopics!
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    StatBadge(
                                        label = "Sets",
                                        value = "${setsList.size}",
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.weight(1f),
                                        isCompact = true
                                    )
                                    StatBadge(
                                        label = "MCQs",
                                        value = "${topicQuestions.size}",
                                        color = Color(0xFF059669),
                                        modifier = Modifier.weight(1f),
                                        isCompact = true
                                    )
                                    StatBadge(
                                        label = "Subtopics",
                                        value = "${subtopicsList.size}",
                                        color = Color(0xFF2563EB),
                                        modifier = Modifier.weight(1f),
                                        isCompact = true
                                    )
                                }
                            }
                        }
                    }
                }

                // =========================================================
                // 2. TOGGLE ACTION BUTTONS: SWITCH BETWEEN SETS & SUBTOPICS
                // =========================================================
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // TOGGLE 1: SETS
                        val isSetsActive = activeSection == "SETS"
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSetsActive) Color(0xFFEA580C) else Color(0xFFF1F5F9),
                            shadowElevation = if (isSetsActive) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { activeSection = "SETS" }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = if (isSetsActive) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sets (${setsList.size})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    ),
                                    color = if (isSetsActive) Color.White else Color(0xFF475569)
                                )
                            }
                        }

                        // TOGGLE 2: SUBTOPICS
                        val isSubtopicsActive = activeSection == "SUBTOPICS"
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSubtopicsActive) Color(0xFFEA580C) else Color(0xFFF1F5F9),
                            shadowElevation = if (isSubtopicsActive) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { activeSection = "SUBTOPICS" }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = if (isSubtopicsActive) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Subtopics (${subtopicsList.size})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    ),
                                    color = if (isSubtopicsActive) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                // =========================================================
                // 3. 2-COLUMN GRID DISPLAY FOR SETS OR SUBTOPICS
                // =========================================================
                if (activeSection == "SETS") {
                    if (setsList.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassEmpty,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Sets Available",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Practice questions are being prepared for this topic.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    } else {
                        val solidColors = listOf(
                            Color(0xFF059669), // Emerald green
                            Color(0xFF7C3AED), // Vibrant purple
                            Color(0xFFD97706), // Golden orange
                            Color(0xFF2563EB), // Bright blue
                            Color(0xFFE11D48), // Rich pink
                            Color(0xFF0D9488)  // Teal
                        )
                        val chunkedSets = setsList.chunked(2)
                        itemsIndexed(chunkedSets, key = { rowIndex, _ -> "set_row_$rowIndex" }) { rowIndex, rowSets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowSets.forEachIndexed { colIndex, pSet ->
                                    val globalIndex = rowIndex * 2 + colIndex
                                    val bgCol = solidColors[globalIndex % solidColors.size]
                                    val serialNumber = "%02d".format(pSet.setNumber)
                                    val setSubtitle = "${pSet.questionsCount} Questions • Q${pSet.startQuestionNumber}–Q${pSet.endQuestionNumber}"

                                    val hasDbCompleted = testResults.any { it.testName.contains(pSet.title, ignoreCase = true) }
                                    val status = remember(pSet.id, testResults) {
                                        PracticeSessionManager.getSessionStatus(context, pSet.id, hasDbCompleted)
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        CompactGridSetCard(
                                            serialNumber = serialNumber,
                                            title = "Set $serialNumber",
                                            subtitle = setSubtitle,
                                            status = status,
                                            backgroundColor = bgCol,
                                            onClick = { onStartSet(pSet, setsList) }
                                        )
                                    }
                                }
                                if (rowSets.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    if (subtopicsList.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassEmpty,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Subtopics Available",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Practice questions are organized directly in the Sets tab.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    } else {
                        val solidColors = listOf(
                            Color(0xFF2563EB), // Blue
                            Color(0xFF059669), // Emerald
                            Color(0xFF7C3AED), // Purple
                            Color(0xFFEA580C), // Orange
                            Color(0xFFE11D48), // Rose
                            Color(0xFF0891B2)  // Cyan
                        )
                        val chunkedSubs = subtopicsList.chunked(2)
                        itemsIndexed(chunkedSubs, key = { rowIndex, _ -> "sub_row_$rowIndex" }) { rowIndex, rowSubs ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowSubs.forEachIndexed { colIndex, subtopicTitle ->
                                    val globalIndex = rowIndex * 2 + colIndex
                                    val bgCol = solidColors[globalIndex % solidColors.size]
                                    val code = "S%02d".format(globalIndex + 1)
                                    val subQuestionsCount = matchedCountsMap[subtopicTitle] ?: 0

                                    val subtopicId = "subtopic-${activeTopicVal.id}-${subtopicTitle.hashCode()}"
                                    val hasDbCompletedSub = testResults.any { it.testName.contains(subtopicTitle, ignoreCase = true) }
                                    val status = remember(subtopicId, testResults) {
                                        PracticeSessionManager.getSessionStatus(context, subtopicId, hasDbCompletedSub)
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        CompactGridSubtopicCard(
                                            code = code,
                                            title = subtopicTitle,
                                            questionsCount = subQuestionsCount,
                                            status = status,
                                            backgroundColor = bgCol,
                                            onClick = {
                                                val subQuestions = topicQuestions.filter { it.subtopic.equals(subtopicTitle, ignoreCase = true) }
                                                val subtopicPracticeSet = subtopicSetsList.getOrNull(globalIndex) ?: PracticeSetModel(
                                                    id = subtopicId,
                                                    title = subtopicTitle,
                                                    subtitle = "${subQuestions.size} Questions",
                                                    setNumber = globalIndex + 1,
                                                    questionsCount = subQuestions.size,
                                                    startQuestionNumber = 1,
                                                    endQuestionNumber = subQuestions.size,
                                                    questions = subQuestions,
                                                    badge = ""
                                                )
                                                onStartSet(subtopicPracticeSet, subtopicSetsList)
                                            }
                                        )
                                    }
                                }
                                if (rowSubs.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // =========================================================
                // 4. BOTTOM STATUS LEGEND
                // =========================================================
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem(color = Color(0xFF94A3B8), label = "Not Started")
                            LegendItem(color = Color(0xFFF59E0B), label = "In Progress")
                            LegendItem(color = Color(0xFF10B981), label = "Completed")
                        }
                    }
                }
            }
        }

        if (showPracticeAppearanceSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPracticeAppearanceSheet = false },
                containerColor = Color.White
            ) {
                PracticeAppearancePanel(
                    currentSettings = currentAppConfig.advancedFontSettings.practiceAppearance,
                    onSave = { newPa ->
                        PracticeAppearanceManager.saveSettings(context, newPa)
                        val updatedAdvanced = currentAppConfig.advancedFontSettings.copy(
                            practiceAppearance = newPa
                        )
                        FontSettingsManager.saveSettings(context, updatedAdvanced)
                        configRepository.updateAppConfig(
                            currentAppConfig.copy(advancedFontSettings = updatedAdvanced)
                        )
                        showPracticeAppearanceSheet = false
                        Toast.makeText(context, "Practice appearance applied!", Toast.LENGTH_SHORT).show()
                    },
                    onReset = {
                        PracticeAppearanceManager.resetToDefault(context)
                        val defPa = PracticeAppearanceSettings()
                        val updatedAdvanced = currentAppConfig.advancedFontSettings.copy(
                            practiceAppearance = defPa
                        )
                        FontSettingsManager.saveSettings(context, updatedAdvanced)
                        configRepository.updateAppConfig(
                            currentAppConfig.copy(advancedFontSettings = updatedAdvanced)
                        )
                        showPracticeAppearanceSheet = false
                        Toast.makeText(context, "Practice appearance reset to default", Toast.LENGTH_SHORT).show()
                    },
                    onBack = { showPracticeAppearanceSheet = false }
                )
            }
        }
    }
}

// =========================================================================
// 4. TOPIC SIDE-BY-SIDE TAB BUTTONS ("SETS" & "SUBTOPICS")
// =========================================================================

@Composable
fun TopicSideBySideTabsBar(
    selectedTab: String,
    setsCount: Int,
    subtopicsCount: Int,
    onTabSelected: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // TAB 1: SETS
            val isSetsActive = selectedTab == "SETS"
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSetsActive) Color(0xFF2563EB) else Color.Transparent,
                shadowElevation = if (isSetsActive) 2.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onTabSelected("SETS") }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "SETS",
                        tint = if (isSetsActive) Color.White else Color(0xFF64748B),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "SETS",
                        color = if (isSetsActive) Color.White else Color(0xFF1E293B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (isSetsActive) Color.White.copy(alpha = 0.25f) else Color(0xFFEFF6FF),
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "$setsCount",
                                color = if (isSetsActive) Color.White else Color(0xFF2563EB),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // TAB 2: SUBTOPICS
            val isSubtopicsActive = selectedTab == "SUBTOPICS"
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSubtopicsActive) Color(0xFF2563EB) else Color.Transparent,
                shadowElevation = if (isSubtopicsActive) 2.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onTabSelected("SUBTOPICS") }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "SUBTOPICS",
                        tint = if (isSubtopicsActive) Color.White else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "SUBTOPICS",
                        color = if (isSubtopicsActive) Color.White else Color(0xFF1E293B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (isSubtopicsActive) Color.White.copy(alpha = 0.25f) else Color(0xFFEFF6FF),
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "$subtopicsCount",
                                color = if (isSubtopicsActive) Color.White else Color(0xFF2563EB),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. SET CARD ITEM (EXACT RECREATION OF REFERENCE DESIGN - 30% OF SUBJECTS CARD: 50.dp)
// =========================================================================

@Composable
fun SetCardItem(
    serialNumber: String,
    title: String,
    subtitle: String,
    status: String,
    theme: SetCardTheme,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, theme.cardBorder),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .premiumPress(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(theme.cardBgStart, theme.cardBgEnd),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Serial / Set Badge Circle
                Surface(
                    shape = CircleShape,
                    color = theme.accentColor,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Middle Text: Set Title and Subtitle
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "• $subtitle",
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    StatusBadge(status = status, isCompact = true)
                }

                // Right Action Button
                Surface(
                    shape = CircleShape,
                    color = theme.accentColor,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Start $title",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. SUBTOPIC CARD ITEM
// =========================================================================

@Composable
fun SubtopicCardItem(
    code: String,
    title: String,
    questionsCount: Int,
    status: String,
    theme: SubtopicCardTheme,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, theme.cardBorder),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .premiumPress(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(theme.cardBgStart, theme.cardBgEnd),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subtopic Code Pill (S01, S02, S03...)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = theme.pillBg,
                    border = BorderStroke(1.dp, theme.pillBorder),
                    modifier = Modifier.size(width = 46.dp, height = 28.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = code,
                            color = theme.accentColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Subtopic Title & Questions Count Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$questionsCount Questions",
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.5.sp
                        )
                        StatusBadge(status = status, isCompact = true)
                    }
                }

                // Right Action Button
                Surface(
                    shape = CircleShape,
                    color = theme.accentColor,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Start $title",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, isCompact: Boolean = false) {
    val (bgColor, textColor, label, icon) = when (status) {
        "Completed" -> Quadruple(
            Color(0xFFDCFCE7), // Light green background
            Color(0xFF15803D), // Dark green text
            "Completed",
            Icons.Default.CheckCircle
        )
        "Attempting" -> Quadruple(
            Color(0xFFFEF3C7), // Light amber background
            Color(0xFFB45309), // Dark amber text
            "Attempting",
            Icons.Default.PlayCircleFilled
        )
        else -> Quadruple(
            Color(0xFFF1F5F9), // Light slate background
            Color(0xFF475569), // Dark slate text
            "Not Attempted",
            Icons.Default.RadioButtonUnchecked
        )
    }

    Surface(
        shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp),
        color = bgColor,
        modifier = Modifier.padding(top = if (isCompact) 0.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isCompact) 5.dp else 8.dp,
                vertical = if (isCompact) 1.5.dp else 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(if (isCompact) 10.dp else 13.dp)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = if (isCompact) 9.5.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Composable
fun CompactGridSetCard(
    serialNumber: String,
    title: String,
    subtitle: String,
    status: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val isCompleted = status.equals("Completed", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(16.dp))
            .premiumPress(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = serialNumber,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCompleted) Color(0xFFDCFCE7) else Color.White.copy(alpha = 0.25f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayCircleFilled,
                            contentDescription = null,
                            tint = if (isCompleted) Color(0xFF15803D) else Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (isCompleted) "Done" else "Start",
                            color = if (isCompleted) Color(0xFF15803D) else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CompactGridSubtopicCard(
    code: String,
    title: String,
    questionsCount: Int,
    status: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val isCompleted = status.equals("Completed", ignoreCase = true)
    val progress = if (isCompleted) 1f else if (status.equals("Attempting", ignoreCase = true)) 0.5f else 0f
    val progressPercent = (progress * 100).toInt()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .premiumPress(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subtopic number circular badge
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = code,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                // Circular right-arrow icon
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open $title",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$questionsCount Questions",
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$progressPercent%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF475569)
        )
    }
}

