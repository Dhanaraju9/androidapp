package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SampleData
import com.example.db.AppDatabase
import com.example.model.LegalDocType
import com.example.model.LegalDocument
import com.example.model.SupportFaq
import com.example.repository.AppRepository
import com.example.ui.theme.LocalAppConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data Classes for Sectioned Documents and Social Configuration
data class DocSection(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val order: Int = 0
)

data class AdminContactSettings(
    val supportEmail: String? = null,
    val supportPhone: String? = null,
    val whatsappNumber: String? = null,
    val whatsappLink: String? = null,
    val telegramId: String? = null,
    val telegramLink: String? = null,
    val supportWebsite: String? = null,
    val socialLinks: List<SocialLink> = emptyList()
)

data class SocialLink(
    val platform: String,
    val url: String,
    val value: String
)

/**
 * Normalizes section keys into specific LegalDocType.
 */
fun parseSection(section: String?): LegalDocType? {
    if (section.isNullOrBlank()) return null
    val s = section.trim().lowercase()
    return when {
        s == "faq" || s == "faqs" || s.contains("faq") -> LegalDocType.FAQ
        s == "privacy" || s.contains("privacy") -> LegalDocType.PRIVACY_POLICY
        s == "terms" || s.contains("terms") || s.contains("condition") -> LegalDocType.TERMS_CONDITIONS
        s == "refund" || s.contains("refund") || s.contains("cancel") -> LegalDocType.REFUND_CANCELLATION
        s == "about" || s.contains("about") -> LegalDocType.ABOUT_US
        s == "contact" || s.contains("contact") -> LegalDocType.CONTACT_US
        s == "help" || s.contains("help") -> LegalDocType.HELP_CENTER
        s == "deletion" || s.contains("delete") -> LegalDocType.ACCOUNT_DELETION
        s.contains("subscription") -> LegalDocType.SUBSCRIPTION_TERMS
        s.contains("disclaimer") -> LegalDocType.DISCLAIMER
        s.contains("copyright") -> LegalDocType.COPYRIGHT_POLICY
        s.contains("report") -> LegalDocType.REPORTING_POLICY
        else -> LegalDocType.values().find { it.id.equals(s, ignoreCase = true) || it.name.equals(s, ignoreCase = true) }
    }
}

