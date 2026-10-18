package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.*
import com.example.util.GamificationManager
import com.example.util.OfflineSyncManager
import com.example.util.PreviousYearPaperStore

fun getContentScaleByName(scaleType: String?): ContentScale {
    return when (scaleType?.lowercase()) {
        "crop" -> ContentScale.Crop
        "fit" -> ContentScale.Fit
        "fillbounds" -> ContentScale.FillBounds
        "fillwidth" -> ContentScale.FillWidth
        "inside" -> ContentScale.Inside
        else -> ContentScale.Crop
    }
}

fun getFontWeightByName(weightName: String?): FontWeight {
    return when (weightName?.lowercase()) {
        "light" -> FontWeight.Light
        "normal" -> FontWeight.Normal
        "medium" -> FontWeight.Medium
        "semibold" -> FontWeight.SemiBold
        "bold" -> FontWeight.Bold
        "extrabold" -> FontWeight.ExtraBold
        "black" -> FontWeight.Black
        else -> FontWeight.Bold
    }
}

fun getFontFamilyByName(fontName: String?): FontFamily {
    return when (fontName?.lowercase()?.trim()) {
        "serif" -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        "sans-serif", "sansserif" -> FontFamily.SansSerif
        else -> FontFamily.Default
    }
}

@Composable
fun LeftNavigationDrawerContent(
    drawerConfig: DrawerConfig = SampleData.drawerConfigState.value,
    userProfile: UserProfileModel = SampleData.userProfileState.value,
    isAdminLoggedIn: Boolean = false,
    isDarkMode: Boolean = false,
    onCloseDrawer: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToMockTestGenerator: () -> Unit,
    onNavigateToAvailableMockTests: () -> Unit,
    onNavigateToCurrentAffairs: () -> Unit,
    onNavigateToPerformance: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToSpeedDuel: () -> Unit = {},
    onNavigateToOfflineManager: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToScientificCalculator: () -> Unit,
    onNavigateToPrivacySection: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    onShareApp: () -> Unit,
    onOpenDailyReminder: () -> Unit = {},
    onNavigateToAiMentor: () -> Unit = {},
    onNavigateToSavedQuestions: () -> Unit = {},
    onNavigateToAppSelection: () -> Unit = {},
    onReportIssue: () -> Unit = {},
    onNavigateToPreviousYearPapers: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeApp = remember { SampleData.getActiveApp() }
    val headerConfig = drawerConfig.header

    val drawerBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(drawerConfig.drawerWidthDp.dp),
        color = drawerBg,
        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // =========================================================================
            // 1. TOP HEADER (EXACT RECREATION WITH SAFE AREA INSETS & ADMIN CUSTOMIZATION)
            // =========================================================================
            val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val effectiveHeaderHeight = (headerConfig.headerHeightDp.dp + statusBarTop + 12.dp).coerceAtLeast(165.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(effectiveHeaderHeight)
            ) {
                // Background image or sky & landscape
                if (headerConfig.showBackgroundImage && headerConfig.backgroundImageUrl.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = headerConfig.backgroundImageUrl,
                        contentDescription = "Header Background",
                        contentScale = getContentScaleByName(headerConfig.imageScaleType),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Sky gradient
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF60A5FA),
                                    Color(0xFF3B82F6),
                                    Color(0xFF1D4ED8),
                                    Color(0xFF1E3A8A)
                                )
                            )
                        )

                        // Sun glow in top right
                        drawCircle(
                            center = Offset(width * 0.8f, height * 0.25f),
                            radius = height * 0.45f,
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFEF08A).copy(alpha = 0.5f),
                                    Color(0xFFFDE047).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )

                        // Soft clouds vector paths
                        val cloudPath = Path().apply {
                            moveTo(width * 0.1f, height * 0.3f)
                            cubicTo(width * 0.15f, height * 0.18f, width * 0.35f, height * 0.2f, width * 0.4f, height * 0.32f)
                            cubicTo(width * 0.45f, height * 0.25f, width * 0.6f, height * 0.28f, width * 0.65f, height * 0.35f)
                            lineTo(width * 0.1f, height * 0.35f)
                            close()
                        }
                        drawPath(cloudPath, Color.White.copy(alpha = 0.25f))

                        // Hill & Statue outline on left center
                        val hillPath = Path().apply {
                            moveTo(0f, height)
                            lineTo(0f, height * 0.65f)
                            quadraticTo(width * 0.2f, height * 0.5f, width * 0.4f, height * 0.72f)
                            quadraticTo(width * 0.7f, height * 0.55f, width, height * 0.68f)
                            lineTo(width, height)
                            close()
                        }
                        drawPath(hillPath, Color(0xFF1E3A8A).copy(alpha = 0.7f))

                        // Statue Silhouette on hill
                        val statuePath = Path().apply {
                            val sx = width * 0.22f
                            val sy = height * 0.52f
                            moveTo(sx - 8f, sy)
                            lineTo(sx - 6f, sy - 18f)
                            cubicTo(sx - 10f, sy - 24f, sx + 10f, sy - 24f, sx + 6f, sy - 18f)
                            lineTo(sx + 8f, sy)
                            close()
                        }
                        drawPath(statuePath, Color(0xFFFEF08A).copy(alpha = 0.85f))

                        // Lake & Bridge on right
                        val bridgePath = Path().apply {
                            moveTo(width * 0.55f, height * 0.78f)
                            quadraticTo(width * 0.75f, height * 0.72f, width, height * 0.75f)
                        }
                        drawPath(
                            path = bridgePath,
                            color = Color.White.copy(alpha = 0.4f),
                            style = Stroke(width = 3f)
                        )
                    }
                }

                // Header Dark/Overlay Tint for contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(headerConfig.imageOverlayColorHex).copy(alpha = headerConfig.overlayOpacity))
                )

                // Close (X) button on top right - placed safely below status bar
                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 10.dp, end = 12.dp)
                        .size(32.dp)
                        .background(Color.White, CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Menu",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Slogan watermark on top right area - placed safely below status bar
                if (headerConfig.sloganTagline.isNotEmpty()) {
                    Text(
                        text = "${headerConfig.sloganTagline} ✨",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = 16.dp, end = 52.dp)
                    )
                }

                // Main Content inside Header - starts below the status bar in safe area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Brand Logo + Titles Row (APPSC GROUP-2 section centered and slightly downward in safe area)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, end = 40.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        // App Logo Badge
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(headerConfig.logoBgColorHex))
                                .border(2.dp, Color(headerConfig.logoBorderColorHex), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconByName(headerConfig.logoIconName, Icons.Default.School),
                                contentDescription = "Logo",
                                tint = Color(headerConfig.logoBorderColorHex),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.70f),
                                border = BorderStroke(1.dp, Color(headerConfig.logoBorderColorHex).copy(alpha = 0.6f)),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.5.dp)
                                ) {
                                    Text(
                                        text = if (headerConfig.title.isNotBlank() && headerConfig.title != "APPSC Group-2" && headerConfig.title != "APPSC GROUP-2") headerConfig.title else activeApp.name,
                                        color = Color(headerConfig.titleTextColorHex),
                                        fontSize = headerConfig.titleFontSizeSp.sp,
                                        fontWeight = getFontWeightByName(headerConfig.titleFontWeight),
                                        fontFamily = getFontFamilyByName(headerConfig.fontFamily),
                                        letterSpacing = 0.5.sp
                                    )
                                    if (headerConfig.titleSparkle.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = headerConfig.titleSparkle,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                            val displayQuote = when {
                                headerConfig.quotation.isNotBlank() -> "“${headerConfig.quotation}”"
                                headerConfig.subtitle.isNotBlank() && !headerConfig.subtitle.equals("APPSC Competitive Exam Hub", ignoreCase = true) && !headerConfig.subtitle.contains("APPSC", ignoreCase = true) -> headerConfig.subtitle
                                activeApp.description.isNotBlank() -> activeApp.description
                                else -> "“Success belongs to those who prepare today.”"
                            }
                            Text(
                                text = displayQuote,
                                color = Color(headerConfig.quotationTextColorHex),
                                fontSize = headerConfig.quotationFontSizeSp.sp,
                                fontWeight = getFontWeightByName(headerConfig.quotationFontWeight),
                                fontFamily = getFontFamilyByName(headerConfig.fontFamily),
                                fontStyle = FontStyle.Italic,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Slogan Banner Row at bottom of header
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = headerConfig.sloganText,
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Colored underlines bar
                            Row(
                                modifier = Modifier.fillMaxWidth(0.92f),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFFF97316))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF22C55E))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF38BDF8))
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 2. SCROLLABLE MENU BODY
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // A. TOP CARDS (My Profile & Settings, Syllabus Home)
                val topCards = drawerConfig.items.filter { it.sectionKey == "MAIN" && it.isVisible }
                    .sortedBy { it.order }

                topCards.forEach { item ->
                    if (item.id == "my_profile") {
                        ExactDrawerCardItem(
                            item = item,
                            isDarkMode = isDarkMode,
                            customTitle = if (userProfile.isLoggedIn) userProfile.name else item.title,
                            customSubtitle = if (userProfile.isLoggedIn) userProfile.email else item.subtitle,
                            onClick = {
                                onCloseDrawer()
                                onNavigateToProfile()
                            }
                        )
                    } else if (item.id == "syllabus_home") {
                        ExactDrawerCardItem(
                            item = item,
                            isDarkMode = isDarkMode,
                            onClick = {
                                onCloseDrawer()
                                onNavigateToHome()
                            }
                        )
                    } else {
                        ExactDrawerCardItem(
                            item = item,
                            isDarkMode = isDarkMode,
                            onClick = onCloseDrawer
                        )
                    }
                }

                // B. EXAM HUB & PRACTICE SECTION
                val examHubHeader = drawerConfig.sections.find { it.sectionKey == "EXAM_HUB" }
                if (examHubHeader != null && examHubHeader.isVisible) {
                    val filteredScript = examHubHeader.scriptText?.takeIf {
                        !it.contains("Practice Makes Progress", ignoreCase = true)
                    }
                    ExactDrawerSectionHeader(
                        section = examHubHeader,
                        title = examHubHeader.title,
                        icon = getIconByName(examHubHeader.iconName, Icons.Default.MenuBook),
                        scriptText = filteredScript,
                        isDarkMode = isDarkMode
                    )
                }

                val examHubItems = drawerConfig.items.filter { 
                    it.sectionKey == "EXAM_HUB" && it.isVisible
                }.sortedBy { it.order }

                val availableCount = SampleData.generatedMockTestsState.size
                val isOfflineMode by OfflineSyncManager.isOfflineModeSimulated
                val dailyRank by GamificationManager.currentUserRankDaily
                val displayRank = if (dailyRank > 0) dailyRank else 28

                examHubItems.forEach { item ->
                    val resolvedItem = when (item.id) {
                        "available_mock_tests" -> item.copy(badgeText = "$availableCount TESTS")
                        "state_leaderboard" -> item.copy(
                            title = "Rank #$displayRank",
                            subtitle = "State Live Leaderboard",
                            badgeText = "LIVE",
                            badgeBgColorHex = 0xFFEAB308,
                            badgeTextColorHex = 0xFF0F172A
                        )
                        "offline_cloud_sync" -> item.copy(
                            title = "Sync Status",
                            subtitle = if (isOfflineMode) "Offline Mode" else "Cloud Backup Synced",
                            badgeText = if (isOfflineMode) "OFFLINE" else "SYNCED",
                            badgeBgColorHex = if (isOfflineMode) 0xFF0D9488 else 0xFF16A34A,
                            badgeTextColorHex = 0xFFFFFFFF
                        )
                        "previous_year_papers" -> {
                            val pypCount = PreviousYearPaperStore.getPapersForApp(context, activeApp.id).size
                            item.copy(badgeText = "$pypCount PAPERS")
                        }
                        else -> item
                    }

                    val clickAction: () -> Unit = when (item.id) {
                        "mock_test_generator" -> onNavigateToMockTestGenerator
                        "available_mock_tests", "mock_tests" -> onNavigateToAvailableMockTests
                        "current_affairs" -> onNavigateToCurrentAffairs
                        "performance_analytics" -> onNavigateToPerformance
                        "state_leaderboard" -> onNavigateToLeaderboard
                        "offline_cloud_sync" -> onNavigateToOfflineManager
                        "bookmarks" -> onNavigateToSavedQuestions
                        "previous_year_papers" -> onNavigateToPreviousYearPapers
                        else -> ({})
                    }

                    ExactDrawerCardItem(
                        item = resolvedItem,
                        isDarkMode = isDarkMode,
                        onClick = {
                            onCloseDrawer()
                            clickAction()
                        }
                    )
                }

                // C. EXAM TOOLS SECTION
                val examToolsHeader = drawerConfig.sections.find { it.sectionKey == "EXAM_TOOLS" }
                if (examToolsHeader != null && examToolsHeader.isVisible) {
                    ExactDrawerSectionHeader(
                        section = examToolsHeader,
                        title = examToolsHeader.title,
                        icon = getIconByName(examToolsHeader.iconName, Icons.Default.Settings),
                        scriptText = examToolsHeader.scriptText,
                        isDarkMode = isDarkMode
                    )
                }

                val examToolsItems = drawerConfig.items.filter { it.sectionKey == "EXAM_TOOLS" && it.isVisible }
                    .sortedBy { it.order }

                examToolsItems.forEach { item ->
                    val clickAction: () -> Unit = when (item.id) {
                        "subscriptions" -> onNavigateToSubscription
                        "scientific_calculator" -> onNavigateToScientificCalculator
                        "share_app" -> onShareApp
                        else -> ({})
                    }

                    ExactDrawerCardItem(
                        item = item,
                        isDarkMode = isDarkMode,
                        onClick = {
                            onCloseDrawer()
                            clickAction()
                        }
                    )
                }

                // D. LEGAL & SUPPORT SECTION
                val legalHeader = drawerConfig.sections.find { it.sectionKey == "LEGAL_SUPPORT" }
                if (legalHeader != null && legalHeader.isVisible) {
                    ExactDrawerSectionHeader(
                        section = legalHeader,
                        title = legalHeader.title,
                        icon = getIconByName(legalHeader.iconName, Icons.Default.VerifiedUser),
                        scriptText = legalHeader.scriptText,
                        isDarkMode = isDarkMode
                    )
                }

                val legalItems = drawerConfig.items.filter { it.sectionKey == "LEGAL_SUPPORT" && it.isVisible }
                    .sortedBy { it.order }

                legalItems.forEach { item ->
                    if (item.id == "report_issue") {
                        ExactDrawerCardItem(
                            item = item,
                            isDarkMode = isDarkMode,
                            onClick = {
                                onCloseDrawer()
                                onReportIssue()
                            }
                        )
                    } else {
                        val sectionKeyMapping = when (item.id) {
                            "legal_refund_policy" -> "refund_policy"
                            "legal_privacy_policy" -> "privacy_policy"
                            "legal_terms_conditions" -> "terms_conditions"
                            "legal_faq" -> "faq"
                            "legal_about_us" -> "about_us"
                            "legal_contact_us" -> "contact_us"
                            "legal_help_center" -> "help_center"
                            "account_deletion" -> "account_deletion"
                            else -> "privacy_policy"
                        }

                        ExactDrawerCardItem(
                            item = item,
                            isDarkMode = isDarkMode,
                            onClick = {
                                onCloseDrawer()
                                onNavigateToPrivacySection(sectionKeyMapping)
                            }
                        )
                    }
                }

                // Switch Exam Portal Shortcut (ONLY FOR ADMINS)
                if (isAdminLoggedIn) {
                    ExactDrawerCardItem(
                        item = com.example.model.DrawerMenuItemConfig(
                            id = "switch_portal",
                            sectionKey = "ACCOUNT",
                            title = "Switch Exam Portal",
                            subtitle = "Change your target exam",
                            iconName = "swap_horiz",
                            iconTintHex = 0xFF2563EB,
                            cardBgColorHex = 0xFFEFF6FF,
                            cardBorderColorHex = 0xFFBFDBFE,
                            iconBoxBgColorHex = 0xFFDBEAFE
                        ),
                        isDarkMode = isDarkMode,
                        onClick = {
                            onCloseDrawer()
                            onNavigateToAppSelection()
                        }
                    )
                }

                // E. ACCOUNT & ADMIN SECTION
                val accountItems = drawerConfig.items.filter { it.sectionKey == "ACCOUNT" && it.isVisible }
                    .sortedBy { it.order }

                accountItems.forEach { item ->
                    val label = if (userProfile.isLoggedIn) "Logout (${userProfile.name})" else "Candidate Account / Login"
                    val resolved = item.copy(
                        title = label,
                        iconName = if (userProfile.isLoggedIn) "logout" else "person"
                    )

                    ExactDrawerCardItem(
                        item = resolved,
                        isDarkMode = isDarkMode,
                        onClick = {
                            onCloseDrawer()
                            if (userProfile.isLoggedIn) {
                                SampleData.logout()
                            } else {
                                onNavigateToLogin()
                            }
                        }
                    )
                }

                // Admin Panel Shortcut Button if Admin is logged in
                if (isAdminLoggedIn) {
                    ExactDrawerCardItem(
                        item = DrawerMenuItemConfig(
                            id = "admin_panel",
                            sectionKey = "ACCOUNT",
                            title = "Admin Panel & Customizer",
                            subtitle = "Manage header, menu & global config",
                            iconName = "admin_panel_settings",
                            badgeText = "ADMIN",
                            badgeBgColorHex = 0xFFDC2626,
                            cardBgColorHex = 0xFFFEF2F2,
                            cardBorderColorHex = 0xFFFCA5A5,
                            iconBoxBgColorHex = 0xFFFEE2E2,
                            iconTintHex = 0xFFDC2626
                        ),
                        isDarkMode = isDarkMode,
                        onClick = {
                            onCloseDrawer()
                            onNavigateToAdminPanel()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // F. FOOTER DECORATION (Andhra Pradesh Map & Slogan Card)
                ExactDrawerFooterCard(isDarkMode = isDarkMode)
            }
        }
    }
}

