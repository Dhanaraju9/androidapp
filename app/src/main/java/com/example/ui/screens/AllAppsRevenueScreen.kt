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
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.TransactionModel
import com.example.ui.components.AppBadgeChip
import java.text.SimpleDateFormat
import java.util.*

/**
 * Section 7: ALL APPS REVENUE
 * Aggregates financial analytics across ALL apps:
 * - Total Revenue — All Apps
 * - Today's Revenue, Monthly Revenue, Yearly Revenue
 * - Subscription Revenue
 * - Successful Payments, Failed Payments, Refunds
 * - Revenue by App breakdown (visual progress bars + table)
 * - Subscription plan performance
 * - Multi-app transactions table with search & filter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllAppsRevenueScreen(
    onNavigateToAppRevenue: (String) -> Unit = {}
) {
    val appsList = SampleData.appsListState
    val allTxns = SampleData.transactionsState
    val allRefunds = SampleData.refundsState

    val now = System.currentTimeMillis()
    val oneDay = 86400000L
    val oneMonth = 30L * oneDay
    val oneYear = 365L * oneDay

    val successfulTxns = allTxns.filter { it.status == PaymentStatus.SUCCESS }
    val failedTxns = allTxns.filter { it.status == PaymentStatus.FAILED }
    val refundedTxns = allTxns.filter { it.status == PaymentStatus.REFUNDED }

    val totalRevenue = successfulTxns.sumOf { it.amount }
    val todayRevenue = successfulTxns.filter { now - it.timestamp <= oneDay }.sumOf { it.amount }
    val monthlyRevenue = successfulTxns.filter { now - it.timestamp <= oneMonth }.sumOf { it.amount }
    val yearlyRevenue = successfulTxns.filter { now - it.timestamp <= oneYear }.sumOf { it.amount }

    val totalRefundAmount = allRefunds.sumOf { it.amount }

    var selectedAppFilter by remember { mutableStateOf("ALL") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTransactions = allTxns.filter { txn ->
        val matchesApp = selectedAppFilter == "ALL" || txn.appId == selectedAppFilter
        val matchesStatus = when (selectedStatusFilter) {
            "SUCCESS" -> txn.status == PaymentStatus.SUCCESS
            "FAILED" -> txn.status == PaymentStatus.FAILED
            "REFUNDED" -> txn.status == PaymentStatus.REFUNDED
            else -> true
        }
        val matchesSearch = txn.userName.contains(searchQuery, ignoreCase = true) ||
                txn.id.contains(searchQuery, ignoreCase = true) ||
                txn.planName.contains(searchQuery, ignoreCase = true)
        matchesApp && matchesStatus && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Revenue Hero Card
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
                                listOf(Color(0xFF047857), Color(0xFF0D9488), Color(0xFF0284C7))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                        imageVector = Icons.Default.CurrencyRupee,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ALL APPS REVENUE",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Consolidated financial performance across all ${appsList.size} apps",
                                        fontSize = 12.sp,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "LIVE AUDIT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Total Revenue Prominent Display
                        Column {
                            Text(
                                text = "TOTAL REVENUE — ALL APPS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA7F3D0)
                            )
                            Text(
                                text = "₹${String.format("%,.2f", totalRevenue)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                        // Time Horizon Breakdowns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimeStatItem(
                                label = "TODAY",
                                amount = "₹${String.format("%,.0f", todayRevenue)}",
                                modifier = Modifier.weight(1f)
                            )
                            TimeStatItem(
                                label = "THIS MONTH",
                                amount = "₹${String.format("%,.0f", monthlyRevenue)}",
                                modifier = Modifier.weight(1f)
                            )
                            TimeStatItem(
                                label = "THIS YEAR",
                                amount = "₹${String.format("%,.0f", yearlyRevenue)}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Payment Outcomes Grid (Success, Failed, Refunds)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PaymentOutcomeCard(
                    title = "Successful Payments",
                    count = "${successfulTxns.size} txns",
                    amount = "₹${String.format("%,.0f", totalRevenue)}",
                    color = Color(0xFF10B981),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                PaymentOutcomeCard(
                    title = "Failed Payments",
                    count = "${failedTxns.size} txns",
                    amount = "₹${String.format("%,.0f", failedTxns.sumOf { it.amount })}",
                    color = Color(0xFFEF4444),
                    icon = Icons.Default.Cancel,
                    modifier = Modifier.weight(1f)
                )
                PaymentOutcomeCard(
                    title = "Refunds Issued",
                    count = "${refundedTxns.size} refunds",
                    amount = "₹${String.format("%,.0f", totalRefundAmount)}",
                    color = Color(0xFFF59E0B),
                    icon = Icons.Default.Replay,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Revenue by App Breakdown (Section 7 Requirement)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Revenue by App Distribution",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Individual contribution of each competitive portal",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Text(
                            text = "${appsList.size} Apps Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // App-wise breakdown rows with progress bars
                    appsList.forEach { app ->
                        val appTxns = allTxns.filter { it.appId == app.id && it.status == PaymentStatus.SUCCESS }
                        val appRev = appTxns.sumOf { it.amount }
                        val percentage = if (totalRevenue > 0) (appRev / totalRevenue).toFloat() else 0f

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToAppRevenue(app.id) }
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(app.primaryColorHex))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = app.name,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${appTxns.size} orders)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${String.format("%,.0f", appRev)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${(percentage * 100).toInt()}%)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { percentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(app.primaryColorHex),
                                trackColor = Color(0xFFF1F5F9)
                            )
                        }
                    }
                }
            }
        }

        // Consolidated Transactions Explorer
        item {
            Text(
                text = "Consolidated Transactions Log",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        // Filter Controls
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
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by user, transaction ID or plan...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // App Filter Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedAppFilter == "ALL",
                            onClick = { selectedAppFilter = "ALL" },
                            label = { Text("All Apps (${allTxns.size})", fontSize = 11.sp) }
                        )
                        appsList.forEach { app ->
                            val count = allTxns.count { it.appId == app.id }
                            FilterChip(
                                selected = selectedAppFilter == app.id,
                                onClick = { selectedAppFilter = app.id },
                                label = { Text("${app.code} ($count)", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Transactions list items
        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions match the selected criteria.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }
        } else {
            items(filteredTransactions) { txn ->
                val statusColor = when (txn.status) {
                    PaymentStatus.SUCCESS -> Color(0xFF10B981)
                    PaymentStatus.FAILED -> Color(0xFFEF4444)
                    PaymentStatus.REFUNDED -> Color(0xFFF59E0B)
                    else -> Color(0xFF64748B)
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(statusColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (txn.paymentMethod) {
                                        PaymentMethod.UPI -> Icons.Default.QrCode
                                        PaymentMethod.CREDIT_CARD, PaymentMethod.DEBIT_CARD -> Icons.Default.CreditCard
                                        else -> Icons.Default.AccountBalance
                                    },
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = txn.userName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AppBadgeChip(appId = txn.appId)
                                }
                                Text(
                                    text = "${txn.planName} • ${txn.id}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(txn.timestamp)),
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${String.format("%,.0f", txn.amount)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = statusColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = txn.status.name,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeStatItem(
    label: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA7F3D0))
            Text(text = amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun PaymentOutcomeCard(
    title: String,
    count: String,
    amount: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            }
            Text(text = amount, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            Text(text = count, fontSize = 9.5.sp, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}
