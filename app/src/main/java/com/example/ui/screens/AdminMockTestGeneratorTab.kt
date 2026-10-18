package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.MockTestGeneratorAdminConfig
import com.example.model.MockTestMode
import com.example.model.MockTestSectionConfig
import com.example.model.TargetScopeType
import com.example.ui.components.AppTargetSelector

private fun parseHexColorSafe(hex: String, fallback: Color): Color {
    return try {
        val clean = hex.removePrefix("#").trim()
        val colorInt = when (clean.length) {
            6 -> (0xFF000000 or clean.toLong(16)).toInt()
            8 -> clean.toLong(16).toInt()
            else -> return fallback
        }
        Color(colorInt)
    } catch (_: Exception) {
        fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMockTestGeneratorTab() {
    val context = LocalContext.current
    val activeApp = SampleData.getActiveApp()
    val savedConfig = SampleData.getMockTestGeneratorConfigForApp(activeApp.id)

    var targetScope by remember { mutableStateOf(savedConfig.targetScope) }
    var selectedAppIds by remember { mutableStateOf(savedConfig.selectedAppIds.ifEmpty { listOf(activeApp.id) }) }

    var backgroundColorHex by remember { mutableStateOf(savedConfig.backgroundColorHex) }
    var cardBackgroundColorHex by remember { mutableStateOf(savedConfig.cardBackgroundColorHex) }
    var buttonColorHex by remember { mutableStateOf(savedConfig.buttonColorHex) }
    var accentColorHex by remember { mutableStateOf(savedConfig.accentColorHex) }
    var generateButtonText by remember { mutableStateOf(savedConfig.generateButtonText) }
    var headerDesignStyle by remember { mutableStateOf(savedConfig.headerDesignStyle) }

    // Section maps
    val sectionsMap = remember {
        mutableStateMapOf<String, MockTestSectionConfig>().apply {
            putAll(savedConfig.sections)
        }
    }

    var activeEditingMode by remember { mutableStateOf<MockTestMode?>(null) }

    fun applySave() {
        val updatedConfig = MockTestGeneratorAdminConfig(
            sections = sectionsMap.toMap(),
            backgroundColorHex = backgroundColorHex,
            cardBackgroundColorHex = cardBackgroundColorHex,
            buttonColorHex = buttonColorHex,
            accentColorHex = accentColorHex,
            headerDesignStyle = headerDesignStyle,
            generateButtonText = generateButtonText,
            targetScope = targetScope,
            selectedAppIds = selectedAppIds
        )
        val scopeType = when (targetScope) {
            "ALL" -> TargetScopeType.ALL_APPS
            "SELECTED" -> TargetScopeType.SELECT_APPS
            else -> TargetScopeType.SINGLE_APP
        }
        SampleData.updateMockTestGeneratorConfig(updatedConfig, scopeType, selectedAppIds)
        val scopeMsg = when (scopeType) {
            TargetScopeType.ALL_APPS -> "All Apps"
            TargetScopeType.SELECT_APPS -> "${selectedAppIds.size} Selected Apps"
            TargetScopeType.SINGLE_APP -> activeApp.name
        }
        Toast.makeText(context, "Mock Test Generator config saved for $scopeMsg!", Toast.LENGTH_SHORT).show()
    }

    fun resetToDefaults() {
        val scopeType = when (targetScope) {
            "ALL" -> TargetScopeType.ALL_APPS
            "SELECTED" -> TargetScopeType.SELECT_APPS
            else -> TargetScopeType.SINGLE_APP
        }
        SampleData.resetMockTestGeneratorConfig(scopeType, activeApp.id, selectedAppIds)
        val def = MockTestGeneratorAdminConfig()
        backgroundColorHex = def.backgroundColorHex
        cardBackgroundColorHex = def.cardBackgroundColorHex
        buttonColorHex = def.buttonColorHex
        accentColorHex = def.accentColorHex
        generateButtonText = def.generateButtonText
        headerDesignStyle = def.headerDesignStyle
        sectionsMap.clear()
        sectionsMap.putAll(def.sections)
        Toast.makeText(context, "Reset to default generator styles.", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color(0xFF93C5FD),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mock Tests Generator Admin",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage test generation modes, visual themes, cards & scope controls",
                            color = Color(0xFFBFDBFE),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 2. Multi-App Scope Selector
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Deployment Scope",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val currentScopeType = when (targetScope) {
                        "ALL" -> TargetScopeType.ALL_APPS
                        "SELECTED" -> TargetScopeType.SELECT_APPS
                        else -> TargetScopeType.SINGLE_APP
                    }
                    AppTargetSelector(
                        scope = currentScopeType,
                        selectedAppIds = selectedAppIds,
                        onScopeChange = { newScope ->
                            targetScope = when (newScope) {
                                TargetScopeType.ALL_APPS -> "ALL"
                                TargetScopeType.SELECT_APPS -> "SELECTED"
                                TargetScopeType.SINGLE_APP -> "CURRENT"
                            }
                        },
                        onAppToggled = { appId, isChecked ->
                            selectedAppIds = if (isChecked) {
                                (selectedAppIds + appId).distinct()
                            } else {
                                selectedAppIds - appId
                            }
                        },
                        onSelectAll = {
                            selectedAppIds = SampleData.appsListState.map { it.id }
                        },
                        onDeselectAll = {
                            selectedAppIds = emptyList()
                        },
                        onSingleAppSelected = { appId ->
                            selectedAppIds = listOf(appId)
                        }
                    )
                }
            }
        }

        // 3. Test Modes (Section Management)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ViewModule, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Test Generation Modes (Cards)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Text(
                        text = "Customize cards for Full-Length, Subject-Wise, Unit-Wise & Topic-Wise tests",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    MockTestMode.values().forEach { mode ->
                        val sec = sectionsMap[mode.name] ?: MockTestSectionConfig(mode)
                        val startColor = parseHexColorSafe(sec.backgroundGradientStart, Color(0xFF1E3A8A))
                        val endColor = parseHexColorSafe(sec.backgroundGradientEnd, Color(0xFF3B82F6))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (sec.isEnabled) Color(0xFFE2E8F0) else Color(0xFFF1F5F9)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Brush.linearGradient(listOf(startColor, endColor))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (mode) {
                                                MockTestMode.FULL_LENGTH -> Icons.Default.Assignment
                                                MockTestMode.SUBJECT_WISE -> Icons.Default.MenuBook
                                                MockTestMode.UNIT_WISE -> Icons.Default.AccountTree
                                                MockTestMode.TOPIC_WISE -> Icons.Default.Psychology
                                            },
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sec.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = sec.description,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1
                                        )
                                    }
                                    Switch(
                                        checked = sec.isEnabled,
                                        onCheckedChange = { isChecked ->
                                            sectionsMap[mode.name] = sec.copy(isEnabled = isChecked)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { activeEditingMode = mode },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit Card Style", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Overall Visual Styling
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generator Screen Theme",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = backgroundColorHex,
                        onValueChange = { backgroundColorHex = it },
                        label = { Text("Screen Background Color (Hex)") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColorSafe(backgroundColorHex, Color(0xFFF8FAFC)))
                                    .border(1.dp, Color.Gray, CircleShape)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cardBackgroundColorHex,
                        onValueChange = { cardBackgroundColorHex = it },
                        label = { Text("Card Background Color (Hex)") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColorSafe(cardBackgroundColorHex, Color.White))
                                    .border(1.dp, Color.Gray, CircleShape)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = buttonColorHex,
                        onValueChange = { buttonColorHex = it },
                        label = { Text("Primary Action Button Color (Hex)") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColorSafe(buttonColorHex, Color(0xFF2563EB)))
                                    .border(1.dp, Color.Gray, CircleShape)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = generateButtonText,
                        onValueChange = { generateButtonText = it },
                        label = { Text("Generate Button Text") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // 5. Action Buttons (Save & Reset)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { resetToDefaults() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF64748B))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Defaults", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { applySave() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deploy Changes", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // Modal dialog for editing specific card styling
    if (activeEditingMode != null) {
        val mode = activeEditingMode!!
        val currentSec = sectionsMap[mode.name] ?: MockTestSectionConfig(mode)

        var editedName by remember(mode) { mutableStateOf(currentSec.name) }
        var editedDesc by remember(mode) { mutableStateOf(currentSec.description) }
        var editedStartColor by remember(mode) { mutableStateOf(currentSec.backgroundGradientStart) }
        var editedEndColor by remember(mode) { mutableStateOf(currentSec.backgroundGradientEnd) }
        var editedAccentColor by remember(mode) { mutableStateOf(currentSec.accentColor) }

        AlertDialog(
            onDismissRequest = { activeEditingMode = null },
            title = {
                Text(
                    text = "Edit ${mode.displayName} Card",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Card Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedDesc,
                        onValueChange = { editedDesc = it },
                        label = { Text("Card Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedStartColor,
                        onValueChange = { editedStartColor = it },
                        label = { Text("Gradient Start Color (Hex)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedEndColor,
                        onValueChange = { editedEndColor = it },
                        label = { Text("Gradient End Color (Hex)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedAccentColor,
                        onValueChange = { editedAccentColor = it },
                        label = { Text("Accent Color (Hex)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        sectionsMap[mode.name] = currentSec.copy(
                            name = editedName,
                            description = editedDesc,
                            backgroundGradientStart = editedStartColor,
                            backgroundGradientEnd = editedEndColor,
                            accentColor = editedAccentColor
                        )
                        activeEditingMode = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Save Card")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { activeEditingMode = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
