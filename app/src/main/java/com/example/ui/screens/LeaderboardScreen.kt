package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.repository.LeaderboardRepository
import com.example.ui.components.*
import kotlinx.coroutines.launch

/**
 * Premium Competitive Leaderboard Screen for Exam Preparation App.
 * Built with Midnight Navy, Emerald, and Metallic Gold design aesthetics.
 * Features Overall, Weekly, Monthly, Exam-wise, Subject-wise rankings,
 * League/Tier progress, Top 3 Podium, Nearby Ranks, and Anti-Cheat XP.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val repository = remember { LeaderboardRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    // Repository states
    val config by repository.configState.collectAsState()
    val allCandidates by repository.allCandidates.collectAsState()
    val currentUserEntry by repository.currentUserEntry.collectAsState()
    val isLoading by repository.isLoading.collectAsState()

    // UI States
    var selectedTab by remember { mutableStateOf(LeaderboardTab.OVERALL) }
    var selectedDistrict by remember { mutableStateOf("All") }
    var showDistrictDropdown by remember { mutableStateOf(false) }
    var selectedSubjectKey by remember { mutableStateOf<String?>("indian_history") }
    var isFullLeaderboardExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog States
    var selectedCandidateForProfile by remember { mutableStateOf<LeaderboardCandidate?>(null) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }

    val districts = listOf(
        "All", "Visakhapatnam", "Vijayawada", "Guntur", "Tirupati",
        "Kurnool", "Nellore", "Rajahmundry", "Kakinada", "Anantapur", "Kadapa"
    )

    val subjects = listOf(
        "indian_history" to "Indian History",
        "indian_polity" to "Indian Polity",
        "indian_economy" to "Economy & Planning",
        "geography_ap" to "Geography of AP & India"
    )

    // Ranked candidates based on active tab and filters
    val rankedCandidates = remember(allCandidates, selectedTab, selectedDistrict, selectedSubjectKey) {
        repository.getRankedCandidates(
            tab = selectedTab,
            selectedDistrict = selectedDistrict,
            activeAppId = "appsc-group2",
            subjectFilter = if (selectedTab == LeaderboardTab.SUBJECT) selectedSubjectKey else null
        )
    }

    // Top 3 Podium Candidates
    val topThree = remember(rankedCandidates) {
        rankedCandidates.take(3)
    }

    // Nearby Candidates (3-5 above, user, 3-5 below)
    val nearbyCandidates = remember(allCandidates, selectedTab, selectedDistrict) {
        repository.getNearbyCandidates(
            tab = selectedTab,
            selectedDistrict = selectedDistrict,
            activeAppId = "appsc-group2"
        )
    }

    // Current User Standing
    val userStanding = remember(rankedCandidates, currentUserEntry) {
        rankedCandidates.find { it.isCurrentUser } ?: currentUserEntry ?: rankedCandidates.firstOrNull()
    }

    // League progress calculation
    val userLeague = userStanding?.league ?: ExamLeague.BRONZE
    val userXp = userStanding?.xp ?: 0
    val leagueProgress = remember(userXp, userLeague, config) {
        repository.getLeagueProgress(userXp, userLeague, config)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🏆 Leaderboard",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF34D399),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "APPSC Group-2 • Competitive Aspirant Rankings",
                            fontSize = 11.5.sp,
                            color = Color(0xFFD4AF37)
                        )
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        BackAndHomeTopActions(
                            onBack = onBack,
                            onNavigateToHome = onNavigateToHome,
                            size = 36,
                            iconSize = 18
                        )
                    }
                },
                actions = {
                    // Privacy Mode Button
                    IconButton(
                        onClick = { showPrivacyDialog = true },
                        modifier = Modifier.testTag("btn_leaderboard_privacy")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Privacy Settings",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Historical Standings Button
                    IconButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier.testTag("btn_leaderboard_history")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Leaderboard History",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Admin Engine Controls
                    IconButton(
                        onClick = { showAdminDialog = true },
                        modifier = Modifier.testTag("btn_admin_leaderboard_controls")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Engine Controls",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0F1D),
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Sticky User Rank Bar at bottom for rapid accessibility
            if (userStanding != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sticky_user_standing_bar"),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f)),
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .border(1.5.dp, Color(0xFF10B981), CircleShape)
                            ) {
                                Text(
                                    text = "#${userStanding.rank}",
                                    color = Color(0xFF34D399),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Your Current Rank",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Medium
                                    )
                                    RankMovementPill(rankDelta = userStanding.rankDelta)
                                }
                                Text(
                                    text = "${userStanding.displayName} (${userStanding.district})",
                                    fontSize = 13.5.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${userStanding.xp} XP",
                                    fontSize = 13.5.sp,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "${userStanding.accuracyPercentage.toInt()}% Acc",
                                    fontSize = 11.sp,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0A0F1D),
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Sub-Tabs Selector: Overall | Weekly | Monthly | Exam | Subject
            item {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color(0xFF131D31),
                    contentColor = Color.White,
                    indicator = {},
                    divider = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .testTag("leaderboard_sub_tabs")
                ) {
                    LeaderboardTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF1E293B) else Color.Transparent)
                                .clickable { selectedTab = tab }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.title,
                                color = if (isSelected) Color(0xFFFFD700) else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 2. Filters Row (District Dropdown & Subject Selector)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // District Filter Dropdown Pill
                        Box {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF131D31),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier
                                    .clickable { showDistrictDropdown = true }
                                    .testTag("btn_district_filter")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (selectedDistrict == "All") "District: All AP" else selectedDistrict,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showDistrictDropdown,
                                onDismissRequest = { showDistrictDropdown = false },
                                modifier = Modifier.background(Color(0xFF0F172A))
                            ) {
                                districts.forEach { district ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (district == "All") "All AP Districts" else district,
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                        },
                                        onClick = {
                                            selectedDistrict = district
                                            showDistrictDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Live Candidates Counter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text(
                                text = "${rankedCandidates.size} Aspirants Competing",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Subject Pills (Displayed if Subject tab is active)
                    if (selectedTab == LeaderboardTab.SUBJECT) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            subjects.forEach { (key, label) ->
                                val isSelected = selectedSubjectKey == key
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFF1E293B) else Color(0xFF131D31),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFFFFD700) else Color(0xFF334155)
                                    ),
                                    modifier = Modifier.clickable { selectedSubjectKey = key }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFFFFD700) else Color(0xFF94A3B8),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Competitive League / Tier Progress Banner
            item {
                LeagueProgressCard(
                    currentLeague = userLeague,
                    currentXp = userXp,
                    leagueRank = userStanding?.rank ?: 14,
                    progress = leagueProgress
                )
            }

            // 4. Top 3 Podium
            item {
                PodiumTopThreeView(
                    topThree = topThree,
                    onCandidateClick = { candidate ->
                        selectedCandidateForProfile = candidate
                    }
                )
            }

            // 5. User's Personal Rank Card (Detailed In-Stream View)
            if (userStanding != null) {
                item {
                    PersonalRankCard(
                        userCandidate = userStanding,
                        onPrivacyClick = { showPrivacyDialog = true }
                    )
                }
            }

            // 6. Nearby Ranks Display (3-5 above, user, 3-5 below)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NEARBY COMPETITION ORBIT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color(0xFFD4AF37)
                            )
                            Text(
                                text = "Aspirants ranked closely around your score",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Toggle button for full leaderboard
                        TextButton(
                            onClick = { isFullLeaderboardExpanded = !isFullLeaderboardExpanded },
                            modifier = Modifier.testTag("btn_toggle_full_leaderboard")
                        ) {
                            Text(
                                text = if (isFullLeaderboardExpanded) "Show Orbit Only" else "View All (${rankedCandidates.size})",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }

                    // Render Nearby Ranks if not expanded to full view
                    if (!isFullLeaderboardExpanded) {
                        nearbyCandidates.forEach { candidate ->
                            CandidateRowItem(
                                candidate = candidate,
                                onClick = { selectedCandidateForProfile = candidate }
                            )
                        }
                    }
                }
            }

            // 7. Full Leaderboard (When expanded) with Search filter
            if (isFullLeaderboardExpanded) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("leaderboard_search_input"),
                        placeholder = { Text("Search candidate or district...", color = Color(0xFF64748B), fontSize = 12.5.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF131D31),
                            unfocusedContainerColor = Color(0xFF131D31),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                val filteredList = rankedCandidates.filter {
                    if (searchQuery.isBlank()) true
                    else it.displayName.contains(searchQuery, ignoreCase = true) || it.district.contains(searchQuery, ignoreCase = true)
                }

                items(filteredList) { candidate ->
                    CandidateRowItem(
                        candidate = candidate,
                        onClick = { selectedCandidateForProfile = candidate }
                    )
                }
            }

            // 8. Weekly Competitive Challenges Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_challenges_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "WEEKLY COMPETITIVE CHALLENGES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ChallengeProgressRow(title = "Complete 5 Mock Tests", progress = 3, target = 5, xpReward = 50)
                        Spacer(modifier = Modifier.height(8.dp))
                        ChallengeProgressRow(title = "Maintain 90% Practice Accuracy", progress = 88, target = 90, xpReward = 40, isPercentage = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        ChallengeProgressRow(title = "Solve 200 Practice MCQs", progress = 145, target = 200, xpReward = 30)
                        Spacer(modifier = Modifier.height(8.dp))
                        ChallengeProgressRow(title = "Complete Monthly Current Affairs Set", progress = 1, target = 1, xpReward = 25)
                    }
                }
            }

            // Bottom Spacing for sticky rank bar
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // -----------------------------------------------------------------------
    // Interactive Dialogs
    // -----------------------------------------------------------------------

    // Public Candidate Profile Dialog
    if (selectedCandidateForProfile != null) {
        CandidatePublicProfileDialog(
            candidate = selectedCandidateForProfile!!,
            onDismissRequest = { selectedCandidateForProfile = null }
        )
    }

    // Privacy Settings Dialog
    if (showPrivacyDialog && userStanding != null) {
        PrivacySettingsDialog(
            currentMode = userStanding.privacyMode,
            onSaveMode = { newMode ->
                coroutineScope.launch {
                    repository.updatePrivacyMode(newMode)
                }
            },
            onDismissRequest = { showPrivacyDialog = false }
        )
    }

    // Historical Performance Dialog
    if (showHistoryDialog) {
        LeaderboardHistoryDialog(
            userStanding = userStanding,
            onDismissRequest = { showHistoryDialog = false }
        )
    }

    // Admin Controls Dialog
    if (showAdminDialog) {
        val monitoringStats = remember(allCandidates) { repository.getMonitoringStats() }
        AdminLeaderboardControlsDialog(
            config = config,
            monitoringStats = monitoringStats,
            onSaveConfig = { newConfig ->
                coroutineScope.launch {
                    repository.updateConfig(newConfig)
                }
            },
            onResetWeekly = {
                coroutineScope.launch {
                    repository.resetWeeklyPeriod()
                }
            },
            onDismissRequest = { showAdminDialog = false }
        )
    }
}

@Composable
private fun ChallengeProgressRow(
    title: String,
    progress: Int,
    target: Int,
    xpReward: Int,
    isPercentage: Boolean = false
) {
    val pct = if (target > 0) (progress.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
    val isCompleted = progress >= target

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 11.5.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isPercentage) "$progress%" else "$progress/$target",
                    fontSize = 11.sp,
                    color = if (isCompleted) Color(0xFF34D399) else Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+$xpReward XP",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isCompleted) Color(0xFF10B981) else Color(0xFF38BDF8),
            trackColor = Color(0xFF1E293B)
        )
    }
}
