package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.AppTenantModel
import com.example.model.PromotionalPosterModel
import com.example.util.AdminSessionManager
import com.example.util.AdminRole
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.UUID
import java.util.concurrent.TimeUnit
import android.app.Activity

/**
 * Admin Login Dialog / Full Screen Composable
 */
@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var phoneNumber by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${SampleData.getActiveApp().name} Admin Login",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (errorMessage != null) {
                    Surface(color = Color(0xFFFEF2F2), border = BorderStroke(1.dp, Color(0xFFFCA5A5)), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(errorMessage!!, modifier = Modifier.padding(10.dp), color = Color(0xFFDC2626), fontSize = 12.sp)
                    }
                }

                if (!isOtpSent) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number (+91...)") },
                        placeholder = { Text("+91XXXXXXXXXX") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                } else {
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = { Text("Enter OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    if (!isOtpSent) {
                        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(context as Activity)
                            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                    FirebaseAuth.getInstance().signInWithCredential(credential)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) onLoginSuccess()
                                            else errorMessage = "Auto-verification failed."
                                        }
                                }
                                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                                    verificationId = id
                                    isOtpSent = true
                                    isLoading = false
                                }
                                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                                    isLoading = false
                                    errorMessage = e.message
                                }
                            })
                            .build()
                        PhoneAuthProvider.verifyPhoneNumber(options)
                    } else {
                        val credential = PhoneAuthProvider.getCredential(verificationId!!, otpCode)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) onLoginSuccess()
                                else errorMessage = "Verification failed."
                            }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Loading..." else if (isOtpSent) "Verify OTP" else "Send OTP")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    )
}

// Define responsive spacing constants
val AdminSpacing = 8.dp

@Composable
fun AdminCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(AdminSpacing),
            content = content
        )
    }
}

