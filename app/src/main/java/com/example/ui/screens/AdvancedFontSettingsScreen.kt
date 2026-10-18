package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdvancedFontSettings
import com.example.repository.AppConfigRepository
import com.example.ui.theme.resolveFontFamily
import com.example.util.FontSettingsManager

// Curated preset for exam preparation styling
private data class TypographyPreset(
    val name: String,
    val description: String,
    val emoji: String,
    val headingFont: String,
    val bodyFont: String,
    val headingWeight: String,
    val bodyWeight: String,
    val scale: Float,
    val gradientColors: List<Color>,
    val badge: String
)

private val AVAILABLE_FONTS = listOf(
    "Roboto",
    "Inter",
    "Poppins",
    "Montserrat",
    "Playfair Display",
    "Merriweather",
    "Lora",
    "Lato",
    "Open Sans",
    "Nunito",
    "Raleway",
    "Ubuntu",
    "Source Sans 3",
    "Mukta",
    "PT Sans",
    "Noto Sans",
    "Noto Sans Telugu",
    "Noto Serif Telugu",
    "Mallanna",
    "Mandali"
)

private val FONT_WEIGHTS = listOf("Light", "Regular", "Medium", "Semi-Bold", "Bold", "Extra-Bold")
private val BODY_FONT_WEIGHTS = listOf("Light", "Regular", "Medium", "Semi-Bold", "Bold")
private val LINE_SPACINGS = listOf("Compact", "Normal", "Comfortable", "Spacious")
private val CORNER_STYLES = listOf("Sharp", "Balanced", "Soft Rounded", "Pill")
private val LETTER_SPACINGS = listOf(
    "Condensed" to -0.4f,
    "Standard" to 0.0f,
    "Relaxed" to 0.4f,
    "Wide" to 0.8f
)

