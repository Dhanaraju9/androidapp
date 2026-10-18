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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.ui.components.AppSpecificContextBar
import java.text.SimpleDateFormat
import java.util.*

/**
 * Section 6: REVENUE — APP-SPECIFIC
 * Strictly displays financial data for ONE selected app.
 * Never mixes the revenue of different apps inside an individual app's revenue dashboard.
 * Shows:
 * - Today, Yesterday, This Week, This Month, This Year, Total Revenue
 * - Subscription Revenue
 * - Coupon / Discount impact
 * - Successful payments, Failed payments, Refunds
 * - App-specific transactions list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSpecificRevenueScreen(
    initialAppId: String = SampleData.currentActiveAppId.value
) {
    var selectedAppId by remember { mutableStateOf(initialAppId) }
    val appsList = SampleData.appsListState
    val currentApp = appsList.find { it.id == selectedAppId } ?: appsList.first()

    // Strict Data Isolation: ONLY transactions belonging to selectedAppId
    val appTxns = SampleData.transactionsState.filter { it.appId == selectedAppId }
    val appRefunds = SampleData.refundsState.filter { it.appId == selectedAppId }

    val now = System.currentTimeMillis()
    val oneDay = 86400000L
    val oneWeek = 7L * oneDay
    val oneMonth = 30L * oneDay
    val oneYear = 365L * oneDay

    val successfulTxns = appTxns.filter { it.status == PaymentStatus.SUCCESS }
    val failedTxns = appTxns.filter { it.status == PaymentStatus.FAILED }
    val refundedTxns = appTxns.filter { it.status == PaymentStatus.REFUNDED }

    val totalRevenue = successfulTxns.sumOf { it.amount }
    val todayRevenue = successfulTxns.filter { now - it.timestamp <= oneDay }.sumOf { it.amount }
    val yesterdayRevenue = successfulTxns.filter { (now - it.timestamp > oneDay) && (now - it.timestamp <= 2 * oneDay) }.sumOf { it.amount }
    val thisWeekRevenue = successfulTxns.filter { now - it.timestamp <= oneWeek }.sumOf { it.amount }
    val thisMonthRevenue = successfulTxns.filter { now - it.timestamp <= oneMonth }.sumOf { it.amount }
    val thisYearRevenue = successfulTxns.filter { now - it.timestamp <= oneYear }.sumOf { it.amount }

    val totalDiscountGiven = appTxns.sumOf { it.discountAmount }
    val totalRefundedAmount = appRefunds.sumOf { it.amount }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val displayedTxns = when (selectedFilter) {
        "SUCCESS" -> successfulTxns
        "FAILED" -> failedTxns
        "REFUNDED" -> refundedTxns
        else -> appTxns
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top App Selector Context Bar
        item {
            AppSpecificContextBar(
                selectedAppId = selectedAppId,
                onAppSelected = { newAppId ->
                    selectedAppId = newAppId
                    SampleData.setActiveApp(newAppId)
                }
            )
        }

        // App Revenue Hero Card
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
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${currentApp.name} Revenue",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Isolated financial ledger • Currency: ${currentApp.currencyCode}",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ISOLATED LEDGER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Prominent Total App Revenue
                        Column {
                            Text(
                                text = "TOTAL APP REVENUE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE047)
                            )
                            Text(
                                text = "${currentApp.currencySymbol}${String.format("%,.2f", totalRevenue)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                        // Time Horizon Horizontal Scroller/Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TimeBox(label = "TODAY", amount = "₹${String.format("%,.0f", todayRevenue)}", modifier = Modifier.weight(1f))
                            TimeBox(label = "YESTERDAY", amount = "₹${String.format("%,.0f", yesterdayRevenue)}", modifier = Modifier.weight(1f))
                            TimeBox(label = "THIS WEEK", amount = "₹${String.format("%,.0f", thisWeekRevenue)}", modifier = Modifier.weight(1f))
                            TimeBox(label = "THIS MONTH", amount = "₹${String.format("%,.0f", thisMonthRevenue)}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Financial Metrics Grid: Year, Discount, Refunds
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialCard(
                    title = "This Year Revenue",
                    amount = "${currentApp.currencySymbol}${String.format("%,.0f", thisYearRevenue)}",
                    subtext = "Annual run-rate",
                    color = Color(0xFF10B981),
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f)
                )
                FinancialCard(
                    title = "Coupon Impact",
                    amount = "-${currentApp.currencySymbol}${String.format("%,.0f", totalDiscountGiven)}",
                    subtext = "Promotional discounts",
                    color = Color(0xFF3B82F6),
                    icon = Icons.Default.ConfirmationNumber,
                    modifier = Modifier.weight(1f)
                )
                FinancialCard(
                    title = "Refunds",
                    amount = "${currentApp.currencySymbol}${String.format("%,.0f", totalRefundedAmount)}",
                    subtext = "${appRefunds.size} claims approved",
                    color = Color(0xFFEF4444),
                    icon = Icons.Default.Replay,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Payment Status Breakdown
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusCountPill(label = "Successful", count = "${successfulTxns.size}", color = Color(0xFF10B981))
                    StatusCountPill(label = "Failed", count = "${failedTxns.size}", color = Color(0xFFEF4444))
                    StatusCountPill(label = "Refunded", count = "${refundedTxns.size}", color = Color(0xFFF59E0B))
                    StatusCountPill(label = "Total Orders", count = "${appTxns.size}", color = Color(0xFF2563EB))
                }
            }
        }

        // Transactions Header & Filter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentApp.name} Orders (${displayedTxns.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All", fontSize = 10.5.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "SUCCESS",
                        onClick = { selectedFilter = "SUCCESS" },
                        label = { Text("Success", fontSize = 10.5.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "FAILED",
                        onClick = { selectedFilter = "FAILED" },
                        label = { Text("Failed", fontSize = 10.5.sp) }
                    )
                }
            }
        }

        // App-specific transactions list
        if (displayedTxns.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions found for this status filter.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }
        } else {
            items(displayedTxns) { txn ->
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
                                    .size(36.dp)
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
                                Text(
                                    text = txn.userName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${txn.planName} • ID: ${txn.id}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                if (txn.couponCode != null) {
                                    Text(
                                        text = "Coupon: ${txn.couponCode} (-₹${txn.discountAmount.toInt()})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2563EB)
                                    )
                                }
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
private fun TimeBox(
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
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
            Text(text = amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun FinancialCard(
    title: String,
    amount: String,
    subtext: String,
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
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            }
            Text(text = amount, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            Text(text = subtext, fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StatusCountPill(
    label: String,
    count: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
        Text(text = label, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
    }
}
