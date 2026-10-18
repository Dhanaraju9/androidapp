package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExamLeague
import com.example.model.LeaderboardConfigModel
import com.example.repository.LeaderboardRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dedicated Admin Panel Tab for controlling the Competitive Leaderboard Engine.
 * Provides live telemetry, anti-cheating audit inspection, configurable XP rules,
 * fair scoring weights, and period lifecycle management.
 */
@Composable
fun AdminLeaderboardTab(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { LeaderboardRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    val config by repository.configState.collectAsState()
    val allCandidates by repository.allCandidates.collectAsState()
    val monitoringStats = remember(allCandidates) { repository.getMonitoringStats() }

    var editableConfig by remember(config) { mutableStateOf(config) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_leaderboard_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Leaderboard & Fair Scoring Engine",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Manage server-verified XP calculations, anti-cheat limits, and periods.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // Success banner
        if (saveSuccessMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                        Text(saveSuccessMessage!!, color = Color(0xFF34D399), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Telemetry Section
        item {
            Text(
                text = "LIVE ASPIRANT MONITORING TELEMETRY",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF60A5FA),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                AdminStatCard(title = "Total Candidates", value = "${monitoringStats.totalParticipants}", color = Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                AdminStatCard(title = "Active Participants", value = "${monitoringStats.activeParticipants}", color = Color(0xFF34D399), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                AdminStatCard(title = "Avg Accuracy", value = "${monitoringStats.averageAccuracy.toInt()}%", color = Color(0xFFFFD700), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                AdminStatCard(title = "Top Fair Score", value = "${monitoringStats.topScore.toInt()}", color = Color(0xFFFB923C), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                AdminStatCard(title = "Total XP Distributed", value = "${monitoringStats.totalXpDistributed}", color = Color(0xFFA78BFA), modifier = Modifier.weight(1f))
            }
        }

        // League Distribution breakdown
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LEAGUE DISTRIBUTION BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFCBD5E1),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ExamLeague.values().forEach { league ->
                        val count = allCandidates.count { it.league == league }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(league.iconEmoji)
                                Text(league.title, color = Color(league.colorHex), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("$count Candidates", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Centralized XP Rules Configuration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CENTRALIZED XP REWARD CONFIGURATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFBBF24),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AdminSlider(
                        label = "XP per Correct Answer: +${editableConfig.xpPerCorrectAnswer} XP",
                        value = editableConfig.xpPerCorrectAnswer.toFloat(),
                        range = 5f..30f,
                        onValueChange = { editableConfig = editableConfig.copy(xpPerCorrectAnswer = it.toInt()) }
                    )

                    AdminSlider(
                        label = "XP per Mock Test Completion: +${editableConfig.xpPerMockTestCompletion} XP",
                        value = editableConfig.xpPerMockTestCompletion.toFloat(),
                        range = 20f..150f,
                        onValueChange = { editableConfig = editableConfig.copy(xpPerMockTestCompletion = it.toInt()) }
                    )

                    AdminSlider(
                        label = "XP per Daily Practice Streak: +${editableConfig.xpPerDailyStreak} XP",
                        value = editableConfig.xpPerDailyStreak.toFloat(),
                        range = 10f..50f,
                        onValueChange = { editableConfig = editableConfig.copy(xpPerDailyStreak = it.toInt()) }
                    )

                    AdminSlider(
                        label = "XP per Topic Completion: +${editableConfig.xpPerTopicCompletion} XP",
                        value = editableConfig.xpPerTopicCompletion.toFloat(),
                        range = 10f..60f,
                        onValueChange = { editableConfig = editableConfig.copy(xpPerTopicCompletion = it.toInt()) }
                    )
                }
            }
        }

        // Fair Scoring Weights
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FAIR SCORING ALGORITHM WEIGHTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF34D399),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Prevents spamming easy questions. Blends accuracy, mock tests, and streak consistency.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    AdminSlider(
                        label = "Accuracy Weight Factor: ${(editableConfig.accuracyWeight * 100).toInt()}%",
                        value = (editableConfig.accuracyWeight * 100).toFloat(),
                        range = 10f..60f,
                        onValueChange = { editableConfig = editableConfig.copy(accuracyWeight = it.toDouble() / 100.0) }
                    )

                    AdminSlider(
                        label = "Mock Test Performance Weight: ${(editableConfig.mockTestWeight * 100).toInt()}%",
                        value = (editableConfig.mockTestWeight * 100).toFloat(),
                        range = 10f..60f,
                        onValueChange = { editableConfig = editableConfig.copy(mockTestWeight = it.toDouble() / 100.0) }
                    )

                    AdminSlider(
                        label = "Study Consistency Weight: ${(editableConfig.consistencyWeight * 100).toInt()}%",
                        value = (editableConfig.consistencyWeight * 100).toFloat(),
                        range = 10f..60f,
                        onValueChange = { editableConfig = editableConfig.copy(consistencyWeight = it.toDouble() / 100.0) }
                    )
                }
            }
        }

        // League XP Thresholds
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LEAGUE PROMOTION XP THRESHOLDS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AdminSlider(
                        label = "🥈 Silver League Min XP: ${editableConfig.silverThresholdXp} XP",
                        value = editableConfig.silverThresholdXp.toFloat(),
                        range = 500f..2000f,
                        onValueChange = { editableConfig = editableConfig.copy(silverThresholdXp = it.toInt()) }
                    )

                    AdminSlider(
                        label = "🥇 Gold League Min XP: ${editableConfig.goldThresholdXp} XP",
                        value = editableConfig.goldThresholdXp.toFloat(),
                        range = 2000f..5000f,
                        onValueChange = { editableConfig = editableConfig.copy(goldThresholdXp = it.toInt()) }
                    )

                    AdminSlider(
                        label = "🏆 Platinum League Min XP: ${editableConfig.platinumThresholdXp} XP",
                        value = editableConfig.platinumThresholdXp.toFloat(),
                        range = 5000f..9000f,
                        onValueChange = { editableConfig = editableConfig.copy(platinumThresholdXp = it.toInt()) }
                    )

                    AdminSlider(
                        label = "💎 Diamond League Min XP: ${editableConfig.diamondThresholdXp} XP",
                        value = editableConfig.diamondThresholdXp.toFloat(),
                        range = 9000f..15000f,
                        onValueChange = { editableConfig = editableConfig.copy(diamondThresholdXp = it.toInt()) }
                    )
                }
            }
        }

        // Action Buttons: Save Config & Period Reset
        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        repository.updateConfig(editableConfig)
                        saveSuccessMessage = "Leaderboard engine configuration saved and synced to Cloud Firestore!"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_save_leaderboard_admin_config"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Engine Configuration", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!showResetConfirm) {
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth().testTag("btn_admin_reset_weekly"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger Weekly Period Reset & Standings Archival")
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF451A1A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "⚠️ Are you sure you want to trigger the Weekly Reset? All current candidate weekly XP points will be archived to history and reset to 0.",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.resetWeeklyPeriod()
                                        showResetConfirm = false
                                        saveSuccessMessage = "Weekly period reset complete. Standings archived into history."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                            ) {
                                Text("Confirm Reset & Archive")
                            }
                            TextButton(onClick = { showResetConfirm = false }) {
                                Text("Cancel", color = Color(0xFF94A3B8))
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

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = title, fontSize = 9.5.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun AdminSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.5.sp, color = Color(0xFFCBD5E1))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF38BDF8),
                activeTrackColor = Color(0xFF0284C7),
                inactiveTrackColor = Color(0xFF0F172A)
            )
        )
    }
}