/**
 * Main Admin Dashboard Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onSwitchToUserPage: () -> Unit,
    onLogout: () -> Unit = {},
    onLogoutToHome: () -> Unit = onLogout,
    onLogoutToLogin: () -> Unit = onLogout,
    onNavigateToSubscription: () -> Unit = {}
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) }
    // Grouped logically
    val tabs = listOf(
        "Dashboard", "App Management", "Syllabus", "MCQ Upload", "Question Reports",
        "Highlighting", "Current Affairs", "Posters", "Images",
        "Wallpapers", "Top Bar", "Borders", "Menu", "Carousel", "Users",
        "Subscriptions", "Coupons", "Revenue", "Notifications", "Live",
        "Support", "Tenant Generator", "PYP Manager"
    )

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSearchAppDialog by remember { mutableStateOf(false) }
    var broadcastNoticeText by remember { mutableStateOf("🎯 APPSC Group-2 Prelims & Mains Comprehensive Test Series is now Live! Practice new sets daily.") }

    BackHandler {
        if (activeTab != 0) {
            activeTab = 0
        } else {
            onSwitchToUserPage()
        }
    }

    // Search and Switch Exam App Dialog
    if (showSearchAppDialog) {
        AdminAppSearchDialog(
            onDismiss = { showSearchAppDialog = false },
            onAppSelected = { selectedApp ->
                SampleData.setActiveApp(selectedApp.id)
                showSearchAppDialog = false
                Toast.makeText(context, "Switched to ${selectedApp.name}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        if (activeTab != 0) {
                            activeTab = 0
                        } else {
                            onSwitchToUserPage()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showSearchAppDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            val activeApp = SampleData.getActiveApp()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeApp.name,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Switch App", tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = "Admin Portal",
                                fontSize = 10.5.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Compact Quick Search Button
                    IconButton(onClick = { showSearchAppDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Apps",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Compact Switch to User View Chip Button
                    IconButton(onClick = onSwitchToUserPage) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "User View",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Compact Logout Button
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val activeApp = SampleData.getActiveApp()

            // Quick Category Filter Bar
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val filterPills = listOf(
                    "Overview" to 0,
                    "Academic & MCQs" to 2,
                    "Design & UI" to 8,
                    "Users & Revenue" to 15,
                    "System & Support" to 19
                )
                items(filterPills) { (pillTitle, targetIndex) ->
                    val isSelectedGroup = when (pillTitle) {
                        "Overview" -> activeTab in 0..1 || activeTab == 20
                        "Academic & MCQs" -> activeTab in 2..7
                        "Design & UI" -> activeTab in 8..14
                        "Users & Revenue" -> activeTab in 15..18
                        "System & Support" -> activeTab in 19..22
                        else -> false
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelectedGroup) Color(0xFFEFF6FF) else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (isSelectedGroup) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.clickable { activeTab = targetIndex }
                    ) {
                        Text(
                            text = pillTitle,
                            fontSize = 11.sp,
                            fontWeight = if (isSelectedGroup) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelectedGroup) Color(0xFF2563EB) else Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Compact Admin Profile & Status Header Bar
            val isReviewer = AdminSessionManager.isReviewerSession(context)
            Surface(
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isReviewer) Color(0xFFD97706) else Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isReviewer) "GR" else "SD",
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AdminSessionManager.getAdminName(context),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${AdminSessionManager.getAdminEmail(context)})",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        color = if (isReviewer) Color(0xFF78350F) else Color(0xFF065F46),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isReviewer) Color(0xFFFBBF24) else Color(0xFF34D399))
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isReviewer) "Reviewer" else "Super Admin",
                                color = if (isReviewer) Color(0xFFFDE68A) else Color(0xFF34D399),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.White,
                contentColor = Color(0xFF2563EB),
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (activeTab == index) Color(0xFF2563EB) else Color(0xFF64748B)
                            )
                        }
                    )
                }
            }

            // Tab Contents with smooth Crossfade transition to prevent freeze/stuck states
            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(
                    targetState = activeTab,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    label = "AdminTabAnimation"
                ) { currentTab ->
                    when (currentTab) {
                        0 -> AdminDashboardTab(
                            onSwitchToUser = onSwitchToUserPage,
                            onLogoutClick = { showLogoutDialog = true },
                            onGoToPosters = { activeTab = 7 },
                            onGoToSyllabus = { activeTab = 2 },
                            onGoToWallpapers = { activeTab = 9 }
                        )
                        1 -> AdminAppsManagementTab(
                            onNavigateToTenantGenerator = { activeTab = 21 }
                        )
                        2 -> AdminSyllabusTab()
                        3 -> AdminMcqUploadTab()
                        4 -> AdminQuestionReportsTab()
                        5 -> AdminQuestionHighlightTab()
                        6 -> AdminCurrentAffairsTab()
                        7 -> AdminPosterManagement(
                            onNavigateToSubscription = onNavigateToSubscription,
                            onSwitchToUserHome = onSwitchToUserPage
                        )
                        8 -> AdminCardImageManagementTab()
                        9 -> AdminWallpaperManagementTab()
                        10 -> AdminTopBarManagerTab()
                        11 -> AdminCardBordersTab()
                        12 -> AdminLeftMenuManagerTab()
                        13 -> AdminCulturalCarouselTab()
                        14 -> AdminUsersTab()
                        15 -> AdminSubscriptionTab()
                        16 -> AdminCouponTab()
                        17 -> AdminRevenueTab()
                        18 -> AdminNotificationsTab()
                        19 -> AdminLiveDashboardTab()
                        20 -> AdminLegalSupportTab()
                        21 -> AdminTenantGeneratorTab()
                        22 -> AdminPreviousYearPapersTab()
                        else -> AdminDashboardTab(
                            onSwitchToUser = onSwitchToUserPage,
                            onLogoutClick = { showLogoutDialog = true },
                            onGoToPosters = { activeTab = 7 },
                            onGoToSyllabus = { activeTab = 2 },
                            onGoToWallpapers = { activeTab = 9 }
                        )
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Logout of Admin Session?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "This will end your administrative session and clear the persistent login token.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "Admin: ${AdminSessionManager.getAdminEmail(context)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Where would you like to be redirected?",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            AdminSessionManager.logout(context)
                            Toast.makeText(context, "Admin Logged Out. Redirecting to Home Screen...", Toast.LENGTH_SHORT).show()
                            onLogoutToHome()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Logout & Return to Home Screen", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            showLogoutDialog = false
                            AdminSessionManager.logout(context)
                            Toast.makeText(context, "Admin Logged Out. Opening Login Screen...", Toast.LENGTH_SHORT).show()
                            onLogoutToLogin()
                        },
                        border = BorderStroke(1.dp, Color(0xFF2563EB)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Logout & Return to Primary Login", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

/**
 * Tab 1: Admin Dashboard & Metrics
 */
