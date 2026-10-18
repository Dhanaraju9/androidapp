package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Question
import com.example.ui.theme.*

data class SubjectPalette(
    val gradientStart: Color,
    val gradientEnd: Color,
    val accentColor: Color,
    val textColor: Color,
    val icon: ImageVector,
    val iconBgColor: Color,
    val cardBgColor: Color
)

fun getSubjectPalette(subjectId: String, isDark: Boolean): SubjectPalette {
    return when (subjectId.lowercase()) {
        "subj-history", "history", "subj-ap-history", "ap-history", "subj-indian-history" -> SubjectPalette(
            gradientStart = if (isDark) Color(0xFF7C2D12) else Color(0xFFFFEDE5),
            gradientEnd = if (isDark) Color(0xFF451A03) else Color(0xFFFFD1BF),
            accentColor = Color(0xFFEA580C),
            textColor = if (isDark) Color(0xFFFFEBE3) else Color(0xFF7C2D12),
            icon = Icons.Default.History,
            iconBgColor = if (isDark) Color(0xFF9A3412) else Color(0xFFFFDDD2),
            cardBgColor = if (isDark) Color(0xFF2C1B15) else Color(0xFFFFF7F5)
        )
        "subj-polity", "polity", "subj-constitution", "constitution" -> SubjectPalette(
            gradientStart = if (isDark) Color(0xFF1E3A8A) else Color(0xFFEFF6FF),
            gradientEnd = if (isDark) Color(0xFF172554) else Color(0xFFDBEAFE),
            accentColor = Color(0xFF2563EB),
            textColor = if (isDark) Color(0xFFEFF6FF) else Color(0xFF1E40AF),
            icon = Icons.Default.Gavel,
            iconBgColor = if (isDark) Color(0xFF1D4ED8) else Color(0xFFBFDBFE),
            cardBgColor = if (isDark) Color(0xFF111E3A) else Color(0xFFF8FAFC)
        )
        "subj-geography", "geography" -> SubjectPalette(
            gradientStart = if (isDark) Color(0xFF065F46) else Color(0xFFE6F4EA),
            gradientEnd = if (isDark) Color(0xFF022C22) else Color(0xFFC2E7D9),
            accentColor = Color(0xFF059669),
            textColor = if (isDark) Color(0xFFE6F4EA) else Color(0xFF065F46),
            icon = Icons.Default.Public,
            iconBgColor = if (isDark) Color(0xFF047857) else Color(0xFFA3E2C9),
            cardBgColor = if (isDark) Color(0xFF0B251E) else Color(0xFFF4FAF7)
        )
        "subj-economy", "economy" -> SubjectPalette(
            gradientStart = if (isDark) Color(0xFF4C1D95) else Color(0xFFF5F3FF),
            gradientEnd = if (isDark) Color(0xFF2E1065) else Color(0xFFEDE9FE),
            accentColor = Color(0xFF7C3AED),
            textColor = if (isDark) Color(0xFFF5F3FF) else Color(0xFF5B21B6),
            icon = Icons.Default.TrendingUp,
            iconBgColor = if (isDark) Color(0xFF6D28D9) else Color(0xFFDDD6FE),
            cardBgColor = if (isDark) Color(0xFF1F1235) else Color(0xFFFAF9FF)
        )
        "subj-science", "science", "subj-science-tech", "science-tech" -> SubjectPalette(
            gradientStart = if (isDark) Color(0xFF111827) else Color(0xFFF3F4F6),
            gradientEnd = if (isDark) Color(0xFF030712) else Color(0xFFE5E7EB),
            accentColor = Color(0xFF4B5563),
            textColor = if (isDark) Color(0xFFF9FAFB) else Color(0xFF1F2937),
            icon = Icons.Default.Science,
            iconBgColor = if (isDark) Color(0xFF374151) else Color(0xFFD1D5DB),
            cardBgColor = if (isDark) Color(0xFF1A1D24) else Color(0xFFF9FAFB)
        )
        "subj-mental-ability", "mental-ability" -> SubjectPalette(
            gradientStart = if (isDark) Color(0xFF78350F) else Color(0xFFFEF3C7),
            gradientEnd = if (isDark) Color(0xFF451A03) else Color(0xFFFDE68A),
            accentColor = Color(0xFFD97706),
            textColor = if (isDark) Color(0xFFFEF3C7) else Color(0xFF78350F),
            icon = Icons.Default.Psychology,
            iconBgColor = if (isDark) Color(0xFFB45309) else Color(0xFFFCD34D),
            cardBgColor = if (isDark) Color(0xFF281E15) else Color(0xFFFFFDF5)
        )
        "subj-current-affairs", "current-affairs" -> SubjectPalette(
            gradientStart = if (isDark) Color(0xFF831843) else Color(0xFFFDF2F8),
            gradientEnd = if (isDark) Color(0xFF500724) else Color(0xFFFCE7F3),
            accentColor = Color(0xFFDB2777),
            textColor = if (isDark) Color(0xFFFDF2F8) else Color(0xFF9D174D),
            icon = Icons.Default.Newspaper,
            iconBgColor = if (isDark) Color(0xFFC2185B) else Color(0xFFFFEBEE),
            cardBgColor = if (isDark) Color(0xFF2B121C) else Color(0xFFFFF5F8)
        )
        "subj-society", "society" -> SubjectPalette(
            gradientStart = if (isDark) Color(0xFF14532D) else Color(0xFFF0FDF4),
            gradientEnd = if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7),
            accentColor = Color(0xFF16A34A),
            textColor = if (isDark) Color(0xFFF0FDF4) else Color(0xFF14532D),
            icon = Icons.Default.People,
            iconBgColor = if (isDark) Color(0xFF15803D) else Color(0xFFBBF7D0),
            cardBgColor = if (isDark) Color(0xFF122417) else Color(0xFFF6FCF8)
        )
        else -> {
            val hash = subjectId.hashCode()
            val themeIndex = kotlin.math.abs(hash) % 4
            when (themeIndex) {
                0 -> SubjectPalette(
                    gradientStart = if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF),
                    gradientEnd = if (isDark) Color(0xFF311042) else Color(0xFFE0E7FF),
                    accentColor = Color(0xFF4F46E5),
                    textColor = if (isDark) Color(0xFFEEF2FF) else Color(0xFF3730A3),
                    icon = Icons.Default.MenuBook,
                    iconBgColor = if (isDark) Color(0xFF4338CA) else Color(0xFFC7D2FE),
                    cardBgColor = if (isDark) Color(0xFF19182C) else Color(0xFFF9FAFF)
                )
                1 -> SubjectPalette(
                    gradientStart = if (isDark) Color(0xFF0F172A) else Color(0xFFECFEFF),
                    gradientEnd = if (isDark) Color(0xFF0F2C59) else Color(0xFFCFFAFE),
                    accentColor = Color(0xFF06B6D4),
                    textColor = if (isDark) Color(0xFFECFEFF) else Color(0xFF0E7490),
                    icon = Icons.Default.MenuBook,
                    iconBgColor = if (isDark) Color(0xFF0891B2) else Color(0xFFA5F3FC),
                    cardBgColor = if (isDark) Color(0xFF131B2B) else Color(0xFFF0FDFF)
                )
                2 -> SubjectPalette(
                    gradientStart = if (isDark) Color(0xFF581C87) else Color(0xFFFAF5FF),
                    gradientEnd = if (isDark) Color(0xFF3B0764) else Color(0xFFF3E8FF),
                    accentColor = Color(0xFF8B5CF6),
                    textColor = if (isDark) Color(0xFFFAF5FF) else Color(0xFF6B21A8),
                    icon = Icons.Default.MenuBook,
                    iconBgColor = if (isDark) Color(0xFF7C3AED) else Color(0xFFE9D5FF),
                    cardBgColor = if (isDark) Color(0xFF221535) else Color(0xFFFCF9FF)
                )
                else -> SubjectPalette(
                    gradientStart = if (isDark) Color(0xFF3F1B0B) else Color(0xFFFEF3C7),
                    gradientEnd = if (isDark) Color(0xFF421504) else Color(0xFFFFE4E6),
                    accentColor = Color(0xFFD97706),
                    textColor = if (isDark) Color(0xFFFFE4E6) else Color(0xFF9E2A2B),
                    icon = Icons.Default.MenuBook,
                    iconBgColor = if (isDark) Color(0xFFB45309) else Color(0xFFFCD34D),
                    cardBgColor = if (isDark) Color(0xFF251611) else Color(0xFFFFF9F9)
                )
            }
        }
    }
}