// =========================================================================
// COMPONENT: EXACT DRAWER CARD ITEM
// =========================================================================
@Composable
private fun ExactDrawerCardItem(
    item: DrawerMenuItemConfig,
    isDarkMode: Boolean = false,
    customTitle: String? = null,
    customSubtitle: String? = null,
    onClick: () -> Unit
) {
    val displayTitle = customTitle ?: item.title
    val displaySubtitle = customSubtitle ?: item.subtitle

    val titleFontFamily = getFontFamilyByName(item.fontFamily)
    val titleFontWeight = getFontWeightByName(item.fontWeight)

    val iconTint = Color(item.iconTintHex)

    val rawBgColor = Color(item.cardBgColorHex)
    val containerColor = if (isDarkMode) {
        iconTint.copy(alpha = 0.12f).compositeOver(Color(0xFF1E293B))
    } else {
        // Prevent black or grey background colors in navigation menu options
        val isBlackOrGrey = rawBgColor == Color.Black || rawBgColor == Color.DarkGray || rawBgColor == Color.Gray || rawBgColor == Color.LightGray ||
                (rawBgColor.red == rawBgColor.green && rawBgColor.green == rawBgColor.blue && rawBgColor.red < 0.85f) ||
                (rawBgColor.red < 0.15f && rawBgColor.green < 0.15f && rawBgColor.blue < 0.15f)
        if (isBlackOrGrey) Color(0xFFEFF6FF) else rawBgColor
    }

    val borderColor = if (isDarkMode) {
        iconTint.copy(alpha = 0.28f)
    } else {
        Color(item.cardBorderColorHex)
    }

    val iconBoxBg = if (isDarkMode) {
        iconTint.copy(alpha = 0.16f)
    } else {
        Color(item.iconBoxBgColorHex)
    }

    val titleColor = item.titleTextColorHex?.let { Color(it) }
        ?: if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtitleColor = item.subtitleTextColorHex?.let { Color(it) }
        ?: if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    val cardShape = RoundedCornerShape(item.cornerRadiusDp.dp)
    val gradientBrush = if (item.useGradient) {
        val start = Color(item.gradientStartColorHex)
        val end = Color(item.gradientEndColorHex)
        when (item.gradientDirection) {
            "Horizontal" -> Brush.horizontalGradient(listOf(start, end))
            "Vertical" -> Brush.verticalGradient(listOf(start, end))
            else -> Brush.linearGradient(listOf(start, end))
        }
    } else null

    Surface(
        onClick = onClick,
        shape = cardShape,
        color = if (gradientBrush != null) Color.Transparent else containerColor,
        border = BorderStroke(item.borderThicknessDp.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .shadow(0.5.dp, cardShape)
            .then(
                if (gradientBrush != null) Modifier.background(gradientBrush, cardShape)
                else Modifier
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (item.showCardImage && item.cardImageUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = item.cardImageUrl,
                    contentDescription = null,
                    contentScale = getContentScaleByName(item.cardImageScaleType),
                    alpha = item.cardImageOpacity,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(item.cardImageOverlayColorHex))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (item.hasLeftBarAccent) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(28.dp)
                                .clip(CircleShape)
                                .background(Color(item.leftBarColorHex))
                        )
                    }

                    if (item.showIcon) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(item.iconBoxCornerRadiusDp.dp))
                                .background(iconBoxBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconByName(item.iconName, Icons.Default.Circle),
                                contentDescription = displayTitle,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = displayTitle,
                            color = titleColor,
                            fontWeight = titleFontWeight,
                            fontFamily = titleFontFamily,
                            fontSize = item.titleFontSizeSp.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!displaySubtitle.isNullOrEmpty()) {
                            Text(
                                text = displaySubtitle,
                                color = subtitleColor,
                                fontWeight = FontWeight.Medium,
                                fontFamily = titleFontFamily,
                                fontSize = item.subtitleFontSizeSp.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!item.badgeText.isNullOrEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(item.badgeBgColorHex)
                        ) {
                            Text(
                                text = item.badgeText,
                                color = Color(item.badgeTextColorHex),
                                fontSize = item.badgeFontSizeSp.sp,
                                fontWeight = getFontWeightByName(item.badgeFontWeight),
                                fontFamily = titleFontFamily,
                                letterSpacing = 0.3.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = if (isDarkMode) Color(0xFF64748B) else iconTint.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// COMPONENT: EXACT DRAWER SECTION HEADER
// =========================================================================
@Composable
private fun ExactDrawerSectionHeader(
    section: DrawerSectionHeaderConfig? = null,
    title: String = section?.title ?: "",
    icon: ImageVector = getIconByName(section?.iconName ?: "", Icons.Default.MenuBook),
    scriptText: String? = section?.scriptText,
    isDarkMode: Boolean = false
) {
    if (section != null && !section.isVisible) return

    val fontFamily = getFontFamilyByName(section?.fontFamily ?: "Inter")
    val titleWeight = getFontWeightByName(section?.fontWeight ?: "ExtraBold")
    val titleColor = section?.titleColorHex?.let { Color(it) }
        ?: if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF1E3A8A)
    val iconTint = section?.iconTintHex?.let { Color(it) }
        ?: if (isDarkMode) Color(0xFF60A5FA) else Color(0xFF1E3A8A)
    val scriptColor = section?.scriptTextColorHex?.let { Color(it) }
        ?: if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF2563EB)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = (section?.fontSizeSp ?: 12f).sp,
                    fontWeight = titleWeight,
                    fontFamily = fontFamily,
                    letterSpacing = 0.8.sp
                )
            }

            val cleanScriptText = scriptText?.takeIf {
                !it.contains("Practice Makes Progress", ignoreCase = true)
            }
            if (!cleanScriptText.isNullOrEmpty()) {
                Text(
                    text = cleanScriptText,
                    color = scriptColor,
                    fontSize = (section?.scriptFontSizeSp ?: 10f).sp,
                    fontWeight = getFontWeightByName(section?.scriptFontWeight ?: "Bold"),
                    fontFamily = fontFamily,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        HorizontalDivider(
            color = if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1),
            thickness = 1.dp
        )
    }
}

