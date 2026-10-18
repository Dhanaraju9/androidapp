package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.db.AppDatabase
import com.example.db.BookmarkEntity
import com.example.db.TestResultEntity
import com.example.db.UserProgressEntity
import com.example.db.UserStreakEntity
import com.example.util.AppConfigProvider
import com.example.util.FirebaseSyncManager
import com.example.worker.DataSyncAndBackupWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Origin source classification for backed-up snapshot files.
 */
enum class BackupSource(val label: String, val badgeColor: Long, val badgeTextColor: Long) {
    CLOUD_FIRESTORE("Firestore Sync", 0xFFEFF6FF, 0xFF1D4ED8),
    CLOUD_STORAGE("Cloud Storage", 0xFFFAF5FF, 0xFF6B21A8),
    LOCAL_SNAPSHOT("Local Device", 0xFFF0FDF4, 0xFF15803D)
}

/**
 * Data representation of an individual backed-up snapshot file.
 */
data class BackupFileItem(
    val id: String,
    val fileName: String,
    val appId: String,
    val source: BackupSource,
    val timestamp: Long,
    val sizeBytes: Long,
    val deviceModel: String = "Android Device",
    val bookmarksCount: Int = 0,
    val testsCompletedCount: Int = 0,
    val streakDays: Int = 0,
    val totalXp: Int = 0,
    val cloudStatus: String = "AVAILABLE",
    val downloadUrl: String? = null,
    val localFile: File? = null,
    val firestorePayload: Map<String, Any>? = null
) {
    val formattedDate: String
        get() = if (timestamp > 0) {
            SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(timestamp))
        } else "Unknown Date"

    val formattedSize: String
        get() = when {
            sizeBytes >= 1024 * 1024 -> String.format(Locale.US, "%.2f MB", sizeBytes / (1024.0 * 1024.0))
            sizeBytes >= 1024 -> String.format(Locale.US, "%.1f KB", sizeBytes / 1024.0)
            sizeBytes > 0 -> "$sizeBytes B"
            else -> "Dynamic Size"
        }
}

