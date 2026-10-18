package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppLuminousBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceBoardScreen(
    results: List<com.example.model.TestResult> = emptyList(),
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToPractice: ((String) -> Unit)? = null
) {
    BackHandler { onBack() }
    
    AppLuminousBackground(screenName = "My Progress") {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("My Progress & Analytics", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Your preparation at a glance", fontSize = 12.sp, color = Color.Gray)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Settings */ }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Overall Performance Hero Card
                item {
                    OverallPerformanceHeroCard()
                }
                
                // Study Streak
                item {
                    StudyStreakCard()
                }

                // Performance Trend
                item {
                    PerformanceTrendCard()
                }

                // Badges & Achievements
                item {
                    BadgesAchievementsCard()
                }
            }
        }
    }
}

@Composable
fun BadgesAchievementsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Badges & Achievements", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Achievement Placeholder
                BadgeItem(title = "7-Day Streak", icon = "🔥")
                BadgeItem(title = "Mastery > 90%", icon = "🏆")
                BadgeItem(title = "Mock Test Ace", icon = "⭐")
            }
        }
    }
}

@Composable
fun BadgeItem(title: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Box(
            modifier = Modifier.size(60.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun OverallPerformanceHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Overall Score", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.size(120.dp).background(Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("85%", style = MaterialTheme.typography.headlineLarge, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Performance Status: Excellent", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun StudyStreakCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Study Streak", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("🔥 12 Days", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun PerformanceTrendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Performance Trend", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.height(200.dp).fillMaxWidth().background(Color.LightGray)) {
                Text("Chart Placeholder", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