// Helpers for color parsing and iconography
fun parseColorHex(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

fun getIconForName(iconName: String?): ImageVector {
    return when (iconName?.lowercase()?.trim()) {
        "shield", "privacy", "security" -> Icons.Default.Shield
        "assignment", "terms", "rules" -> Icons.Default.Assignment
        "currencyexchange", "refund", "card", "payment" -> Icons.Default.CurrencyExchange
        "star", "subscription", "premium" -> Icons.Default.Star
        "delete", "remove", "trash", "danger" -> Icons.Default.Delete
        "info", "disclaimer", "about" -> Icons.Default.Info
        "copyright", "patent" -> Icons.Default.Copyright
        "flag", "report" -> Icons.Default.Flag
        "contactsupport", "contact", "support", "helpdesk" -> Icons.Default.ContactSupport
        "quiz", "faq", "question" -> Icons.Default.Quiz
        "help", "guide", "manual" -> Icons.Default.Help
        "business", "company", "office" -> Icons.Default.Business
        "email", "mail" -> Icons.Default.Email
        "phone", "call" -> Icons.Default.Phone
        "chat", "whatsapp" -> Icons.Default.Chat
        "send", "telegram" -> Icons.Default.Send
        "language", "website", "web" -> Icons.Default.Language
        "gavel", "legal" -> Icons.Default.Gavel
        "account", "profile", "user" -> Icons.Default.AccountCircle
        "history" -> Icons.Default.History
        "notifications", "bell" -> Icons.Default.Notifications
        "settings", "gear" -> Icons.Default.Settings
        else -> Icons.Default.Info
    }
}

fun getIconForType(type: LegalDocType): ImageVector {
    return when (type) {
        LegalDocType.PRIVACY_POLICY -> Icons.Default.Shield
        LegalDocType.TERMS_CONDITIONS -> Icons.Default.Assignment
        LegalDocType.REFUND_CANCELLATION -> Icons.Default.CurrencyExchange
        LegalDocType.SUBSCRIPTION_TERMS -> Icons.Default.Star
        LegalDocType.ACCOUNT_DELETION -> Icons.Default.Delete
        LegalDocType.DISCLAIMER -> Icons.Default.Info
        LegalDocType.COPYRIGHT_POLICY -> Icons.Default.Copyright
        LegalDocType.REPORTING_POLICY -> Icons.Default.Flag
        LegalDocType.CONTACT_US -> Icons.Default.ContactSupport
        LegalDocType.FAQ -> Icons.Default.Quiz
        LegalDocType.HELP_CENTER -> Icons.Default.Help
        LegalDocType.ABOUT_US -> Icons.Default.Business
    }
}

// Light theme semantic system
fun getScreenAccentColor(type: LegalDocType?): Color {
    if (type == null) return Color(0xFF2563EB) // Blue for Hub
    return when (type) {
        LegalDocType.PRIVACY_POLICY -> Color(0xFF059669) // Green/Teal
        LegalDocType.TERMS_CONDITIONS -> Color(0xFFD97706) // Orange/Amber
        LegalDocType.REFUND_CANCELLATION -> Color(0xFF2563EB) // Blue
        LegalDocType.FAQ -> Color(0xFF7C3AED) // Purple
        LegalDocType.ABOUT_US -> Color(0xFF4F46E5) // Indigo
        LegalDocType.CONTACT_US -> Color(0xFFE11D48) // Crimson Red
        LegalDocType.HELP_CENTER -> Color(0xFF0D9488) // Teal
        LegalDocType.ACCOUNT_DELETION -> Color(0xFFDC2626) // Dangerous Red
        else -> Color(0xFF2563EB)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalSupportCenterScreen(
    initialSection: String? = null,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {}
) {
    val config = LocalAppConfig.current
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember(config.appId) { AppRepository(database, config.appId) }
    val uriHandler = LocalUriHandler.current

    val allDocs by repository.allLegalDocuments.collectAsStateWithLifecycle()
    val allFaqs by repository.allFaqs.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        com.example.util.FirebaseSyncManager.fetchAndSyncLegalSupport(repository) { success ->
            android.util.Log.d("PrivacyTermsScreen", "Firestore LegalSupport Sync success=$success")
        }
    }

    // Filter documents and FAQs for the active app
    val appDocs = remember(allDocs, config.appId) {
        allDocs.filter { it.isEnabled && (it.mappedAppIds.isEmpty() || it.mappedAppIds.contains(config.appId)) }
    }
    val appFaqs = remember(allFaqs, config.appId) {
        val filtered = allFaqs.filter { it.isEnabled && (it.mappedAppIds.isEmpty() || it.mappedAppIds.contains(config.appId)) }
        if (filtered.isNotEmpty()) filtered else SampleData.getDefaultFaqs()
    }

    var currentSection by remember(initialSection) {
        mutableStateOf<LegalDocType?>(parseSection(initialSection))
    }
    var showDeletionDialog by remember { mutableStateOf(false) }

    val activeDoc = remember(currentSection, appDocs) {
        if (currentSection == null) null
        else appDocs.find { it.type == currentSection }
            ?: SampleData.getDefaultLegalDocs().find { it.type == currentSection }
    }

    val accentColor = getScreenAccentColor(currentSection)

    val handleBack: () -> Unit = {
        if (initialSection != null) {
            onBack()
        } else if (currentSection != null) {
            currentSection = null
        } else {
            onBack()
        }
    }

    BackHandler {
        handleBack()
    }

    val currentTitle = when (currentSection) {
        LegalDocType.FAQ -> "Frequently Asked Questions"
        LegalDocType.PRIVACY_POLICY -> "Privacy Policy"
        LegalDocType.TERMS_CONDITIONS -> "Terms & Conditions"
        LegalDocType.REFUND_CANCELLATION -> "Refund & Cancellation"
        LegalDocType.ABOUT_US -> "About Us"
        LegalDocType.CONTACT_US -> "Contact Us"
        LegalDocType.HELP_CENTER -> "Help Centre"
        LegalDocType.ACCOUNT_DELETION -> "Account & Data Deletion"
        LegalDocType.SUBSCRIPTION_TERMS -> "Subscription Terms"
        LegalDocType.DISCLAIMER -> "Disclaimer"
        LegalDocType.COPYRIGHT_POLICY -> "Copyright Policy"
        LegalDocType.REPORTING_POLICY -> "Question Reporting Policy"
        null -> "Infinity Help & Support"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        val headerIcon = activeDoc?.iconName?.let { getIconForName(it) } ?: currentSection?.let { getIconForType(it) } ?: Icons.Default.Support
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = headerIcon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentTitle,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Infinity Premium Portal",
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        IconButton(
                            onClick = handleBack,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF334155)
                            )
                        }
                        IconButton(
                            onClick = onNavigateToHome,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = Color(0xFF2563EB)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                ),
                modifier = Modifier.shadow(2.dp)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingVals ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
        ) {
            AnimatedContent(
                targetState = currentSection,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "LegalSupportTransition"
            ) { targetSection ->
                when (targetSection) {
                    LegalDocType.FAQ -> {
                        DedicatedFaqView(faqs = appFaqs, accentColor = accentColor)
                    }
                    LegalDocType.CONTACT_US -> {
                        DedicatedContactView(
                            config = config,
                            doc = activeDoc,
                            accentColor = accentColor
                        )
                    }
                    LegalDocType.HELP_CENTER -> {
                        DedicatedHelpCenterView(
                            config = config,
                            doc = activeDoc,
                            onNavigateToFaqs = { currentSection = LegalDocType.FAQ },
                            onNavigateToContact = { currentSection = LegalDocType.CONTACT_US },
                            accentColor = accentColor
                        )
                    }
                    LegalDocType.ACCOUNT_DELETION -> {
                        DedicatedAccountDeletionView(
                            config = config,
                            doc = activeDoc,
                            onDeleteClick = { showDeletionDialog = true },
                            onWebPortalClick = { uriHandler.openUri("https://example.com/delete-account?app=${config.appId}") },
                            accentColor = accentColor
                        )
                    }
                    LegalDocType.ABOUT_US -> {
                        DedicatedAboutUsView(
                            config = config,
                            doc = activeDoc,
                            accentColor = accentColor
                        )
                    }
                    null -> {
                        LegalSupportHubView(
                            appDocs = appDocs,
                            onSelectItem = { selectedType -> currentSection = selectedType }
                        )
                    }
                    else -> {
                        val targetDoc = activeDoc ?: LegalDocument(
                            type = targetSection ?: LegalDocType.PRIVACY_POLICY,
                            title = currentTitle,
                            content = "Content is currently being updated. Please check back shortly.",
                            mappedAppIds = emptyList()
                        )

                        DedicatedLegalDocumentView(
                            doc = targetDoc,
                            appName = config.appName,
                            accentColor = accentColor,
                            onNavigateToContact = { currentSection = LegalDocType.CONTACT_US }
                        )
                    }
                }
            }
        }
    }

    if (showDeletionDialog) {
        DeletionRequestDialog(
            accentColor = accentColor,
            onDismiss = { showDeletionDialog = false },
            onConfirm = {
                showDeletionDialog = false
                Toast.makeText(context, "Your account deletion request has been submitted successfully.", Toast.LENGTH_LONG).show()
                onBack()
            }
        )
    }
}

