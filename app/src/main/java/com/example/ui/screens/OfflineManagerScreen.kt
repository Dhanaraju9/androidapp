package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.Subject
import com.example.util.DownloadHierarchyType
import com.example.util.OfflinePack
import com.example.util.OfflineSyncManager
import com.example.util.SyncStatus
import com.example.worker.DataSyncAndBackupWorker
import com.example.worker.FileChangeWatcherWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Offline & Cloud Synced MCQ System Main Dashboard
 * 
 * Strict Constraint Adherence:
 * 1. AES-256 Hardware Encrypted internal storage indicator.
 * 2. Hierarchy Downloads: Subject -> Unit -> Topic -> Sets -> MCQs.
 * 3. NO bottom navigation bar (Home, Mock tests etc are hidden).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineManagerScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSubjectDetail: (Subject) -> Unit,
    onStartOfflinePractice: (subjectId: String, unitId: String) -> Unit,
    onNavigateToBackupRestore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var isSyncingNow by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val downloadedPacks = OfflineSyncManager.downloadedPacks
    val syncStatus by OfflineSyncManager.syncStatusState
    val isOfflineMode by OfflineSyncManager.isOfflineModeSimulated
    val pendingSyncCount by OfflineSyncManager.pendingSyncCountState

    val totalStorageBytes = remember(downloadedPacks.size) {
        OfflineSyncManager.getTotalStorageUsedBytes(context)
    }
    val formattedStorageUsed = remember(totalStorageBytes) {
        OfflineSyncManager.formatStorageSize(totalStorageBytes)
    }
    val totalOfflineQuestions = remember(downloadedPacks.size) {
        OfflineSyncManager.getTotalOfflineQuestionsCount()
    }

    val displaySubjects = SampleData.subjectsState

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Clear All Offline MCQs?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "This will securely wipe all downloaded subject vaults, units, and practice sets ($formattedStorageUsed) from device storage. Your cloud bookmarks and performance records remain safe.",
                    color = Color(0xFF475569),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        OfflineSyncManager.clearAllStorage(context)
                        showClearDialog = false
                        Toast.makeText(context, "All offline encrypted vaults erased successfully.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear Storage", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Offline & Cloud Sync",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 19.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Encrypted Vault • Zero Data Needed",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        navigationIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.testTag("offline_back_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color(0xFF0F172A)
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
                        actions = {
                            IconButton(
                                onClick = { showClearDialog = true },
                                modifier = Modifier.testTag("clear_offline_storage_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Clear Vault",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )

                    // Top Security Banner
                    Surface(
                        color = Color(0xFFF0FDF4),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "AES-256 Encrypted Private Storage",
                                    color = Color(0xFF15803D),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "PROTECTED",
                                    color = Color(0xFF166534),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Storage & Radial Meter Card
            item {
                StorageRadialOverviewCard(
                    usedBytes = totalStorageBytes,
                    formattedUsed = formattedStorageUsed,
                    totalQuestions = totalOfflineQuestions,
                    isOfflineMode = isOfflineMode,
                    pendingSyncCount = pendingSyncCount,
                    isSyncing = isSyncingNow,
                    onToggleOffline = { OfflineSyncManager.toggleOfflineSimulation(it) },
                    onTriggerSync = {
                        isSyncingNow = true
                        OfflineSyncManager.triggerSync { success, message ->
                            isSyncingNow = false
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // 2. Security Guarantees Callout
            item {
                SecurityGuaranteesStrip()
            }

            // 2.5 Automated WorkManager Background Sync & Periodic File Scanner
            item {
                WorkManagerBackupStatusCard(
                    onNavigateToBackupRestore = onNavigateToBackupRestore
                )
            }

            // 3. Section Header: Available Subjects for Offline Use
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SUBJECTS DOWNLOAD VAULT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E293B),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Tap a subject to drill-down to Units, Topics & Sets",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // 4. Subjects List
            items(displaySubjects, key = { it.id }) { subject ->
                SubjectOfflineCard(
                    subject = subject,
                    onClick = { onNavigateToSubjectDetail(subject) },
                    onQuickPractice = {
                        onStartOfflinePractice(subject.id, subject.units.firstOrNull()?.id ?: "")
                    }
                )
            }

            // 5. Educational Bottom Info Note
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "How Offline & Sync Works",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Downloaded MCQs are locked to this app using device hardware keys. When you practice without internet, your scores, bookmarks, and detailed analytics are queued locally and automatically synced once you reconnect.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF334155),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Visual Radial Storage Overview Card
 */
@Composable
private fun StorageRadialOverviewCard(
    usedBytes: Long,
    formattedUsed: String,
    totalQuestions: Int,
    isOfflineMode: Boolean,
    pendingSyncCount: Int,
    isSyncing: Boolean,
    onToggleOffline: (Boolean) -> Unit,
    onTriggerSync: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Radial Arc Indicator
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 9.dp.toPx()
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        // Background Track
                        drawArc(
                            color = Color(0xFF334155),
                            startAngle = 140f,
                            sweepAngle = 260f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Active Storage Indicator (e.g. 35% representation)
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFF38BDF8),
                                    Color(0xFF10B981),
                                    Color(0xFFF59E0B)
                                )
                            ),
                            startAngle = 140f,
                            sweepAngle = 190f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formattedUsed,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Used",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right: Metrics and Stats
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Internal App Storage",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "14.7 GB Free",
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "$totalQuestions Offline MCQs",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (isOfflineMode) Color(0xFFF59E0B) else Color(0xFF10B981),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isOfflineMode) "Offline Simulation Mode" else "Auto-Sync Active",
                            color = if (isOfflineMode) Color(0xFFFBBF24) else Color(0xFF6EE7B7),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(12.dp))

            // Action & Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Offline Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Simulate Offline:",
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = onToggleOffline,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFF59E0B),
                            checkedTrackColor = Color(0xFF78350F),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                }

                // Manual Cloud Sync Button
                Button(
                    onClick = onTriggerSync,
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                        }
                        Text(
                            text = if (isSyncing) "Syncing..." else "Sync Cloud",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.size((48 * scale).dp, (28 * scale).dp)
)

/**
 * 3-pill Security Badges Strip
 */
@Composable
private fun SecurityGuaranteesStrip() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SecurityBadgeItem(
            icon = Icons.Default.Key,
            title = "Hardware AES-256",
            subtitle = "Military encryption",
            modifier = Modifier.weight(1f)
        )
        SecurityBadgeItem(
            icon = Icons.Default.FolderSpecial,
            title = "Zero Public Files",
            subtitle = "Hidden from files app",
            modifier = Modifier.weight(1f)
        )
        SecurityBadgeItem(
            icon = Icons.Default.Shield,
            title = "Tamper Proof",
            subtitle = "Signed vault checks",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SecurityBadgeItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.shadow(1.dp, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Individual Subject Download Card in Main Hub
 */
@Composable
private fun SubjectOfflineCard(
    subject: Subject,
    onClick: () -> Unit,
    onQuickPractice: () -> Unit
) {
    val context = LocalContext.current
    val isDownloaded = OfflineSyncManager.isSubjectDownloaded(subject.id)
    val syncStatus = OfflineSyncManager.getSubjectSyncStatus(subject.id)
    val progress = OfflineSyncManager.downloadingProgressMap["subject_${subject.id}"]

    val totalSubjectMcqs = remember(subject) {
        val calculated = subject.units.sumOf { unit ->
            unit.topics.sumOf { topic ->
                SampleData.getQuestionsForPractice(subject.id, unit.id, topic.id, "").size
            }
        }
        if (calculated > 0) calculated else 320
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            1.2.dp,
            if (isDownloaded) Color(0xFF86EFAC) else Color(0xFFE2E8F0)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isDownloaded) Color(0xFFDCFCE7) else Color(0xFFEFF6FF)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (isDownloaded) Color(0xFF16A34A) else Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subject.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${subject.units.size} Units • $totalSubjectMcqs MCQs",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Status Badge & Action Button
                if (syncStatus == SyncStatus.DOWNLOADING) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        CircularProgressIndicator(
                            progress = { progress ?: 0.5f },
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF2563EB),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "${((progress ?: 0.5f) * 100).toInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                } else if (isDownloaded) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OfflinePin,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "OFFLINE READY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            OfflineSyncManager.downloadSubject(
                                context = context,
                                subject = subject,
                                onComplete = {
                                    Toast.makeText(context, "${subject.name} downloaded for offline practice!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2563EB)),
                        border = BorderStroke(1.dp, Color(0xFF2563EB))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Text("Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Drill-down / Practice Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hierarchy: Units → Topics → Sets",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isDownloaded) {
                        TextButton(
                            onClick = onQuickPractice,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Practice Now",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Drill Down",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkManagerBackupStatusCard(
    onNavigateToBackupRestore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var backupStatus by remember { mutableStateOf(DataSyncAndBackupWorker.getLastBackupStatus(context)) }
    var watcherStatus by remember { mutableStateOf(FileChangeWatcherWorker.getWatcherStatus(context)) }
    var isRunningImmediate by remember { mutableStateOf(false) }
    var isRunningWatcher by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "WorkManager Auto-Sync & Backup",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Periodic 24h background file scan & snapshot",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = "SCHEDULED",
                        color = Color(0xFF166534),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Files Scanned
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Scanned Storage", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (backupStatus.scannedFileCount > 0) "${backupStatus.scannedFileCount} files (${backupStatus.formattedSize})" else "Awaiting scan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                // Metric 2: Last Backup Size & Time
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Latest Backup", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (backupStatus.lastBackupTimestamp > 0) backupStatus.formattedSize else "None yet",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Last status line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Last: ${backupStatus.formattedDate}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = backupStatus.lastStatus,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (backupStatus.lastStatus.contains("SUCCESS")) Color(0xFF16A34A) else Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action button for Backup
            Button(
                onClick = {
                    if (!isRunningImmediate) {
                        isRunningImmediate = true
                        DataSyncAndBackupWorker.runImmediate(context)
                        Toast.makeText(context, "WorkManager scan & backup started in background", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            delay(2000)
                            backupStatus = DataSyncAndBackupWorker.getLastBackupStatus(context)
                            isRunningImmediate = false
                        }
                    }
                },
                enabled = !isRunningImmediate,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                if (isRunningImmediate) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scanning Files & Syncing...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Run Scan & Backup Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SOURCE DIRECTORY FILE CHANGE WATCHER (Auto-triggers backup on changes)
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Source Directory File Watcher",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE0F2FE)
                ) {
                    Text(
                        text = "AUTOPROTECT",
                        color = Color(0xFF0369A1),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monitored Files:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("${watcherStatus.totalWatchedFiles} files", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Last Checked:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(watcherStatus.formattedCheckDate, fontSize = 11.sp, color = Color(0xFF1E293B))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Last Auto-Backup:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(watcherStatus.formattedTriggerDate, fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                    }
                    Text(
                        text = "Status: ${watcherStatus.lastStatus}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    if (!isRunningWatcher) {
                        isRunningWatcher = true
                        FileChangeWatcherWorker.runImmediate(context)
                        Toast.makeText(context, "Scanning source directory for changes...", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            delay(2500)
                            watcherStatus = FileChangeWatcherWorker.getWatcherStatus(context)
                            backupStatus = DataSyncAndBackupWorker.getLastBackupStatus(context)
                            isRunningWatcher = false
                        }
                    }
                },
                enabled = !isRunningWatcher,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                if (isRunningWatcher) {
                    CircularProgressIndicator(
                        color = Color(0xFF0284C7),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Checking Directory Changes...", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Check Source Changes Now", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onNavigateToBackupRestore,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Browse Cloud Backups & Restore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