// =========================================================================
// COMPONENT: EXACT DRAWER FOOTER CARD
// =========================================================================
@Composable
private fun ExactDrawerFooterCard(
    isDarkMode: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFE0F2FE),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF1E293B) else Color(0xFFBAE6FD)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Map outline icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "AP Map",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "Together for a Better Andhra",
                        color = if (isDarkMode) Color.White else Color(0xFF0C192E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(modifier = Modifier.size(12.dp, 2.dp).background(Color(0xFFF97316)))
                        Box(modifier = Modifier.size(12.dp, 2.dp).background(Color(0xFF22C55E)))
                        Box(modifier = Modifier.size(12.dp, 2.dp).background(Color(0xFF0284C7)))
                    }
                }
            }

            Text(
                text = "Learn Practice Succeed ✨",
                color = Color(0xFF0284C7),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

// Helper to map string icon names to ImageVectors
fun getIconByName(iconName: String, fallback: ImageVector = Icons.Default.Circle): ImageVector {
    return when (iconName.lowercase()) {
        "person", "account" -> Icons.Default.Person
        "home" -> Icons.Default.Home
        "auto_awesome", "sparkle" -> Icons.Default.AutoAwesome
        "assignment", "test", "quiz_list", "history_edu", "history", "pyp", "previous_year_papers" -> Icons.Default.Assignment
        "newspaper", "article" -> Icons.Default.Newspaper
        "bar_chart", "analytics" -> Icons.Default.BarChart
        "emoji_events", "trophy" -> Icons.Default.EmojiEvents
        "flash_on", "bolt", "lightning" -> Icons.Default.Bolt
        "cloud_sync", "sync" -> Icons.Default.CloudSync
        "workspace_premium", "crown", "pro" -> Icons.Default.WorkspacePremium
        "calculate", "calculator" -> Icons.Default.Calculate
        "share" -> Icons.Default.Share
        "alarm", "reminder" -> Icons.Default.Alarm
        "receipt_long", "refund" -> Icons.Default.ReceiptLong
        "security", "privacy" -> Icons.Default.Security
        "description", "terms" -> Icons.Default.Description
        "quiz", "faq" -> Icons.Default.Quiz
        "info", "about" -> Icons.Default.Info
        "contact_support", "contact" -> Icons.Default.ContactSupport
        "help", "help_center" -> Icons.Default.Help
        "delete_forever", "delete" -> Icons.Default.DeleteForever
        "bug_report", "bug", "report" -> Icons.Default.BugReport
        "logout" -> Icons.Default.Logout
        "school", "cap" -> Icons.Default.School
        "menu_book", "book" -> Icons.Default.MenuBook
        "settings", "gear" -> Icons.Default.Settings
        "verified_user" -> Icons.Default.VerifiedUser
        "admin_panel_settings", "admin" -> Icons.Default.AdminPanelSettings
        else -> fallback
    }
}