private val PRESETS = listOf(
    TypographyPreset(
        name = "Modern Aspirant",
        description = "Clean, high-legibility geometric styling for fast reading",
        emoji = "🚀",
        headingFont = "Poppins",
        bodyFont = "Roboto",
        headingWeight = "Bold",
        bodyWeight = "Regular",
        scale = 1.0f,
        gradientColors = listOf(Color(0xFF2563EB), Color(0xFF38BDF8)),
        badge = "RECOMMENDED"
    ),
    TypographyPreset(
        name = "Executive Classic",
        description = "Prestigious serif titles paired with sharp modern body text",
        emoji = "🏛️",
        headingFont = "Playfair Display",
        bodyFont = "Inter",
        headingWeight = "Bold",
        bodyWeight = "Regular",
        scale = 1.02f,
        gradientColors = listOf(Color(0xFF7C3AED), Color(0xFFA855F7)),
        badge = "PRESTIGE"
    ),
    TypographyPreset(
        name = "Scholar Editorial",
        description = "Editorial warmth with book-style typography for deep study",
        emoji = "🏆",
        headingFont = "Merriweather",
        bodyFont = "Open Sans",
        headingWeight = "Bold",
        bodyWeight = "Medium",
        scale = 1.05f,
        gradientColors = listOf(Color(0xFFD97706), Color(0xFFFBBF24)),
        badge = "EDITORIAL"
    ),
    TypographyPreset(
        name = "High-Contrast Speed",
        description = "Punchy headlines and streamlined body for rapid MCQ recall",
        emoji = "⚡",
        headingFont = "Montserrat",
        bodyFont = "Lato",
        headingWeight = "Extra-Bold",
        bodyWeight = "Regular",
        scale = 1.0f,
        gradientColors = listOf(Color(0xFF059669), Color(0xFF34D399)),
        badge = "FOCUS"
    ),
    TypographyPreset(
        name = "Heritage Bilingual",
        description = "Authentic Telugu & English typography tailored for APPSC",
        emoji = "🌸",
        headingFont = "Noto Serif Telugu",
        bodyFont = "Noto Sans Telugu",
        headingWeight = "Bold",
        bodyWeight = "Regular",
        scale = 1.05f,
        gradientColors = listOf(Color(0xFFE11D48), Color(0xFFFB7185)),
        badge = "BILINGUAL"
    ),
    TypographyPreset(
        name = "Minimalist Neo",
        description = "Ergonomic rounded typeface system gentle on the eyes",
        emoji = "💎",
        headingFont = "Ubuntu",
        bodyFont = "Nunito",
        headingWeight = "Semi-Bold",
        bodyWeight = "Regular",
        scale = 0.98f,
        gradientColors = listOf(Color(0xFF0284C7), Color(0xFF06B6D4)),
        badge = "EYE COMFORT"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFontSettingsScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {}
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val configRepository = remember { AppConfigRepository.getInstance() }
    val currentAppConfig by configRepository.currentAppConfig.collectAsState()

    // Active persisted settings
    val savedSettings = remember(currentAppConfig.advancedFontSettings) {
        currentAppConfig.advancedFontSettings
    }

    // Working draft settings for real-time Live Preview before committing
    var draftSettings by remember(savedSettings) {
        mutableStateOf(savedSettings)
    }

    // Real-time comparison mode: toggle to preview original vs new draft
    var isComparingOriginal by remember { mutableStateOf(false) }

    // Selected customization tab
    var selectedCategoryTab by remember { mutableIntStateOf(0) }
    val categoryTabs = listOf("Presets", "Fonts", "Sizes & Scale", "Weights & Style", "Wallpapers & Themes")

    // Check if there are unsaved changes
    val hasUnsavedChanges = remember(draftSettings, savedSettings) {
        draftSettings.fontFamily != savedSettings.fontFamily ||
        draftSettings.headingFontFamily != savedSettings.headingFontFamily ||
        draftSettings.bodyFontFamily != savedSettings.bodyFontFamily ||
        draftSettings.titleFontSize != savedSettings.titleFontSize ||
        draftSettings.headingFontSize != savedSettings.headingFontSize ||
        draftSettings.bodyFontSize != savedSettings.bodyFontSize ||
        draftSettings.questionFontSize != savedSettings.questionFontSize ||
        draftSettings.headingFontWeight != savedSettings.headingFontWeight ||
        draftSettings.bodyFontWeight != savedSettings.bodyFontWeight ||
        draftSettings.typographyScale != savedSettings.typographyScale ||
        draftSettings.lineSpacing != savedSettings.lineSpacing ||
        draftSettings.letterSpacingScale != savedSettings.letterSpacingScale ||
        draftSettings.cornerRadiusStyle != savedSettings.cornerRadiusStyle ||
        draftSettings.backgroundWallpaperTheme != savedSettings.backgroundWallpaperTheme
    }

    var showResetConfirmationDialog by remember { mutableStateOf(false) }

    val activeDisplaySettings = if (isComparingOriginal) savedSettings else draftSettings

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "App Visual Appearance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF0F172A)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF6366F1).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = "GLOBAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4F46E5),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        Text(
                            text = "Customize app-wide typography, font weights & UI scale",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF1E293B)
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
                    TextButton(
                        onClick = { showResetConfirmationDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            // Sticky Floating Action Bar for Confirm & Apply
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status indicator row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (hasUnsavedChanges) Color(0xFFF59E0B) else Color(0xFF10B981),
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = if (hasUnsavedChanges) "Draft modified • Live preview active" else "Current style saved & active app-wide",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (hasUnsavedChanges) Color(0xFFB45309) else Color(0xFF047857)
                            )
                        }

                        if (hasUnsavedChanges) {
                            TextButton(
                                onClick = { draftSettings = savedSettings },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Discard Draft", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                    }

                    // Main Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Compare Button
                        OutlinedButton(
                            onClick = { isComparingOriginal = !isComparingOriginal },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, if (isComparingOriginal) Color(0xFF6366F1) else Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isComparingOriginal) Color(0xFFEEF2FF) else Color.White,
                                contentColor = if (isComparingOriginal) Color(0xFF4F46E5) else Color(0xFF334155)
                            )
                        ) {
                            Icon(
                                imageVector = if (isComparingOriginal) Icons.Default.VisibilityOff else Icons.Default.Compare,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isComparingOriginal) "Draft" else "Compare",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Confirm & Apply Button
                        Button(
                            onClick = {
                                // Preserve existing practice appearance settings completely
                                val updatedSettings = draftSettings.copy(
                                    practiceAppearance = savedSettings.practiceAppearance
                                )
                                FontSettingsManager.saveSettings(context, updatedSettings)
                                configRepository.updateAppConfig(
                                    currentAppConfig.copy(advancedFontSettings = updatedSettings)
                                )
                                isComparingOriginal = false
                                Toast.makeText(
                                    context,
                                    "✨ Global appearance applied app-wide!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            enabled = hasUnsavedChanges,
                            modifier = Modifier
                                .weight(2f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F46E5),
                                disabledContainerColor = Color(0xFFE2E8F0)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (hasUnsavedChanges) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Confirm & Apply",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (hasUnsavedChanges) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // =========================================================================
            // 1. REAL-TIME LIVE PREVIEW CARD (Interactive exam preparation representation)
            // =========================================================================
            item {
                LivePreviewCard(
                    settings = activeDisplaySettings,
                    isComparing = isComparingOriginal,
                    onToggleCompare = { isComparingOriginal = !isComparingOriginal }
                )
            }

            // =========================================================================
            // 2. CATEGORY TABS (Navigation between Presets, Fonts, Sizes, Weights)
            // =========================================================================
            item {
                SingleSelectPillRow(
                    items = categoryTabs,
                    selectedIndex = selectedCategoryTab,
                    onSelect = { selectedCategoryTab = it }
                )
            }

            // =========================================================================
            // 3. TAB CONTENT
            // =========================================================================
            when (selectedCategoryTab) {
                0 -> {
                    // Curated Presets
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeader(
                                title = "Curated Aesthetic Presets",
                                subtitle = "One-tap harmonious pairings engineered for focus & reading stamina"
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                PRESETS.forEach { preset ->
                                    PresetCard(
                                        preset = preset,
                                        isSelected = draftSettings.headingFontFamily == preset.headingFont &&
                                                draftSettings.bodyFontFamily == preset.bodyFont &&
                                                draftSettings.headingFontWeight == preset.headingWeight,
                                        onSelect = {
                                            draftSettings = draftSettings.copy(
                                                fontFamily = preset.bodyFont,
                                                headingFontFamily = preset.headingFont,
                                                bodyFontFamily = preset.bodyFont,
                                                headingFontWeight = preset.headingWeight,
                                                bodyFontWeight = preset.bodyWeight,
                                                typographyScale = preset.scale
                                            )
                                            isComparingOriginal = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Fonts Tab: App-Wide, Heading, Body
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SectionHeader(
                                title = "Font Families & Pairing",
                                subtitle = "Choose distinct typefaces for section titles and readable body content"
                            )

                            // Quick App-Wide Font Selection
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Heading Font Style", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                            Text("Applies to screen titles, subjects & card headers", fontSize = 12.sp, color = Color(0xFF64748B))
                                        }
                                        BadgePill(text = draftSettings.headingFontFamily, color = Color(0xFF4F46E5))
                                    }

                                    FontCarousel(
                                        fonts = AVAILABLE_FONTS,
                                        selectedFont = draftSettings.headingFontFamily,
                                        onSelect = {
                                            draftSettings = draftSettings.copy(headingFontFamily = it)
                                            isComparingOriginal = false
                                        }
                                    )
                                }
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Body Font Style", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                            Text("Applies to question texts, notes, options & descriptions", fontSize = 12.sp, color = Color(0xFF64748B))
                                        }
                                        BadgePill(text = draftSettings.bodyFontFamily, color = Color(0xFF0D9488))
                                    }

                                    FontCarousel(
                                        fonts = AVAILABLE_FONTS,
                                        selectedFont = draftSettings.bodyFontFamily,
                                        onSelect = {
                                            draftSettings = draftSettings.copy(bodyFontFamily = it, fontFamily = it)
                                            isComparingOriginal = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Sizes & Scale Tab
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SectionHeader(
                                title = "Font Sizes & Global Scale",
                                subtitle = "Fine-tune overall proportions or adjust individual text layers"
                            )

                            // Global Scale Slider
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Overall Typography Scale", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                            Text("Scales all text across every screen proportionally", fontSize = 12.sp, color = Color(0xFF64748B))
                                        }
                                        BadgePill(
                                            text = "${(draftSettings.typographyScale * 100).toInt()}%",
                                            color = Color(0xFF7C3AED)
                                        )
                                    }

                                    Slider(
                                        value = draftSettings.typographyScale,
                                        onValueChange = {
                                            draftSettings = draftSettings.copy(typographyScale = it)
                                            isComparingOriginal = false
                                        },
                                        valueRange = 0.85f..1.25f,
                                        steps = 7,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF7C3AED),
                                            activeTrackColor = Color(0xFF7C3AED)
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        listOf(
                                            "Compact (85%)" to 0.85f,
                                            "Standard (100%)" to 1.0f,
                                            "Comfortable (115%)" to 1.15f,
                                            "Large (125%)" to 1.25f
                                        ).forEach { (label, value) ->
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (kotlin.math.abs(draftSettings.typographyScale - value) < 0.03f) FontWeight.Bold else FontWeight.Normal,
                                                color = if (kotlin.math.abs(draftSettings.typographyScale - value) < 0.03f) Color(0xFF7C3AED) else Color(0xFF94A3B8),
                                                modifier = Modifier.clickable {
                                                    draftSettings = draftSettings.copy(typographyScale = value)
                                                    isComparingOriginal = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Individual Size Adjustments
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("Granular Size Adjustments", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))

                                    SizeStepperRow(
                                        label = "Title Font Size",
                                        description = "App & subject hero titles",
                                        value = draftSettings.titleFontSize,
                                        range = 18f..28f,
                                        onValueChange = { draftSettings = draftSettings.copy(titleFontSize = it) }
                                    )

                                    HorizontalDivider(color = Color(0xFFF1F5F9))

                                    SizeStepperRow(
                                        label = "Heading Font Size",
                                        description = "Topic cards & section headers",
                                        value = draftSettings.headingFontSize,
                                        range = 15f..24f,
                                        onValueChange = { draftSettings = draftSettings.copy(headingFontSize = it) }
                                    )

                                    HorizontalDivider(color = Color(0xFFF1F5F9))

                                    SizeStepperRow(
                                        label = "Body / Question Size",
                                        description = "Questions, explanations & text passages",
                                        value = draftSettings.questionFontSize,
                                        range = 13f..20f,
                                        onValueChange = {
                                            draftSettings = draftSettings.copy(
                                                questionFontSize = it,
                                                bodyFontSize = it,
                                                optionFontSize = it - 1f
                                            )
                                        }
                                    )

                                    HorizontalDivider(color = Color(0xFFF1F5F9))

                                    SizeStepperRow(
                                        label = "Button & Label Size",
                                        description = "Action chips, badges & navigation labels",
                                        value = draftSettings.buttonFontSize,
                                        range = 11f..17f,
                                        onValueChange = {
                                            draftSettings = draftSettings.copy(
                                                buttonFontSize = it,
                                                labelFontSize = it - 2f
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Weights & Style Tab
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SectionHeader(
                                title = "Font Weights & Visual Geometry",
                                subtitle = "Set heading boldness, line rhythm, and card corner styling"
                            )

                            // Heading Font Weight
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Heading Font Weight", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                    SingleSelectChipGroup(
                                        options = FONT_WEIGHTS,
                                        selectedOption = draftSettings.headingFontWeight,
                                        onSelect = {
                                            draftSettings = draftSettings.copy(headingFontWeight = it)
                                            isComparingOriginal = false
                                        }
                                    )
                                }
                            }

                            // Body Font Weight
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Body Font Weight", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                    SingleSelectChipGroup(
                                        options = BODY_FONT_WEIGHTS,
                                        selectedOption = draftSettings.bodyFontWeight,
                                        onSelect = {
                                            draftSettings = draftSettings.copy(bodyFontWeight = it, fontWeight = it)
                                            isComparingOriginal = false
                                        }
                                    )
                                }
                            }

                            // Line Spacing Rhythm
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Line Spacing Rhythm", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                    SingleSelectChipGroup(
                                        options = LINE_SPACINGS,
                                        selectedOption = draftSettings.lineSpacing,
                                        onSelect = {
                                            draftSettings = draftSettings.copy(lineSpacing = it)
                                            isComparingOriginal = false
                                        }
                                    )
                                }
                            }

                            // Letter Spacing Tracking
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Letter Spacing (Tracking)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        LETTER_SPACINGS.forEach { (label, value) ->
                                            val isSelected = kotlin.math.abs(draftSettings.letterSpacingScale - value) < 0.1f
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) Color(0xFF4F46E5) else Color(0xFFF1F5F9),
                                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF4F46E5) else Color(0xFFE2E8F0)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        draftSettings = draftSettings.copy(letterSpacingScale = value)
                                                        isComparingOriginal = false
                                                    }
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) Color.White else Color(0xFF475569)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                             // Corner Radius Style
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Card Corner Geometry", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                    SingleSelectChipGroup(
                                        options = CORNER_STYLES,
                                        selectedOption = draftSettings.cornerRadiusStyle,
                                        onSelect = {
                                            draftSettings = draftSettings.copy(cornerRadiusStyle = it)
                                            isComparingOriginal = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // Wallpapers & Themes Tab
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SectionHeader(
                                title = "App Background Wallpapers & Themes",
                                subtitle = "Select a premium background theme reflecting across Subjects, Units, Topics, Sets, Subtopics & Current Affairs"
                            )

                            val availableThemes = listOf(
                                Triple("Midnight Royal Prestige", "Ultra-luxurious obsidian canvas with royal gold & sapphire luminous aura", listOf(Color(0xFF080C18), Color(0xFF0E1A36), Color(0xFFF59E0B))),
                                Triple("Clean Slate", "Crisp, ultra-clean professional slate with subtle modern tone", listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))),
                                Triple("Royal Parchment", "Warm prestigious cream & golden amber for focused reading", listOf(Color(0xFFFEFCE8), Color(0xFFFEF3C7))),
                                Triple("Subtle Heritage", "Classic traditional soft ivory with a gentle aura", listOf(Color(0xFFFFFBEB), Color(0xFFF3E8FF))),
                                Triple("Emerald Breeze", "Refreshing study-focused mint & emerald gradient", listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))),
                                Triple("Twilight Slate", "Sleek modern dark mode slate for late-night study", listOf(Color(0xFF0F172A), Color(0xFF1E293B))),
                                Triple("Golden Glow", "Warm sunrise gold with uplifting ambiance", listOf(Color(0xFFFFFBEB), Color(0xFFFDE68A))),
                                Triple("Deep Azure", "Calm ocean blue gradient for serene concentration", listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE)))
                            )

                            availableThemes.forEach { (themeName, themeDesc, colors) ->
                                val isSelected = draftSettings.backgroundWallpaperTheme.equals(themeName, ignoreCase = true)
                                Card(
                                    onClick = {
                                        draftSettings = draftSettings.copy(backgroundWallpaperTheme = themeName)
                                        isComparingOriginal = false
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFFEEF2FF) else Color.White
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF6366F1) else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Brush.verticalGradient(colors))
                                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = if (themeName == "Twilight Slate") Color.White else Color(0xFF4F46E5),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(
                                                    text = themeName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                                if (isSelected) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFF6366F1)
                                                    ) {
                                                        Text(
                                                            text = "ACTIVE",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = themeDesc,
                                                fontSize = 13.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Reset confirmation dialog
    if (showResetConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFFEF4444))
                    Text("Reset Visual Appearance?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "This will restore app-wide fonts, weights, and scale to the default system values. Your practice card appearance (Sets, Subtopics, Current Affairs) will NOT be affected.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        FontSettingsManager.resetGlobalSettings(context)
                        val defaultSettings = AdvancedFontSettings(
                            practiceAppearance = savedSettings.practiceAppearance
                        )
                        FontSettingsManager.saveSettings(context, defaultSettings)
                        configRepository.updateAppConfig(
                            currentAppConfig.copy(advancedFontSettings = defaultSettings)
                        )
                        draftSettings = defaultSettings
                        isComparingOriginal = false
                        showResetConfirmationDialog = false
                        Toast.makeText(context, "Appearance reset to default", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Reset All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// =========================================================================
// SUBCOMPONENTS
// =========================================================================

@Composable
private fun LivePreviewCard(
    settings: AdvancedFontSettings,
    isComparing: Boolean,
    onToggleCompare: () -> Unit
) {
    val headingFamily = resolveFontFamily(settings.headingFontFamily.ifBlank { settings.fontFamily })
    val bodyFamily = resolveFontFamily(settings.bodyFontFamily.ifBlank { settings.fontFamily })

    val headingWeight = when (settings.headingFontWeight) {
        "Light" -> FontWeight.Light
        "Regular" -> FontWeight.Normal
        "Medium" -> FontWeight.Medium
        "Semi-Bold" -> FontWeight.SemiBold
        "Bold" -> FontWeight.Bold
        "Extra-Bold" -> FontWeight.Black
        else -> FontWeight.Bold
    }

    val bodyWeight = when (settings.bodyFontWeight) {
        "Light" -> FontWeight.Light
        "Regular" -> FontWeight.Normal
        "Medium" -> FontWeight.Medium
        "Semi-Bold" -> FontWeight.SemiBold
        "Bold" -> FontWeight.Bold
        else -> FontWeight.Normal
    }

    val scale = settings.typographyScale.coerceIn(0.75f, 1.35f)
    val cornerRadius = when (settings.cornerRadiusStyle) {
        "Sharp" -> 6.dp
        "Soft Rounded" -> 20.dp
        "Pill" -> 28.dp
        else -> 14.dp
    }

    Card(
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(
            width = if (isComparing) 2.dp else 1.dp,
            color = if (isComparing) Color(0xFF6366F1) else Color(0xFFE2E8F0)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Live Preview Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isComparing) {
                                listOf(Color(0xFF334155), Color(0xFF475569))
                            } else {
                                listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                            }
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (isComparing) Color(0xFF94A3B8) else Color(0xFF10B981), CircleShape)
                        )
                        Text(
                            text = if (isComparing) "ORIGINAL APPLIED STYLE" else "REAL-TIME LIVE PREVIEW",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { onToggleCompare() }
                    ) {
                        Text(
                            text = if (isComparing) "Viewing Original" else "Live Draft",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Simulated App Screen Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Exam Category & Title
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "APPSC GROUP-II • GENERAL STUDIES",
                        fontFamily = bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (11f * scale).sp,
                        color = Color(0xFF6366F1),
                        letterSpacing = (settings.letterSpacingScale + 0.5f).sp
                    )
                    Text(
                        text = "Indian Economy & AP Bifurcation Act",
                        fontFamily = headingFamily,
                        fontWeight = headingWeight,
                        fontSize = (settings.titleFontSize * 0.95f * scale).sp,
                        lineHeight = (settings.titleFontSize * 1.25f * scale).sp,
                        color = Color(0xFF0F172A),
                        letterSpacing = settings.letterSpacingScale.sp
                    )
                }

                // Topic Card Snippet
                Surface(
                    shape = RoundedCornerShape(cornerRadius),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "UNIT 3 • PRACTICE MCQ",
                                fontFamily = bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = (10f * scale).sp,
                                color = Color(0xFF0D9488)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "150 MARKS",
                                    fontSize = (9f * scale).sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF059669),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Which constitutional article empowers the President to establish an Inter-State Council?",
                            fontFamily = bodyFamily,
                            fontWeight = bodyWeight,
                            fontSize = (settings.questionFontSize * scale).sp,
                            lineHeight = (settings.questionFontSize * 1.35f * scale).sp,
                            color = Color(0xFF1E293B)
                        )

                        // Sample Option
                        Surface(
                            shape = RoundedCornerShape((cornerRadius.value * 0.7f).dp),
                            color = Color(0xFFEEF2FF),
                            border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "(B) Article 263 — Inter-State Council Provisions",
                                    fontFamily = bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = (settings.optionFontSize * scale).sp,
                                    color = Color(0xFF3730A3)
                                )
                            }
                        }
                    }
                }

                // Metric Tags Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        label = "Heading",
                        value = "${settings.headingFontFamily} (${settings.headingFontWeight})",
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "Body",
                        value = "${settings.bodyFontFamily} (${settings.bodyFontWeight})",
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "Scale",
                        value = "${(scale * 100).toInt()}%",
                        modifier = Modifier.width(64.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PresetCard(
    preset: TypographyPreset,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF4F46E5) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Emoji Avatar in gradient box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(preset.gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Text(preset.emoji, fontSize = 22.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = preset.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = preset.gradientColors.first().copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = preset.badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = preset.gradientColors.first(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = preset.description,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${preset.headingFont} + ${preset.bodyFont} (${(preset.scale * 100).toInt()}%)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4F46E5)
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4F46E5))
            )
        }
    }
}