@Composable
private fun AdminDashboardTab(
    onSwitchToUser: () -> Unit,
    onLogoutClick: () -> Unit,
    onGoToPosters: () -> Unit,
    onGoToSyllabus: () -> Unit,
    onGoToWallpapers: () -> Unit,
    onGoToAnimations: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeAppId = SampleData.currentActiveAppId.value
    val subjectsForApp = SampleData.getSubjectsForApp(activeAppId)
    val questionsForApp = SampleData.questionsState.filter { it.appId == activeAppId }

    val totalSubjects = subjectsForApp.size
    val totalUnits = subjectsForApp.sumOf { it.units.size }
    val totalTopics = subjectsForApp.sumOf { it.units.sumOf { u -> u.topics.size } }
    val totalQuestions = questionsForApp.size
    
    val staticSets = subjectsForApp.sumOf { it.units.sumOf { u -> u.topics.sumOf { t -> t.practiceSetsCount } } }
    val dynamicSets = questionsForApp.map { it.setName }.filter { it.isNotBlank() }.distinct().size
    val totalSets = staticSets + dynamicSets

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Quick Action: Switch to User Page Banner
        item {
            Surface(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Switch to User Page",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = "యూజర్ స్క్రీన్‌కి మారండి (Student View)",
                                fontSize = 12.sp,
                                color = Color(0xFF3B82F6)
                            )
                        }
                    }

                    Button(
                        onClick = onSwitchToUser,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Switch", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Action: Premium Animation Engine Studio Banner
        item {
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGoToAnimations() }
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesomeMotion,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Premium Animation Engine & Motion Studio",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "12 Presets • 24 Component Targets • Cascade, 3D Flip, Transitions & Live Sandbox",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Button(
                        onClick = onGoToAnimations,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Configure", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Live Platform Metrics Grid
        item {
            Text(
                text = "Live Platform Metrics",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isExpanded = maxWidth >= 600.dp
                val totalUsersCount = SampleData.adminUsersState.size
                val activeUsersCount = SampleData.adminUsersState.count { it.status == com.example.model.UserAccountStatus.ACTIVE }
                val premiumUsersCount = SampleData.adminUsersState.count { it.isPremium }

                if (isExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard("Total Users", "$totalUsersCount", "Registered Users", Icons.Default.People, Color(0xFF3B82F6), Modifier.weight(1f)) {}
                            MetricCard("Active Users", "$activeUsersCount", "Online Recently", Icons.Default.Group, Color(0xFF10B981), Modifier.weight(1f)) {}
                            MetricCard("Premium Users", "$premiumUsersCount", "Paid Subscriptions", Icons.Default.Star, Color(0xFFF59E0B), Modifier.weight(1f)) {}
                            MetricCard("Total Subjects", "$totalSubjects", "Syllabus Subjects", Icons.Default.MenuBook, Color(0xFF8B5CF6), Modifier.weight(1f), onGoToSyllabus)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard("Total Units", "$totalUnits", "Chapters/Units", Icons.Default.LibraryBooks, Color(0xFFEC4899), Modifier.weight(1f), onGoToSyllabus)
                            MetricCard("Total Topics", "$totalTopics", "Granular Topics", Icons.Default.Topic, Color(0xFF06B6D4), Modifier.weight(1f), onGoToSyllabus)
                            MetricCard("Total MCQs", "$totalQuestions", "Question Bank", Icons.Default.Quiz, Color(0xFFF43F5E), Modifier.weight(1f), onGoToSyllabus)
                            MetricCard("Total Sets", "$totalSets", "Practice Sets", Icons.Default.FormatListBulleted, Color(0xFF14B8A6), Modifier.weight(1f), onGoToSyllabus)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard("Total Users", "$totalUsersCount", "Registered", Icons.Default.People, Color(0xFF3B82F6), Modifier.weight(1f)) {}
                            MetricCard("Active Users", "$activeUsersCount", "Online", Icons.Default.Group, Color(0xFF10B981), Modifier.weight(1f)) {}
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard("Premium Users", "$premiumUsersCount", "Subscriptions", Icons.Default.Star, Color(0xFFF59E0B), Modifier.weight(1f)) {}
                            MetricCard("Total Subjects", "$totalSubjects", "Subjects", Icons.Default.MenuBook, Color(0xFF8B5CF6), Modifier.weight(1f), onGoToSyllabus)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard("Total Units", "$totalUnits", "Units", Icons.Default.LibraryBooks, Color(0xFFEC4899), Modifier.weight(1f), onGoToSyllabus)
                            MetricCard("Total Topics", "$totalTopics", "Topics", Icons.Default.Topic, Color(0xFF06B6D4), Modifier.weight(1f), onGoToSyllabus)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard("Total MCQs", "$totalQuestions", "Questions", Icons.Default.Quiz, Color(0xFFF43F5E), Modifier.weight(1f), onGoToSyllabus)
                            MetricCard("Total Sets", "$totalSets", "Sets", Icons.Default.FormatListBulleted, Color(0xFF14B8A6), Modifier.weight(1f), onGoToSyllabus)
                        }
                    }
                }
            }
        }

        // Quick Controls
        item {
            Text(
                text = "Quick Administrative Controls",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminActionRow(
                    icon = Icons.Default.Campaign,
                    iconTint = Color(0xFF2563EB),
                    title = "Manage Promotional & Festival Banners",
                    subtitle = "Toggle active banners, reorder carousel, add flash offers",
                    onClick = onGoToPosters
                )
                AdminActionRow(
                    icon = Icons.Default.Wallpaper,
                    iconTint = Color(0xFF2563EB),
                    title = "Manage App Wallpapers & Screen Backgrounds",
                    subtitle = "Update entire app or specific screen backgrounds with multi-app targeting",
                    onClick = onGoToWallpapers
                )
                AdminActionRow(
                    icon = Icons.Default.AutoStories,
                    iconTint = Color(0xFF059669),
                    title = "APPSC Group-2 Syllabus & Topic Sets",
                    subtitle = "Inspect units, weightage, and question coverage",
                    onClick = onGoToSyllabus
                )
                AdminActionRow(
                    icon = Icons.Default.NotificationAdd,
                    iconTint = Color(0xFFD97706),
                    title = "Publish Student Notification Bulletin",
                    subtitle = "Send announcements on exam dates and syllabus updates",
                    onClick = { /* handled in tabs */ }
                )
            }
        }

        // Admin Session & Security (Logout Card)
        item {
            Surface(
                color = Color(0xFFFFF1F2),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.2.dp, Color(0xFFFECDD3)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE11D48)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Admin Session & Security",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = Color(0xFF9F1239)
                                )
                                Text(
                                    text = "Active: ${AdminSessionManager.getAdminEmail(context)}",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFBE123C)
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFFBE123C).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Persistent",
                                color = Color(0xFFBE123C),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "To end your administrator session and clear the persistent login token, tap Logout below. You will be prompted to redirect to the Home screen or Candidate Login screen.",
                        fontSize = 12.sp,
                        color = Color(0xFF4C0519),
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = onLogoutClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout of Admin Session", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}



