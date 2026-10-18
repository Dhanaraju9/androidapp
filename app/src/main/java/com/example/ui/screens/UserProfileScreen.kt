package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.db.AppDatabase
import com.example.model.LegalDocType
import com.example.model.LegalDocument
import com.example.repository.AppRepository
import com.example.ui.components.DailyPracticeReminderDialog
import com.example.ui.components.ReportIssueDialog
import com.example.ui.components.ShareAppBottomSheet
import com.example.ui.theme.LocalAppConfig
import com.example.util.AppShareManager
import com.example.util.NotificationSettingsManager
import com.example.util.PermissionManager
import com.example.worker.NotificationScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.example.ui.theme.LocalWindowSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSavedQuestions: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToPracticeHistory: () -> Unit = {},
    onNavigateToMockTestHistory: () -> Unit = {},
    onNavigateToAppearanceSettings: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = onNavigateToPracticeHistory,
    onNavigateToNotificationSettings: () -> Unit = {}
) {
    val config = LocalAppConfig.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = false

    val brandPrimary = Color(0xFF1D4ED8) // Elegant Royal Blue
    val brandSecondary = Color(0xFF7C3AED) // Luxurious Violet
    val brandAccent = Color(0xFF0F766E) // Premium Teal

    // Smooth entry transition state
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember(config.appId) { AppRepository(database, config.appId) }
    val userProgressRepo = remember(context) { com.example.repository.UserProgressRepository.getInstance(context) }
    
    val realUserProgress by userProgressRepo.getUserProgress(config.appId).collectAsStateWithLifecycle(
        initialValue = com.example.model.UserProgress()
    )
    val perfSummary by userProgressRepo.getPerformanceSummary(config.appId).collectAsStateWithLifecycle(
        initialValue = com.example.model.MockTestPerformanceSummary()
    )

    val allLegalDocs by repository.allLegalDocuments.collectAsStateWithLifecycle()
    val appDocs = remember(allLegalDocs, config.appId) {
        allLegalDocs.filter { it.mappedAppIds.isEmpty() || it.mappedAppIds.contains(config.appId) }
            .groupBy { it.type }
            .map { it.value.first() }
            .sortedBy { it.order }
    }

    var selectedDoc by remember { mutableStateOf<LegalDocument?>(null) }
    val userProfile by SampleData.userProfileState

    BackHandler {
        if (selectedDoc != null) {
            selectedDoc = null
        } else {
            onBack()
        }
    }

    // Dialogs / Sheets / Collapsibles States
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmationDialog by remember { mutableStateOf(false) }
    var showShareBottomSheet by remember { mutableStateOf(false) }
    var showDailyPracticeReminderDialog by remember { mutableStateOf(false) }
    var showReportIssueDialog by remember { mutableStateOf(false) }
    
    var showCancelConfirmationDialog by remember { mutableStateOf(false) }
    var cancellationStep by remember { mutableStateOf(1) }
    var selectedReason by remember { mutableStateOf("Too expensive") }
    var otherReasonDetails by remember { mutableStateOf("") }

    var showPaymentHistory by remember { mutableStateOf(false) }
    var showInvoices by remember { mutableStateOf(false) }
    var showAppPermissions by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val file = java.io.File(context.cacheDir, "profile_pic_${System.currentTimeMillis()}.jpg")
                        java.io.FileOutputStream(file).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        SampleData.userProfileState.value = SampleData.userProfileState.value.copy(
                            profileImageUrl = file.absolutePath
                        )
                        snackbarHostState.showSnackbar("Profile picture updated successfully!")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val isOneDayPassActive = userProfile.expiresAt > System.currentTimeMillis() || userProfile.isProSubscribed

    // Redesigned with premium bright white background and elegant colorful elements
    val surfaceColor = Color.White
    val cardBorderColor = Color(0xFFE2E8F0) // Soft elegant border
    val textPrimary = Color(0xFF1E293B) // Premium deep navy/slate, not black
    val textSecondary = Color(0xFF64748B) // Slate gray for secondary details
    
    val accentBlue = Color(0xFF0284C7) // Sky Blue
    val accentGreen = Color(0xFF0D9488) // Rich Teal
    val accentOrange = Color(0xFFD97706) // Tasteful Gold

    val mainBackground = Color(0xFFFAFBFE) // Soft slate-white luxurious background

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = surfaceColor,
                tonalElevation = 1.dp,
                shadowElevation = 1.dp
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Profile & Settings",
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = textPrimary
                                )
                            }
                            IconButton(onClick = onNavigateToHome) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home",
                                    tint = brandPrimary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = brandPrimary
                            )
                        }
                        IconButton(onClick = {
                            scope.launch { snackbarHostState.showSnackbar("Profile sync complete.") }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = textSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        containerColor = mainBackground
    ) { paddingVals ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedDoc != null) {
                LegalDocumentViewerWrapper(
                    doc = selectedDoc!!,
                    appName = config.appName,
                    onBack = { selectedDoc = null }
                )
            } else {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 30 }, animationSpec = tween(400))
                ) {
                    val windowSize = LocalWindowSizeClass.current
                    val isTablet = windowSize?.widthSizeClass == WindowWidthSizeClass.Medium || windowSize?.widthSizeClass == WindowWidthSizeClass.Expanded

                    // Prep Stats Data
                    val allSubjects = SampleData.getSubjectsForApp(config.appId)
                    val totalTopics = allSubjects.sumOf { s -> s.units.sumOf { u -> u.topics.size } }.coerceAtLeast(1)
                    val mastered = realUserProgress.completedTopicIds.size
                    val attempted = maxOf(realUserProgress.completedQuestionsCount, perfSummary.totalQuestionsAttempted)
                    val correct = if (perfSummary.totalCorrectAnswers > 0) perfSummary.totalCorrectAnswers
                    else ((attempted * (realUserProgress.accuracyPercentage / 100f)).toInt()).coerceAtLeast(0)
                    val accuracy = if (attempted > 0) "%.1f%%".format((correct.toFloat() / attempted) * 100f) else "0.0%"

                    @Composable
                    fun ProfileAndStatsContent() {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Beautiful modern Profile header card with gradient elements
                            ProfileHeaderCard(
                                userProfile = userProfile,
                                config = config,
                                brandPrimary = brandPrimary,
                                brandSecondary = brandSecondary,
                                brandAccent = brandAccent,
                                isDark = isDark,
                                isOneDayPassActive = isOneDayPassActive,
                                onAvatarClick = {
                                    imagePickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            )

                            // Clean interactive Prep Statistics card
                            PrepStatisticsCard(
                                mastered = mastered,
                                totalTopics = totalTopics,
                                accuracy = accuracy,
                                attempted = attempted,
                                accentBlue = accentBlue,
                                accentGreen = accentGreen,
                                accentOrange = accentOrange,
                                isDark = isDark,
                                onNavigateToPerformance = onNavigateToPerformance
                            )
                        }
                    }

                    @Composable
                    fun SettingsCategoryLists() {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // SECTION A: CANDIDATE ACCOUNT
                            SettingsCategoryCard(
                                categoryTitle = "Candidate Account",
                                brandPrimary = brandPrimary,
                                isDark = isDark,
                                cardBorderColor = cardBorderColor,
                                surfaceColor = surfaceColor
                            ) {
                                SettingsItemRow(
                                    icon = Icons.Default.AccountCircle,
                                    iconTint = accentBlue,
                                    title = "Edit Profile Info",
                                    subtitle = "Update details & target district",
                                    onClick = { showEditProfileDialog = true },
                                    isDark = isDark
                                )
                                HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))
                                SettingsItemRow(
                                    icon = Icons.Default.WorkspacePremium,
                                    iconTint = accentOrange,
                                    title = "Subscription Suite",
                                    subtitle = if (isOneDayPassActive) "Active Plan validity & benefits" else "Unlock premium prep tools & tests",
                                    onClick = onNavigateToSubscription,
                                    isDark = isDark
                                )
                                if (isOneDayPassActive) {
                                    HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))
                                    SettingsItemRow(
                                        icon = Icons.Default.HourglassEmpty,
                                        iconTint = Color(0xFFEF4444),
                                        title = "Cancel Auto-Renewal",
                                        subtitle = "Turn off automated payment renewal",
                                        onClick = { showCancelConfirmationDialog = true },
                                        isDark = isDark
                                    )
                                }
                                HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))
                                SettingsItemRow(
                                    icon = Icons.Default.Language,
                                    iconTint = brandPrimary,
                                    title = "Preferred Language",
                                    subtitle = "Currently: ${userProfile.preferredLanguage}",
                                    onClick = { showLanguageDialog = true },
                                    isDark = isDark
                                )
                            }

                            // SECTION B: APPEARANCE & SCREEN SETTINGS
                            SettingsCategoryCard(
                                categoryTitle = "Appearance & Screen Settings",
                                brandPrimary = brandPrimary,
                                isDark = isDark,
                                cardBorderColor = cardBorderColor,
                                surfaceColor = surfaceColor
                            ) {
                                SettingsItemRow(
                                    icon = Icons.Default.Palette,
                                    iconTint = brandAccent,
                                    title = "Appearance & Themes",
                                    subtitle = "Question cards styling, custom fonts & sizes",
                                    onClick = onNavigateToAppearanceSettings,
                                    isDark = isDark
                                )
                                HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))
                                SettingsItemRow(
                                    icon = Icons.Default.Calculate,
                                    iconTint = accentBlue,
                                    title = "Scientific Calculator",
                                    subtitle = "Show floating calculator tool in practice arena",
                                    isDark = isDark,
                                    onClick = {
                                        val current = com.example.repository.AppConfigRepository.getInstance().getCurrentConfig()
                                        com.example.repository.AppConfigRepository.getInstance().updateAppConfig(
                                            current.copy(isScientificCalculatorEnabled = !current.isScientificCalculatorEnabled)
                                        )
                                    },
                                    trailing = {
                                        Switch(
                                            checked = config.isScientificCalculatorEnabled,
                                            onCheckedChange = { enabled ->
                                                val current = com.example.repository.AppConfigRepository.getInstance().getCurrentConfig()
                                                com.example.repository.AppConfigRepository.getInstance().updateAppConfig(
                                                    current.copy(isScientificCalculatorEnabled = enabled)
                                                )
                                            },
                                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = brandPrimary)
                                        )
                                    }
                                )
                                HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))
                                SettingsItemRow(
                                    icon = Icons.Default.NotificationsActive,
                                    iconTint = accentGreen,
                                    title = "Notification Preferences",
                                    subtitle = "Custom alerts for tests, syllabus & reminders",
                                    onClick = onNavigateToNotificationSettings,
                                    isDark = isDark
                                )
                                HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))
                                SettingsItemRow(
                                    icon = Icons.Default.Alarm,
                                    iconTint = accentOrange,
                                    title = "Practice Goal & Reminder",
                                    subtitle = "Daily: ${NotificationSettingsManager.getDailyQuestionTarget(context)} Qs • ${NotificationSettingsManager.getFormattedReminderTime(context)}",
                                    onClick = { showDailyPracticeReminderDialog = true },
                                    isDark = isDark
                                )
                            }

                            // SECTION C: PAYMENT TRANSACTIONS & SECURITY
                            SettingsCategoryCard(
                                categoryTitle = "Payment Transactions & Security",
                                brandPrimary = brandPrimary,
                                isDark = isDark,
                                cardBorderColor = cardBorderColor,
                                surfaceColor = surfaceColor
                            ) {
                                SettingsItemRow(
                                    icon = Icons.Default.ReceiptLong,
                                    iconTint = accentBlue,
                                    title = "Payment History",
                                    subtitle = "View active/failed subscription payments",
                                    isDark = isDark,
                                    onClick = { showPaymentHistory = !showPaymentHistory },
                                    trailing = {
                                        Icon(
                                            imageVector = if (showPaymentHistory) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = textSecondary
                                        )
                                    }
                                )
                                
                                AnimatedVisibility(visible = showPaymentHistory) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        PaymentHistoryRowItem(
                                            planTitle = "Achiever 6-Month Pass",
                                            transactionDate = "Sept 16, 2026",
                                            orderId = "rzp_pay_948210",
                                            amountPaid = "₹799.00",
                                            invoiceStatus = "SUCCESS",
                                            statusColor = accentGreen,
                                            isDark = isDark
                                        )
                                        HorizontalDivider(color = cardBorderColor)
                                        PaymentHistoryRowItem(
                                            planTitle = "Ranker 12-Month Pass",
                                            transactionDate = "Sept 15, 2026",
                                            orderId = "rzp_pay_failed_481",
                                            amountPaid = "₹999.00",
                                            invoiceStatus = "FAILED",
                                            statusColor = Color(0xFFEF4444),
                                            isDark = isDark
                                        )
                                    }
                                }

                                HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))

                                SettingsItemRow(
                                    icon = Icons.Default.Description,
                                    iconTint = brandPrimary,
                                    title = "Downloadable Invoices",
                                    subtitle = "Get PDFs of tax invoices",
                                    isDark = isDark,
                                    onClick = { showInvoices = !showInvoices },
                                    trailing = {
                                        Icon(
                                            imageVector = if (showInvoices) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = textSecondary
                                        )
                                    }
                                )

                                AnimatedVisibility(visible = showInvoices) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        InvoiceRowItem(
                                            invoiceId = "INV-2026-003",
                                            invoicePlan = "Achiever 6-Month Pass",
                                            invoiceDate = "16 Sept 2026",
                                            amount = "₹799.00",
                                            onView = {
                                                scope.launch { snackbarHostState.showSnackbar("Opening Invoice INV-2026-003...") }
                                            },
                                            onDownload = {
                                                Toast.makeText(context, "Invoice INV-2026-003 downloaded!", Toast.LENGTH_SHORT).show()
                                            },
                                            isDark = isDark
                                        )
                                    }
                                }

                                HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))

                                SettingsItemRow(
                                    icon = Icons.Default.Security,
                                    iconTint = accentGreen,
                                    title = "Security & System Permissions",
                                    subtitle = "View camera, notification & system rights",
                                    isDark = isDark,
                                    onClick = { showAppPermissions = !showAppPermissions },
                                    trailing = {
                                        Icon(
                                            imageVector = if (showAppPermissions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = textSecondary
                                        )
                                    }
                                )

                                AnimatedVisibility(visible = showAppPermissions) {
                                    val permissions = remember { PermissionManager.getPermissionStatuses(context) }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        permissions.forEach { (name, granted) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(name, fontSize = 12.5.sp, color = textPrimary)
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (granted) accentGreen.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = if (granted) "GRANTED" else "DENIED",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (granted) accentGreen else Color(0xFFEF4444),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = { PermissionManager.openAppSettings(context) },
                                            colors = ButtonDefaults.buttonColors(containerColor = cardBorderColor),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Open Device Settings App →", fontSize = 11.5.sp, color = textPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // SECTION D: HELP, SUPPORT & LEGAL
                            SettingsCategoryCard(
                                categoryTitle = "Help, Support & Legal",
                                brandPrimary = brandPrimary,
                                isDark = isDark,
                                cardBorderColor = cardBorderColor,
                                surfaceColor = surfaceColor
                            ) {
                                SettingsItemRow(
                                    icon = Icons.Default.BugReport,
                                    iconTint = Color(0xFFEF4444),
                                    title = "Report Technical Issue",
                                    subtitle = "Bug reporting, screenshot submissions & advice",
                                    onClick = { showReportIssueDialog = true },
                                    isDark = isDark
                                )
                                HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))
                                SettingsItemRow(
                                    icon = Icons.Default.ContactSupport,
                                    iconTint = brandPrimary,
                                    title = "Support Desk & FAQs",
                                    subtitle = "Contact us directly via Email or Support link",
                                    onClick = onNavigateToPrivacy,
                                    isDark = isDark
                                )
                                HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))
                                SettingsItemRow(
                                    icon = Icons.Default.Shield,
                                    iconTint = accentBlue,
                                    title = "Privacy Policy Guidelines",
                                    subtitle = "General security, cloud sync & data rules",
                                    isDark = isDark,
                                    onClick = {
                                        val doc = appDocs.find { it.type == LegalDocType.PRIVACY_POLICY }
                                        if (doc != null) selectedDoc = doc else onNavigateToPrivacy()
                                    }
                                )
                                HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))
                                SettingsItemRow(
                                    icon = Icons.Default.Gavel,
                                    iconTint = accentOrange,
                                    title = "Terms & Conditions",
                                    subtitle = "Platform rules & user conduct agreements",
                                    isDark = isDark,
                                    onClick = {
                                        val doc = appDocs.find { it.type == LegalDocType.TERMS_CONDITIONS }
                                        if (doc != null) selectedDoc = doc else onNavigateToPrivacy()
                                    }
                                )
                                HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))
                                SettingsItemRow(
                                    icon = Icons.Default.Info,
                                    iconTint = brandSecondary,
                                    title = "About",
                                    subtitle = "${config.appName} • Version 3.2.0",
                                    onClick = { showAboutDialog = true },
                                    isDark = isDark
                                )
                                HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(horizontal = 14.dp))
                                SettingsItemRow(
                                    icon = Icons.Default.Share,
                                    iconTint = brandPrimary,
                                    title = "Share Application",
                                    subtitle = "Spread the word to other exam aspirants",
                                    onClick = { showShareBottomSheet = true },
                                    isDark = isDark
                                )
                            }
                        }
                    }

                    @Composable
                    fun LogoutButtonContent() {
                        Button(
                            onClick = {
                                if (userProfile.isLoggedIn) {
                                    showLogoutConfirmationDialog = true
                                } else {
                                    onNavigateToLogin()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (userProfile.isLoggedIn) Color(0xFFFEE2E2) else brandPrimary.copy(alpha = 0.15f),
                                contentColor = if (userProfile.isLoggedIn) Color(0xFFEF4444) else brandPrimary
                            ),
                            border = BorderStroke(1.dp, if (userProfile.isLoggedIn) Color(0xFFFCA5A5) else brandPrimary.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = if (userProfile.isLoggedIn) Icons.Default.Logout else Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (userProfile.isLoggedIn) "Logout Session" else "Log In / Register",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Adaptive Layout for Phones vs Tablets
                    if (isTablet) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingVals)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Left Column (Profile, Stats and Logout Button)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ProfileAndStatsContent()
                                Spacer(modifier = Modifier.height(8.dp))
                                LogoutButtonContent()
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // Right Column (Categorized settings lists)
                            Column(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SettingsCategoryLists()
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    } else {
                        // Standard Single Scrollable Column for Phones
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingVals)
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            item {
                                ProfileAndStatsContent()
                            }
                            item {
                                SettingsCategoryLists()
                            }
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                LogoutButtonContent()
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // --- POPUP DIALOGS ---

    // Edit profile dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(userProfile.name) }
        var editPhone by remember { mutableStateOf(userProfile.phone) }
        var editEmail by remember { mutableStateOf(userProfile.email) }
        var editDistrict by remember { mutableStateOf(userProfile.district) }
        var districtExpanded by remember { mutableStateOf(false) }

        val apDistricts = listOf(
            "Visakhapatnam", "Vijayawada (NTR)", "Guntur", "Tirupati", "Kurnool",
            "Nellore (SPSR)", "Kakinada", "Rajahmundry (East Godavari)", "Kadapa (YSR)",
            "Anantapur", "Srikakulam", "Vizianagaram", "Eluru", "Ongole (Prakasam)",
            "Chittoor", "Nandyal", "Bapatla", "Palnadu", "Dr. B.R. Ambedkar Konaseema",
            "Anakapalli", "Parvathipuram Manyam", "Alluri Sitharama Raju",
            "Sri Sathya Sai", "Annamayya", "West Godavari"
        )

        val isNameValid = editName.trim().length >= 3
        val isPhoneValid = editPhone.trim().matches(Regex("^[6-9]\\d{9}$"))
        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(editEmail.trim()).matches()
        val isFormValid = isNameValid && isPhoneValid && isEmailValid

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = !isNameValid
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) editPhone = it },
                        label = { Text("Mobile Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = !isPhoneValid
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = !isEmailValid
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editDistrict,
                            onValueChange = { editDistrict = it },
                            label = { Text("District") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { districtExpanded = !districtExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = districtExpanded,
                            onDismissRequest = { districtExpanded = false },
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            apDistricts.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = {
                                        editDistrict = d
                                        districtExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isFormValid) {
                            SampleData.userProfileState.value = SampleData.userProfileState.value.copy(
                                name = editName.trim(),
                                phone = editPhone.trim(),
                                email = editEmail.trim(),
                                district = editDistrict.trim()
                            )
                            showEditProfileDialog = false
                            scope.launch { snackbarHostState.showSnackbar("Profile updated!") }
                        }
                    },
                    enabled = isFormValid,
                    colors = ButtonDefaults.buttonColors(containerColor = brandPrimary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = surfaceColor
        )
    }

    // Language selection
    if (showLanguageDialog) {
        val languages = listOf("Telugu & English", "English Only", "Telugu Only", "Hindi")
        var currentLang by remember { mutableStateOf(userProfile.preferredLanguage) }

        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    languages.forEach { l ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentLang = l }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = currentLang == l,
                                onClick = { currentLang = l },
                                colors = RadioButtonDefaults.colors(selectedColor = brandPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(l, color = textPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SampleData.userProfileState.value = SampleData.userProfileState.value.copy(
                            preferredLanguage = currentLang
                        )
                        showLanguageDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = brandPrimary)
                ) {
                    Text("Apply", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = surfaceColor
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About ${config.appName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Version: 3.2.0 (Build 2026.09)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("Tenant Application: ${config.appName}", fontSize = 12.sp, color = textSecondary)
                    Text("Comprehensive learning and preparation tools for State and Central Competitive Recruitment exams.", fontSize = 12.5.sp, color = textSecondary, lineHeight = 18.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = brandPrimary)
                ) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = surfaceColor
        )
    }

    // Logout confirmation dialog
    if (showLogoutConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmationDialog = false },
            title = { Text("Logout?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to end your learning session on this device?", color = textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmationDialog = false
                        SampleData.logout()
                        onNavigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmationDialog = false }) {
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = surfaceColor
        )
    }

    // Subscription Cancel auto-renewal flow
    if (showCancelConfirmationDialog) {
        val reasons = listOf("Too expensive", "Not using enough", "Content issues", "Glitches", "Completed exam prep", "Other")
        AlertDialog(
            onDismissRequest = {
                showCancelConfirmationDialog = false
                cancellationStep = 1
            },
            title = {
                Text(
                    text = if (cancellationStep == 1) "Cancel auto-renewal?" else "Confirm Cancellation",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (cancellationStep == 1) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp).verticalScroll(rememberScrollState())
                    ) {
                        reasons.forEach { r ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedReason = r }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedReason == r,
                                    onClick = { selectedReason = r },
                                    colors = RadioButtonDefaults.colors(selectedColor = brandPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(r, color = textPrimary)
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Auto-billing will stop immediately, but you still keep VIP access benefits until active subscription expiry date.",
                        color = textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                if (cancellationStep == 1) {
                    Button(
                        onClick = { cancellationStep = 2 },
                        colors = ButtonDefaults.buttonColors(containerColor = brandPrimary)
                    ) {
                        Text("Next", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            showCancelConfirmationDialog = false
                            cancellationStep = 1
                            SampleData.cancelSubscription(selectedReason, if (selectedReason == "Other") otherReasonDetails else null)
                            scope.launch { snackbarHostState.showSnackbar("Auto-renewal cancelled successfully.") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Confirm", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCancelConfirmationDialog = false
                        cancellationStep = 1
                    }
                ) {
                    Text("Keep Subscribed", color = brandPrimary)
                }
            },
            containerColor = surfaceColor
        )
    }

    if (showShareBottomSheet) {
        ShareAppBottomSheet(
            appName = config.appName,
            onDismissRequest = { showShareBottomSheet = false }
        )
    }

    if (showDailyPracticeReminderDialog) {
        DailyPracticeReminderDialog(
            onDismissRequest = { showDailyPracticeReminderDialog = false }
        )
    }

    if (showReportIssueDialog) {
        ReportIssueDialog(
            onDismiss = { showReportIssueDialog = false }
        )
    }
}

// =====================================================
// --- REDESIGNED SUPPORTING HELPER COMPOSABLES ---
// =====================================================

@Composable
fun ProfileHeaderCard(
    userProfile: com.example.model.UserProfileModel,
    config: com.example.model.AppConfiguration,
    brandPrimary: Color,
    brandSecondary: Color,
    brandAccent: Color,
    isDark: Boolean,
    isOneDayPassActive: Boolean,
    onAvatarClick: () -> Unit
) {
    val cardBorder = Color(0xFFE2E8F0)
    val textPrimary = Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)

    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFEFF6FF), // Soft Sky Blue tint
                            Color(0xFFF5F3FF), // Soft Violet tint
                            Color(0xFFFFFDF5)  // Soft Gold/White tint
                        )
                    )
                )
        ) {
            // Luxurious Gold Crown/Star decoration in the top corner
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = Color(0xFFF59E0B).copy(alpha = 0.15f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(96.dp)
                    .padding(10.dp)
                    .graphicsLayer(rotationZ = 15f)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Modern Avatar Circle with press feedback and gradient borders
                    var isAvatarPressed by remember { mutableStateOf(false) }
                    val avatarScale by animateFloatAsState(targetValue = if (isAvatarPressed) 0.95f else 1f, label = "avatar_scale")
                    
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier
                            .graphicsLayer(scaleX = avatarScale, scaleY = avatarScale)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isAvatarPressed = true
                                        tryAwaitRelease()
                                        isAvatarPressed = false
                                    },
                                    onTap = { onAvatarClick() }
                                )
                            }
                    ) {
                        val ringBrush = Brush.sweepGradient(
                            colors = listOf(brandPrimary, brandAccent, brandSecondary, brandPrimary)
                        )
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(ringBrush, CircleShape)
                                .padding(3.dp)
                                .background(Color.White, CircleShape)
                                .padding(2.dp)
                                .clip(CircleShape)
                        ) {
                            if (userProfile.profileImageUrl != null) {
                                AsyncImage(
                                    model = java.io.File(userProfile.profileImageUrl!!),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(brandPrimary.copy(alpha = 0.1f))
                                ) {
                                    Text(
                                        text = userProfile.name.split(" ")
                                            .mapNotNull { it.firstOrNull()?.uppercase() }
                                            .take(2)
                                            .joinToString(""),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = brandPrimary
                                    )
                                }
                            }
                        }
                        
                        // Edit camera badge overlay
                        Surface(
                            shape = CircleShape,
                            color = brandPrimary,
                            border = BorderStroke(1.5.dp, Color.White),
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Edit photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    // User text info
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = userProfile.name.ifBlank { "Candidate" },
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            
                            if (isOneDayPassActive) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF7C3AED), Color(0xFFD97706))
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "★ PREMIUM PRO",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userProfile.email.ifBlank { "candidate@appscprep.in" },
                            fontSize = 12.5.sp,
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = userProfile.phone.ifBlank { "+91 98765 43210" },
                            fontSize = 12.5.sp,
                            color = textSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = brandPrimary.copy(alpha = 0.1f),
                            border = BorderStroke(0.5.dp, brandPrimary.copy(alpha = 0.25f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = null,
                                    tint = brandPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = userProfile.targetExam.ifBlank { config.appName },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandPrimary
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
fun PrepStatisticsCard(
    mastered: Int,
    totalTopics: Int,
    accuracy: String,
    attempted: Int,
    accentBlue: Color,
    accentGreen: Color,
    accentOrange: Color,
    isDark: Boolean,
    onNavigateToPerformance: () -> Unit
) {
    val surfaceColor = Color.White
    val cardBorderColor = Color(0xFFE2E8F0)
    val textPrimary = Color(0xFF1E293B)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = accentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Prep Analytics",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
                TextButton(
                    onClick = onNavigateToPerformance,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "Full Analytics →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMiniWidget(
                    title = "Topics Mastered",
                    value = "$mastered / $totalTopics",
                    icon = Icons.Default.AutoStories,
                    color = accentBlue,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                StatMiniWidget(
                    title = "Accuracy Rate",
                    value = accuracy,
                    icon = Icons.Default.GpsFixed,
                    color = accentGreen,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                StatMiniWidget(
                    title = "MCQs Attempted",
                    value = "$attempted",
                    icon = Icons.Default.Check,
                    color = accentOrange,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    categoryTitle: String,
    brandPrimary: Color,
    isDark: Boolean,
    cardBorderColor: Color,
    surfaceColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = categoryTitle.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = brandPrimary,
            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp, start = 4.dp),
            letterSpacing = 1.sp
        )
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = BorderStroke(1.dp, cardBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDark: Boolean,
    trailing: @Composable (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, label = "press_scale")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .padding(vertical = 12.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Elegant colored micro-box for icons
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = if (isDark) 0.15f else 0.08f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun StatMiniWidget(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isHovered) 1.03f else 1f, label = "hover_scale")

    Surface(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHovered = true
                        tryAwaitRelease()
                        isHovered = false
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = if (isDark) 0.12f else 0.05f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PaymentHistoryRowItem(
    planTitle: String,
    transactionDate: String,
    orderId: String,
    amountPaid: String,
    invoiceStatus: String,
    statusColor: Color,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.15f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = planTitle,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = "$transactionDate • $orderId",
                    fontSize = 10.5.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amountPaid,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = statusColor.copy(alpha = 0.18f)
            ) {
                Text(
                    text = invoiceStatus,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun InvoiceRowItem(
    invoiceId: String,
    invoicePlan: String,
    invoiceDate: String,
    amount: String,
    onView: () -> Unit,
    onDownload: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = invoiceId,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = "$invoicePlan • $invoiceDate",
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = amount,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )

            IconButton(
                onClick = onView,
                modifier = Modifier
                    .size(32.dp)
                    .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "View PDF",
                    tint = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB),
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = onDownload,
                modifier = Modifier
                    .size(32.dp)
                    .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Download PDF",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
fun CountdownTimerView(expiresAt: Long, activatedAt: Long, isOneDayPassActive: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VALIDITY STATUS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
            Text(
                text = if (isOneDayPassActive) "Unlimited Practice Active" else "Plan Lapsed",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOneDayPassActive) Color(0xFF34D399) else Color(0xFFF87171)
            )
        }
    }
}

@Composable
private fun LegalDocumentViewerWrapper(doc: LegalDocument, appName: String, onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val processedContent = remember(doc.content, appName) {
        doc.content.replace("{{APP_NAME}}", appName)
            .replace("{{COMPANY_NAME}}", "$appName Learning Systems")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = doc.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF0F172A))
            }
        }

        Text(
            text = "Last Updated: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(doc.lastUpdated))}",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        HorizontalDivider(color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = processedContent,
            fontSize = 14.5.sp,
            color = Color(0xFF334155),
            lineHeight = 22.sp,
            textAlign = TextAlign.Justify
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}
