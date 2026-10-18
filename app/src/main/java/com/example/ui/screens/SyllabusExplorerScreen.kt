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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
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
    val totalTopics = currentSubject.units.sumOf { it.topics.size }
    val totalQuestions = remember(currentSubject.id, SampleData.questionsState.size) { SampleData.getQuestionsCountForSubject(currentSubject.id) }
    val isPremium = SampleData.isUserPremium()
    var upgradePromptFeature by remember { mutableStateOf<String?>(null) }

    // Active Unit state: null = viewing Units list, non-null = viewing Topics of that Unit
    var activeUnit by remember(currentSubject.id, initialUnit?.id) {
        mutableStateOf(initialUnit)
    }

    // Handle system back button when in Topics view to return to Units view
    BackHandler(enabled = activeUnit != null) {
        activeUnit = null
    }

    AppLuminousBackground(screenName = "Syllabus Explorer") {
        Scaffold(
            topBar = {
                PremiumSectionHeader(
                    title = if (activeUnit == null) currentSubject.name else activeUnit?.name ?: currentSubject.name,
                    level = if (activeUnit == null) HeaderLevel.SUBJECT else HeaderLevel.UNIT,
                    onBack = {
                        if (activeUnit != null) {
                            activeUnit = null
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
            SharedTransitionLayout {
                val sharedScope = this
                AnimatedContent(
                    targetState = activeUnit,
                    label = "UnitToTopicsTransition"
                ) { targetUnit ->
                    val animatedVisibilityScope = this
                    if (targetUnit == null) {
                    // ==========================================
                    // 1. UNITS VIEW (Units List for Subject)
                    // ==========================================
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        


                        // Section Title: Units list instruction
                        item {
                            val totalUnits = currentSubject.units.size
                            val totalTopics = currentSubject.units.sumOf { it.topics.size }
                            val totalSets = totalTopics * 2
                            val totalMcqs = remember(currentSubject.id, SampleData.questionsState.size) { SampleData.getQuestionsCountForSubject(currentSubject.id) }
                            val subjGradient = remember(currentSubject.id, currentSubject.name) {
                                com.example.ui.theme.PremiumCardColorSystem.getGradientForSubject(currentSubject.name, currentSubject.id)
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                border = BorderStroke(1.5.dp, subjGradient.borderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                        Text(
                                            text = currentSubject.name.uppercase(),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

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

                        item {
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
                                        text = "Click any unit below to view its topics",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        // LIST OF UNITS (Each with large FHD Hero Image Card!)
                        itemsIndexed(currentSubject.units) { unitIndex, unit ->
                            val isUnitLocked = !isPremium && unitIndex > 0
                            val unitMcqCount = remember(currentSubject.id, unit.id, SampleData.questionsState.size) {
                                unit.topics.sumOf { SampleData.getQuestionsForPractice(currentSubject.id, unit.id, it.id, "").size }
                            }
                            val unitSetsCount = remember(unit.topics.size) { unit.topics.size * 2 }
                            val unitGradient = remember(unit.id, currentSubject.name) {
                                com.example.ui.theme.PremiumCardColorSystem.getGradientForUnit(unit.unitNumber, currentSubject.name, unit.name)
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(118.dp) // 70% of Subjects Card (168.dp)
                                    .sharedBounds(
                                        sharedContentState = sharedScope.rememberSharedContentState(key = "unit_${unit.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        boundsTransform = { _, _ ->
                                            spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                                        }
                                    )
                                    .premiumPress {
                                        if (isUnitLocked) {
                                            upgradePromptFeature = "Unit ${unit.unitNumber.takeIf { it > 0 } ?: (unitIndex + 1)}: ${unit.name}"
                                        } else {
                                            activeUnit = unit
                                        }
                                    },
                                border = BorderStroke(1.5.dp, if (isUnitLocked) Color(0xFFFFD700).copy(alpha = 0.6f) else unitGradient.borderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(colors = unitGradient.colors))
                                ) {
                                    // Full Bleed Unit FHD Image
                                    UnitFhdCardImage(
                                        unit = unit,
                                        subjectName = currentSubject.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .sharedElement(
                                                state = sharedScope.rememberSharedContentState(key = "unit_image_${unit.id}"),
                                                animatedVisibilityScope = animatedVisibilityScope
                                            )
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
                                                        text = "View Topics",
                                                        color = Color(0xFFFDE68A),
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFDE68A),
                                                        modifier = Modifier.size(13.dp)
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
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                } else {
                    // ==========================================
                    // 2. TOPICS VIEW (Topics List for Unit)
                    // ==========================================
                    val activeUnitVal = targetUnit
                    val unitMcqCount = remember(activeUnitVal.id, SampleData.questionsState.size) { activeUnitVal.topics.sumOf { SampleData.getQuestionsForPractice(currentSubject.id, activeUnitVal.id, it.id, "").size } }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // Section Title: Topics instruction
                        item {
                            val unitTopicsCount = activeUnitVal.topics.size
                            val unitSetsCount = unitTopicsCount * 2
                            val unitMcqsCount = remember(activeUnitVal.id, SampleData.questionsState.size) { activeUnitVal.topics.sumOf { SampleData.getQuestionsForPractice(currentSubject.id, activeUnitVal.id, it.id, "").size } }
                            val unitHeaderGradient = remember(activeUnitVal.id, currentSubject.name) {
                                com.example.ui.theme.PremiumCardColorSystem.getGradientForUnit(activeUnitVal.unitNumber, currentSubject.name, activeUnitVal.name)
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                border = BorderStroke(1.5.dp, unitHeaderGradient.borderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .sharedBounds(
                                        sharedContentState = sharedScope.rememberSharedContentState(key = "unit_${activeUnitVal.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        boundsTransform = { _, _ ->
                                            spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                                        }
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(118.dp) // 70% of Subjects Card (168.dp)
                                        .background(Brush.linearGradient(colors = unitHeaderGradient.colors))
                                ) {
                                    // Unit assigned Image in background
                                    UnitFhdCardImage(
                                        unit = activeUnitVal,
                                        subjectName = currentSubject.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .sharedElement(
                                                state = sharedScope.rememberSharedContentState(key = "unit_image_${activeUnitVal.id}"),
                                                animatedVisibilityScope = animatedVisibilityScope
                                            )
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
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = activeUnitVal.name.uppercase(),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        val unitDone = activeUnitVal.completionPercentage ?: 0
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            StatBadge("Topics", "$unitTopicsCount", Color(0xFF7C3AED), Modifier.weight(1f), isCompact = true)
                                            StatBadge("Sets", "$unitSetsCount", Color(0xFFD97706), Modifier.weight(1f), isCompact = true)
                                            StatBadge("MCQs", "$unitMcqsCount", Color(0xFF059669), Modifier.weight(1f), isCompact = true)
                                            StatBadge("Done", "$unitDone%", Color(0xFF2563EB), Modifier.weight(1f), isCompact = true)
                                        }
                                    }
                                }
                            }
                        }

                        item {
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
                        }

                        // LIST OF TOPICS (Each with large FHD Hero Image Card, identical in style to Subjects & Units!)
                        itemsIndexed(
                            items = activeUnitVal.topics,
                            key = { _, topic -> topic.id },
                            contentType = { _, _ -> "topic" }
                        ) { topicIndex, topic ->
                            val isTopicLocked = !isPremium && topicIndex > 0
                            val topicSets = remember(SampleData.questionsState.size, currentSubject.id, activeUnitVal.id, topic.id) {
                                SampleData.getPracticeSetsForTopic(currentSubject.id, activeUnitVal, topic)
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
                                    .premiumPress {
                                        if (isTopicLocked) {
                                            upgradePromptFeature = "Topic: ${topic.name}"
                                        } else {
                                            onSelectTopic(activeUnitVal, topic)
                                        }
                                    },
                                border = BorderStroke(1.5.dp, if (isTopicLocked) Color(0xFFFFD700).copy(alpha = 0.6f) else topicGradient.borderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(modifier = Modifier
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

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
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