/**
 * Tab 3: Syllabus and Subjects
 */
@Composable
private fun AdminSyllabusTab() {
    AdminSyllabusManagementView()
}

/**
 * Tab 4: Broadcast Bulletin
 */
@Composable
private fun AdminBroadcastTab(
    noticeText: String,
    onNoticeChange: (String) -> Unit
) {
    val context = LocalContext.current
    var isBroadcasting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Broadcast Student Flash Bulletin",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "Update the live announcements banner visible to all APPSC Group-2 aspirants on the Home Screen.",
            fontSize = 12.5.sp,
            color = Color(0xFF64748B)
        )

        OutlinedTextField(
            value = noticeText,
            onValueChange = onNoticeChange,
            label = { Text("Announcement Notice Text") },
            minLines = 4,
            maxLines = 6,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val cleanNotice = noticeText.trim()
                if (cleanNotice.isNotBlank()) {
                    SampleData.broadcastNoticeState.value = cleanNotice
                    com.example.util.FirebaseSyncManager.syncAnnouncementToFirestore(
                        appId = SampleData.currentActiveAppId.value,
                        noticeText = cleanNotice
                    )
                    SampleData.adminNotificationsState.add(
                        0,
                        com.example.model.AdminNotificationModel(
                            id = "notif-announcement-${System.currentTimeMillis()}",
                            title = "📢 Admin Announcement",
                            message = cleanNotice,
                            category = com.example.model.NotificationCategory.IMPORTANT,
                            audience = com.example.model.NotificationAudience.ALL,
                            status = com.example.model.NotificationStatus.SENT,
                            sentAt = System.currentTimeMillis(),
                            priority = "HIGH",
                            iconName = "siren",
                            isFeatured = true,
                            isRead = false,
                            actionText = "View"
                        )
                    )
                    Toast.makeText(context, "📢 Announcement broadcasted & synchronized with User App!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Please enter an announcement notice first.", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Broadcast Notice", fontWeight = FontWeight.Bold)
        }

        Surface(
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Live Student Banner Preview:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = noticeText.ifEmpty { "No active notice" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Metric Card Component
 */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = Color(0xFF0F172A)
            )
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF334155),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Admin Action Row
 */
@Composable
private fun AdminActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Clearly Visible Search & Fast Switch Dialog for Admin Panel Exam Apps
 * Allows Admin to quickly find and switch to any Exam App without scrolling through a long list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppSearchDialog(
    onDismiss: () -> Unit,
    onAppSelected: (AppTenantModel) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val allApps = SampleData.appsListState
    val activeApp = SampleData.getActiveApp()

    val categories = remember(allApps) {
        listOf("All") + allApps.map { it.category }.filter { it.isNotBlank() }.distinct()
    }

    val filteredApps = remember(searchQuery, selectedCategory, allApps) {
        allApps.filter { app ->
            val matchesCategory = selectedCategory == "All" || app.category.equals(selectedCategory, ignoreCase = true)
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                app.name.lowercase().contains(q) ||
                app.shortName.lowercase().contains(q) ||
                app.examName.lowercase().contains(q) ||
                app.id.lowercase().contains(q) ||
                app.category.lowercase().contains(q) ||
                app.region.lowercase().contains(q)
            matchesCategory && matchesQuery
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Search & Switch Exam App",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Quick switcher for multi-exam tenants",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, exam, category (e.g. APPSC, UPSC)...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Filter Chips
                if (categories.size > 2) {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Results Counter
                Text(
                    text = "${filteredApps.size} apps found • Click to switch active exam",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )

                // Apps List
                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Exam Apps match \"$searchQuery\"", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredApps, key = { it.id }) { app ->
                            val isActive = app.id == activeApp.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isActive) Color(0xFFEFF6FF) else Color.White,
                                border = BorderStroke(
                                    width = if (isActive) 1.5.dp else 1.dp,
                                    color = if (isActive) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAppSelected(app) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isActive) Color(0xFF2563EB) else Color(0xFFF1F5F9)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = app.shortName.take(3).uppercase(),
                                                color = if (isActive) Color.White else Color(0xFF475569),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = app.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = Color(0xFF0F172A),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (isActive) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFFDCFCE7)
                                                    ) {
                                                        Text(
                                                            text = "ACTIVE",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = Color(0xFF16A34A),
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${app.examName} • ${app.category}",
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF64748B),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onAppSelected(app) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isActive) Color(0xFF16A34A) else Color(0xFF2563EB)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            text = if (isActive) "Current" else "Switch",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
