package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.PaymentStatus
import com.example.model.UserAccountStatus

/**
 * Section 9: ALL APPS DASHBOARD
 * Central multi-app overview aggregating metrics across all competitive applications:
 * - Total Users (All Apps)
 * - Total Active Users
 * - Total Subscribers
 * - Total Revenue
 * - Total MCQs & Questions
 * - Total Question Reports
 * - Detailed App-wise Breakdown Cards & Comparison Table
 */
@Composable
fun AllAppsDashboardScreen(
    onNavigateToAppDashboard: (String) -> Unit = {},
    onNavigateToAppRevenue: (String) -> Unit = {}
) {
    val appsList = SampleData.appsListState
    val allUsers = SampleData.adminUsersState
    val allTxns = SampleData.transactionsState
    val allReports = SampleData.questionReportsState
    val allSubjects = SampleData.subjectsState
    val allQuestions = SampleData.questionsState

    // Aggregations across all apps
    val totalUsersCount = allUsers.size
    val totalActiveUsers = allUsers.count { it.status == UserAccountStatus.ACTIVE }
    val totalSubscribers = allUsers.count { it.isPremium }
    val totalRevenue = allTxns.filter { it.status == PaymentStatus.SUCCESS }.sumOf { it.amount }
    val totalQuestionsCount = allQuestions.size
    val totalReportsCount = allReports.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Hero Card
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
                                listOf(Color(0xFF1E1B4B), Color(0xFF2563EB))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ALL APPS DASHBOARD",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Centralized multi-tenant portal overview (${appsList.size} registered applications)",
                                        fontSize = 12.sp,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Text(
                                    text = "MULTI-APP CLUSTER ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA7F3D0),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // High-level Metrics Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricPill(
                                label = "TOTAL REVENUE",
                                value = "₹${String.format("%,.0f", totalRevenue)}",
                                icon = Icons.Default.CurrencyRupee,
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "TOTAL USERS",
                                value = "$totalUsersCount",
                                icon = Icons.Default.People,
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "SUBSCRIBERS",
                                value = "$totalSubscribers",
                                icon = Icons.Default.WorkspacePremium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Secondary System Statistics Grid
        item {
            Text(
                text = "Overall System Statistics",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Active Users",
                    value = "$totalActiveUsers",
                    subtext = "${((totalActiveUsers.toDouble() / totalUsersCount.coerceAtLeast(1)) * 100).toInt()}% active rate",
                    color = Color(0xFF10B981),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Questions",
                    value = "$totalQuestionsCount",
                    subtext = "Across all syllabus sets",
                    color = Color(0xFF3B82F6),
                    icon = Icons.Default.Quiz,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Question Reports",
                    value = "$totalReportsCount",
                    subtext = "${allReports.count { it.status == "PENDING" }} unresolved",
                    color = Color(0xFFF59E0B),
                    icon = Icons.Default.Flag,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Applications",
                    value = "${appsList.count { it.isActive }} Live",
                    subtext = "${appsList.size} configured",
                    color = Color(0xFF8B5CF6),
                    icon = Icons.Default.Apps,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // App-wise Breakdown Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "App-wise Performance Breakdown",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Isolated metrics calculated strictly per tenant application",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        // App cards list
        items(appsList) { app ->
            val appUsers = allUsers.filter { it.appId == app.id }
            val appActiveUsers = appUsers.count { it.status == UserAccountStatus.ACTIVE }
            val appSubscribers = appUsers.count { it.isPremium }
            val appTxns = allTxns.filter { it.appId == app.id && it.status == PaymentStatus.SUCCESS }
            val appRevenue = appTxns.sumOf { it.amount }
            val appReports = allReports.filter { it.appId == app.id }
            val appSubjects = allSubjects.filter { it.appId == app.id }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // App Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(app.primaryColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = app.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${app.code} • ${app.examCategory}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (app.isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            border = BorderStroke(
                                1.dp,
                                if (app.isActive) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                            )
                        ) {
                            Text(
                                text = if (app.isActive) "LIVE" else "DRAFT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (app.isActive) Color(0xFF16A34A) else Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // App 4-grid stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppMiniStat(
                            label = "Revenue",
                            value = "₹${String.format("%,.0f", appRevenue)}",
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        AppMiniStat(
                            label = "Users",
                            value = "${appUsers.size} ($appActiveUsers act)",
                            color = Color(0xFF2563EB),
                            modifier = Modifier.weight(1f)
                        )
                        AppMiniStat(
                            label = "Subscribers",
                            value = "$appSubscribers",
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        AppMiniStat(
                            label = "Reports",
                            value = "${appReports.size}",
                            color = if (appReports.isEmpty()) Color(0xFF64748B) else Color(0xFFEF4444),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Action buttons to jump to isolated screens
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToAppDashboard(app.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("App Dashboard", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { onNavigateToAppRevenue(app.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(app.primaryColorHex)),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("App Revenue", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFFDE047),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE2E8F0)
                )
            }
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, fontSize = 11.5.sp, color = Color(0xFF64748B))
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
                Text(text = subtext, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AppMiniStat(
    label: String,
    value: String,
    color: Color,
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
            Text(text = label, fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Text(
                text = value,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