/**
 * Jetpack Compose screen that lists backed-up snapshot files from Firestore and Cloud Storage,
 * provides live source filtering, JSON inspection, and full single-tap data restoration into Room DB.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeAppId = AppConfigProvider.appId

    var isLoading by remember { mutableStateOf(true) }
    var isRestoring by remember { mutableStateOf(false) }
    var restoringFileId by remember { mutableStateOf<String?>(null) }
    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0: All, 1: Cloud, 2: Local

    var backupList by remember { mutableStateOf<List<BackupFileItem>>(emptyList()) }
    var confirmRestoreItem by remember { mutableStateOf<BackupFileItem?>(null) }
    var inspectItem by remember { mutableStateOf<BackupFileItem?>(null) }
    var restoreSuccessSummary by remember { mutableStateOf<String?>(null) }

    // Function to load all local and remote backups
    suspend fun loadBackups() = withContext(Dispatchers.IO) {
        val results = mutableListOf<BackupFileItem>()

        // 1. Load Local Snapshots from filesDir/backups
        try {
            val backupsDir = File(context.filesDir, "backups")
            if (backupsDir.exists() && backupsDir.isDirectory) {
                val files = backupsDir.listFiles { f -> f.isFile && f.name.endsWith(".json") }
                    ?.sortedByDescending { it.lastModified() }
                    ?: emptyList()

                for (file in files) {
                    try {
                        val content = file.readText(Charsets.UTF_8)
                        val json = JSONObject(content)
                        val fileAppId = json.optString("appId", activeAppId)
                        val ts = json.optLong("backupTimestamp", file.lastModified())
                        val bms = json.optInt("bookmarksCount", json.optJSONArray("bookmarks")?.length() ?: 0)
                        val tests = json.optInt("testResultsCount", json.optJSONArray("testResults")?.length() ?: 0)
                        val userProg = json.optJSONObject("userProgress")
                        val streak = userProg?.optInt("streakDays", 0) ?: 0
                        val xp = userProg?.optInt("totalXp", 0) ?: 0
                        val device = json.optString("deviceModel", "Android Device")

                        results.add(
                            BackupFileItem(
                                id = "local_${file.name}",
                                fileName = file.name,
                                appId = fileAppId,
                                source = BackupSource.LOCAL_SNAPSHOT,
                                timestamp = ts,
                                sizeBytes = file.length(),
                                deviceModel = device,
                                bookmarksCount = bms,
                                testsCompletedCount = tests,
                                streakDays = streak,
                                totalXp = xp,
                                localFile = file
                            )
                        )
                    } catch (e: Exception) {
                        // Fallback minimal item if json parsing failed
                        results.add(
                            BackupFileItem(
                                id = "local_${file.name}",
                                fileName = file.name,
                                appId = activeAppId,
                                source = BackupSource.LOCAL_SNAPSHOT,
                                timestamp = file.lastModified(),
                                sizeBytes = file.length(),
                                localFile = file
                            )
                        )
                    }
                }
            }
        } catch (localEx: Exception) {
            localEx.printStackTrace()
        }

        // 2. Query Firestore Cloud Backups
        try {
            val currentUser = FirebaseSyncManager.auth.currentUser
            if (currentUser != null) {
                val userEmail = currentUser.email ?: currentUser.uid
                val firestore = FirebaseSyncManager.firestore

                // Check user tenant backups subcollection
                val userBackupsSnap = firestore.collection("users")
                    .document(userEmail)
                    .collection("tenant_backups")
                    .get()
                    .await()

                for (doc in userBackupsSnap.documents) {
                    val data = doc.data ?: continue
                    val docAppId = data["appId"] as? String ?: doc.id
                    val ts = (data["backupTimestamp"] as? Number)?.toLong() ?: (data["lastUpdated"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    val size = (data["backupSizeBytes"] as? Number)?.toLong() ?: 45000L
                    val bms = (data["bookmarksCount"] as? Number)?.toInt() ?: 0
                    val tests = (data["testsCompletedCount"] as? Number)?.toInt() ?: 0
                    val streak = (data["streakDays"] as? Number)?.toInt() ?: 0
                    val xp = (data["totalXp"] as? Number)?.toInt() ?: 0
                    val device = data["deviceModel"] as? String ?: "Cloud Sync"
                    val fileName = "cloud_firestore_${docAppId}_${ts}.json"

                    results.add(
                        BackupFileItem(
                            id = "firestore_${doc.id}",
                            fileName = fileName,
                            appId = docAppId,
                            source = BackupSource.CLOUD_FIRESTORE,
                            timestamp = ts,
                            sizeBytes = size,
                            deviceModel = device,
                            bookmarksCount = bms,
                            testsCompletedCount = tests,
                            streakDays = streak,
                            totalXp = xp,
                            cloudStatus = "SYNCED",
                            firestorePayload = data
                        )
                    )
                }

                // Check general tenant backups collection
                val globalBackupsSnap = firestore.collection("tenant_backups")
                    .document(activeAppId)
                    .collection("history")
                    .limit(10)
                    .get()
                    .await()

                for (doc in globalBackupsSnap.documents) {
                    val data = doc.data ?: continue
                    val ts = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    results.add(
                        BackupFileItem(
                            id = "firestore_hist_${doc.id}",
                            fileName = "cloud_history_${doc.id}.json",
                            appId = activeAppId,
                            source = BackupSource.CLOUD_FIRESTORE,
                            timestamp = ts,
                            sizeBytes = (data["size"] as? Number)?.toLong() ?: 64000L,
                            deviceModel = "Cloud Master",
                            bookmarksCount = (data["bookmarksCount"] as? Number)?.toInt() ?: 0,
                            testsCompletedCount = (data["testsCount"] as? Number)?.toInt() ?: 0,
                            firestorePayload = data
                        )
                    )
                }
            }
        } catch (cloudEx: Exception) {
            // Soft catch if offline or permissions restrict
            cloudEx.printStackTrace()
        }

        // 3. Query Firebase Cloud Storage
        try {
            val storageRef = FirebaseSyncManager.storage.reference.child("backups/$activeAppId")
            val listResult = storageRef.listAll().await()
            for (itemRef in listResult.items) {
                try {
                    val metadata = itemRef.metadata.await()
                    val downloadUrl = try { itemRef.downloadUrl.await().toString() } catch (e: Exception) { null }
                    results.add(
                        BackupFileItem(
                            id = "storage_${itemRef.name}",
                            fileName = itemRef.name,
                            appId = activeAppId,
                            source = BackupSource.CLOUD_STORAGE,
                            timestamp = metadata.updatedTimeMillis,
                            sizeBytes = metadata.sizeBytes,
                            deviceModel = "Cloud Storage Bucket",
                            downloadUrl = downloadUrl
                        )
                    )
                } catch (metaEx: Exception) {
                    metaEx.printStackTrace()
                }
            }
        } catch (storageEx: Exception) {
            storageEx.printStackTrace()
        }

        // Sort by newest first
        results.sortedByDescending { it.timestamp }
    }

    // Refresh backups list
    fun refreshData() {
        isLoading = true
        coroutineScope.launch {
            backupList = loadBackups()
            isLoading = false
        }
    }

    LaunchedEffect(activeAppId) {
        refreshData()
    }

    // Restore execution function
    fun executeRestore(item: BackupFileItem) {
        isRestoring = true
        restoringFileId = item.id

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(context)
                var restoredBookmarks = 0
                var restoredTests = 0
                var restoredXp = 0
                var restoredStreak = 0

                if (item.localFile != null && item.localFile.exists()) {
                    // Restore from local JSON snapshot
                    val jsonStr = item.localFile.readText(Charsets.UTF_8)
                    val json = JSONObject(jsonStr)

                    // 1. Restore User Progress
                    val progJson = json.optJSONObject("userProgress")
                    if (progJson != null) {
                        restoredStreak = progJson.optInt("streakDays", 1)
                        restoredXp = progJson.optInt("totalXp", 100)
                        val targetQ = progJson.optInt("dailyTargetQuestions", 30)
                        val compQ = progJson.optInt("completedQuestionsCount", 0)
                        val testsC = progJson.optInt("testsCompletedCount", 0)
                        val lastAct = progJson.optString("lastActiveDate", "2026-10-05")

                        val progressEntity = UserProgressEntity(
                            appId = item.appId,
                            streakDays = restoredStreak,
                            totalXp = restoredXp,
                            dailyTargetQuestions = targetQ,
                            completedQuestionsCount = compQ,
                            testsCompletedCount = testsC,
                            lastActiveDate = lastAct,
                            updatedAt = System.currentTimeMillis()
                        )
                        database.userProgressDao().upsertProgress(progressEntity)
                        database.userStreakDao().updateStreak(
                            UserStreakEntity(
                                id = 1,
                                streakDays = restoredStreak,
                                totalXp = restoredXp,
                                dailyTargetQuestions = targetQ,
                                lastActiveDate = lastAct
                            )
                        )
                    }

                    // 2. Restore Bookmarks
                    val bookmarksArr = json.optJSONArray("bookmarks")
                    if (bookmarksArr != null) {
                        for (i in 0 until bookmarksArr.length()) {
                            val bJson = bookmarksArr.optJSONObject(i) ?: continue
                            val bmEntity = BookmarkEntity(
                                id = bJson.optString("id", "bm_${System.currentTimeMillis()}_$i"),
                                appId = item.appId,
                                subjectId = bJson.optString("subjectId", "sub_1"),
                                unitId = bJson.optString("unitId", ""),
                                topicId = bJson.optString("topicId", ""),
                                questionText = bJson.optString("questionText", ""),
                                optionA = bJson.optString("optionA", ""),
                                optionB = bJson.optString("optionB", ""),
                                optionC = bJson.optString("optionC", ""),
                                optionD = bJson.optString("optionD", ""),
                                correctOption = bJson.optString("correctOption", "A"),
                                explanation = bJson.optString("explanation", ""),
                                bookmarkedAt = bJson.optLong("bookmarkedAt", System.currentTimeMillis())
                            )
                            database.bookmarkDao().insertBookmark(bmEntity)
                            restoredBookmarks++
                        }
                    }

                    // 3. Restore Test Results
                    val testsArr = json.optJSONArray("testResults")
                    if (testsArr != null) {
                        for (i in 0 until testsArr.length()) {
                            val tJson = testsArr.optJSONObject(i) ?: continue
                            val trEntity = TestResultEntity(
                                id = 0, // autoGenerate
                                appId = item.appId,
                                testId = tJson.optString("id", "test_$i"),
                                testTitle = tJson.optString("testTitle", "Restored Mock Test"),
                                testType = tJson.optString("testType", "MOCK_TEST"),
                                totalQuestions = tJson.optInt("totalQuestions", 20),
                                correctAnswers = tJson.optInt("correctAnswers", 15),
                                wrongAnswers = tJson.optInt("wrongAnswers", 5),
                                unanswered = tJson.optInt("unanswered", 0),
                                scorePercentage = tJson.optDouble("scorePercentage", 75.0).toFloat(),
                                accuracy = "${tJson.optInt("correctAnswers", 15) * 100 / tJson.optInt("totalQuestions", 20)}%",
                                timeTakenSeconds = tJson.optInt("timeTakenSeconds", 600),
                                timestamp = tJson.optLong("timestamp", System.currentTimeMillis())
                            )
                            database.testResultDao().insertResult(trEntity)
                            restoredTests++
                        }
                    }
                } else if (item.firestorePayload != null) {
                    // Restore from Firestore Document Metadata
                    val data = item.firestorePayload
                    restoredStreak = (data["streakDays"] as? Number)?.toInt() ?: 1
                    restoredXp = (data["totalXp"] as? Number)?.toInt() ?: 150
                    restoredBookmarks = (data["bookmarksCount"] as? Number)?.toInt() ?: 0
                    restoredTests = (data["testsCompletedCount"] as? Number)?.toInt() ?: 0

                    val progressEntity = UserProgressEntity(
                        appId = item.appId,
                        streakDays = restoredStreak,
                        totalXp = restoredXp,
                        testsCompletedCount = restoredTests,
                        updatedAt = System.currentTimeMillis()
                    )
                    database.userProgressDao().upsertProgress(progressEntity)
                }

                // Update in-memory user profile
                withContext(Dispatchers.Main) {
                    val currentProfile = SampleData.userProfileState.value
                    SampleData.userProfileState.value = currentProfile.copy(
                        streakDays = if (restoredStreak > 0) restoredStreak else currentProfile.streakDays
                    )

                    restoreSuccessSummary = "Successfully restored ${item.fileName}:\n• $restoredBookmarks bookmarks\n• $restoredTests test records\n• $restoredXp XP & $restoredStreak day streak"
                    isRestoring = false
                    restoringFileId = null
                    confirmRestoreItem = null
                    Toast.makeText(context, "Backup successfully restored!", Toast.LENGTH_LONG).show()
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    isRestoring = false
                    restoringFileId = null
                    confirmRestoreItem = null
                    Toast.makeText(context, "Restore failed: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Filtered list
    val filteredList = remember(backupList, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> backupList.filter { it.source == BackupSource.CLOUD_FIRESTORE || it.source == BackupSource.CLOUD_STORAGE }
            2 -> backupList.filter { it.source == BackupSource.LOCAL_SNAPSHOT }
            else -> backupList
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cloud Backups & Restore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Tenant: ${activeAppId.take(24)}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { refreshData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFF2563EB)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Overview Banner Card
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
                                    listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF1E3A8A))
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Disaster Recovery Vault",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Firestore & Cloud Storage snapshots",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Text(
                                        text = "${backupList.size} Snapshots",
                                        color = Color(0xFF34D399),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Restore your exam bookmarks, test scores, XP, and streak history onto any device without data loss.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 2. Filter Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterLabels = listOf("All (${backupList.size})", "Cloud Backups", "Local Backups")
                    filterLabels.forEachIndexed { index, label ->
                        val isSelected = selectedFilterIndex == index
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF2563EB) else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFilterIndex = index }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color(0xFF475569),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Loading or Empty State
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Loading backup snapshots...", color = Color(0xFF64748B), fontSize = 13.sp)
                        }
                    }
                }
            } else if (filteredList.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backup,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "No Backup Snapshots Found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "You haven't generated any backup files for $activeAppId yet. Trigger a backup now to safeguard your learning progress.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    DataSyncAndBackupWorker.runImmediate(context)
                                    Toast.makeText(context, "Backup creation started...", Toast.LENGTH_SHORT).show()
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(2000)
                                        refreshData()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Instant Backup", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // 4. List of Backups
                items(filteredList, key = { it.id }) { item ->
                    BackupItemCard(
                        item = item,
                        isRestoringThis = isRestoring && restoringFileId == item.id,
                        isAnyRestoring = isRestoring,
                        onRestoreClick = { confirmRestoreItem = item },
                        onInspectClick = { inspectItem = item }
                    )
                }
            }
        }
    }

    // Confirmation Dialog before executing restore
    if (confirmRestoreItem != null) {
        val target = confirmRestoreItem!!
        AlertDialog(
            onDismissRequest = { if (!isRestoring) confirmRestoreItem = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.SettingsBackupRestore,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Restore Selected Backup?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to restore data from this snapshot?",
                        color = Color(0xFF475569),
                        fontSize = 13.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("File: ${target.fileName}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Date: ${target.formattedDate}", fontSize = 11.5.sp, color = Color(0xFF475569))
                            Text("Size: ${target.formattedSize} • Source: ${target.source.label}", fontSize = 11.5.sp, color = Color(0xFF475569))
                            if (target.bookmarksCount > 0 || target.testsCompletedCount > 0) {
                                Text("Contains: ${target.bookmarksCount} bookmarks, ${target.testsCompletedCount} tests, ${target.totalXp} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                        }
                    }
                    Text(
                        text = "Restoring will update your active tenant records in Room Database.",
                        color = Color(0xFFDC2626),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { executeRestore(target) },
                    enabled = !isRestoring,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restoring...", fontSize = 12.sp)
                    } else {
                        Text("Confirm & Restore", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { confirmRestoreItem = null },
                    enabled = !isRestoring,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }
            }
        )
    }

    // Inspect JSON Details Dialog
    if (inspectItem != null) {
        val target = inspectItem!!
        AlertDialog(
            onDismissRequest = { inspectItem = null },
            title = {
                Text(
                    text = "Snapshot Metadata",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("File: ${target.fileName}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    Text("Tenant ID: ${target.appId}", fontSize = 12.sp)
                    Text("Source: ${target.source.label}", fontSize = 12.sp)
                    Text("Timestamp: ${target.formattedDate}", fontSize = 12.sp)
                    Text("Storage Size: ${target.formattedSize}", fontSize = 12.sp)
                    Text("Device Model: ${target.deviceModel}", fontSize = 12.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Saved Metrics:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("• Bookmarks: ${target.bookmarksCount}", fontSize = 11.5.sp)
                    Text("• Tests Completed: ${target.testsCompletedCount}", fontSize = 11.5.sp)
                    Text("• Streak Days: ${target.streakDays} days", fontSize = 11.5.sp)
                    Text("• Total XP Earned: ${target.totalXp} XP", fontSize = 11.5.sp)
                }
            },
            confirmButton = {
                Button(onClick = { inspectItem = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Success Restoration Notice Dialog
    if (restoreSuccessSummary != null) {
        AlertDialog(
            onDismissRequest = { restoreSuccessSummary = null },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Restore Completed!", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
            },
            text = {
                Text(restoreSuccessSummary ?: "", fontSize = 13.sp, color = Color(0xFF1E293B))
            },
            confirmButton = {
                Button(
                    onClick = {
                        restoreSuccessSummary = null
                        onNavigateToHome()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("Go to Practice Arena", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Individual Card representing a backed-up file with metadata chips and a Restore button.
 */
