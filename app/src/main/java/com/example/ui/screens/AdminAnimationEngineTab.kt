package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.util.AnimationConfigManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminAnimationEngineTab(
    selectedAppId: String,
    adminEmail: String = "sobhadhanaraju1@gmail.com"
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Working copy of the config for editing & live preview
    var workingConfig by remember(selectedAppId) {
        mutableStateOf(
            AnimationConfigManager.draftConfigState.value
                ?: AnimationConfigManager.getActiveEngineConfig(selectedAppId)
        )
    }

    val isPreviewMode by AnimationConfigManager.isPreviewModeActive
    val slowMoFactor by AnimationConfigManager.sandboxSlowMoFactor

    fun updateWorkingConfig(newConfig: AnimationEngineConfig) {
        workingConfig = newConfig
        AnimationConfigManager.draftConfigState.value = newConfig
    }

    // Scope selection state ("ALL", "CURRENT", "SCREEN")
    var selectedScopeMode by remember { mutableStateOf(workingConfig.targetScope) }
    var selectedScreenKey by remember { mutableStateOf("Home Screen") }
    val availableScreens = remember {
        listOf(
            "Home Screen",
            "Syllabus Explorer",
            "Topic Sets",
            "Practice Arena",
            "Prelims & Mains",
            "Current Affairs",
            "Mock Tests"
        )
    }

    // Component filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedTarget by remember { mutableStateOf<AnimatableTarget?>(AnimatableTarget.SUBJECT_CARDS) }

    // Sandbox preview state
    var previewTab by remember { mutableIntStateOf(0) } // 0 = Hierarchy/Cards, 1 = Screen Transitions, 2 = MCQ & Feedback
    var previewReplayKey by remember { mutableIntStateOf(1) }
    var sandboxSubjectExpanded by remember { mutableStateOf(true) }
    var sandboxExpandedUnitIndex by remember { mutableIntStateOf(0) }
    var sandboxCardFlipped by remember { mutableStateOf(false) }
    var sandboxScreenPage by remember { mutableIntStateOf(0) }
    var sandboxSelectedOption by remember { mutableStateOf<String?>("B") }
    var sandboxFeedbackTrigger by remember { mutableIntStateOf(1) }

    // Dialog states
    var showRollbackDialog by remember { mutableStateOf(false) }
    var showJsonDialog by remember { mutableStateOf(false) }
    var jsonInputText by remember { mutableStateOf("") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }

    val categories = remember {
        listOf(
            "All",
            "Syllabus Hierarchy",
            "Home & Navigation",
            "Quiz & Exam Arena",
            "Overlays & Menus",
            "Micro-Interactions",
            "System States"
        )
    }

    val filteredTargets = remember(searchQuery, selectedCategory) {
        AnimatableTarget.entries.filter { target ->
            val matchesCategory = selectedCategory == "All" || target.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                target.displayName.contains(searchQuery, ignoreCase = true) ||
                target.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    // Helper to read/write spec for the currently selected scope
    fun getSpecForTarget(target: AnimatableTarget): ComponentAnimationSpec {
        return if (selectedScopeMode == "SCREEN") {
            workingConfig.screenOverrides[selectedScreenKey]?.get(target.name)
                ?: workingConfig.componentSpecs[target.name]
                ?: AnimationPresets.getDefaultSpecForTarget(target)
        } else {
            workingConfig.componentSpecs[target.name]
                ?: AnimationPresets.getDefaultSpecForTarget(target)
        }
    }

    fun updateSpecForTarget(target: AnimatableTarget, updatedSpec: ComponentAnimationSpec) {
        if (selectedScopeMode == "SCREEN") {
            val updatedScreens = workingConfig.screenOverrides.toMutableMap()
            val currentScreenMap = (updatedScreens[selectedScreenKey] ?: emptyMap()).toMutableMap()
            currentScreenMap[target.name] = updatedSpec
            updatedScreens[selectedScreenKey] = currentScreenMap
            updateWorkingConfig(workingConfig.copy(screenOverrides = updatedScreens))
        } else {
            val updatedComponents = workingConfig.componentSpecs.toMutableMap()
            updatedComponents[target.name] = updatedSpec
            updateWorkingConfig(workingConfig.copy(componentSpecs = updatedComponents))
        }
    }

    val master = workingConfig.masterConfig

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // =====================================================================
        // 1. TOP COMMAND BAR: LIVE PREVIEW, SAVE DRAFT, PUBLISH, ROLLBACK, JSON
        // =====================================================================
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0284C7).copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesomeMotion,
                                contentDescription = "Animation Engine",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "PREMIUM ANIMATION ENGINE",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = "v${workingConfig.versionNumber} • ${master.activePresetId.displayName} • 24 Component Targets",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Live Preview Mode Toggle Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isPreviewMode) Color(0xFF059669) else Color(0xFF1E293B),
                        border = BorderStroke(
                            1.dp,
                            if (isPreviewMode) Color(0xFF34D399) else Color(0xFF475569)
                        ),
                        modifier = Modifier
                            .testTag("admin_anim_preview_toggle")
                            .clickable {
                                val next = !isPreviewMode
                                AnimationConfigManager.draftConfigState.value = workingConfig
                                AnimationConfigManager.isPreviewModeActive.value = next
                                AnimationConfigManager.triggerReplay()
                                Toast.makeText(
                                    context,
                                    if (next) "Live Preview ON (Testing Draft Live Across App!)" else "Live Preview OFF (Showing Published Config)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (isPreviewMode) Icons.Default.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = "Live Preview",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isPreviewMode) "LIVE PREVIEW ON" else "PREVIEW OFF",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                // Action Buttons Row 1: Save Draft & Publish Live
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            AnimationConfigManager.saveDraft(context, workingConfig)
                            Toast.makeText(context, "Draft saved locally", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_anim_save_draft_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Draft", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            isPublishing = true
                            val scopeToPublish = if (selectedScopeMode == "SCREEN") "CURRENT" else selectedScopeMode
                            AnimationConfigManager.publishConfig(
                                context = context,
                                config = workingConfig.copy(
                                    updatedBy = adminEmail,
                                    notes = "Published ${master.activePresetId.displayName}"
                                ),
                                scope = scopeToPublish,
                                selectedAppIds = listOf(selectedAppId),
                                onComplete = { _, msg ->
                                    isPublishing = false
                                    workingConfig = AnimationConfigManager.getActiveEngineConfig(selectedAppId)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        enabled = !isPublishing,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .weight(1.25f)
                            .testTag("admin_anim_publish_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPublishing) "Publishing..." else "Publish Live",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Action Buttons Row 2: Rollback, Export/Import JSON, Reset All
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showRollbackDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFBBF24)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Rollback (${AnimationConfigManager.versionHistoryState.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            jsonInputText = AnimationConfigManager.serializeConfigToJson(workingConfig, pretty = true)
                            showJsonDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC084FC)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import / Export", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(0.85f)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }

        // =====================================================================
        // 2. GLOBAL CONTROLS, PERFORMANCE & ACCESSIBILITY SAFEGUARDS
        // =====================================================================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MASTER ANIMATION & SCOPE CONTROLS",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Priority: Component > Screen > App ($selectedAppId) > Global > Default",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = master.masterEnabled,
                        onCheckedChange = { enabled ->
                            updateWorkingConfig(
                                workingConfig.copy(masterConfig = master.copy(masterEnabled = enabled))
                            )
                        },
                        modifier = Modifier.testTag("admin_anim_master_switch")
                    )
                }

                // Scope Selector Pills: All Apps (Global), Current App, or Per-Screen Override
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "Global (All Apps)",
                        "CURRENT" to "App: $selectedAppId",
                        "SCREEN" to "Per-Screen Override"
                    ).forEach { (modeKey, modeLabel) ->
                        val isSelected = selectedScopeMode == modeKey
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF1D4ED8) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedScopeMode = modeKey
                                    val newScope = if (modeKey == "ALL") "ALL" else "CURRENT"
                                    updateWorkingConfig(workingConfig.copy(targetScope = newScope))
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = modeLabel,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // If Screen-Specific mode is active, show screen selector row
                AnimatedVisibility(visible = selectedScopeMode == "SCREEN") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableScreens.forEach { screenName ->
                            val isSelected = selectedScreenKey == screenName
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedScreenKey = screenName },
                                label = {
                                    Text(
                                        text = screenName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Global Speed Multiplier with quick presets
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Global Speed Multiplier",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = String.format(Locale.US, "%.2fx", master.speedMultiplier),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Color(0xFF2563EB)
                        )
                    }
                    Slider(
                        value = master.speedMultiplier,
                        onValueChange = {
                            updateWorkingConfig(
                                workingConfig.copy(masterConfig = master.copy(speedMultiplier = it))
                            )
                        },
                        valueRange = 0.25f..2.5f
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Slow (0.5x)" to 0.5f,
                            "Normal (1.0x)" to 1.0f,
                            "Fast (1.4x)" to 1.4f,
                            "Instant (2.2x)" to 2.2f
                        ).forEach { (label, mult) ->
                            val isChosen = kotlin.math.abs(master.speedMultiplier - mult) < 0.06f
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) Color(0xFFDBEAFE) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        updateWorkingConfig(
                                            workingConfig.copy(masterConfig = master.copy(speedMultiplier = mult))
                                        )
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    }
                }

                // Global Intensity Level & Screen Navigation Transition
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        EnumDropdownSelector(
                            label = "Motion Intensity Level",
                            selectedValue = master.intensityLevel,
                            options = AnimationIntensityLevel.entries,
                            displayLabel = { it.displayName },
                            onSelected = {
                                updateWorkingConfig(
                                    workingConfig.copy(masterConfig = master.copy(intensityLevel = it))
                                )
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        EnumDropdownSelector(
                            label = "Screen Navigation Transition",
                            selectedValue = master.screenTransitionType,
                            options = ScreenTransitionAnimationType.entries,
                            displayLabel = { it.displayName },
                            onSelected = {
                                updateWorkingConfig(
                                    workingConfig.copy(masterConfig = master.copy(screenTransitionType = it))
                                )
                            }
                        )
                    }
                }

                // Accessibility & Performance Safeguard Toggles
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SafeguardChip(
                        label = "Reduced Motion Mode",
                        checked = master.reducedMotionMode,
                        onCheckedChange = {
                            updateWorkingConfig(
                                workingConfig.copy(masterConfig = master.copy(reducedMotionMode = it))
                            )
                        }
                    )
                    SafeguardChip(
                        label = "Respect OS Animator Scale",
                        checked = master.respectSystemAnimatorScale,
                        onCheckedChange = {
                            updateWorkingConfig(
                                workingConfig.copy(masterConfig = master.copy(respectSystemAnimatorScale = it))
                            )
                        }
                    )
                    SafeguardChip(
                        label = "Low-End Device Safe Mode",
                        checked = master.lowEndPerformanceMode,
                        onCheckedChange = {
                            updateWorkingConfig(
                                workingConfig.copy(masterConfig = master.copy(lowEndPerformanceMode = it))
                            )
                        }
                    )
                }
            }
        }

        // =====================================================================
        // 3. 12 READY-MADE PREMIUM ANIMATION PRESETS
        // =====================================================================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "12 READY-MADE ANIMATION PRESETS",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "One-tap curated motion profiles — fully customizable per component below",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEDE9FE)
                    ) {
                        Text(
                            text = master.activePresetId.displayName,
                            color = Color(0xFF6D28D9),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimationPresetId.entries.forEach { preset ->
                        val isSelected = master.activePresetId == preset
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFFF8FAFC)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .width(205.dp)
                                .clickable {
                                    val updated = AnimationConfigManager.applyPreset(workingConfig, preset)
                                    updateWorkingConfig(updated)
                                    AnimationConfigManager.isPreviewModeActive.value = true
                                    AnimationConfigManager.triggerReplay()
                                    previewReplayKey++
                                    Toast.makeText(
                                        context,
                                        "Preset '${preset.displayName}' applied!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.displayName,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.5.sp,
                                        color = if (isSelected) Color.White else Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFFE2E8F0)
                                    ) {
                                        Text(
                                            text = preset.badge,
                                            color = if (isSelected) Color.White else Color(0xFF475569),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = preset.description,
                                    fontSize = 10.5.sp,
                                    color = if (isSelected) Color(0xFFCBD5E1) else Color(0xFF64748B),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 4. INTERACTIVE LIVE PREVIEW SANDBOX
        // =====================================================================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.5.dp, Color(0xFF3B82F6).copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "INTERACTIVE LIVE PREVIEW SANDBOX",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Test Subject -> Unit -> Topic cascades, 3D flips, screen transitions & MCQ feedback",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                val nextSlow = if (slowMoFactor > 0.6f) 0.35f else 1.0f
                                AnimationConfigManager.sandboxSlowMoFactor.value = nextSlow
                                previewReplayKey++
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (slowMoFactor < 0.9f) "Slow-Mo ON" else "1x Speed",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                AnimationConfigManager.draftConfigState.value = workingConfig
                                AnimationConfigManager.isPreviewModeActive.value = true
                                AnimationConfigManager.triggerReplay()
                                previewReplayKey++
                                sandboxFeedbackTrigger++
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Replay", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                }

                // Sandbox Mode Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to "Subject → Unit → Topic",
                        1 to "Screen Transitions",
                        2 to "MCQ & Micro-Motion"
                    ).forEach { (idx, title) ->
                        val active = previewTab == idx
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) Color(0xFF0F172A) else Color.White,
                            border = BorderStroke(1.dp, if (active) Color(0xFF0F172A) else Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    AnimationConfigManager.draftConfigState.value = workingConfig
                                    AnimationConfigManager.isPreviewModeActive.value = true
                                    previewTab = idx
                                    previewReplayKey++
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (active) Color.White else Color(0xFF334155),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Sandbox Content Area
                key(previewReplayKey) {
                    when (previewTab) {
                        0 -> {
                            // Hierarchy & Card Interaction Preview
                            val subjectSpec = getSpecForTarget(AnimatableTarget.SUBJECT_CARDS)
                            val unitSpec = getSpecForTarget(AnimatableTarget.UNIT_CARDS)
                            val topicSpec = getSpecForTarget(AnimatableTarget.TOPIC_CARDS)
                            val chevronRotation = AppMotionSystem.animateAccordionChevronRotation(
                                isExpanded = sandboxSubjectExpanded
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Simulated Subject Card
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .premiumEntranceAndExit(
                                            spec = subjectSpec,
                                            index = 0,
                                            visible = true,
                                            triggerKey = previewReplayKey,
                                            slowMoFactor = slowMoFactor
                                        )
                                        .premiumVisualPolish(subjectSpec.polishEffectType)
                                        .premiumCardInteraction(
                                            target = AnimatableTarget.SUBJECT_CARDS
                                        ) {
                                            if (subjectSpec.interactionType.name.contains("FLIP")) {
                                                sandboxCardFlipped = !sandboxCardFlipped
                                            } else {
                                                sandboxSubjectExpanded = !sandboxSubjectExpanded
                                            }
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                                                )
                                            )
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (sandboxCardFlipped) "SUBJECT BACK-FACE INSIGHTS" else "INDIAN HISTORY & CULTURE",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "Entrance: ${subjectSpec.entranceType.displayName} • Press: ${subjectSpec.interactionType.displayName}",
                                                    color = Color(0xFFBFDBFE),
                                                    fontSize = 10.5.sp
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = Color(0xFFFDE68A),
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .rotate(chevronRotation)
                                                    .clickable { sandboxSubjectExpanded = !sandboxSubjectExpanded }
                                            )
                                        }
                                    }
                                }

                                // Cascading Unit & Topic Cards
                                AnimatedVisibility(
                                    visible = sandboxSubjectExpanded,
                                    enter = AppMotionSystem.cascadeContainerEnterTransition(),
                                    exit = AppMotionSystem.cascadeContainerExitTransition()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(start = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            "Unit 1: Indus Valley & Vedic Period",
                                            "Unit 2: Mauryan & Gupta Empires"
                                        ).forEachIndexed { uIdx, uTitle ->
                                            val isUnitOpen = sandboxExpandedUnitIndex == uIdx
                                            CascadeItem(
                                                index = uIdx,
                                                visible = sandboxSubjectExpanded,
                                                target = AnimatableTarget.UNIT_CARDS
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Card(
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = Color(0xFF0F766E)
                                                        ),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .premiumCardInteraction(
                                                                target = AnimatableTarget.UNIT_CARDS
                                                            ) {
                                                                sandboxExpandedUnitIndex =
                                                                    if (isUnitOpen) -1 else uIdx
                                                            }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = uTitle,
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 12.sp
                                                                )
                                                                Text(
                                                                    text = "Unit Cascade (${unitSpec.staggerDelayMs}ms stagger • ${unitSpec.durationMs}ms)",
                                                                    color = Color(0xFF99F6E4),
                                                                    fontSize = 10.sp
                                                                )
                                                            }
                                                            Icon(
                                                                imageVector = Icons.Default.ExpandMore,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier
                                                                    .size(18.dp)
                                                                    .rotate(if (isUnitOpen) 180f else 0f)
                                                            )
                                                        }
                                                    }

                                                    // Cascading Topics inside Unit
                                                    AnimatedVisibility(
                                                        visible = isUnitOpen,
                                                        enter = AppMotionSystem.cascadeContainerEnterTransition(),
                                                        exit = AppMotionSystem.cascadeContainerExitTransition()
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(start = 12.dp),
                                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            listOf(
                                                                "Topic 1.1: Harappan Town Planning & Seals",
                                                                "Topic 1.2: Rigvedic Polity & Literature",
                                                                "Topic 1.3: Buddhism & Jainism Councils"
                                                            ).forEachIndexed { tIdx, tTitle ->
                                                                CascadeItem(
                                                                    index = tIdx,
                                                                    visible = isUnitOpen,
                                                                    target = AnimatableTarget.TOPIC_CARDS
                                                                ) {
                                                                    Surface(
                                                                        shape = RoundedCornerShape(10.dp),
                                                                        color = Color(0xFF4F46E5),
                                                                        modifier = Modifier.fillMaxWidth()
                                                                    ) {
                                                                        Row(
                                                                            modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Text(
                                                                                text = tTitle,
                                                                                color = Color.White,
                                                                                fontWeight = FontWeight.SemiBold,
                                                                                fontSize = 11.5.sp
                                                                            )
                                                                            Text(
                                                                                text = topicSpec.entranceType.displayName,
                                                                                color = Color(0xFFE0E7FF),
                                                                                fontSize = 9.5.sp
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Screen Transition Preview
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Active Transition: ${master.screenTransitionType.displayName} (${master.screenTransitionDurationMs}ms)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = { sandboxScreenPage = (sandboxScreenPage + 1) % 3 },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Switch Screen →", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                AnimatedContent(
                                    targetState = sandboxScreenPage,
                                    transitionSpec = {
                                        val isForward = targetState > initialState
                                        PremiumAnimationEngine.buildScreenTransition(
                                            masterConfig = master,
                                            isForward = isForward,
                                            slowMoFactor = slowMoFactor
                                        )
                                    },
                                    label = "SandboxScreenTransition"
                                ) { page ->
                                    val colors = when (page) {
                                        0 -> listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6))
                                        1 -> listOf(Color(0xFF7C3AED), Color(0xFFA855F7))
                                        else -> listOf(Color(0xFF059669), Color(0xFF10B981))
                                    }
                                    val title = when (page) {
                                        0 -> "SCREEN A: HOME DASHBOARD"
                                        1 -> "SCREEN B: SYLLABUS EXPLORER"
                                        else -> "SCREEN C: PRACTICE ARENA"
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Brush.linearGradient(colors))
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = title,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Tap 'Switch Screen →' to test ${master.screenTransitionType.displayName}",
                                                    color = Color.White.copy(alpha = 0.85f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // MCQ Feedback, Counter & Progress Preview
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Animated Score Counter:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF334155)
                                        )
                                        val animatedScore = rememberAnimatedCountUp(
                                            targetValue = if (sandboxSelectedOption == "B") 120 else 85,
                                            target = AnimatableTarget.STATISTICS_BADGES_COUNTERS,
                                            durationMs = 550
                                        )
                                        Text(
                                            text = "$animatedScore pts",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF059669)
                                        )
                                    }
                                }

                                // Interactive Option A (Wrong Shake) & Option B (Correct Pulse)
                                listOf(
                                    "A" to "Option A: Tap to test Wrong Answer Shake",
                                    "B" to "Option B: Tap to test Correct Answer Pulse"
                                ).forEach { (optKey, optLabel) ->
                                    val isSelected = sandboxSelectedOption == optKey
                                    val isCorrect = optKey == "B"
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = when {
                                            !isSelected -> Color.White
                                            isCorrect -> Color(0xFFDCFCE7)
                                            else -> Color(0xFFFEE2E2)
                                        },
                                        border = BorderStroke(
                                            1.5.dp,
                                            when {
                                                !isSelected -> Color(0xFFCBD5E1)
                                                isCorrect -> Color(0xFF16A34A)
                                                else -> Color(0xFFDC2626)
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .premiumAnswerFeedback(
                                                isSelected = isSelected,
                                                isCorrect = isCorrect,
                                                triggerKey = sandboxFeedbackTrigger
                                            )
                                            .clickable {
                                                sandboxSelectedOption = optKey
                                                sandboxFeedbackTrigger++
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = optLabel,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            Icon(
                                                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = if (isCorrect) Color(0xFF16A34A) else Color(0xFFDC2626),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 5. PER-COMPONENT ANIMATION SELECTOR & PARAMETER TUNING (24 TARGETS)
        // =====================================================================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "PER-COMPONENT ANIMATION MATRIX (${filteredTargets.size} / 24 TARGETS)",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search component (e.g. Subject Cards, Topic Cards, MCQ, Accordion)...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = cat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }
            }
        }

        // List of Expandable Component Configuration Cards
        filteredTargets.forEach { target ->
            val spec = getSpecForTarget(target)
            val isExpanded = expandedTarget == target

            ComponentAnimationControlCard(
                target = target,
                spec = spec,
                isExpanded = isExpanded,
                onToggleExpand = {
                    expandedTarget = if (isExpanded) null else target
                },
                onUpdateSpec = { updated ->
                    updateSpecForTarget(target, updated)
                },
                onResetTarget = {
                    val resetConfig = AnimationConfigManager.resetComponentToDefault(workingConfig, target)
                    updateWorkingConfig(resetConfig)
                    Toast.makeText(context, "${target.displayName} reset to default", Toast.LENGTH_SHORT).show()
                },
                onTestInSandbox = {
                    AnimationConfigManager.draftConfigState.value = workingConfig
                    AnimationConfigManager.isPreviewModeActive.value = true
                    AnimationConfigManager.triggerReplay()
                    previewReplayKey++
                    Toast.makeText(context, "Previewing ${target.displayName} live!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // =========================================================================
    // DIALOG 1: VERSION HISTORY & ROLLBACK
    // =========================================================================
    if (showRollbackDialog) {
        val dateFormat = remember { SimpleDateFormat("dd MMM HH:mm:ss", Locale.getDefault()) }
        AlertDialog(
            onDismissRequest = { showRollbackDialog = false },
            title = {
                Text("Version History & Rollback", fontWeight = FontWeight.Black, fontSize = 16.sp)
            },
            text = {
                val history = AnimationConfigManager.versionHistoryState
                if (history.isEmpty()) {
                    Text("No saved version history snapshots yet. Publish a configuration to record a snapshot.")
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        history.take(8).forEachIndexed { idx, snap ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "v${snap.versionNumber} • ${snap.masterConfig.activePresetId.displayName}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${dateFormat.format(Date(snap.updatedAt))} • ${snap.notes}",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            if (idx == 0) {
                                                AnimationConfigManager.rollbackToPreviousVersion(context)?.let { restored ->
                                                    workingConfig = restored
                                                }
                                            } else {
                                                AnimationConfigManager.publishConfig(
                                                    context = context,
                                                    config = snap,
                                                    scope = snap.targetScope
                                                )
                                                workingConfig = snap
                                            }
                                            showRollbackDialog = false
                                            Toast.makeText(
                                                context,
                                                "Rolled back to v${snap.versionNumber}!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Restore", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRollbackDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // =========================================================================
    // DIALOG 2: EXPORT / IMPORT JSON CONFIGURATION
    // =========================================================================
    if (showJsonDialog) {
        AlertDialog(
            onDismissRequest = { showJsonDialog = false },
            title = {
                Text("Export / Import Animation JSON", fontWeight = FontWeight.Black, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Copy the JSON below to back up or share, or paste a valid AnimationEngineConfig JSON to import:",
                        fontSize = 11.5.sp,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = jsonInputText,
                        onValueChange = { jsonInputText = it },
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(jsonInputText))
                            Toast.makeText(context, "JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Copy JSON", fontSize = 11.5.sp)
                    }
                    Button(
                        onClick = {
                            val imported = AnimationConfigManager.parseConfigFromJson(jsonInputText)
                            if (imported != null) {
                                updateWorkingConfig(imported)
                                showJsonDialog = false
                                Toast.makeText(context, "Configuration imported successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Invalid JSON format. Changes rejected safely.", Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Text("Import JSON", fontSize = 11.5.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showJsonDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================================================================
    // DIALOG 3: RESET ALL TO DEFAULT CONFIRMATION
    // =========================================================================
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset All Animations to Default?", fontWeight = FontWeight.Black) },
            text = {
                Text("This will restore the Cascade Accordion Pro default configuration across all 24 components.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val def = AnimationConfigManager.resetAllToDefault(context)
                        workingConfig = def
                        showResetConfirmDialog = false
                        Toast.makeText(context, "All animations reset to Cascade Accordion Pro defaults!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Reset All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SafeguardChip(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    FilterChip(
        selected = checked,
        onClick = { onCheckedChange(!checked) },
        leadingIcon = {
            Icon(
                imageVector = if (checked) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
        },
        label = {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComponentAnimationControlCard(
    target: AnimatableTarget,
    spec: ComponentAnimationSpec,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onUpdateSpec: (ComponentAnimationSpec) -> Unit,
    onResetTarget: () -> Unit,
    onTestInSandbox: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) Color(0xFFF8FAFC) else Color.White
        ),
        border = BorderStroke(
            width = if (isExpanded) 1.5.dp else 1.dp,
            color = if (isExpanded) Color(0xFF2563EB) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDBEAFE)
                        ) {
                            Text(
                                text = target.category,
                                color = Color(0xFF1D4ED8),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = target.displayName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${spec.entranceType.displayName} • ${spec.interactionType.displayName} • ${spec.durationMs}ms (Stagger ${spec.staggerDelayMs}ms)",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Switch(
                        checked = spec.enabled,
                        onCheckedChange = { onUpdateSpec(spec.copy(enabled = it)) }
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = Color(0xFF475569)
                    )
                }
            }

            // Expanded Detailed Controls
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Description + Quick Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = target.description,
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = onTestInSandbox,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Preview", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onResetTarget,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Reset Default", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 1. Entrance Animation Selector (28 effects)
                    EnumDropdownSelector(
                        label = "Entrance Effect (${EntranceAnimationType.entries.size} Effects)",
                        selectedValue = spec.entranceType,
                        options = EntranceAnimationType.entries,
                        displayLabel = { it.displayName },
                        onSelected = { onUpdateSpec(spec.copy(entranceType = it)) }
                    )

                    // 2. Exit Animation Selector (15 effects)
                    EnumDropdownSelector(
                        label = "Exit Effect (${ExitAnimationType.entries.size} Effects)",
                        selectedValue = spec.exitType,
                        options = ExitAnimationType.entries,
                        displayLabel = { it.displayName },
                        onSelected = { onUpdateSpec(spec.copy(exitType = it)) }
                    )

                    // 3. Card Interaction Selector (22 effects)
                    EnumDropdownSelector(
                        label = "Card / Touch Interaction (${CardInteractionAnimationType.entries.size} Effects)",
                        selectedValue = spec.interactionType,
                        options = CardInteractionAnimationType.entries,
                        displayLabel = { it.displayName },
                        onSelected = { onUpdateSpec(spec.copy(interactionType = it)) }
                    )

                    // 4. Hierarchy / Accordion Transition (13 effects) + Single-Open vs Multi-Open Toggle
                    EnumDropdownSelector(
                        label = "Hierarchy / Accordion Transition (${HierarchyDrillDownAnimationType.entries.size} Effects)",
                        selectedValue = spec.hierarchyType,
                        options = HierarchyDrillDownAnimationType.entries,
                        displayLabel = { it.displayName },
                        onSelected = {
                            val singleOpen = it != HierarchyDrillDownAnimationType.MULTI_OPEN_ACCORDION
                            onUpdateSpec(spec.copy(hierarchyType = it, singleOpenAccordion = singleOpen))
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Single-Open Accordion Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "When ON, opening a Subject or Unit collapses the previously open sibling",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Switch(
                            checked = spec.singleOpenAccordion,
                            onCheckedChange = { single ->
                                onUpdateSpec(
                                    spec.copy(
                                        singleOpenAccordion = single,
                                        hierarchyType = if (single) HierarchyDrillDownAnimationType.SINGLE_OPEN_ACCORDION
                                        else HierarchyDrillDownAnimationType.MULTI_OPEN_ACCORDION
                                    )
                                )
                            }
                        )
                    }

                    // 5. List/Scroll, Micro-Interaction, Quiz/MCQ, Loading, and Visual Polish Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            EnumDropdownSelector(
                                label = "List & Scroll Motion",
                                selectedValue = spec.listScrollType,
                                options = ListScrollAnimationType.entries,
                                displayLabel = { it.displayName },
                                onSelected = { onUpdateSpec(spec.copy(listScrollType = it)) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            EnumDropdownSelector(
                                label = "Micro-Interaction",
                                selectedValue = spec.microInteractionType,
                                options = MicroInteractionAnimationType.entries,
                                displayLabel = { it.displayName },
                                onSelected = { onUpdateSpec(spec.copy(microInteractionType = it)) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            EnumDropdownSelector(
                                label = "Quiz / MCQ Motion",
                                selectedValue = spec.quizAnimationType,
                                options = QuizExamAnimationType.entries,
                                displayLabel = { it.displayName },
                                onSelected = { onUpdateSpec(spec.copy(quizAnimationType = it)) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            EnumDropdownSelector(
                                label = "Loading / State Effect",
                                selectedValue = spec.loadingAnimationType,
                                options = LoadingStateAnimationType.entries,
                                displayLabel = { it.displayName },
                                onSelected = { onUpdateSpec(spec.copy(loadingAnimationType = it)) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            EnumDropdownSelector(
                                label = "Visual Polish Effect",
                                selectedValue = spec.polishEffectType,
                                options = VisualPolishEffectType.entries,
                                displayLabel = { it.displayName },
                                onSelected = {
                                    onUpdateSpec(
                                        spec.copy(
                                            polishEffectType = it,
                                            continuousEffectEnabled = it != VisualPolishEffectType.NONE
                                        )
                                    )
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            EnumDropdownSelector(
                                label = "Easing Curve",
                                selectedValue = spec.easingCurve,
                                options = AnimationEasingCurve.entries,
                                displayLabel = { it.displayName },
                                onSelected = { onUpdateSpec(spec.copy(easingCurve = it)) }
                            )
                        }
                    }

                    EnumDropdownSelector(
                        label = "Motion Direction",
                        selectedValue = spec.direction,
                        options = AnimationDirection.entries,
                        displayLabel = { it.displayName },
                        onSelected = { onUpdateSpec(spec.copy(direction = it)) }
                    )

                    // 6. Fine-Tuning Parameter Sliders
                    ParameterSliderRow(
                        label = "Duration",
                        valueText = "${spec.durationMs} ms",
                        value = spec.durationMs.toFloat(),
                        valueRange = 0f..1500f,
                        onValueChange = { onUpdateSpec(spec.copy(durationMs = it.toInt())) }
                    )

                    ParameterSliderRow(
                        label = "Exit Duration",
                        valueText = "${spec.exitDurationMs} ms",
                        value = spec.exitDurationMs.toFloat(),
                        valueRange = 0f..1000f,
                        onValueChange = { onUpdateSpec(spec.copy(exitDurationMs = it.toInt())) }
                    )

                    ParameterSliderRow(
                        label = "Stagger Delay per Child",
                        valueText = "${spec.staggerDelayMs} ms (Max ${spec.maxStaggerItems} items)",
                        value = spec.staggerDelayMs.toFloat(),
                        valueRange = 0f..250f,
                        onValueChange = { onUpdateSpec(spec.copy(staggerDelayMs = it.toInt())) }
                    )

                    ParameterSliderRow(
                        label = "Spring Damping Ratio",
                        valueText = String.format(Locale.US, "%.2f", spec.dampingRatio),
                        value = spec.dampingRatio,
                        valueRange = 0.25f..1.0f,
                        onValueChange = { onUpdateSpec(spec.copy(dampingRatio = it)) }
                    )

                    ParameterSliderRow(
                        label = "Spring Stiffness",
                        valueText = "${spec.stiffness.toInt()}",
                        value = spec.stiffness,
                        valueRange = 100f..1500f,
                        onValueChange = { onUpdateSpec(spec.copy(stiffness = it)) }
                    )

                    ParameterSliderRow(
                        label = "Slide / Cascade Offset Distance",
                        valueText = "${spec.translationDistanceDp.toInt()} dp",
                        value = spec.translationDistanceDp,
                        valueRange = 0f..80f,
                        onValueChange = { onUpdateSpec(spec.copy(translationDistanceDp = it)) }
                    )

                    ParameterSliderRow(
                        label = "Initial Entrance Scale",
                        valueText = String.format(Locale.US, "%.2fx", spec.initialScale),
                        value = spec.initialScale,
                        valueRange = 0.5f..1.2f,
                        onValueChange = { onUpdateSpec(spec.copy(initialScale = it)) }
                    )

                    ParameterSliderRow(
                        label = "Press / Tap Scale",
                        valueText = String.format(Locale.US, "%.2fx", spec.pressScale),
                        value = spec.pressScale,
                        valueRange = 0.85f..1.05f,
                        onValueChange = { onUpdateSpec(spec.copy(pressScale = it)) }
                    )

                    ParameterSliderRow(
                        label = "3D Rotation / Flip Angle",
                        valueText = "${spec.rotationDegrees.toInt()}° (Camera ${spec.cameraDistance.toInt()})",
                        value = spec.rotationDegrees,
                        valueRange = 0f..90f,
                        onValueChange = { onUpdateSpec(spec.copy(rotationDegrees = it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> EnumDropdownSelector(
    label: String,
    selectedValue: T,
    options: List<T>,
    displayLabel: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569)
        )
        Box {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayLabel(selectedValue),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 320.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = displayLabel(option),
                                fontWeight = if (option == selectedValue) FontWeight.ExtraBold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ParameterSliderRow(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Text(
                text = valueText,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2563EB)
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.height(26.dp)
        )
    }
}
