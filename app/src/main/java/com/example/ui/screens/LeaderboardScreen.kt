package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.db.AppDatabase
import com.example.model.*
import com.example.repository.LeaderboardRepository
import com.example.repository.UserProgressRepository

// Pastel Pink & Champagne Luxury Palette
private val PaletteHeaderBgStart = Color(0xFFFFF0F5)
private val PaletteHeaderBgEnd = Color(0xFFFFF8FA)
private val PaletteTitleMagenta = Color(0xFFD81B60)
private val PaletteSubtitleText = Color(0xFF8C6274)
private val PaletteActiveTabBg = Color(0xFFE91E63)
private val PaletteInactiveTabBorder = Color(0xFFF8BBD0)
private val PaletteGoldAccent = Color(0xFFFFA000)
private val PaletteSilverAccent = Color(0xFF9E9E9E)
private val PaletteBronzeAccent = Color(0xFFE65100)
private val PaletteCardBorder = Color(0xFFF8BBD0)
private val PaletteCardBg = Color(0xFFFFFFFF)
private val PaletteYourRankBg = Color(0xFFFFEBF2)
private val PaletteYourRankBorder = Color(0xFFF48FB1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {}
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val currentAppId = SampleData.currentActiveAppId.value
    val db = remember { AppDatabase.getInstance(context) }
    val leaderboardRepo = remember { LeaderboardRepository.getInstance(context) }
    val userProgressRepo = remember { UserProgressRepository(db) }

    val allCandidates by leaderboardRepo.allCandidates.collectAsState()
    val currentUserCandidate by leaderboardRepo.currentUserEntry.collectAsState()
    val isLoading by leaderboardRepo.isLoading.collectAsState()

    val userProgress by userProgressRepo.getUserProgress(currentAppId)
        .collectAsState(initial = UserProgress(appId = currentAppId))
    val performanceSummary by userProgressRepo.getPerformanceSummary(currentAppId)
        .collectAsState(initial = MockTestPerformanceSummary())

    // Active Filter Tabs
    var selectedFilterTab by remember { mutableStateOf("Overall") }
    var selectedTimePeriod by remember { mutableStateOf("Overall") }
    var showPeriodMenu by remember { mutableStateOf(false) }

    var selectedTablePeriod by remember { mutableStateOf("This Month") }
    var showTablePeriodMenu by remember { mutableStateOf(false) }

    // Top 3 Candidates
    val top1 = remember(allCandidates) {
        allCandidates.find { it.rank == 1 } ?: LeaderboardCandidate(
            userId = "1", displayName = "Priya S", xp = 3450, accuracyPercentage = 96f,
            testsCompleted = 1250, streakDays = 42, rank = 1
        )
    }
    val top2 = remember(allCandidates) {
        allCandidates.find { it.rank == 2 } ?: LeaderboardCandidate(
            userId = "2", displayName = "Karthik M", xp = 2980, accuracyPercentage = 96f,
            testsCompleted = 1250, streakDays = 42, rank = 2
        )
    }
    val top3 = remember(allCandidates) {
        allCandidates.find { it.rank == 3 } ?: LeaderboardCandidate(
            userId = "3", displayName = "Sneha P", xp = 2760, accuracyPercentage = 90f,
            testsCompleted = 980, streakDays = 28, rank = 3
        )
    }

    // Ranks 4 to 10 for table
    val tableCandidates = remember(allCandidates) {
        val filtered = allCandidates.filter { it.rank in 4..10 }
        if (filtered.isNotEmpty()) filtered
        else listOf(
            LeaderboardCandidate(userId = "4", displayName = "Aditya K", xp = 2660, accuracyPercentage = 88f, testsCompleted = 920, streakDays = 24, rank = 4),
            LeaderboardCandidate(userId = "5", displayName = "Divya R", xp = 2420, accuracyPercentage = 86f, testsCompleted = 860, streakDays = 22, rank = 5),
            LeaderboardCandidate(userId = "6", displayName = "Manoj R", xp = 2310, accuracyPercentage = 84f, testsCompleted = 780, streakDays = 20, rank = 6),
            LeaderboardCandidate(userId = "7", displayName = "Harsha V", xp = 2150, accuracyPercentage = 82f, testsCompleted = 720, streakDays = 18, rank = 7),
            LeaderboardCandidate(userId = "8", displayName = "Meena K", xp = 1980, accuracyPercentage = 80f, testsCompleted = 680, streakDays = 16, rank = 8),
            LeaderboardCandidate(userId = "9", displayName = "Rohit P", xp = 1760, accuracyPercentage = 78f, testsCompleted = 620, streakDays = 14, rank = 9),
            LeaderboardCandidate(userId = "10", displayName = "Nikhil T", xp = 1620, accuracyPercentage = 76f, testsCompleted = 580, streakDays = 12, rank = 10)
        )
    }

    // User's Own Performance Values
    val userRankDisplay = currentUserCandidate?.rank ?: 15
    val userPointsDisplay = currentUserCandidate?.xp ?: (userProgress.totalXp.takeIf { it > 0 } ?: 1380)
    val userAccuracyDisplay = (currentUserCandidate?.accuracyPercentage ?: (performanceSummary.overallAccuracy.replace("%", "").toFloatOrNull() ?: 86f)).toInt()
    val userQuestionsDisplay = currentUserCandidate?.testsCompleted ?: (userProgress.completedQuestionsCount.takeIf { it > 0 } ?: 520)
    val userStreakDisplay = currentUserCandidate?.streakDays ?: (userProgress.streakDays.takeIf { it > 0 } ?: 18)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PaletteHeaderBgStart,
                        PaletteHeaderBgEnd,
                        Color(0xFFFFF8F0)
                    )
                )
            )
    ) {
        val isTablet = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back Arrow + Leaderboard Title Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(1.dp, PaletteCardBorder),
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable { onBack() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = PaletteTitleMagenta,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // 3D Trophy Icon
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFF8E1),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Trophy",
                                        tint = PaletteGoldAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "LeaderBoard",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PaletteTitleMagenta,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Top Learners Make a Brighter Future",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PaletteSubtitleText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Right actions: Home Shortcut + Time Period Dropdown
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(1.dp, PaletteCardBorder),
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable { onNavigateToHome() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Home",
                                        tint = PaletteTitleMagenta,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Time Period Dropdown
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, PaletteCardBorder),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { showPeriodMenu = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = PaletteTitleMagenta,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = selectedTimePeriod,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PaletteTitleMagenta
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = PaletteTitleMagenta,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showPeriodMenu,
                                    onDismissRequest = { showPeriodMenu = false }
                                ) {
                                    listOf("Overall", "This Month", "This Week", "Today").forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option, fontWeight = FontWeight.Bold) },
                                            onClick = {
                                                selectedTimePeriod = option
                                                showPeriodMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .widthIn(max = if (isTablet) 860.dp else 600.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // SECTION 1: 5 Horizontal Filter Tabs Bar
                item {
                    val filterTabs = listOf(
                        Triple("Overall", Icons.Default.LocalFireDepartment, "Overall"),
                        Triple("Prelims", Icons.Outlined.MenuBook, "Prelims"),
                        Triple("Mains", Icons.Outlined.Description, "Mains"),
                        Triple("Subjects", Icons.Outlined.School, "Subjects"),
                        Triple("Monthly", Icons.Outlined.CalendarMonth, "Monthly")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filterTabs) { (tabKey, icon, label) ->
                            val isActive = selectedFilterTab == tabKey
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isActive) PaletteActiveTabBg else Color.White,
                                border = BorderStroke(1.dp, if (isActive) PaletteActiveTabBg else PaletteInactiveTabBorder),
                                shadowElevation = if (isActive) 3.dp else 1.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { selectedFilterTab = tabKey }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isActive) Color.White else PaletteTitleMagenta,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else PaletteTitleMagenta
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION 2: Top 3 Learners Podium Section (Gold, Silver, Bronze)
                item {
                    TopThreePodiumView(
                        top1 = top1,
                        top2 = top2,
                        top3 = top3
                    )
                }

                // SECTION 3: Top Learners Table Card (Ranks 4-10)
                item {
                    TopLearnersTableCard(
                        candidates = tableCandidates,
                        selectedPeriod = selectedTablePeriod,
                        onPeriodClick = { showTablePeriodMenu = true },
                        showPeriodMenu = showTablePeriodMenu,
                        onDismissMenu = { showTablePeriodMenu = false },
                        onSelectPeriod = { period ->
                            selectedTablePeriod = period
                            showTablePeriodMenu = false
                        }
                    )
                }

                // SECTION 4: Your Rank Highlight Banner
                item {
                    YourRankBannerCard(
                        rank = userRankDisplay,
                        points = userPointsDisplay,
                        accuracy = userAccuracyDisplay,
                        questions = userQuestionsDisplay,
                        streak = userStreakDisplay
                    )
                }

                // SECTION 5: 4-Quadrant Performance Grid
                item {
                    if (isTablet) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    YourPerformanceQuadrantCard(
                                        accuracy = userAccuracyDisplay,
                                        points = userPointsDisplay,
                                        questions = userQuestionsDisplay,
                                        streak = userStreakDisplay
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    BadgesAndAchievementsQuadrantCard()
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    SubjectWiseRankQuadrantCard()
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    WeeklyProgressQuadrantCard()
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            YourPerformanceQuadrantCard(
                                accuracy = userAccuracyDisplay,
                                points = userPointsDisplay,
                                questions = userQuestionsDisplay,
                                streak = userStreakDisplay
                            )
                            BadgesAndAchievementsQuadrantCard()
                            SubjectWiseRankQuadrantCard()
                            WeeklyProgressQuadrantCard()
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 1: Top 3 Learners Podium View
// ----------------------------------------------------------------------------
@Composable
private fun TopThreePodiumView(
    top1: LeaderboardCandidate,
    top2: LeaderboardCandidate,
    top3: LeaderboardCandidate
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, PaletteCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Rank 2 (Silver/Purple - Left)
                PodiumColumnItem(
                    candidate = top2,
                    rank = 2,
                    podiumHeight = 110.dp,
                    accentColor = PaletteSilverAccent,
                    podiumColor = Color(0xFFE1BEE7),
                    crownColor = Color(0xFFBA68C8)
                )

                // Rank 1 (Gold - Center)
                PodiumColumnItem(
                    candidate = top1,
                    rank = 1,
                    podiumHeight = 145.dp,
                    accentColor = PaletteGoldAccent,
                    podiumColor = Color(0xFFFFECB3),
                    crownColor = Color(0xFFFFD54F)
                )

                // Rank 3 (Bronze/Pink - Right)
                PodiumColumnItem(
                    candidate = top3,
                    rank = 3,
                    podiumHeight = 90.dp,
                    accentColor = PaletteBronzeAccent,
                    podiumColor = Color(0xFFFFCDD2),
                    crownColor = Color(0xFFE57373)
                )
            }
        }
    }
}

@Composable
private fun PodiumColumnItem(
    candidate: LeaderboardCandidate,
    rank: Int,
    podiumHeight: androidx.compose.ui.unit.Dp,
    accentColor: Color,
    podiumColor: Color,
    crownColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Crown Icon
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = crownColor,
            modifier = Modifier.size(if (rank == 1) 28.dp else 22.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Avatar with Badge
        Box(contentAlignment = Alignment.BottomCenter) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFF3E5F5),
                border = BorderStroke(2.dp, crownColor),
                modifier = Modifier.size(if (rank == 1) 56.dp else 48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = candidate.displayName.take(2).uppercase().ifBlank { "AP" },
                        fontSize = if (rank == 1) 18.sp else 15.sp,
                        fontWeight = FontWeight.Black,
                        color = PaletteTitleMagenta
                    )
                }
            }

            // Medal / Rank Number pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = crownColor,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .offset(y = 6.dp)
                    .height(18.dp)
            ) {
                Text(
                    text = "#$rank",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Name
        Text(
            text = candidate.displayName.ifBlank { "Aspirant" },
            fontSize = if (rank == 1) 13.5.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E181C),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Points
        Text(
            text = "${candidate.xp} pts",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = PaletteSubtitleText
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Podium Block
        Surface(
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = podiumColor,
            border = BorderStroke(1.dp, crownColor.copy(alpha = 0.5f)),
            modifier = Modifier
                .width(if (rank == 1) 85.dp else 75.dp)
                .height(podiumHeight)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$rank",
                    fontSize = if (rank == 1) 36.sp else 28.sp,
                    fontWeight = FontWeight.Black,
                    color = crownColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 2: Top Learners Table Card (Ranks 4-10)
// ----------------------------------------------------------------------------
@Composable
private fun TopLearnersTableCard(
    candidates: List<LeaderboardCandidate>,
    selectedPeriod: String,
    onPeriodClick: () -> Unit,
    showPeriodMenu: Boolean,
    onDismissMenu: () -> Unit,
    onSelectPeriod: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PaletteCardBg),
        border = BorderStroke(1.dp, PaletteCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title & Subtitle + Period Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Top Learners",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PaletteTitleMagenta
                    )
                    Text(
                        text = "Real-time state candidate standings",
                        fontSize = 11.sp,
                        color = PaletteSubtitleText
                    )
                }

                Box {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF0F5),
                        border = BorderStroke(1.dp, PaletteCardBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onPeriodClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedPeriod,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PaletteTitleMagenta
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = PaletteTitleMagenta,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showPeriodMenu,
                        onDismissRequest = onDismissMenu
                    ) {
                        listOf("This Month", "This Week", "Overall").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontWeight = FontWeight.Bold) },
                                onClick = { onSelectPeriod(option) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Candidates rows (4 to 10)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                candidates.forEach { candidate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF8F9), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Rank Number + Avatar + Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "#${candidate.rank}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PaletteSubtitleText,
                                modifier = Modifier.width(32.dp)
                            )

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF3E5F5),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = candidate.displayName.take(2).uppercase().ifBlank { "AP" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PaletteTitleMagenta
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = candidate.displayName.ifBlank { "Aspirant" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E181C),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${candidate.streakDays}d Streak • ${candidate.accuracyPercentage.toInt()}% Acc",
                                    fontSize = 10.5.sp,
                                    color = PaletteSubtitleText
                                )
                            }
                        }

                        // Right: Points + Chevron
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${candidate.xp} pts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = PaletteTitleMagenta
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = PaletteSubtitleText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 3: Your Rank Highlight Banner
// ----------------------------------------------------------------------------
@Composable
private fun YourRankBannerCard(
    rank: Int,
    points: Int,
    accuracy: Int,
    questions: Int,
    streak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PaletteYourRankBg),
        border = BorderStroke(1.5.dp, PaletteYourRankBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = PaletteActiveTabBg,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Your Current Rank",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PaletteSubtitleText
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#$rank",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = PaletteTitleMagenta
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF0F5)
                            ) {
                                Text(
                                    text = "Top 15% State Merit",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PaletteTitleMagenta,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PaletteActiveTabBg
                ) {
                    Text(
                        text = "$points pts",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = PaletteCardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // 3 mini stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$accuracy%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = PaletteTitleMagenta)
                    Text(text = "Accuracy", fontSize = 10.5.sp, color = PaletteSubtitleText)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$questions", fontSize = 16.sp, fontWeight = FontWeight.Black, color = PaletteTitleMagenta)
                    Text(text = "Questions", fontSize = 10.5.sp, color = PaletteSubtitleText)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔥 ${streak}d", fontSize = 16.sp, fontWeight = FontWeight.Black, color = PaletteTitleMagenta)
                    Text(text = "Active Streak", fontSize = 10.5.sp, color = PaletteSubtitleText)
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 4: Quadrant 1 - Your Performance
// ----------------------------------------------------------------------------
@Composable
private fun YourPerformanceQuadrantCard(
    accuracy: Int,
    points: Int,
    questions: Int,
    streak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PaletteCardBg),
        border = BorderStroke(1.dp, PaletteCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = PaletteTitleMagenta, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Your Performance", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PaletteTitleMagenta)
                }
                Text("Diagnostics", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PaletteTitleMagenta)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Donut Chart on Left
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        drawArc(color = Color(0xFFFFEBEE), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round))
                        drawArc(color = PaletteActiveTabBg, startAngle = -90f, sweepAngle = 360f * (accuracy / 100f), useCenter = false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$accuracy%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PaletteTitleMagenta)
                        Text("Accuracy", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = PaletteSubtitleText)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Stats List on Right
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("$points", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E181C))
                            Text("Total Points", fontSize = 10.sp, color = PaletteSubtitleText)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🟢", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("$questions", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E181C))
                            Text("Questions Attempted", fontSize = 10.sp, color = PaletteSubtitleText)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("$streak Days", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E181C))
                            Text("Current Streak", fontSize = 10.sp, color = PaletteSubtitleText)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 5: Quadrant 2 - Badges & Achievements