@Composable
private fun DedicatedLegalDocumentView(
    doc: LegalDocument,
    appName: String,
    accentColor: Color,
    onNavigateToContact: () -> Unit
) {
    val scrollState = rememberScrollState()
    val processedContent = remember(doc.content, appName) {
        doc.content.replace("{{APP_NAME}}", "Infinity")
            .replace("{{COMPANY_NAME}}", "Infinity Exam Systems")
    }

    val sections = remember(doc.sectionsJson) {
        if (!doc.sectionsJson.isNullOrBlank()) {
            try {
                val typeToken = object : TypeToken<List<DocSection>>() {}.type
                Gson().fromJson<List<DocSection>>(doc.sectionsJson, typeToken).sortedBy { it.order }
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()
    }

    // Pre-defined default detailed sections for core policies if database sections are empty
    val fallbackSections = remember(doc.type) {
        when (doc.type) {
            LegalDocType.REFUND_CANCELLATION -> listOf(
                DocSection("ref_1", "1. Strict 24-Hour Review Window", "We understand payment slip-ups happen. We grant a brief 24-hour review period for unintended multiple transactions or subscription plan mistakes."),
                DocSection("ref_2", "2. Accidental Double Charges", "If you are charged twice due to system latency or bank gateway delays, submit both transaction IDs within 24 hours for a full manual refund of the duplicate fee."),
                DocSection("ref_3", "3. No Refund Post Access Consumption", "Once a premium mock test is unlocked, answers submitted, or preparation sets downloaded, we cannot offer any refund due to digital content access."),
                DocSection("ref_4", "4. Cancellation of Active Subscriptions", "You can cancel your subscription plan renewal at any moment. Your premium features will stay active until the current billing cycle expires."),
                DocSection("ref_5", "5. Refund Gateway & Processing Cycle", "All authorized refunds are processed through Razorpay back into the source banking account. This generally takes 5-7 business working days."),
                DocSection("ref_6", "6. Fraudulent Dispute Submissions", "Initiating false payment chargebacks or chargeback fraud on genuine active subscriptions will lead to automatic, instant candidate profile bans."),
                DocSection("ref_7", "7. Dedicated Billing Support Helpdesk", "Need clarification regarding a billing deduction? Reach out directly to our candidate support helpline at billing@infinityprep.com.")
            )
            LegalDocType.PRIVACY_POLICY -> listOf(
                DocSection("priv_1", "1. Information We Collect", "We gather specific data including your display name, registered email address, mobile number, and chosen state/exam filters to set up your account profile."),
                DocSection("priv_2", "2. Academic Performance Tracking", "To generate premium mock insights, dynamic score grids, and daily streaks, we record and store your answered question histories."),
                DocSection("priv_3", "3. Device Logs & Unique ID", "To prevent subscription abuse (such as sharing login details with unentitled users), we lock registration logs to your primary active device ID."),
                DocSection("priv_4", "4. Bank-Grade Encryption Security", "All data transfer between your smartphone and the Infinity databases is shielded using AES-256 and HTTPS secure network protocols."),
                DocSection("priv_5", "5. No Third-Party Selling or Trading", "We adhere to a strict policy: we never sell, rent, lease, or trade your personal email, profile details, or phone contact lists to advertising brokers."),
                DocSection("priv_6", "6. System Cookie Storage usage", "We utilize small secure session keys local to your device to preserve login states and specific dark/light theme choices."),
                DocSection("priv_7", "7. Your Access & Rectification Rights", "You retain absolute control to view, correct, update, export, or permanently erase your user credentials directly from our profile control panel."),
                DocSection("priv_8", "8. Specific Data Retention Guidelines", "We retain mock performance databases as long as your candidate account remains registered and active to keep analytics charts accurate."),
                DocSection("priv_9", "9. Safety of Candidate Children", "Our educational materials are engineered exclusively for adult candidates preparing for governmental recruitment examinations."),
                DocSection("priv_10", "10. Major Policy Revision Notifications", "Whenever we revise privacy steps, we notify you transparently via high-priority push messages or dynamic main screen pop-up notifications."),
                DocSection("priv_11", "11. Dedicated Privacy Support Desk", "For serious privacy inquiries, reach our official data security officer at privacy@infinityprep.com.")
            )
            LegalDocType.TERMS_CONDITIONS -> listOf(
                DocSection("t_1", "1. Binding Agreement Acceptance", "By downloading, registering, or using Infinity, you agree to comply with and be bound by these legal, governing terms."),
                DocSection("t_2", "2. Single Candidate User License", "We grant you a individual, non-exclusive, non-transferable license to access premium exam questions strictly for personal preparation."),
                DocSection("t_3", "3. Candidate Account Security", "You are fully responsible for guarding your mobile OTPs and login details. Do not share your credentials with anyone."),
                DocSection("t_4", "4. Intellectual Property Rights", "The entire bank of MCQs, mock test structures, subject descriptions, explanations, and visual icons are property of Infinity."),
                DocSection("t_5", "5. Billing Gateways & Paid Plans", "Subscription charges are managed safely through encrypted third-party processors. Active plans are non-refundable."),
                DocSection("t_6", "6. Play Store Auto-Renewal Cycle", "Auto-recurring packages renew automatically at the end of the term. You can disable auto-renewal directly inside the Google Play account."),
                DocSection("t_7", "7. Strictly Prohibited Platform Usage", "Redistributing, scraping, screen-recording, or publishing our proprietary question banks onto other websites will trigger legal notices."),
                DocSection("t_8", "8. Accuracy & Material 'As-Is' Warranty", "Our teachers verify questions with extreme care. However, exam materials are delivered 'as-is' without legal warranties of absolute success."),
                DocSection("t_9", "9. Uptime & Server Maintenance Windows", "We aim for 99.9% server availability. Regular system security checks are scheduled during off-peak hours with brief prior notifications."),
                DocSection("t_10", "10. Comprehensive Liability Limitations", "Infinity is not liable for mock score fluctuations, exam results, or smartphone hardware performance compatibility limitations."),
                DocSection("t_11", "11. Terms Breach Account Termination", "Violating intellectual property agreements or attempting system attacks will result in immediate profile bans with no refund of active plans."),
                DocSection("t_12", "12. Governing Jurisdiction & Regulations", "These terms are interpreted and governed under state and federal educational guidelines and laws of the country."),
                DocSection("t_13", "13. Scheduled Amendments & Variations", "We periodically update and vary terms to support new exam features. Continuous usage of the app implies agreement to updated terms.")
            )
            else -> emptyList()
        }
    }

    val activeSections = if (sections.isNotEmpty()) sections else fallbackSections

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Document Meta Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = doc.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "OFFICIAL POLICY",
                            fontSize = 9.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val updatedDate = if (doc.lastUpdated > 0) {
                    SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(doc.lastUpdated))
                } else "15 March 2026"

                Text(
                    text = "Last Updated: $updatedDate",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Quick Summary Box
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = accentColor.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Quick Summary",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (doc.type) {
                            LegalDocType.REFUND_CANCELLATION -> "Digital packages (Mock Tests & Current Affairs) are activated instantly and are non-refundable. Exception reviews are strictly processed for double transactions within 24 hours."
                            LegalDocType.PRIVACY_POLICY -> "Your data security is our top priority. We encrypt progress reports, study preferences, and contact metrics. We never sell your personal information to third-party brokers."
                            LegalDocType.TERMS_CONDITIONS -> "Your access to Infinity mocks and notes constitutes a binding agreement. redistribution or bulk scraping of our exam databases is strictly illegal."
                            else -> "Our policies are designed to maintain academic integrity, transparent billing, and user security. If you have inquiries, write to support."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Sectioned Content Cards
        if (activeSections.isNotEmpty()) {
            activeSections.forEach { section ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = section.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = section.content.replace("{{APP_NAME}}", "Infinity"),
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        } else {
            // Raw Fallback content card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = processedContent,
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Bottom "Need Help" Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Still have questions?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Our candidate care team is available 24/7 to resolve billing, technical, or content inquiries.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onNavigateToContact,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.ContactSupport, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contact Support Hub", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DedicatedFaqView(
    faqs: List<SupportFaq>,
    accentColor: Color
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Account", "Subscription", "Payments", "Mock Tests", "Current Affairs", "App Usage", "Technical", "Refunds")

    // Fallback default FAQs if database list is empty
    val defaultFaqs = remember {
        listOf(
            SupportFaq("df_1", "Can I log in on multiple devices?", "No. To protect candidate account integrity and prevent subscription leakage, Infinity licenses are strictly restricted to one active smartphone device at any given time.", order = 1),
            SupportFaq("df_2", "How do I reset my account password?", "You can perform password resets easily by clicking on 'Forgot Password' directly in the Login portal or raising a quick ticket with support.", order = 2),
            SupportFaq("df_3", "Are there free trial mock tests available?", "Yes, indeed! We provide multiple free full-length mock tests, topical practice sets, and daily current affairs digests to help you explore before upgrading.", order = 3),
            SupportFaq("df_4", "How do I upgrade to the PRO plan?", "Simply tap on the subscription card in your main screen dashboard or profile screen. Select your targets to pay instantly.", order = 4),
            SupportFaq("df_5", "What secure payment gateways are supported?", "We fully support payments via UPI (Google Pay, PhonePe, Paytm), Credit/Debit cards, and Net Banking networks, processed securely through Razorpay.", order = 5),
            SupportFaq("df_6", "Payment was cut but subscription not unlocked.", "Don't worry. Sometimes communication lags occur between banking networks. The amount is automatically unlocked or reversed within 12-24 hours. Ping support for help.", order = 6),
            SupportFaq("df_7", "How is the mock test negative marking computed?", "Negative marking relies on your set metrics. By default, standard governmental APPSC rules apply, subtracting 0.33 marks for each incorrect MCQ.", order = 7),
            SupportFaq("df_8", "Can I pause an active mock test and resume?", "Yes! Infinity is equipped with an auto-save local engine. If your app closes, your mock test remains paused so you can resume exactly where you left off.", order = 8)
        )
    }

    val activeFaqs = if (faqs.isNotEmpty()) faqs else defaultFaqs

    val filteredFaqs = remember(activeFaqs, searchQuery, selectedCategory) {
        activeFaqs.filter {
            val matchesSearch = it.question.contains(searchQuery, ignoreCase = true) ||
                    it.answer.contains(searchQuery, ignoreCase = true)
            val matchesCat = if (selectedCategory == "All") true else {
                // Approximate category matching based on question/answer text keywords
                when (selectedCategory) {
                    "Account" -> it.question.contains("account", ignoreCase = true) || it.question.contains("login", ignoreCase = true) || it.question.contains("reset", ignoreCase = true)
                    "Subscription" -> it.question.contains("subscription", ignoreCase = true) || it.question.contains("pro", ignoreCase = true) || it.question.contains("trial", ignoreCase = true)
                    "Payments" -> it.question.contains("payment", ignoreCase = true) || it.question.contains("gateway", ignoreCase = true) || it.question.contains("charge", ignoreCase = true)
                    "Mock Tests" -> it.question.contains("mock", ignoreCase = true) || it.question.contains("marking", ignoreCase = true) || it.question.contains("resume", ignoreCase = true)
                    "Current Affairs" -> it.question.contains("current", ignoreCase = true) || it.question.contains("affairs", ignoreCase = true)
                    "Refunds" -> it.question.contains("refund", ignoreCase = true) || it.question.contains("reversal", ignoreCase = true)
                    else -> true
                }
            }
            matchesSearch && matchesCat
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Search Input Card
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(12.dp)),
                placeholder = { Text("Search frequently asked questions...", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentColor) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B))
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF1E293B),
                    unfocusedTextColor = Color(0xFF1E293B)
                ),
                singleLine = true
            )
        }

        // Horizontal Category Chips
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF475569)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color.Transparent else Color(0xFFE2E8F0),
                            selectedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        // FAQs Results List
        if (filteredFaqs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Quiz, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No matching FAQs found", color = Color(0xFF64748B), fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        } else {
            items(filteredFaqs) { faq ->
                FaqAccordionItem(faq, accentColor = accentColor)
            }
        }

        // Still Need Help Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Help, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Still have questions?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text("Submit an ticket directly to our candidate desk.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}