private fun getSubjectDisplayName(subjectId: String): String {
    return when (subjectId.lowercase()) {
        "subj-history", "history", "subj-indian-history" -> "Indian History"
        "subj-society", "society" -> "Indian Society"
        "subj-geography", "geography" -> "Geography"
        "subj-current-affairs", "current-affairs" -> "Current Affairs"
        "subj-polity", "polity", "subj-constitution", "constitution" -> "Indian Polity"
        "subj-economy", "economy" -> "Indian Economy"
        "subj-science", "science", "subj-science-tech", "science-tech" -> "Science & Tech"
        "subj-ap-history" -> "AP History"
        "subj-mental-ability" -> "Mental Ability"
        "all" -> "All Subjects"
        else -> subjectId.replace("subj-", "").replace("-", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedQuestionsScreen(
    savedQuestions: List<Question>,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onRemoveBookmark: (String) -> Unit,
    onPracticeSaved: (List<Question>, String) -> Unit,
    onNavigateToExplore: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf("all") }
    var selectedTopicName by remember { mutableStateOf("all") }
    var viewMode by remember { mutableStateOf("hierarchy") } // "hierarchy" or "flat"
    var expandedQuestionIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expandedSubjects by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expandedTopics by remember { mutableStateOf<Set<String>>(emptySet()) }
    var questionToDelete by remember { mutableStateOf<Question?>(null) }

    val subjectsInBookmarks = remember(savedQuestions) {
        savedQuestions.map { it.subjectId }.distinct().filter { it.isNotBlank() }
    }

    val topicsInSelectedSubject = remember(savedQuestions, selectedSubjectId) {
        savedQuestions.filter { q ->
            selectedSubjectId == "all" || q.subjectId.equals(selectedSubjectId, ignoreCase = true)
        }.map { q ->
            if (q.subtopic.isNotBlank()) q.subtopic 
            else if (q.topicId.isNotBlank()) q.topicId 
            else "General Bookmarks"
        }.distinct().filter { it.isNotBlank() }
    }

    // Reset selected topic if it doesn't belong to the newly selected subject
    LaunchedEffect(selectedSubjectId) {
        selectedTopicName = "all"
    }

    val filteredQuestions = remember(savedQuestions, searchQuery, selectedSubjectId, selectedTopicName) {
        savedQuestions.filter { q ->
            val matchesSubject = selectedSubjectId == "all" || q.subjectId.equals(selectedSubjectId, ignoreCase = true)
            val topicName = if (q.subtopic.isNotBlank()) q.subtopic else if (q.topicId.isNotBlank()) q.topicId else "General Bookmarks"
            val matchesTopic = selectedTopicName == "all" || topicName.equals(selectedTopicName, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    q.questionText.contains(searchQuery, ignoreCase = true) ||
                    q.subtopic.contains(searchQuery, ignoreCase = true) ||
                    q.topicId.contains(searchQuery, ignoreCase = true) ||
                    q.options.values.any { it.contains(searchQuery, ignoreCase = true) }
            matchesSubject && matchesTopic && matchesSearch
        }
    }

    // Expand all subjects and topics on load/search change automatically for instant view
    LaunchedEffect(filteredQuestions) {
        expandedSubjects = filteredQuestions.map { it.subjectId }.toSet()
        expandedTopics = filteredQuestions.map { "${it.subjectId}_${if (it.subtopic.isNotBlank()) it.subtopic else if (it.topicId.isNotBlank()) it.topicId else "General Bookmarks"}" }.toSet()
    }

    if (questionToDelete != null) {
        val target = questionToDelete!!
        AlertDialog(
            onDismissRequest = { questionToDelete = null },
            title = {
                Text(
                    text = "Remove Bookmark?",
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "Remove this high-quality MCQ from your revision bank?",
                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveBookmark(target.id)
                        questionToDelete = null
                        Toast.makeText(context, "Bookmark removed instantly", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Remove", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { questionToDelete = null }) {
                    Text("Cancel", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569))
                }
            },
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        )
    }

    AppLuminousBackground(screenName = "Saved Questions") {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Premium Revision Hub",
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = (-0.5).sp
                                )
                            }
                            Text(
                                text = "${savedQuestions.size} Bookmarked Questions • 100% Offline Ready",
                                color = Color(0xFF3B82F6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                            }
                            IconButton(onClick = onNavigateToHome) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home",
                                    tint = Color(0xFF2563EB)
                                )
                            }
                        }
                    },
                    actions = {
                        FilledTonalButton(
                            onClick = {
                                viewMode = if (viewMode == "hierarchy") "flat" else "hierarchy"
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (viewMode == "hierarchy") Color(0xFF2563EB) else Color(0xFFEFF6FF),
                                contentColor = if (viewMode == "hierarchy") Color.White else Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (viewMode == "hierarchy") Icons.Default.AccountTree else Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (viewMode == "hierarchy") "Hierarchy" else "Flat List",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                if (filteredQuestions.isNotEmpty()) {
                    Surface(
                        color = if (isDark) Color(0xFF1E293B) else Color.White,
                        tonalElevation = 8.dp,
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${filteredQuestions.size} Questions Selected",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Ready for target practice",
                                    fontSize = 11.sp,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            }

                            Button(
                                onClick = {
                                    onPracticeSaved(filteredQuestions, "Custom Revision Practice")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Practice Selected", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // PREMIUM HEADER BANNER
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (isDark) {
                                        listOf(Color(0xFF1E3A8A), Color(0xFF581C87))
                                    } else {
                                        listOf(Color(0xFFEFF6FF), Color(0xFFF5F3FF))
                                    }
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🎯 MCQ REVISION HUB",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = if (isDark) Color.White else Color(0xFF1E3A8A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Handpicked questions categorized automatically for focused preparation.",
                                    fontSize = 11.sp,
                                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                    lineHeight = 16.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0xFF3B82F6).copy(alpha = 0.3f) else Color(0xFF3B82F6).copy(alpha = 0.1f),
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${savedQuestions.size}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isDark) Color.White else Color(0xFF1D4ED8)
                                    )
                                    Text(
                                        text = "SAVED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isDark) Color.White else Color(0xFF1D4ED8)
                                    )
                                }
                            }
                        }
                    }
                }

                // SEARCH BAR
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search among bookmarked questions...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(18.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                        unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                    ),
                    singleLine = true
                )

                // SUBJECT HORIZONTAL FILTER ROW
                Text(
                    text = "FILTER BY SUBJECT",
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedSubjectId == "all",
                            onClick = { selectedSubjectId = "all" },
                            label = { Text("All Subjects", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White,
                                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF),
                                labelColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF2563EB)
                            ),
                            border = null
                        )
                    }

                    items(subjectsInBookmarks) { subjectId ->
                        val palette = getSubjectPalette(subjectId, isDark)
                        val isSelected = selectedSubjectId.equals(subjectId, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubjectId = subjectId },
                            label = { Text(getSubjectDisplayName(subjectId), fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.accentColor,
                                selectedLabelColor = Color.White,
                                containerColor = if (isDark) Color(0xFF1E293B) else palette.gradientStart,
                                labelColor = if (isSelected) Color.White else palette.textColor
                            ),
                            border = null
                        )
                    }
                }

                // TOPIC HORIZONTAL FILTER ROW (Visible if any subject has topics)
                if (topicsInSelectedSubject.isNotEmpty()) {
                    Text(
                        text = "FILTER BY TOPIC",
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedTopicName == "all",
                                onClick = { selectedTopicName = "all" },
                                label = { Text("All Topics", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF10B981),
                                    selectedLabelColor = Color.White,
                                    containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFECFDF5),
                                    labelColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF047857)
                                ),
                                border = null
                            )
                        }

                        items(topicsInSelectedSubject) { topicName ->
                            val isSelected = selectedTopicName.equals(topicName, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTopicName = topicName },
                                label = { Text(topicName, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF10B981),
                                    selectedLabelColor = Color.White,
                                    containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFECFDF5),
                                    labelColor = if (isSelected) Color.White else Color(0xFF047857)
                                ),
                                border = null
                            )
                        }
                    }
                }

                // CONTENT AREA
                if (filteredQuestions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No bookmarked questions match your criteria.",
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search query or filters to find saved MCQs.",
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        if (viewMode == "hierarchy") {
                            // SUBJECTS > TOPICS > QUESTIONS HIERARCHY VIEW
                            val questionsBySubject = filteredQuestions.groupBy { it.subjectId }

                            questionsBySubject.forEach { (subjectId, subjectQuestions) ->
                                val subjectName = getSubjectDisplayName(subjectId)
                                val isSubjectExpanded = expandedSubjects.contains(subjectId)
                                val palette = getSubjectPalette(subjectId, isDark)

                                item(key = "subj_$subjectId") {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
                                        border = BorderStroke(1.5.dp, palette.accentColor.copy(alpha = 0.5f)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            // Subject Header Row
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        expandedSubjects = if (isSubjectExpanded) {
                                                            expandedSubjects - subjectId
                                                        } else {
                                                            expandedSubjects + subjectId
                                                        }
                                                    }
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            colors = listOf(palette.gradientStart, palette.gradientEnd)
                                                        )
                                                    )
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = palette.iconBgColor,
                                                        modifier = Modifier.size(40.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = palette.icon,
                                                                contentDescription = null,
                                                                tint = palette.accentColor,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = subjectName,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 15.sp,
                                                            color = palette.textColor
                                                        )
                                                        Text(
                                                            text = "${subjectQuestions.size} Bookmarks Available",
                                                            fontSize = 11.sp,
                                                            color = palette.textColor.copy(alpha = 0.8f),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            onPracticeSaved(subjectQuestions, "$subjectName Revision Practice")
                                                        },
                                                        border = BorderStroke(1.dp, palette.accentColor),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.accentColor),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(30.dp)
                                                    ) {
                                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text("Practice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        imageVector = if (isSubjectExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = null,
                                                        tint = palette.textColor
                                                    )
                                                }
                                            }

                                            // Topics under Subject
                                            AnimatedVisibility(visible = isSubjectExpanded) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                                                        .padding(12.dp),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    val questionsByTopic = subjectQuestions.groupBy { 
                                                        if (it.subtopic.isNotBlank()) it.subtopic 
                                                        else if (it.topicId.isNotBlank()) it.topicId 
                                                        else "General Bookmarks" 
                                                    }

                                                    questionsByTopic.forEach { (topicName, topicQuestions) ->
                                                        val topicKey = "${subjectId}_$topicName"
                                                        val isTopicExpanded = expandedTopics.contains(topicKey)

                                                        Card(
                                                            shape = RoundedCornerShape(12.dp),
                                                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
                                                            border = BorderStroke(1.dp, palette.accentColor.copy(alpha = 0.3f)),
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Column {
                                                                // Topic Header Row
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .clickable {
                                                                            expandedTopics = if (isTopicExpanded) {
                                                                                expandedTopics - topicKey
                                                                            } else {
                                                                                expandedTopics + topicKey
                                                                            }
                                                                        }
                                                                        .padding(12.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                                ) {
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        modifier = Modifier.weight(1f)
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Bookmark,
                                                                            contentDescription = null,
                                                                            tint = palette.accentColor,
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(8.dp))
                                                                        Text(
                                                                            text = topicName,
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontSize = 13.sp,
                                                                            color = if (isDark) Color.White else Color(0xFF0F172A)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Badge(
                                                                            containerColor = palette.iconBgColor,
                                                                            contentColor = palette.accentColor
                                                                        ) {
                                                                            Text("${topicQuestions.size}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                                        }
                                                                    }

                                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                                        TextButton(
                                                                            onClick = {
                                                                                onPracticeSaved(topicQuestions, "$topicName Revision")
                                                                            },
                                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                                        ) {
                                                                            Text("Practice (${topicQuestions.size})", fontSize = 11.sp, fontWeight = FontWeight.Black, color = palette.accentColor)
                                                                        }
                                                                        Icon(
                                                                            imageVector = if (isTopicExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                                            contentDescription = null,
                                                                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                    }
                                                                }

                                                                // Questions under Topic
                                                                AnimatedVisibility(visible = isTopicExpanded) {
                                                                    Column(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .padding(8.dp),
                                                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                                                    ) {
                                                                        topicQuestions.forEach { question ->
                                                                            val isExpanded = expandedQuestionIds.contains(question.id)
                                                                            SavedQuestionCard(
                                                                                question = question,
                                                                                isExpanded = isExpanded,
                                                                                onToggleExpand = {
                                                                                    expandedQuestionIds = if (isExpanded) {
                                                                                        expandedQuestionIds - question.id
                                                                                    } else {
                                                                                        expandedQuestionIds + question.id
                                                                                    }
                                                                                },
                                                                                onPracticeSingle = {
                                                                                    onPracticeSaved(listOf(question), "Saved Question Practice")
                                                                                },
                                                                                onDeleteClick = {
                                                                                    questionToDelete = question
                                                                                },
                                                                                onShareClick = {
                                                                                    shareQuestion(question, context)
                                                                                }
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
                        } else {
                            // FLAT LIST VIEW
                            items(filteredQuestions, key = { it.id }) { question ->
                                val isExpanded = expandedQuestionIds.contains(question.id)

                                SavedQuestionCard(
                                    question = question,
                                    isExpanded = isExpanded,
                                    onToggleExpand = {
                                        expandedQuestionIds = if (isExpanded) {
                                            expandedQuestionIds - question.id
                                        } else {
                                            expandedQuestionIds + question.id
                                        }
                                    },
                                    onPracticeSingle = {
                                        onPracticeSaved(listOf(question), "Saved Question Practice")
                                    },
                                    onDeleteClick = {
                                        questionToDelete = question
                                    },
                                    onShareClick = {
                                        shareQuestion(question, context)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun shareQuestion(question: Question, context: android.content.Context) {
    val shareBody = buildString {
        append("📌 APPSC Group 2 MCQ Practice\n\n")
        append(question.questionText)
        append("\n\n")
        append("A) ${question.options["A"]}\n")
        append("B) ${question.options["B"]}\n")
        append("C) ${question.options["C"]}\n")
        append("D) ${question.options["D"]}\n\n")
        append("✅ Correct Answer: Option ${question.correctOption}\n")
        if (!question.explanation.isNullOrBlank()) {
            append("📖 Explanation: ${question.explanation}\n")
        }
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "APPSC Group 2 Saved MCQ")
        putExtra(Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Question"))
}

@Composable
private fun SavedQuestionCard(
    question: Question,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onPracticeSingle: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val palette = getSubjectPalette(question.subjectId, isDark)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val label = if (question.subtopic.isNotBlank()) question.subtopic else getSubjectDisplayName(question.subjectId)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = palette.iconBgColor,
                        border = BorderStroke(1.dp, palette.accentColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = label,
                            color = palette.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Remove Bookmark",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question.questionText,
                color = if (isDark) Color.White else Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("A", "B", "C", "D").forEach { optKey ->
                    val optText = question.options[optKey] ?: ""
                    if (optText.isNotBlank()) {
                        val isCorrect = optKey.equals(question.correctOption, ignoreCase = true)
                        val defaultOptionBorder = when (optKey) {
                            "A" -> Color(0xFF2563EB).copy(alpha = 0.2f)
                            "B" -> Color(0xFF7C3AED).copy(alpha = 0.2f)
                            "C" -> Color(0xFF059669).copy(alpha = 0.2f)
                            else -> Color(0xFFD97706).copy(alpha = 0.2f)
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCorrect) {
                                if (isDark) Color(0xFF064E3B) else Color(0xFFECFDF5)
                            } else {
                                if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
                            },
                            border = BorderStroke(
                                if (isCorrect) 1.5.dp else 1.dp,
                                if (isCorrect) Color(0xFF10B981) else defaultOptionBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isCorrect) Color(0xFF10B981) else if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = optKey,
                                            color = if (isCorrect) Color.White else if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = optText,
                                    color = if (isCorrect) {
                                        if (isDark) Color(0xFFD1FAE5) else Color(0xFF065F46)
                                    } else {
                                        if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                                    },
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Correct Answer",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val hasValidExplanation = !question.explanation.isNullOrBlank() &&
                    question.explanation.trim().lowercase() != "no explanation available for this question." &&
                    question.explanation.trim().lowercase() != "no explanation available."

            if (hasValidExplanation) {
                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF172554) else Color(0xFFF0F9FF))
                            .border(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFBAE6FD), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Detailed Explanation / వివరణ",
                                color = if (isDark) Color(0xFF93C5FD) else Color(0xFF0369A1),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = question.explanation,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF0F172A),
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = if (hasValidExplanation) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasValidExplanation) {
                    TextButton(
                        onClick = onToggleExpand,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isExpanded) "Hide Solution" else "View Solution",
                            fontSize = 11.5.sp,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onPracticeSingle,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isDark) Color(0xFF3B1808) else Color(0xFFFFEDE5),
                        contentColor = Color(0xFFEA580C)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Practice", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