// ----------------------------------------------------------------------------
@Composable
private fun BadgesAndAchievementsQuadrantCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PaletteCardBg),
        border = BorderStroke(1.dp, PaletteCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = PaletteTitleMagenta, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Badges & Achievements", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PaletteTitleMagenta)
                }
                Text("Honors", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PaletteTitleMagenta)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BadgeItem(emoji = "👑", title = "Top 100", bg = Color(0xFFFFF8E1), border = Color(0xFFFFD54F))
                BadgeItem(emoji = "🎓", title = "Master", bg = Color(0xFFF3E5F5), border = Color(0xFFCE93D8))
                BadgeItem(emoji = "🎯", title = "Accuracy", bg = Color(0xFFE3F2FD), border = Color(0xFF90CAF9))
                BadgeItem(emoji = "🔥", title = "Streak", bg = Color(0xFFFBE9E7), border = Color(0xFFFFAB91))
            }
        }
    }
}

@Composable
private fun BadgeItem(emoji: String, title: String, bg: Color, border: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bg,
            border = BorderStroke(1.5.dp, border),
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 24.sp)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PaletteSubtitleText)
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 6: Quadrant 3 - Subject-wise Rank
// ----------------------------------------------------------------------------
@Composable
private fun SubjectWiseRankQuadrantCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PaletteCardBg),
        border = BorderStroke(1.dp, PaletteCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = PaletteTitleMagenta, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subject-wise Rank", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PaletteTitleMagenta)
                }
                Text("State Merit", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PaletteTitleMagenta)
            }

            Spacer(modifier = Modifier.height(12.dp))

            val subjectsList = listOf(
                Triple("🏛️ Indian Polity", "#8", "2,450 pts"),
                Triple("💹 Economy", "#12", "2,180 pts"),
                Triple("🌐 Geography", "#15", "1,760 pts"),
                Triple("📜 Modern History", "#18", "1,420 pts")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                subjectsList.forEach { (subject, rankStr, ptsStr) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF8F9), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(subject, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E181C))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFE0B2)) {
                                Text(rankStr, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🏆 $ptsStr", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PaletteTitleMagenta)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PaletteSubtitleText, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 7: Quadrant 4 - Weekly Progress Chart
// ----------------------------------------------------------------------------
@Composable
private fun WeeklyProgressQuadrantCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PaletteCardBg),
        border = BorderStroke(1.dp, PaletteCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = PaletteTitleMagenta, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Weekly Progress", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PaletteTitleMagenta)
                }
                Text("7-Day Trend", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PaletteTitleMagenta)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val weekData = listOf(
                "Mon" to 120, "Tue" to 150, "Wed" to 180,
                "Thu" to 160, "Fri" to 200, "Sat" to 220, "Sun" to 190
            )
            val maxVal = 250f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weekData.forEach { (day, valNum) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("$valNum", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PaletteTitleMagenta)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((80 * (valNum / maxVal)).dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(PaletteActiveTabBg, Color(0xFFFFB74D))
                                    ),
                                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(day, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = PaletteSubtitleText)
                    }
                }
            }
        }
    }
}
