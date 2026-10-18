package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.PaymentStatus
import com.example.model.UserAccountStatus
import com.example.ui.components.AppSpecificContextBar

/**
 * Section 8: APP-SPECIFIC DASHBOARD
 * Displays metrics strictly isolated for a single selected competitive exam app.
 * Nothing from another app is mixed into this dashboard.
 */
@Composable
fun AppSpecificDashboardScreen(
    initialAppId: String = SampleData.currentActiveAppId.value,
    onNavigateToAppRevenue: (String) -> Unit = {},
    onNavigateToUsers: (String) -> Unit = {},
    onNavigateToSyllabus: (String) -> Unit = {}
) {
    var selectedAppId by remember { mutableStateOf(initialAppId) }
    val appsList = SampleData.appsListState
    val currentApp = appsList.find { it.id == selectedAppId } ?: appsList.first()

    // Isolated Data Queries: STRICTLY filtered by selectedAppId
    val appUsers = SampleData.adminUsersState.filter { it.appId == selectedAppId }
    val appTxns = SampleData.transactionsState.filter { it.appId == selectedAppId && it.status == PaymentStatus.SUCCESS }
    val appReports = SampleData.questionReportsState.filter { it.appId == selectedAppId }
    val appSubjects = SampleData.getSubjectsForApp(selectedAppId)
    val appCurrentAffairs = SampleData.currentAffairsState.filter { it.mappedAppIds.contains(selectedAppId) || it.mappedAppIds.contains("ALL") }

    val now = System.currentTimeMillis()
    val sevenDays = 7L * 86400000L

    val totalUsers = appUsers.size
    val activeUsers = appUsers.count { it.status == UserAccountStatus.ACTIVE }
    val newUsers = appUsers.count { now - it.registeredAt <= sevenDays }
    val activeSubscribers = appUsers.count { it.isPremium }
    val appRevenue = appTxns.sumOf { it.amount }

    // MCQ calculations in app's syllabus
    val totalUnits = appSubjects.sumOf { it.units.size }
    val totalTopics = appSubjects.sumOf { it.units.sumOf { u -> u.topics.size } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Selector Bar (MANDATORY Requirement 11)
        item {
            AppSpecificContextBar(
                selectedAppId = selectedAppId,
                onAppSelected = { newAppId ->
                    selectedAppId = newAppId
                    SampleData.setActiveApp(newAppId)
                }
            )
        }

        // App Hero Analytics Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(currentApp.secondaryColorHex),
                                    Color(currentApp.primaryColorHex)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${currentApp.name} Analytics",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Dedicated app operational overview • Category: ${currentApp.examCategory}",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = currentApp.code,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // High-level App Metrics Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("APP REVENUE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                                    Text("₹${String.format("%,.0f", appRevenue)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("TOTAL USERS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                                    Text("$totalUsers", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("SUBSCRIBERS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                                    Text("$activeSubscribers", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Users & Engagement
        item {
            Text(
                text = "Users & Engagement",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Active Users",
                    value = "$activeUsers",
                    subtext = "Currently preparing",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "New Users (7d)",
                    value = "$newUsers",
                    subtext = "Recent signups",
                    icon = Icons.Default.PersonAdd,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Conversion Rate",
                    value = "${if (totalUsers > 0) ((activeSubscribers.toDouble() / totalUsers) * 100).toInt() else 0}%",
                    subtext = "Paid subscribers",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section: Syllabus, MCQs & Questions
        item {
            Text(
                text = "Syllabus Structure & MCQs",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exam Syllabus Content",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        TextButton(
                            onClick = { onNavigateToSyllabus(selectedAppId) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Manage Syllabus", fontSize = 11.5.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SyllabusPill(label = "Subjects", value = "${appSubjects.size}", modifier = Modifier.weight(1f))
                        SyllabusPill(label = "Units", value = "$totalUnits", modifier = Modifier.weight(1f))
                        SyllabusPill(label = "Topics", value = "$totalTopics", modifier = Modifier.weight(1f))
                        SyllabusPill(label = "Current Affairs", value = "${appCurrentAffairs.size}", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Section: Quality & Issues (Question Reports)
        item {
            Text(
                text = "Quality & User Feedback",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Question Reports",
                    value = "${appReports.size}",
                    subtext = "${appReports.count { it.status == "PENDING" }} pending review",
                    icon = Icons.Default.Report,
                    color = if (appReports.isEmpty()) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Mock Test Activity",
                    value = "${appUsers.sumOf { it.totalMockTestsAttempted }}",
                    subtext = "Attempts completed",
                    icon = Icons.Default.Biotech,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Navigation to App-Specific Sections
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigateToUsers(selectedAppId) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View App Users", fontSize = 12.sp)
                }

                Button(
                    onClick = { onNavigateToAppRevenue(selectedAppId) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(currentApp.primaryColorHex))
                ) {
                    Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("App Revenue", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            }
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            Text(text = subtext, fontSize = 9.5.sp, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SyllabusPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.5.sp, color = Color(0xFF64748B))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }
    }
}
