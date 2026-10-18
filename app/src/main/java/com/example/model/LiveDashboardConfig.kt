package com.example.model

import androidx.compose.ui.graphics.Color

data class LiveDashboardMetricCardConfig(
    val title: String,
    val badgeText: String = "",
    val bgGradientStart: String,
    val bgGradientEnd: String,
    val borderColor: String,
    val accentGlowColor: String,
    val textColor: String = "#FFFFFF",
    val badgeBgColor: String = "#FFFFFF",
    val badgeTextColor: String,
    val overrideValue: Int? = null // Optional manual override for count
)

data class LiveDashboardConfig(
    val headerTitlePrefix: String = "LIVE CONTENT",
    val headerTitleSuffix: String = "DASHBOARD",
    val headerSubtitle: String = "Real-time question bank & syllabus metrics",
    val liveIndicatorColor: String = "#00C853", // Vibrant emerald beacon
    val liveBankButtonText: String = "Live Bank",
    val liveBankButtonColor: String = "#00A844",
    val containerBgStart: String = "#FFFDF9",
    val containerBgEnd: String = "#FFFDF9",
    val containerBorderColor: String = "#F1EAD8",

    // 5 Individual Metric Cards with vibrant premium palette
    val subjectsCard: LiveDashboardMetricCardConfig = LiveDashboardMetricCardConfig(
        title = "Subjects",
        badgeText = "",
        bgGradientStart = "#FF526F",
        bgGradientEnd = "#FA2552",
        borderColor = "#FF3358",
        accentGlowColor = "#FF526F",
        textColor = "#FFFFFF",
        badgeBgColor = "#FFFFFF",
        badgeTextColor = "#FA2552"
    ),
    val unitsCard: LiveDashboardMetricCardConfig = LiveDashboardMetricCardConfig(
        title = "Units",
        badgeText = "",
        bgGradientStart = "#00C853",
        bgGradientEnd = "#00A844",
        borderColor = "#00B348",
        accentGlowColor = "#00C853",
        textColor = "#FFFFFF",
        badgeBgColor = "#FFFFFF",
        badgeTextColor = "#00A844"
    ),
    val topicsCard: LiveDashboardMetricCardConfig = LiveDashboardMetricCardConfig(
        title = "Topics",
        badgeText = "",
        bgGradientStart = "#FFB300",
        bgGradientEnd = "#FF8F00",
        borderColor = "#FFA000",
        accentGlowColor = "#FFB300",
        textColor = "#FFFFFF",
        badgeBgColor = "#FFFFFF",
        badgeTextColor = "#FF8F00"
    ),
    val practiceSetsCard: LiveDashboardMetricCardConfig = LiveDashboardMetricCardConfig(
        title = "Practice Sets",
        badgeText = "",
        bgGradientStart = "#8B5CF6",
        bgGradientEnd = "#6D28D9",
        borderColor = "#7C3AED",
        accentGlowColor = "#8B5CF6",
        textColor = "#FFFFFF",
        badgeBgColor = "#FFFFFF",
        badgeTextColor = "#6D28D9"
    ),
    val totalMcqsCard: LiveDashboardMetricCardConfig = LiveDashboardMetricCardConfig(
        title = "Total MCQs",
        badgeText = "",
        bgGradientStart = "#FF2D87",
        bgGradientEnd = "#E91E63",
        borderColor = "#E11D48",
        accentGlowColor = "#FF2D87",
        textColor = "#FFFFFF",
        badgeBgColor = "#FFFFFF",
        badgeTextColor = "#E91E63"
    )
)

fun parseHexColor(hexString: String, defaultColor: Color): Color {
    return try {
        val clean = hexString.trim().removePrefix("#")
        val longVal = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or longVal)
        } else {
            Color(longVal)
        }
    } catch (e: Exception) {
        defaultColor
    }
}