@Composable
private fun FontCarousel(
    fonts: List<String>,
    selectedFont: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(fonts) { fontName ->
            val isSelected = fontName.equals(selectedFont, ignoreCase = true)
            val fontFam = resolveFontFamily(fontName)

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Color(0xFF4F46E5) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, if (isSelected) Color(0xFF4F46E5) else Color(0xFFCBD5E1)),
                modifier = Modifier.clickable { onSelect(fontName) }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Aa",
                        fontFamily = fontFam,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isSelected) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = fontName,
                        fontFamily = fontFam,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleSelectPillRow(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Color(0xFF1E293B) else Color.White,
                border = BorderStroke(1.dp, if (isSelected) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                modifier = Modifier.clickable { onSelect(index) }
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF475569),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SingleSelectChipGroup(
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option.equals(selectedOption, ignoreCase = true)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Color(0xFF4F46E5) else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, if (isSelected) Color(0xFF4F46E5) else Color(0xFFCBD5E1)),
                modifier = Modifier.clickable { onSelect(option) }
            ) {
                Text(
                    text = option,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF334155),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SizeStepperRow(
    label: String,
    description: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
            Text(text = description, fontSize = 11.sp, color = Color(0xFF64748B))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = { if (value > range.start) onValueChange(value - 1f) },
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFF1F5F9), CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
            }

            Text(
                text = "${value.toInt()} sp",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.width(42.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = { if (value < range.endInclusive) onValueChange(value + 1f) },
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFF1F5F9), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
        Text(subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
    }
}

@Composable
private fun BadgePill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