@Composable
private fun DedicatedContactView(
    config: com.example.model.AppConfiguration,
    doc: LegalDocument?,
    accentColor: Color
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Parse contact channels from admin if exists
    val adminSettings = remember(doc?.socialLinksJson) {
        if (!doc?.socialLinksJson.isNullOrBlank()) {
            try {
                Gson().fromJson(doc.socialLinksJson, AdminContactSettings::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val email = adminSettings?.supportEmail?.ifBlank { null } ?: config.supportEmail
    val phone = adminSettings?.supportPhone?.ifBlank { null } ?: config.supportPhone
    val whatsappVal = adminSettings?.whatsappNumber?.ifBlank { null } ?: config.whatsappNumber
    val whatsappLink = adminSettings?.whatsappLink?.ifBlank { null } ?: config.whatsappLink
    val telegramVal = adminSettings?.telegramId?.ifBlank { null } ?: config.telegramId
    val telegramLink = adminSettings?.telegramLink?.ifBlank { null } ?: config.telegramLink
    val website = adminSettings?.supportWebsite?.ifBlank { null } ?: config.apiBaseUrl
    val socialLinks = adminSettings?.socialLinks ?: emptyList()

    // Form inputs state
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Billing & Subscriptions") }
    var messageInput by remember { mutableStateOf("") }
    var isScreenshotAttached by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val subjects = listOf("Billing & Subscriptions", "Mock Test Query", "Academic Content Doubts", "App Crash & Sync Errors", "Refund Reversal Requests")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Tagline
        item {
            Column {
                Text(
                    text = "We're Here to Help",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Reach out via our direct support networks below.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Direct support cards 2x2 simulated grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ContactChannelCard(
                            title = "Email Support",
                            value = email.ifBlank { "support@infinity.com" },
                            buttonText = "Send Email",
                            accentColor = accentColor,
                            icon = Icons.Default.Email,
                            onClick = {
                                val targetMail = email.ifBlank { "support@infinity.com" }
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$targetMail")
                                        putExtra(Intent.EXTRA_SUBJECT, "Infinity Support Request")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    uriHandler.openUri("mailto:$targetMail")
                                }
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ContactChannelCard(
                            title = "WhatsApp Desk",
                            value = whatsappVal.ifBlank { "+91 98765 43210" },
                            buttonText = "Chat Now",
                            accentColor = Color(0xFF10B981),
                            icon = Icons.Default.Chat,
                            onClick = {
                                val cleanNum = whatsappVal.ifBlank { "919876543210" }.replace(Regex("[^0-9]"), "")
                                val targetWa = whatsappLink.ifBlank { "https://wa.me/$cleanNum" }
                                try {
                                    uriHandler.openUri(targetWa)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp is unavailable", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ContactChannelCard(
                            title = "Call Helpline",
                            value = phone.ifBlank { "+91 98765 43210" },
                            buttonText = "Call Now",
                            accentColor = accentColor,
                            icon = Icons.Default.Phone,
                            onClick = {
                                val num = phone.ifBlank { "+919876543210" }
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$num")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Calling is not supported", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ContactChannelCard(
                            title = "Business Query",
                            value = "biz@infinity.com",
                            buttonText = "Contact Biz",
                            accentColor = Color(0xFF6366F1),
                            icon = Icons.Default.Business,
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:biz@infinity.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "Infinity Business Inquiry")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    uriHandler.openUri("mailto:biz@infinity.com")
                                }
                            }
                        )
                    }
                }
            }
        }

        // Send a message interactive form card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Send Us a Message",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Your Display Name", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Your Registered Email", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Select Inquiry Subject:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(6.dp))

                    // Chip group for category subjects
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(subjects) { subject ->
                            val isSel = selectedSubject == subject
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedSubject = subject },
                                label = { Text(subject, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = Color(0xFF475569)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        label = { Text("Describe your support query in detail...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated File Attachment Picker Button
                    OutlinedButton(
                        onClick = { isScreenshotAttached = !isScreenshotAttached },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (isScreenshotAttached) Color(0xFF10B981) else Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isScreenshotAttached) Color(0xFFECFDF5) else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (isScreenshotAttached) Icons.Default.CheckCircle else Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = if (isScreenshotAttached) Color(0xFF10B981) else Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isScreenshotAttached) "Screenshot Attached (img_receipt.png)" else "Attach Receipt/Screenshot (Optional)",
                            color = if (isScreenshotAttached) Color(0xFF065F46) else Color(0xFF475569),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (nameInput.isBlank() || emailInput.isBlank() || messageInput.isBlank()) {
                                Toast.makeText(context, "Please fill in all details before submitting.", Toast.LENGTH_SHORT).show()
                            } else {
                                isSubmitting = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Support Request", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Support operational hours
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Candidate Care Hours",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Monday to Saturday: 9:00 AM - 6:00 PM IST\n• Sunday & Holidays: Urgent email ticketing reviews.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    if (isSubmitting) {
        AlertDialog(
            onDismissRequest = { isSubmitting = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Ticket Submitted!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Thank you $nameInput! Your support ticket on '$selectedSubject' has been received successfully.\n\nOur candidate assistants will review your request and contact you at $emailInput within 2-4 working hours.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSubmitting = false
                        nameInput = ""
                        emailInput = ""
                        messageInput = ""
                        isScreenshotAttached = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun ContactChannelCard(
    title: String,
    value: String,
    buttonText: String,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(value, fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                Text(buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DedicatedHelpCenterView(
    config: com.example.model.AppConfiguration,
    doc: LegalDocument?,
    onNavigateToFaqs: () -> Unit,
    onNavigateToContact: () -> Unit,
    accentColor: Color
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val popularTopics = listOf(
        Pair("Getting Started", Icons.Default.Verified),
        Pair("Account & Login", Icons.Default.AccountBox),
        Pair("Mock Tests", Icons.Default.Timer),
        Pair("Subjects & Topics", Icons.Default.MenuBook),
        Pair("Current Affairs", Icons.Default.History),
        Pair("Subscriptions", Icons.Default.Star),
        Pair("Payments", Icons.Default.CurrencyExchange),
        Pair("Refunds", Icons.Default.CurrencyExchange),
        Pair("Technical Issues", Icons.Default.Settings)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Topic Banners
        item {
            Column {
                Text(
                    text = "Infinity Support Desk",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Search popular assistance guidelines and tutorials.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(12.dp)),
                placeholder = { Text("Search for help topics, guides...", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentColor) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF1E293B),
                    unfocusedTextColor = Color(0xFF1E293B)
                ),
                singleLine = true
            )
        }

        // Popular Help Topics Grid heading
        item {
            Text("Popular Help Topics", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }

        // Topics Items
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (i in popularTopics.indices step 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (j in 0..2) {
                            val index = i + j
                            if (index < popularTopics.size) {
                                val item = popularTopics[index]
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(84.dp)
                                        .clickable {
                                            onNavigateToFaqs()
                                        }
                                        .shadow(1.dp, RoundedCornerShape(12.dp))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = item.second,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.first,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF334155),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Popular Questions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Popular Questions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))

                val popularQs = listOf(
                    "Can I log in on multiple devices?",
                    "My secure payment failed but transaction was debited.",
                    "How to reset my support credentials?",
                    "Can I cancel my ongoing subscription package?"
                )

                popularQs.forEach { q ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToFaqs() }
                            .shadow(1.dp, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(q, fontSize = 12.5.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Didn't find support footer
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Didn't find what you're looking for?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Connect with our direct support assistants immediately.", fontSize = 11.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onNavigateToContact,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Connect Contact Support", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DedicatedAboutUsView(
    config: com.example.model.AppConfiguration,
    doc: LegalDocument?,
    accentColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brand Identity Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Infinity Logo
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "INFINITY",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Empowering Aspirants For a Brighter Tomorrow",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Vision & Mission Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Our Vision & Mission", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "To democratize competitive exam preparation and make premium mock tests and current affairs digests accessible to every single aspirant across the nation, regardless of geographical barriers.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF334155),
                    lineHeight = 19.sp
                )
            }
        }

        // What We Do
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("What We Offer", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Infinity delivers high-quality interactive test environments, realistic full-length and topic-wise mock sets, instant timing marking systems, streak analysis, and daily curated syllabus guidelines.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF334155),
                    lineHeight = 19.sp
                )
            }
        }

        // Why we built this
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Why We Built This", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Traditional competitive testing systems are often too rigid, expensive, and out of touch. We designed Infinity as a real-time, responsive, auto-saving exam companion built purely around the candidate's custom requirements.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF334155),
                    lineHeight = 19.sp
                )
            }
        }

        // Feature highlights grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureSmallHighlightCard("Quality Content", "Curated by top teachers", Icons.Default.Verified, accentColor, modifier = Modifier.weight(1f))
                FeatureSmallHighlightCard("Mock Practice", "Instant marks analysis", Icons.Default.Timer, accentColor, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureSmallHighlightCard("Track Progress", "Streaks & reports", Icons.Default.History, accentColor, modifier = Modifier.weight(1f))
                FeatureSmallHighlightCard("Achieve Goals", "Succeed in exams", Icons.Default.Star, accentColor, modifier = Modifier.weight(1f))
            }
        }

        // Footer version details
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "App Version 1.0.0 (Release Build)\nCopyright © 2026 Infinity Systems. All Rights Reserved.",
            color = Color(0xFF94A3B8),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FeatureSmallHighlightCard(
    title: String,
    desc: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.shadow(1.dp, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), textAlign = TextAlign.Center)
            Text(desc, fontSize = 9.5.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun DedicatedHelpCenterViewFallback(
    config: com.example.model.AppConfiguration,
    doc: LegalDocument?,
    accentColor: Color
) {
    // Left empty since DedicatedHelpCenterView already handles everything inside
}

@Composable
private fun DedicatedAccountDeletionView(
    config: com.example.model.AppConfiguration,
    doc: LegalDocument?,
    onDeleteClick: () -> Unit,
    onWebPortalClick: () -> Unit,
    accentColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Permanent Profile Erasure",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Once initiated, your Infinity candidate account, solved mock records, streaks, performance history, bookmarks, and VIP subscription plans are completely deleted and cannot be recovered.",
                        fontSize = 12.sp,
                        color = Color(0xFF7F1D1D),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Deletion instructions
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("How to Request Deletion", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. You can submit a deletion request instantly in-app using the button below.\n2. Complete the safety double-confirmation steps in the dialog window.\n3. Your account deletion will be initiated instantly and completed within 24 hours.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
            }
        }

        // Action Trigger Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Delete Account In-App", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onWebPortalClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    border = BorderStroke(1.dp, Color(0xFF94A3B8))
                ) {
                    Text("External Web Deletion Request", color = Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LegalSupportHubView(
    appDocs: List<LegalDocument>,
    onSelectItem: (LegalDocType) -> Unit
) {
    val scrollState = rememberScrollState()

    val privacyDoc = appDocs.find { it.type == LegalDocType.PRIVACY_POLICY }
    val termsDoc = appDocs.find { it.type == LegalDocType.TERMS_CONDITIONS }
    val refundDoc = appDocs.find { it.type == LegalDocType.REFUND_CANCELLATION }
    val aboutDoc = appDocs.find { it.type == LegalDocType.ABOUT_US }

    val faqDoc = appDocs.find { it.type == LegalDocType.FAQ }
    val helpDoc = appDocs.find { it.type == LegalDocType.HELP_CENTER }
    val contactDoc = appDocs.find { it.type == LegalDocType.CONTACT_US }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Welcome to Infinity Support Desk",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Find solutions regarding digital refunds, privacy safeguards, subscription agreements, academic helplines, or submit direct tickets.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 18.sp
                )
            }
        }

        // Section header
        Text("Legal Policies & Agreements", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LegalMenuCard(
                title = "Refund & Cancellation Policy",
                subtitle = "Rules for digital packages, double transactions",
                icon = Icons.Default.CurrencyExchange,
                accentColor = Color(0xFF2563EB),
                badgeText = "BILLING",
                onClick = { onSelectItem(LegalDocType.REFUND_CANCELLATION) }
            )

            LegalMenuCard(
                title = "Privacy Policy Agreement",
                subtitle = "How we secure your mock performance data",
                icon = Icons.Default.Shield,
                accentColor = Color(0xFF059669),
                badgeText = "SECURE",
                onClick = { onSelectItem(LegalDocType.PRIVACY_POLICY) }
            )

            LegalMenuCard(
                title = "Terms & Conditions Agreement",
                subtitle = "Aspirant license, scraping rules, payments",
                icon = Icons.Default.Assignment,
                accentColor = Color(0xFFD97706),
                badgeText = "RULES",
                onClick = { onSelectItem(LegalDocType.TERMS_CONDITIONS) }
            )

            LegalMenuCard(
                title = "About Infinity Applet",
                subtitle = "Our exam vision, core features, and version",
                icon = Icons.Default.Business,
                accentColor = Color(0xFF4F46E5),
                badgeText = "BRAND",
                onClick = { onSelectItem(LegalDocType.ABOUT_US) }
            )
        }

        // Section support
        Text("Candidate Help & Assistance Desk", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LegalMenuCard(
                title = "Frequently Asked Questions (FAQs)",
                subtitle = "Instant answers, login reset guidelines",
                icon = Icons.Default.Quiz,
                accentColor = Color(0xFF7C3AED),
                badgeText = "Q&A",
                onClick = { onSelectItem(LegalDocType.FAQ) }
            )

            LegalMenuCard(
                title = "Help Centre Guides",
                subtitle = "App tutorials, exam syllabi guides",
                icon = Icons.Default.Help,
                accentColor = Color(0xFF0D9488),
                badgeText = "HUB",
                onClick = { onSelectItem(LegalDocType.HELP_CENTER) }
            )

            LegalMenuCard(
                title = "Contact Support Desk",
                subtitle = "Active call helpline, academic WhatsApp support",
                icon = Icons.Default.ContactSupport,
                accentColor = Color(0xFFE11D48),
                badgeText = "24/7",
                onClick = { onSelectItem(LegalDocType.CONTACT_US) }
            )
        }

        // Danger Deletion
        Text("Account Safety Center", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))

        LegalMenuCard(
            title = "Account & Profile Deletion",
            subtitle = "Initiate instant deletion of candidate profile",
            icon = Icons.Default.Delete,
            accentColor = Color(0xFFDC2626),
            badgeText = "DANGER",
            onClick = { onSelectItem(LegalDocType.ACCOUNT_DELETION) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LegalMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badgeText: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(1.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accentColor.copy(alpha = 0.08f),
                        border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun FaqAccordionItem(faq: SupportFaq, accentColor: Color) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (expanded) accentColor.copy(alpha = 0.35f) else Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .shadow(1.dp, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = faq.question,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (expanded) accentColor else Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = faq.answer,
                        fontSize = 12.5.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DeletionRequestDialog(
    accentColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var isConfirmedCheckbox by remember { mutableStateOf(false) }
    var confirmationInput by remember { mutableStateOf("") }
    val isReady = isConfirmedCheckbox && confirmationInput.trim().uppercase() == "DELETE"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Confirm Profile Deletion", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Please read these deletion conditions carefully:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF334155)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("• ", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        Text("All exam reports, mock metrics, and streak charts are wiped permanently.", fontSize = 11.5.sp, color = Color(0xFF475569))
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Text("• ", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        Text("Active VIP packages are deleted instantly and cannot be refunded.", fontSize = 11.5.sp, color = Color(0xFF475569))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isConfirmedCheckbox, onCheckedChange = { isConfirmedCheckbox = it })
                    Text("I understand that this action is irreversible and final.", fontSize = 11.5.sp, color = Color(0xFF334155))
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Type \"DELETE\" in capitals below to authorize:", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = confirmationInput,
                        onValueChange = { confirmationInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("DELETE") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEF4444)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isReady,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text("Permanently Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF475569))
            }
        }
    )
}