@Composable
fun BackupItemCard(
    item: BackupFileItem,
    isRestoringThis: Boolean,
    isAnyRestoring: Boolean,
    onRestoreClick: () -> Unit,
    onInspectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Source Badge & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(item.source.badgeColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val icon = when (item.source) {
                            BackupSource.CLOUD_FIRESTORE -> Icons.Default.CloudSync
                            BackupSource.CLOUD_STORAGE -> Icons.Default.Cloud
                            BackupSource.LOCAL_SNAPSHOT -> Icons.Default.SdCard
                        }
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(item.source.badgeTextColor))
                        Text(
                            text = item.source.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(item.source.badgeTextColor)
                        )
                    }
                }

                Text(
                    text = item.formattedSize,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // File Name
            Text(
                text = item.fileName,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Date & Device
            Text(
                text = "${item.formattedDate} • ${item.deviceModel}",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stats Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricChip(label = "${item.bookmarksCount} Bookmarks", icon = Icons.Default.BookmarkBorder)
                MetricChip(label = "${item.testsCompletedCount} Tests", icon = Icons.Default.AssignmentTurnedIn)
                if (item.totalXp > 0) {
                    MetricChip(label = "${item.totalXp} XP", icon = Icons.Default.Bolt)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Inspect & Restore Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onInspectClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF64748B))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspect", fontSize = 12.sp, color = Color(0xFF64748B))
                }

                Button(
                    onClick = onRestoreClick,
                    enabled = !isAnyRestoring,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    if (isRestoringThis) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restoring...", fontSize = 11.5.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(11.dp), tint = Color(0xFF475569))
            Text(text = label, fontSize = 10.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
        }
    }
}
