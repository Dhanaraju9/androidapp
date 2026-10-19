package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import com.example.ui.components.*
import com.example.ui.theme.AppLuminousBackground

/**
 * Syllabus Explorer Screen:
 * Level 1: Subjects -> Shows Units (each Unit with a large FHD image card just like Subjects)
 * Level 2: Units -> Shows Topics (each Topic with a large FHD image card just like Subjects & Units)
 * Level 3: Topics -> Opens Practice Sets & Questions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusExplorerScreen(
    subject: Subject,
    initialUnit: UnitModel? = null,
    onBack: () -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    onSelectTopic: (UnitModel, TopicModel) -> Unit = { _, _ -> },
    onSelectSubjectSets: (Subject) -> Unit = { _ -> },
    onSelectUnitSets: (UnitModel) -> Unit = { _ -> },
    onNavigateToSubscription: () -> Unit = {}
) {
    // Keep in sync with latest SampleData state
    val currentSubject = SampleData.subjectsState.find { it.id == subject.id } ?: subject
    val isPremium = SampleData.isUserPremium()
    var upgradePromptFeature by remember { mutableStateOf<String?>(null) }

    // Subject accordion state (expanded by default when entering Subject Explorer)
    var isSubjectExpanded by remember(currentSubject.id) { mutableStateOf(true) }

    // Single-select or Multi-select Unit accordion state controlled by AnimationConfigManager
    var expandedUnitId by remember(currentSubject.id, initialUnit?.id) {
        mutableStateOf(initialUnit?.id)
    }
    var multiExpandedUnitIds by remember(currentSubject.id, initialUnit?.id) {
        mutableStateOf(if (initialUnit != null) setOf(initialUnit.id) else emptySet<String>())
    }
    val accordionSpec = com.example.util.AnimationConfigManager.resolveSpec(
        com.example.model.AnimatableTarget.ACCORDION_EXPAND_COLLAPSE,
        "Syllabus Explorer"
    )
    val isSingleOpenAccordion = accordionSpec.singleOpenAccordion &&
        accordionSpec.hierarchyType != com.example.model.HierarchyDrillDownAnimationType.MULTI_OPEN_ACCORDION
    val activeUnit = remember(currentSubject.units, expandedUnitId) {
        currentSubject.units.find { it.id == expandedUnitId }
    }

    // Handle system back button when a Unit is expanded to collapse it first
    BackHandler(enabled = expandedUnitId != null || multiExpandedUnitIds.isNotEmpty()) {
        expandedUnitId = null
        multiExpandedUnitIds = emptySet()
    }

    AppLuminousBackground(screenName = "Syllabus Explorer") {
        Scaffold(
            topBar = {
                PremiumSectionHeader(
                    title = activeUnit?.name ?: currentSubject.name,
                    level = if (activeUnit == null) HeaderLevel.SUBJECT else HeaderLevel.UNIT,
                    onBack = {
                        if (expandedUnitId != null) {
                            expandedUnitId = null
                        } else {
                            onBack()
                        }
                    },
                    onNavigateToHome = onNavigateToHome,
                    rightAction = {
                        PremiumSubscriptionBadge(
                            onNavigateToSubscription = onNavigateToSubscription
                        )
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==========================================
                // 1. SUBJECT ACCORDION HEADER CARD
                // ==========================================
                item(key = "subject_header_${currentSubject.id}") {
                    val totalUnits = currentSubject.units.size
                    val totalTopics = currentSubject.units.sumOf { it.topics.size }
                    val totalSets = totalTopics * 2
                    val totalMcqs = remember(currentSubject.id, SampleData.questionsState.size) {
                        SampleData.getQuestionsCountForSubject(currentSubject.id)
                    }
                    val subjGradient = remember(currentSubject.id, currentSubject.name) {
                        com.example.ui.theme.PremiumCardColorSystem.getGradientForSubject(
                            currentSubject.name,
                            currentSubject.id
                        )
                    }
                    val subjectChevronRotation = AppMotionSystem.animateAccordionChevronRotation(
                        isExpanded = isSubjectExpanded,
                        label = "SyllabusSubjectChevron_${currentSubject.id}"
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(
                            if (isSubjectExpanded) 2.dp else 1.5.dp,
                            subjGradient.borderColor
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .premiumHeroExpand(
                                target = com.example.model.AnimatableTarget.SUBJECT_CARDS,
                                isExpanded = isSubjectExpanded,
                                screenKey = "Syllabus Explorer",
                                index = 0
                            )
                            .premiumPress(
                                target = com.example.model.AnimatableTarget.SUBJECT_CARDS,
                                screenKey = "Syllabus Explorer"
                            ) {
                                val nextExpanded = !isSubjectExpanded
                                isSubjectExpanded = nextExpanded
                                if (!nextExpanded) {
                                    expandedUnitId = null
                                    multiExpandedUnitIds = emptySet()
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(168.dp)
                                .background(Brush.linearGradient(colors = subjGradient.colors))
                        ) {
                            // Subject assigned Image in background
                            SubjectFhdCardImage(
                                subject = currentSubject,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Subtle transparent overlay for text readability
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.2f),
                                                Color.Black.copy(alpha = 0.5f),
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentSubject.name.uppercase(),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.45f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExpandMore,
                                            contentDescription = if (isSubjectExpanded) "Collapse Units" else "Expand Units",
                                            tint = Color(0xFFFDE68A),
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .size(16.dp)
                                                .rotate(subjectChevronRotation)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatBadge("Units", "$totalUnits", Color(0xFF2563EB), Modifier.weight(1f))
                                    StatBadge("Topics", "$totalTopics", Color(0xFF7C3AED), Modifier.weight(1f))
                                    StatBadge("Sets", "$totalSets", Color(0xFFD97706), Modifier.weight(1f))
                                    StatBadge("MCQs", "$totalMcqs", Color(0xFF059669), Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                item(key = "select_unit_instruction") {
                    AnimatedVisibility(
                        visible = isSubjectExpanded,
                        enter = AppMotionSystem.cascadeContainerEnterTransition(),
                        exit = AppMotionSystem.cascadeContainerExitTransition()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SELECT A UNIT",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Tap any unit below to expand its topics in place",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 2. UNITS ACCORDION LIST + CASCADING TOPICS
                // ==========================================
                itemsIndexed(
                    items = currentSubject.units,
                    key = { _, unit -> unit.id },
                    contentType = { _, _ -> "unit_accordion_item" }
                ) { unitIndex, unit ->
                    AnimatedVisibility(
                        visible = isSubjectExpanded,
                        enter = AppMotionSystem.cascadeContainerEnterTransition(),
                        exit = AppMotionSystem.cascadeContainerExitTransition()
                    ) {
                        val isUnitLocked = !isPremium && unitIndex > 0
                        val isUnitExpanded = if (isSingleOpenAccordion) {
                            expandedUnitId == unit.id
                        } else {
                            unit.id in multiExpandedUnitIds || expandedUnitId == unit.id
                        }
                        val unitChevronRotation = AppMotionSystem.animateAccordionChevronRotation(
                            isExpanded = isUnitExpanded,
                            label = "SyllabusUnitChevron_${unit.id}"
                        )
                        val unitMcqCount = remember(currentSubject.id, unit.id, SampleData.questionsState.size) {
                            unit.topics.sumOf { SampleData.getQuestionsForPractice(currentSubject.id, unit.id, it.id, "").size }
                        }
                        val unitSetsCount = remember(unit.topics.size) { unit.topics.size * 2 }
                        val unitGradient = remember(unit.id, currentSubject.name) {
                            com.example.ui.theme.PremiumCardColorSystem.getGradientForUnit(unit.unitNumber, currentSubject.name, unit.name)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .cascadeDropItem(
                                    visible = isSubjectExpanded,
                                    index = unitIndex,
                                    target = com.example.model.AnimatableTarget.UNIT_CARDS,
                                    screenKey = "Syllabus Explorer"
                                )
                        ) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(118.dp) // 70% of Subjects Card (168.dp)
                                    .premiumPress(
                                        target = com.example.model.AnimatableTarget.UNIT_CARDS,
                                        screenKey = "Syllabus Explorer"
                                    ) {
                                        if (isUnitLocked) {
                                            upgradePromptFeature = "Unit ${unit.unitNumber.takeIf { it > 0 } ?: (unitIndex + 1)}: ${unit.name}"
                                        } else if (isSingleOpenAccordion) {
                                            expandedUnitId = if (isUnitExpanded) null else unit.id
                                            multiExpandedUnitIds = emptySet()
                                        } else {
                                            if (isUnitExpanded) {
                                                multiExpandedUnitIds = multiExpandedUnitIds - unit.id
                                                if (expandedUnitId == unit.id) expandedUnitId = null
                                            } else {
                                                multiExpandedUnitIds = multiExpandedUnitIds + unit.id
                                            }
                                        }
                                    },
                                border = BorderStroke(
                                    if (isUnitExpanded) 2.dp else 1.5.dp,
                                    if (isUnitLocked) Color(0xFFFFD700).copy(alpha = 0.6f)
                                    else if (isUnitExpanded) Color(0xFFFDE68A)
                                    else unitGradient.borderColor
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isUnitExpanded) 4.dp else 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(colors = unitGradient.colors))
                                ) {
                                    // Full Bleed Unit FHD Image
                                    UnitFhdCardImage(
                                        unit = unit,
                                        subjectName = currentSubject.name,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Gradient Overlay for Text Readability
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = if (isUnitLocked) 0.35f else 0.15f),
                                                        Color.Black.copy(alpha = if (isUnitLocked) 0.65f else 0.45f),
                                                        Color.Black.copy(alpha = if (isUnitLocked) 0.90f else 0.82f)
                                                    )
                                                )
                                            )
                                    )

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Top Badges
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = unitGradient.badgeBg
                                            ) {
                                                Text(
                                                    text = "UNIT ${unit.unitNumber.takeIf { it > 0 } ?: (unitIndex + 1)}",
                                                    color = unitGradient.badgeText,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            if (isUnitLocked) {
                                                PremiumLockBadge(text = "LOCKED (PRO)", isSmall = true)
                                            } else {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (isUnitExpanded) "Hide Topics" else "View Topics",
                                                        color = Color(0xFFFDE68A),
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.ExpandMore,
                                                        contentDescription = if (isUnitExpanded) "Collapse Topics" else "Expand Topics",
                                                        tint = Color(0xFFFDE68A),
                                                        modifier = Modifier
                                                            .size(15.dp)
                                                            .rotate(unitChevronRotation)
                                                    )
                                                }
                                            }
                                        }

                                        // Bottom Information
                                        Column {
                                            Text(
                                                text = SampleData.formatUnitTitle(unit.unitNumber, unit.name),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${unit.topics.size} Topics • $unitSetsCount Sets" + if (unitMcqCount > 0) " • $unitMcqCount MCQs" else "",
                                                color = unitGradient.textColorSecondary,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                fontSize = 11.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            // ==========================================
                            // 3. EXPANDED UNIT -> TOPICS CASCADE ACCORDION
                            // ==========================================
                            AnimatedVisibility(
                                visible = isUnitExpanded,
                                enter = AppMotionSystem.cascadeContainerEnterTransition(),
                                exit = AppMotionSystem.cascadeContainerExitTransition()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp, start = 6.dp, end = 6.dp, bottom = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "UNIT TOPICS",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 0.5.sp
                                                ),
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = "Click any topic to practice MCQs and test sets",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }

                                    unit.topics.forEachIndexed { topicIndex, topic ->
                                        key(topic.id) {
                                            val isTopicLocked = !isPremium && topicIndex > 0
                                            val topicSets = remember(SampleData.questionsState.size, currentSubject.id, unit.id, topic.id) {
                                                SampleData.getPracticeSetsForTopic(currentSubject.id, unit, topic)
                                            }
                                            val totalSetsCount = remember(topicSets) { topicSets.size }
                                            val totalMcqCount = remember(topicSets) { topicSets.sumOf { it.questionsCount } }
                                            val topicGradient = remember(topic.id, topic.name) {
                                                com.example.ui.theme.PremiumCardColorSystem.getGradientForTopic(topicIndex, topic.name, currentSubject.name)
                                            }

                                            Card(
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(96.dp)
                                                    .cascadeDropItem(
                                                        visible = isUnitExpanded,
                                                        index = topicIndex,
                                                        target = com.example.model.AnimatableTarget.TOPIC_CARDS,
                                                        screenKey = "Syllabus Explorer"
                                                    )
                                                    .premiumPress(
                                                        target = com.example.model.AnimatableTarget.TOPIC_CARDS,
                                                        screenKey = "Syllabus Explorer"
                                                    ) {
                                                        if (isTopicLocked) {
                                                            upgradePromptFeature = "Topic: ${topic.name}"
                                                        } else {
                                                            onSelectTopic(unit, topic)
                                                        }
                                                    },
                                                border = BorderStroke(1.5.dp, if (isTopicLocked) Color(0xFFFFD700).copy(alpha = 0.6f) else topicGradient.borderColor),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Brush.linearGradient(colors = topicGradient.colors))
                                                ) {
                                                    // Full Bleed Topic FHD Image
                                                    TopicFhdCardImage(
                                                        topic = topic,
                                                        modifier = Modifier.fillMaxSize()
                                                    )

                                                    // Gradient Overlay for Text Readability
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                Brush.verticalGradient(
                                                                    colors = listOf(
                                                                        Color.Black.copy(alpha = if (isTopicLocked) 0.35f else 0.18f),
                                                                        Color.Black.copy(alpha = if (isTopicLocked) 0.60f else 0.45f),
                                                                        Color.Black.copy(alpha = if (isTopicLocked) 0.82f else 0.72f),
                                                                        Color.Black.copy(alpha = if (isTopicLocked) 0.94f else 0.88f)
                                                                    )
                                                                )
                                                            )
                                                    )

                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                                        verticalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        // Top Row: Topic Badge + Sets/MCQs or Pro Lock
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = topicGradient.badgeBg
                                                            ) {
                                                                Text(
                                                                    text = "TOPIC ${topicIndex + 1}",
                                                                    color = topicGradient.badgeText,
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                            if (isTopicLocked) {
                                                                PremiumLockBadge(text = "LOCKED (PRO)", isSmall = true)
                                                            } else {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    Text(
                                                                        text = "$totalSetsCount Sets",
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                                                        color = Color(0xFFFDE68A)
                                                                    )
                                                                    Text(
                                                                        text = "•",
                                                                        color = Color.White.copy(alpha = 0.6f),
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                                                    )
                                                                    Text(
                                                                        text = "$totalMcqCount MCQs",
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                                                        color = Color.White
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        // Bottom Row: Topic Name & Subtitle + Action Pill (Practice or Unlock)
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                                                Text(
                                                                    text = topic.name,
                                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                                                    color = Color.White,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                if (!topic.subtitle.isNullOrBlank()) {
                                                                    Text(
                                                                        text = topic.subtitle,
                                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                                                        color = Color(0xFFFDE68A).copy(alpha = 0.9f),
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                }
                                                            }

                                                            Surface(
                                                                shape = RoundedCornerShape(12.dp),
                                                                color = if (isTopicLocked) Color(0xFF0F172A) else Color(0xFF10B981),
                                                                border = if (isTopicLocked) BorderStroke(1.dp, Color(0xFFFFD700)) else null
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    if (isTopicLocked) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Lock,
                                                                            contentDescription = null,
                                                                            tint = Color(0xFFFFD700),
                                                                            modifier = Modifier.size(11.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(3.dp))
                                                                        Text(
                                                                            text = "Unlock",
                                                                            color = Color(0xFFFFD700),
                                                                            fontSize = 10.5.sp,
                                                                            fontWeight = FontWeight.Bold
                                                                        )
                                                                    } else {
                                                                        Text(
                                                                            text = "Practice",
                                                                            color = Color.White,
                                                                            fontSize = 10.5.sp,
                                                                            fontWeight = FontWeight.Bold
                                                                        )
                                                                        Spacer(modifier = Modifier.width(3.dp))
                                                                        Icon(
                                                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                                            contentDescription = null,
                                                                            tint = Color.White,
                                                                            modifier = Modifier.size(11.dp)
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

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    upgradePromptFeature?.let { feature ->
        PremiumUpgradePromptDialog(
            title = "Unlock Premium Content",
            featureName = feature,
            description = "Free candidates can access only the first Unit and first Topic Set. Upgrade to Premium for unrestricted access to all syllabus units, topic MCQs, and mock tests.",
            onDismiss = { upgradePromptFeature = null },
            onUpgradeClick = {
                upgradePromptFeature = null
                onNavigateToSubscription()
            }
        )
    }
}
