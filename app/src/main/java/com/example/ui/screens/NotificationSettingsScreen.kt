package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.ui.components.ReportIssueDialog
import com.example.ui.theme.LocalAppConfig
import com.example.util.NotificationSettingsManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {}
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    val config = LocalAppConfig.current
    var showReportIssueDialog by remember { mutableStateOf(false) }

    // Load initial states from local shared preferences
    var overallEnabled by remember { mutableStateOf(NotificationSettingsManager.isNotificationsEnabled(context)) }
    var soundEnabled by remember { mutableStateOf(NotificationSettingsManager.isNotificationSoundEnabled(context)) }
    var vibrationEnabled by remember { mutableStateOf(NotificationSettingsManager.isNotificationVibrationEnabled(context)) }
    
    var studyMaterialsEnabled by remember { mutableStateOf(NotificationSettingsManager.isStudyMaterialsEnabled(context)) }
    var mockTestsEnabled by remember { mutableStateOf(NotificationSettingsManager.isMockTestsEnabled(context)) }
    var examAlertsEnabled by remember { mutableStateOf(NotificationSettingsManager.isExamAlertsEnabled(context)) }
    var systemUpdatesEnabled by remember { mutableStateOf(NotificationSettingsManager.isSystemUpdatesEnabled(context)) }

    // Daily Reminder States
    var dailyPracticeEnabled by remember { mutableStateOf(NotificationSettingsManager.isDailyPracticeReminderEnabled(context)) }
    var dailyTargetCount by remember { mutableStateOf(NotificationSettingsManager.getDailyQuestionTarget(context)) }
    var reminderHour by remember { mutableStateOf(NotificationSettingsManager.getReminderHour(context)) }
    var reminderMinute by remember { mutableStateOf(NotificationSettingsManager.getReminderMinute(context)) }

    var isSaving by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    // Theme styling matching the rest of the application
    val primaryColor = if (isDark) Color(0xFFFFD700) else Color(0xFF1E3A8A)
    val accentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
    val surfaceColor = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E1B4B))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFFE2E8F0))
        )
    }

    // Animation for entries
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            Surface(
                color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Notification Settings",
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("notification_settings_back_btn")
                            ) {
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
                                    tint = Color(0xFF2563EB)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(450)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(450))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // 1. BEAUTIFUL DRAWN HERO HEADER BLOCK
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        border = BorderStroke(1.2.dp, cardBorderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = if (isDark) listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                                        else listOf(Color(0xFFEFF6FF), Color(0xFFFFFFFF))
                                    )
                                )
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                // Draw an attractive notification sound wave Canvas
                                Canvas(modifier = Modifier.size(70.dp)) {
                                    val center = size / 2f
                                    drawCircle(
                                        color = primaryColor.copy(alpha = 0.25f),
                                        radius = 30.dp.toPx(),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    drawCircle(
                                        color = primaryColor.copy(alpha = 0.12f),
                                        radius = 45.dp.toPx(),
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                }
                                Icon(
                                    imageVector = if (overallEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = "Bell Status Icon",
                                    tint = if (overallEnabled) primaryColor else textSecondary,
                                    modifier = Modifier.size(42.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Never Miss Crucial Updates",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Customize push alerts for syllabus releases, job changes, mock scores, and set your self-practice targets.",
                                fontSize = 12.5.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                        }
                    }

                    // 2. MASTER TOGGLE CARD
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        border = BorderStroke(1.2.dp, cardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (overallEnabled) accentColor.copy(alpha = 0.12f) else textSecondary.copy(alpha = 0.12f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhonelinkRing,
                                        contentDescription = null,
                                        tint = if (overallEnabled) accentColor else textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Allow Push Notifications",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Enable all notifications & sound alerts",
                                        fontSize = 11.5.sp,
                                        color = textSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = overallEnabled,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    overallEnabled = it
                                },
                                modifier = Modifier.testTag("master_notification_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = primaryColor,
                                    checkedTrackColor = primaryColor.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }

                    // 3. DETAILED ALERT TYPES CARD (ENABLED ONLY IF MASTER SWITCH IS ON)
                    AnimatedVisibility(
                        visible = overallEnabled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceColor),
                            border = BorderStroke(1.2.dp, cardBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Alert Categories",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = textPrimary,
                                    modifier = Modifier.padding(bottom = 14.dp)
                                )

                                // Category 1: Mock Tests
                                NotificationToggleItem(
                                    icon = Icons.Default.Quiz,
                                    iconTint = Color(0xFFE11D48),
                                    title = "Mock Tests & Live Exams",
                                    description = "Grand Mock Test live alerts, results, performance leaderboards & quiz series reminders.",
                                    checked = mockTestsEnabled,
                                    onCheckedChange = { mockTestsEnabled = it },
                                    isDark = isDark,
                                    testTag = "mock_test_alert_toggle"
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = cardBorderColor)

                                // Category 2: Study Materials
                                NotificationToggleItem(
                                    icon = Icons.Default.Book,
                                    iconTint = Color(0xFF0F766E),
                                    title = "Study Materials & Notes",
                                    description = "New Current Affairs PDFs, revision modules, subject notes, and material update notifications.",
                                    checked = studyMaterialsEnabled,
                                    onCheckedChange = { studyMaterialsEnabled = it },
                                    isDark = isDark,
                                    testTag = "study_materials_alert_toggle"
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = cardBorderColor)

                                // Category 3: Exam Alerts
                                NotificationToggleItem(
                                    icon = Icons.Default.EventNote,
                                    iconTint = Color(0xFFD97706),
                                    title = "Exam & Job Announcements",
                                    description = "Important APPSC notification release dates, hall tickets, and syllabus alteration alerts.",
                                    checked = examAlertsEnabled,
                                    onCheckedChange = { examAlertsEnabled = it },
                                    isDark = isDark,
                                    testTag = "exam_announcements_alert_toggle"
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = cardBorderColor)

                                // Category 4: System Updates
                                NotificationToggleItem(
                                    icon = Icons.Default.SettingsSuggest,
                                    iconTint = Color(0xFF4F46E5),
                                    title = "System & Support Alerts",
                                    description = "Critical server updates, app feature announcements, and account security notifications.",
                                    checked = systemUpdatesEnabled,
                                    onCheckedChange = { systemUpdatesEnabled = it },
                                    isDark = isDark,
                                    testTag = "system_support_alert_toggle"
                                )
                            }
                        }
                    }

                    // 4. PREFERENCES (SOUND/VIBRATION) (ENABLED ONLY IF MASTER IS ON)
                    AnimatedVisibility(
                        visible = overallEnabled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceColor),
                            border = BorderStroke(1.2.dp, cardBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Behaviors & Sounds",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = textPrimary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { soundEnabled = !soundEnabled }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                            contentDescription = null,
                                            tint = textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text("Play Ringtone Sound", fontSize = 13.5.sp, color = textPrimary)
                                    }
                                    Checkbox(
                                        checked = soundEnabled,
                                        onCheckedChange = { soundEnabled = it }
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { vibrationEnabled = !vibrationEnabled }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Vibration,
                                            contentDescription = null,
                                            tint = textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text("Device Vibration Feedback", fontSize = 13.5.sp, color = textPrimary)
                                    }
                                    Checkbox(
                                        checked = vibrationEnabled,
                                        onCheckedChange = { vibrationEnabled = it }
                                    )
                                }
                            }
                        }
                    }

                    // 5. DAILY PRACTICE PRACTICE REMINDERS CARD
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        border = BorderStroke(1.2.dp, cardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Daily Practice Reminders",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Keep up with your study routines",
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                }

                                Switch(
                                    checked = dailyPracticeEnabled,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        dailyPracticeEnabled = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = primaryColor,
                                        checkedTrackColor = primaryColor.copy(alpha = 0.4f)
                                    )
                                )
                            }

                            AnimatedVisibility(
                                visible = dailyPracticeEnabled,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 14.dp)) {
                                    HorizontalDivider(color = cardBorderColor, modifier = Modifier.padding(bottom = 12.dp))

                                    // Target Daily Question Count Slider
                                    Text(
                                        text = "Daily MCQ Goal: $dailyTargetCount Questions",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = textPrimary
                                    )
                                    
                                    Slider(
                                        value = dailyTargetCount.toFloat(),
                                        onValueChange = { dailyTargetCount = it.toInt() },
                                        valueRange = 5f..100f,
                                        steps = 19,
                                        colors = SliderDefaults.colors(
                                            thumbColor = primaryColor,
                                            activeTrackColor = primaryColor,
                                            inactiveTrackColor = cardBorderColor
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Time Picker Selection Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Reminder Alarm Time",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = textPrimary
                                            )
                                            Text(
                                                text = "Alert fires at: ${NotificationSettingsManager.formatTime(reminderHour, reminderMinute)}",
                                                fontSize = 11.5.sp,
                                                color = textSecondary
                                            )
                                        }

                                        Button(
                                            onClick = { showTimePickerDialog = true },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Alarm,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Set Time", fontSize = 12.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. ACTION SAVE BUTTON
                    Button(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                try {
                                    // 1. Save preferences locally
                                    NotificationSettingsManager.saveNotificationsEnabled(context, overallEnabled)
                                    NotificationSettingsManager.saveNotificationSoundEnabled(context, soundEnabled)
                                    NotificationSettingsManager.saveNotificationVibrationEnabled(context, vibrationEnabled)
                                    NotificationSettingsManager.saveStudyMaterialsEnabled(context, studyMaterialsEnabled)
                                    NotificationSettingsManager.saveMockTestsEnabled(context, mockTestsEnabled)
                                    NotificationSettingsManager.saveExamAlertsEnabled(context, examAlertsEnabled)
                                    NotificationSettingsManager.saveSystemUpdatesEnabled(context, systemUpdatesEnabled)

                                    NotificationSettingsManager.saveDailyPracticeReminderEnabled(context, dailyPracticeEnabled)
                                    NotificationSettingsManager.saveDailyQuestionTarget(context, dailyTargetCount)
                                    NotificationSettingsManager.saveReminderTime(context, reminderHour, reminderMinute)

                                    // Update locally stored SampleData profile
                                    SampleData.userProfileState.value = SampleData.userProfileState.value.copy(
                                        dailyGoalQuestions = dailyTargetCount
                                    )

                                    // 2. Upload/Sync preferences to Firebase Firestore
                                    val userEmail = SampleData.userProfileState.value.email.ifBlank { "sobhadhanaraju0@gmail.com" }
                                    
                                    val prefsMap = mapOf(
                                        "notificationsEnabled" to overallEnabled,
                                        "soundEnabled" to soundEnabled,
                                        "vibrationEnabled" to vibrationEnabled,
                                        "studyMaterialsEnabled" to studyMaterialsEnabled,
                                        "mockTestsEnabled" to mockTestsEnabled,
                                        "examAlertsEnabled" to examAlertsEnabled,
                                        "systemUpdatesEnabled" to systemUpdatesEnabled,
                                        "dailyPracticeReminderEnabled" to dailyPracticeEnabled,
                                        "dailyQuestionTarget" to dailyTargetCount,
                                        "reminderTime" to String.format("%02d:%02d", reminderHour, reminderMinute),
                                        "updatedAt" to System.currentTimeMillis()
                                    )

                                    com.example.util.FirebaseSyncManager.firestore.collection("users")
                                        .document(userEmail)
                                        .update("notificationPreferences", prefsMap)
                                        .addOnCompleteListener { task ->
                                            isSaving = false
                                            if (task.isSuccessful) {
                                                Toast.makeText(context, "Preferences synced with Cloud!", Toast.LENGTH_SHORT).show()
                                                onBack()
                                            } else {
                                                // Fallback to setting values if update failed because document doesn't exist
                                                com.example.util.FirebaseSyncManager.firestore.collection("users")
                                                    .document(userEmail)
                                                    .set(mapOf("notificationPreferences" to prefsMap), com.google.firebase.firestore.SetOptions.merge())
                                                    .addOnSuccessListener {
                                                        Toast.makeText(context, "Preferences synced with Cloud!", Toast.LENGTH_SHORT).show()
                                                        onBack()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Toast.makeText(context, "Saved locally! Cloud sync failed: ${e.message}", Toast.LENGTH_LONG).show()
                                                        onBack()
                                                    }
                                            }
                                        }

                                } catch (e: Exception) {
                                    isSaving = false
                                    Toast.makeText(context, "Preferences saved locally!", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("notification_settings_save_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = if (isDark) Color.Black else Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isDark) Color.Black else Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Save & Cloud Sync Settings",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.Black else Color.White
                            )
                        }
                    }

                    // 7. REPORT ISSUE & SUPPORT CARD
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color.White,
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showReportIssueDialog = true }
                                .padding(16.dp)
                                .testTag("settings_report_issue_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (isDark) Color(0xFFEF4444).copy(alpha = 0.22f) else Color(0xFFFEE2E2),
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BugReport,
                                        contentDescription = "Report Issue",
                                        tint = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Report Issue",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Found a bug or glitch? Send feedback to our team",
                                        fontSize = 11.5.sp,
                                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Report Issue Dialog
            if (showReportIssueDialog) {
                ReportIssueDialog(
                    onDismiss = { showReportIssueDialog = false }
                )
            }

            // Custom Time Picker Dialog
            if (showTimePickerDialog) {
                AlertDialog(
                    onDismissRequest = { showTimePickerDialog = false },
                    title = { Text("Choose Practice Hour") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Set hour (24-hour format): $reminderHour", fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = reminderHour.toFloat(),
                                onValueChange = { reminderHour = it.toInt() },
                                valueRange = 0f..23f,
                                steps = 23,
                                colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor)
                            )

                            Text("Set minute: $reminderMinute", fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = reminderMinute.toFloat(),
                                onValueChange = { reminderMinute = it.toInt() },
                                valueRange = 0f..59f,
                                steps = 59,
                                colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showTimePickerDialog = false }) {
                            Text("Apply", color = primaryColor, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDark: Boolean,
    testTag: String
) {
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textPrimary
                )
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        val haptic = LocalHapticFeedback.current
        Switch(
            checked = checked,
            onCheckedChange = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange(it)
            },
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = iconTint,
                checkedTrackColor = iconTint.copy(alpha = 0.35f)
            )
        )
    }
}
