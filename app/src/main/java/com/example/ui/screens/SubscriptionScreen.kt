package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.SampleData
import coil.compose.AsyncImage
import com.example.ui.components.PremiumPaymentGatewayDialog
import com.example.ui.theme.AppLuminousBackground

data class ExactSubPlan(
    val id: String,
    val durationLabel: String,
    val subtitle: String,
    val price: Int,
    val originalPrice: Int,
    val effectiveMonthly: String,
    val discountText: String,
    val iconEmoji: String,
    val buttonColor: Color,
    val cardBgColor: Color,
    val borderColor: Color,
    val isBestValue: Boolean = false,
    val features: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {}
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State variables
    var selectedPlanId by remember { mutableStateOf("12_months") }
    var enableOneDayPass by remember { mutableStateOf(false) }
    var couponInput by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf<String?>(null) }
    var couponDiscountAmount by remember { mutableStateOf(0) }

    // Payment Checkout Modal State
    var showCheckoutModal by remember { mutableStateOf(false) }
    var selectedGateway by remember { mutableStateOf("play_billing") } // "play_billing" or "razorpay"
    var selectedPaymentMode by remember { mutableStateOf("upi") }
    var upiIdInput by remember { mutableStateOf("") }

    // Active verified entitlement & detected installed UPI apps
    val activeEntitlement by com.example.service.SubscriptionPaymentVerificationService.getInstance(context)
        .getActiveSubscriptionFlow(SampleData.currentActiveAppId.value)
        .collectAsState(initial = null)
    val detectedUpiApps = remember(context) { com.example.model.UpiAppDetector.getInstalledUpiApps(context) }
    var showLegalDialog by remember { mutableStateOf<String?>(null) }

    // Initialize Play Billing connection & PurchaseObserver
    LaunchedEffect(Unit) {
        com.example.util.BillingPaymentManager.initializePlayBilling(context)
        com.example.util.PurchaseObserver.initialize(context)
    }

    val activeAppId = SampleData.currentActiveAppId.value
    val repoPlans by com.example.data.SubscriptionRepository.plans.collectAsState()
    val repoBackgrounds by com.example.data.SubscriptionRepository.backgrounds.collectAsState()
    
    val activeBg = repoBackgrounds
        .filter { it.isEnabled && (it.mappedAppIds.contains("ALL") || it.mappedAppIds.contains(activeAppId)) }
        .sortedBy { it.order }
        .firstOrNull()

    val plans = repoPlans
        .filter { it.status == com.example.model.PlanStatus.ACTIVE && !it.isSoftDeleted }
        .filter { it.targetAppIds.isEmpty() || it.targetAppIds.contains("ALL") || it.targetAppIds.contains(activeAppId) }
        .sortedBy { it.displayOrder }
        .map { plan ->
            val formattedTitle = plan.name
            ExactSubPlan(
                id = plan.id,
                durationLabel = formattedTitle,
                subtitle = plan.subtitle,
                price = plan.price,
                originalPrice = plan.originalPrice,
                effectiveMonthly = plan.effectiveMonthly,
                discountText = plan.discountText,
                iconEmoji = plan.iconEmoji,
                buttonColor = Color(android.graphics.Color.parseColor(plan.buttonColorHex)),
                cardBgColor = Color(android.graphics.Color.parseColor(plan.cardBgColorHex)),
                borderColor = Color(android.graphics.Color.parseColor(plan.borderColorHex)),
                isBestValue = plan.isBestValue,
                features = plan.features
            )
        }
    
    // Fallback if no active plans
    val currentPlan = plans.find { it.id == selectedPlanId } ?: plans.firstOrNull() ?: ExactSubPlan(
        id = "fallback",
        durationLabel = "Fallback",
        subtitle = "No active plans",
        price = 0,
        originalPrice = 0,
        effectiveMonthly = "",
        discountText = "",
        iconEmoji = "❓",
        buttonColor = Color.Gray,
        cardBgColor = Color.LightGray,
        borderColor = Color.DarkGray,
        isBestValue = false,
        features = emptyList()
    )
    val finalPrice = if (enableOneDayPass) {
        49
    } else {
        (currentPlan.price - couponDiscountAmount).coerceAtLeast(49)
    }

    AppLuminousBackground(screenName = "Subscription Screen") {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Bottom Action Dock matching subscription.png
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (enableOneDayPass) "✨ 1-Day VIP Pass selected (₹49)"
                                    else "🚀 Proceeding to secure checkout for ${currentPlan.durationLabel} (${currentPlan.subtitle}) - ₹$finalPrice"
                                )
                            }
                            showCheckoutModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF2563EB),
                                        Color(0xFF7C3AED),
                                        Color(0xFFDB2777)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (enableOneDayPass) "Get 1-Day VIP Pass (₹49) →" else "Proceed to Secure Checkout →",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Instant Access • Encrypted & Verified Billing (Google Play • Razorpay)",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Transparent)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
            // =========================================================================
            // 1. TOP HERO BANNER (APPSC GROUP-2 & ANDHRA PRADESH MONTAGE)
            // =========================================================================
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (activeBg != null) Modifier.background(Color.Black)
                            else Modifier.background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1E1B4B), // Deep Indigo
                                        Color(0xFF312E81),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                        )
                ) {
                    if (activeBg != null) {
                        AsyncImage(
                            model = activeBg.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            alpha = 0.6f
                        )
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        // Top Navigation Row: Back Arrow, Home, Title, Help
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = onNavigateToHome,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Home",
                                        tint = Color(0xFF93C5FD),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = SampleData.getActiveApp().name.uppercase(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Prepare | Practice | Perform | Serve Andhra Pradesh",
                                    color = Color(0xFFFDE047), // Gold
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable {
                                        Toast.makeText(context, "Support Hotline: 1800-APPSC-2026", Toast.LENGTH_LONG).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Need Help?",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Inspiring Value Proposition Card
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1E1035),
                            border = BorderStroke(1.2.dp, Color(0xFFFBBF24).copy(alpha = 0.6f)),
                            shadowElevation = 6.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF2E1065).copy(alpha = 0.95f),
                                                Color(0xFF1E1B4B).copy(alpha = 0.98f)
                                            )
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // 1. "One Movie Ticket Cost ,Can Change Your Life" - High-impact Golden Ribbon/Badge
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF59E0B).copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.8f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ConfirmationNumber,
                                                contentDescription = null,
                                                tint = Color(0xFFFDE047),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "One Movie Ticket Cost ,Can Change Your Life",
                                                color = Color(0xFFFEF08A),
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 0.2.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // 2. Telugu Inspirational Lines (Placed right below)
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF0F172A).copy(alpha = 0.55f),
                                        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.35f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(3.5.dp)
                                                    .height(64.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(Color(0xFFFDE047), Color(0xFFEC4899), Color(0xFF8B5CF6))
                                                        )
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "“నీ కోసం ఒక అడుగు…\nనీ కుటుంబం కోసం ఒక నిర్ణయం…\nనీ రాబోయే తరాల కోసం ఒక బలమైన భవిష్యత్తు.”",
                                                color = Color.White,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                lineHeight = 22.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 1B. CURRENT SUBSCRIPTION STATUS & EXPIRY/RENEWAL
            // =========================================================================
            item {
                CurrentSubscriptionStatusCard(
                    activeEntitlement = activeEntitlement,
                    onManageClick = {
                        val provName = if (activeEntitlement?.provider == "GOOGLE_PLAY") "Google Play Store" else "Razorpay Gateway"
                        Toast.makeText(context, "Subscription managed through $provName", Toast.LENGTH_LONG).show()
                    }
                )
            }

            // =========================================================================
            // 2. 1-DAY PASS FACILITY TOGGLE CARD (USER REQUEST)
            // =========================================================================
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (enableOneDayPass) Color(0xFFFEF3C7) else Color.White,
                    border = BorderStroke(
                        width = if (enableOneDayPass) 2.dp else 1.dp,
                        color = if (enableOneDayPass) Color(0xFFD97706) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { enableOneDayPass = !enableOneDayPass }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (enableOneDayPass) Color(0xFFD97706) else Color(0xFFF1F5F9),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = if (enableOneDayPass) Color.White else Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "1 Day VIP Pass",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981)
                                    ) {
                                        Text(
                                            text = "₹49 ONLY",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Full access for 24 hours.",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }

                        Checkbox(
                            checked = enableOneDayPass,
                            onCheckedChange = { enableOneDayPass = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD97706))
                        )
                    }
                }
            }

            // =========================================================================
            // 4. COUPON CODE APPLY FACILITY (USER REQUEST)
            // =========================================================================
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Apply Coupon Code",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = couponInput,
                                onValueChange = { couponInput = it },
                                placeholder = { Text("e.g. APPSC50 or GROUP2") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val code = couponInput.trim().uppercase()
                                    if (code.isNotEmpty()) {
                                        val result = com.example.data.CouponRepository.validateAndGetCoupon(
                                            code = code,
                                            cartAmount = currentPlan.price.toDouble(),
                                            planId = currentPlan.id,
                                            currentUsesByUser = 0,
                                            currentAppId = activeAppId
                                        )
                                        
                                        if (result.isSuccess) {
                                            val validCoupon = result.getOrThrow()
                                            val discount = validCoupon.calculateDiscount(currentPlan.price.toDouble()).toInt()
                                            
                                            appliedCoupon = validCoupon.code
                                            couponDiscountAmount = discount
                                            Toast.makeText(context, "🎉 Coupon ${validCoupon.code} applied! ₹$discount OFF", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val errorMsg = result.exceptionOrNull()?.message ?: "Invalid coupon code"
                                            appliedCoupon = null
                                            couponDiscountAmount = 0
                                            Toast.makeText(context, "❌ $errorMsg", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text("Apply", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (appliedCoupon != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "✅ Coupon '$appliedCoupon' applied successfully (-₹$couponDiscountAmount)",
                                fontSize = 11.5.sp,
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 5. MAIN CONTENT: PREMIUM PLANS
            // =========================================================================
            // PREMIUM PLANS CARDS (EXACT MATCH TO subscription.png)
            items(plans) { plan ->
                val isSelected = selectedPlanId == plan.id && !enableOneDayPass
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = plan.cardBgColor,
                        border = BorderStroke(
                            width = if (plan.isBestValue || isSelected) 3.dp else 2.5.dp,
                            color = if (plan.isBestValue) Color(0xFFD97706) else if (isSelected) plan.buttonColor else plan.borderColor
                        ),
                        shadowElevation = if (plan.isBestValue || isSelected) 8.dp else 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPlanId = plan.id
                                enableOneDayPass = false
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Header Row: Emoji icon + Duration & Subtitle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = plan.iconEmoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = plan.durationLabel,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = plan.subtitle,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedPlanId = plan.id
                                        enableOneDayPass = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = plan.buttonColor)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Pricing Row: ₹999 + Strikethrough + Discount Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "₹${plan.price}",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = plan.effectiveMonthly,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "₹${plan.originalPrice}",
                                            fontSize = 13.sp,
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Medium,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    }
                                }

                                // Discount Pill
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDCFCE7),
                                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                                ) {
                                    Text(
                                        text = plan.discountText,
                                        color = Color(0xFF15803D),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = plan.borderColor.copy(alpha = 0.7f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(14.dp))

                            // Features List
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                plan.features.forEach { feature ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = plan.buttonColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = feature,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF1E293B),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Choose Plan Button
                                Button(
                                    onClick = {
                                        selectedPlanId = plan.id
                                        enableOneDayPass = false
                                        showCheckoutModal = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = plan.buttonColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(46.dp)
                                ) {
                                    Text(
                                        text = "Choose Plan →",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Crown badge removed as per request
                    }
                }

            // =========================================================================
            // 6. PREMIUM FEATURES GRID (8 ICONS)
            // =========================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Premium Features",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val featuresGrid = listOf(
                        Triple("All Subjects Unlocked", Icons.Default.LockOpen, Color(0xFFEFF6FF)),
                        Triple("Unlimited MCQs", Icons.Default.Quiz, Color(0xFFF0FDF4)),
                        Triple("Mock Tests", Icons.Default.Assignment, Color(0xFFFEF3C7)),
                        Triple("Detailed Analysis", Icons.Default.BarChart, Color(0xFFFEE2E2)),
                        Triple("Daily Current Affairs", Icons.Default.Today, Color(0xFFF5F3FF)),
                        Triple("Study Material", Icons.Default.PictureAsPdf, Color(0xFFECFDF5)),
                        Triple("Strategy Videos", Icons.Default.PlayCircle, Color(0xFFFFFBEB)),
                        Triple("Priority Support", Icons.Default.HeadsetMic, Color(0xFFFDF2F8))
                    )

                    // 4 columns grid using 2 rows of 4
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        featuresGrid.chunked(4).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { (label, icon, bg) ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = bg,
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(90.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = Color(0xFF1E293B),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B),
                                                textAlign = TextAlign.Center,
                                                lineHeight = 12.sp,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 7. CHOOSE PAYMENT METHOD (GOOGLE PLAY & RAZORPAY / ALTERNATIVE BILLING)
            // =========================================================================
            item {
                ChoosePaymentMethodCard(
                    selectedGateway = selectedGateway,
                    onSelectGateway = { selectedGateway = it },
                    detectedUpiApps = detectedUpiApps,
                    onProceed = { gateway ->
                        selectedGateway = gateway
                        showCheckoutModal = true
                    }
                )
            }

            // =========================================================================
            // 7B. MANDATORY LEGAL NOTICE & POLICY ACCESS
            // =========================================================================
            item {
                LegalNoticeAndLinksCard(
                    onTermsClick = { showLegalDialog = "terms" },
                    onPrivacyClick = { showLegalDialog = "privacy" },
                    onRefundClick = { showLegalDialog = "refund" }
                )
            }

            // =========================================================================
            // 8. FOOTER QUOTE
            // =========================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "“Better Citizens • Stronger Andhra Pradesh”",
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // =============================================================================
        // PREMIUM CHECKOUT MODAL DIALOG
        // =============================================================================
        if (showCheckoutModal) {
            val selectedPlan = plans.find { it.id == selectedPlanId }
            val is1DayPass = enableOneDayPass
            val planTitle = if (is1DayPass) "1-Day VIP Pass (24 Hours Access)" else currentPlan.durationLabel
            val planSubtitle = if (is1DayPass) "Unlimited Mock Tests & Practice sets" else currentPlan.subtitle
            val origPrice = if (is1DayPass) 49 else currentPlan.originalPrice
            val durationHours = if (is1DayPass) 24L else {
                when(selectedPlan?.id) {
                    "1_month" -> 30 * 24L
                    "3_months" -> 90 * 24L
                    "6_months" -> 180 * 24L
                    "12_months" -> 365 * 24L
                    else -> 24L
                }
            }

            PremiumPaymentGatewayDialog(
                planTitle = planTitle,
                planSubtitle = planSubtitle,
                originalPrice = origPrice,
                finalPrice = finalPrice,
                couponCode = appliedCoupon,
                discountAmount = couponDiscountAmount,
                selectedGateway = selectedGateway,
                onGatewaySelected = { selectedGateway = it },
                selectedPaymentMode = selectedPaymentMode,
                onPaymentModeSelected = { selectedPaymentMode = it },
                upiIdInput = upiIdInput,
                onUpiIdChanged = { upiIdInput = it },
                merchantName = com.example.util.BillingPaymentManager.merchantName,
                merchantId = com.example.util.BillingPaymentManager.merchantId,
                accountId = com.example.util.BillingPaymentManager.accountId,
                onProceedPayment = {
                    if (appliedCoupon != null) {
                        val coupon = com.example.data.CouponRepository.coupons.value.find { it.code.equals(appliedCoupon, ignoreCase = true) }
                        if (coupon != null) {
                            com.example.data.CouponRepository.recordCouponUsage(coupon.id, couponDiscountAmount.toDouble())
                        }
                    }

                    if (selectedGateway == "play_billing") {
                        com.example.util.BillingPaymentManager.launchPlayBillingCheckout(
                            context = context,
                            planId = if (is1DayPass) "1_day_pass" else selectedPlanId,
                            planTitle = planTitle,
                            amountRupees = finalPrice,
                            durationHours = durationHours,
                            onSuccess = {
                                showCheckoutModal = false
                                onBack()
                            }
                        )
                    } else {
                        com.example.util.BillingPaymentManager.launchRazorpayCheckout(
                            context = context,
                            planTitle = planTitle,
                            amountRupees = finalPrice,
                            paymentMode = selectedPaymentMode,
                            upiId = upiIdInput,
                            durationHours = durationHours,
                            onSuccess = {
                                showCheckoutModal = false
                                onBack()
                            }
                        )
                    }
                },
                onTermsClick = { showLegalDialog = "terms" },
                onPrivacyClick = { showLegalDialog = "privacy" },
                onRefundPolicyClick = { showLegalDialog = "refund" },
                onDismiss = { showCheckoutModal = false }
            )
        }

        if (showLegalDialog != null) {
            LegalPolicyModal(
                policyType = showLegalDialog!!,
                onDismiss = { showLegalDialog = null }
            )
        }
      }
    }
  }
}

@Composable
private fun PaymentLogoBadge(name: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PaymentMethodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else Color(0xFF334155),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ExactCrownBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(52.dp)
            .shadow(6.dp, CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD700), // Gold
                        Color(0xFFF59E0B),
                        Color(0xFFB45309)
                    )
                ),
                shape = CircleShape
            )
            .padding(3.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2563EB), // Royal Blue
                        Color(0xFF1E3A8A),
                        Color(0xFF0F172A)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFFFDE047),
            modifier = Modifier
                .size(14.dp)
                .align(Alignment.TopStart)
                .offset(x = 4.dp, y = 6.dp)
        )
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFDE047),
            modifier = Modifier
                .size(10.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-4).dp, y = (-6).dp)
        )

        Text(
            text = "👑",
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

// =============================================================================
// UNIFIED SUBSCRIPTION STATUS & EXPIRY CARD
// =============================================================================
@Composable
private fun CurrentSubscriptionStatusCard(
    activeEntitlement: com.example.db.UserEntitlementEntity?,
    onManageClick: () -> Unit
) {
    val isPremium = activeEntitlement != null && activeEntitlement.status == "ACTIVE" && activeEntitlement.expiryTimestamp > System.currentTimeMillis()

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isPremium) Color(0xFF0F172A) else Color.White,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isPremium) Color(0xFF10B981) else Color(0xFFE2E8F0)
        ),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isPremium) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPremium) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isPremium) Color(0xFF10B981) else Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isPremium) activeEntitlement!!.planTitle else "Free Aspirant Tier",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = if (isPremium) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = if (isPremium) "Unified Verified Premium Entitlement" else "Basic access only",
                            fontSize = 11.5.sp,
                            color = if (isPremium) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPremium) Color(0xFF10B981) else Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = if (isPremium) "ACTIVE PREMIUM" else "FREE",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isPremium) Color.White else Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (isPremium && activeEntitlement != null) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                val expiryFormatted = dateFormat.format(java.util.Date(activeEntitlement.expiryTimestamp))
                val providerLabel = if (activeEntitlement.provider == "GOOGLE_PLAY") "Google Play Billing" else if (activeEntitlement.provider == "ADMIN_GRANTED") "Admin Granted Subscription" else "Razorpay Alternative Billing"

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Payment Provider:", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                        Text(providerLabel, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6EE7B7))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Valid Until:", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                        Text(expiryFormatted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Renewal Status:", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                        Text(
                            if (activeEntitlement.autoRenew) "Auto-Renew Active" else "One-Time Validity Pass",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Transaction ID:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(activeEntitlement.transactionId.take(20), fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            } else {
                Text(
                    text = "Upgrade below to unlock full mock tests, all subject question banks, daily practice sets, and detailed performance rankings.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

// =============================================================================
// CHOOSE PAYMENT METHOD SECTION (GOOGLE PLAY & RAZORPAY ALTERNATIVE BILLING)
// =============================================================================
@Composable
private fun ChoosePaymentMethodCard(
    selectedGateway: String,
    onSelectGateway: (String) -> Unit,
    detectedUpiApps: List<com.example.model.DetectedUpiApp>,
    onProceed: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    text = "Choose Payment Method",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Google Play Billing & India Alternative Billing (Razorpay)",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Google Play Card
            val isPlay = selectedGateway == "play_billing"
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isPlay) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                border = BorderStroke(if (isPlay) 2.dp else 1.dp, if (isPlay) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectGateway("play_billing") }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isPlay,
                                onClick = { onSelectGateway("play_billing") },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Google Play",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDBEAFE)
                        ) {
                            Text(
                                text = "Google Play Billing",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Pay via Google Play supported methods with Google Play security and family sharing:",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )

                    // Google Play supported payment methods chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("💳 Credit/Debit Cards", "🏦 Net Banking", "📱 Google Pay / UPI", "💰 Play Balance", "🎁 Redeem Code").forEach { method ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                            ) {
                                Text(
                                    text = method,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E40AF),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onProceed("play_billing") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Continue with Google Play →", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Razorpay / Alternative Billing Card
            val isRazorpay = selectedGateway == "razorpay"
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isRazorpay) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                border = BorderStroke(if (isRazorpay) 2.dp else 1.dp, if (isRazorpay) Color(0xFF10B981) else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectGateway("razorpay") }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isRazorpay,
                                onClick = { onSelectGateway("razorpay") },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Razorpay / Alternative Billing",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "India User Choice",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Pay via UPI (Instant QR / App Intent), Debit & Credit Cards, Net Banking, and Wallets.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )

                    // Razorpay Supported Payment Methods
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("⚡ Razorpay", "📲 UPI", "💳 Cards", "🏦 Net Banking", "👛 Wallets").forEach { method ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFECFDF5),
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                            ) {
                                Text(
                                    text = method,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF065F46),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Detected apps on device (no hardcoding of unavailable apps)
                    if (detectedUpiApps.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Installed UPI Apps:",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            detectedUpiApps.forEach { app ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Text(
                                        text = "${app.iconEmoji} ${app.displayName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "💡 Supported: Google Pay, PhonePe, Paytm, BHIM & any UPI ID via Instant QR/Intent",
                            fontSize = 10.5.sp,
                            color = Color(0xFF64748B),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    Button(
                        onClick = { onProceed("razorpay") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Continue with Razorpay / UPI →", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// =============================================================================
// MANDATORY LEGAL NOTICE & POLICY LINKS CARD
// =============================================================================
@Composable
private fun LegalNoticeAndLinksCard(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onRefundClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "By purchasing, you agree to our Terms & Conditions and Refund & Cancellation Policy.",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terms & Conditions",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onTermsClick() }
                )
                Text("•", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Text(
                    text = "Privacy Policy",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onPrivacyClick() }
                )
                Text("•", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Text(
                    text = "Refund & Cancellation",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onRefundClick() }
                )
            }
        }
    }
}

// =============================================================================
// LEGAL POLICY VIEWER MODAL
// =============================================================================
@Composable
private fun LegalPolicyModal(
    policyType: String,
    onDismiss: () -> Unit
) {
    val title = when (policyType) {
        "terms" -> "Terms & Conditions"
        "privacy" -> "Privacy Policy"
        else -> "Refund & Cancellation Policy"
    }

    val content = when (policyType) {
        "terms" -> """
            APPSC Prep Portal - Subscription Terms & Conditions
            
            1. Entitlement & Access:
            Premium subscriptions grant non-transferable, instant digital access to comprehensive syllabus MCQs, full-length mock tests, current affairs test series, and performance analytics for Andhra Pradesh competitive exams.
            
            2. Payment Processors:
            Subscriptions can be purchased via Google Play Billing or approved India Alternative Billing (Razorpay). Both payment methods grant the identical Premium entitlement across all devices logged into the user's account.
            
            3. Verification & Activation:
            Premium features are activated exclusively upon server-side signature verification of the payment transaction. Client-side payment callbacks without backend validation do not grant entitlement.
            
            4. User Choice Billing (India):
            In accordance with Google Play policies in India, eligible users are presented with a choice between Google Play Billing and Razorpay Alternative Billing. Service fees and transaction reporting conform with Play Console declarations.
        """.trimIndent()
        "privacy" -> """
            APPSC Prep Portal - Privacy Policy & Data Safety
            
            1. Information Collection:
            We collect basic contact details (email, phone number) for authentication, account synchronization, and transaction verification. Payment credentials (card numbers, UPI PINs, bank details) are processed directly by certified PCI-DSS compliant gateways (Razorpay, Google Play) and are never stored on our servers.
            
            2. Device Queries:
            Device installed app queries are restricted strictly to approved payment apps (such as Google Pay, PhonePe, Paytm) to facilitate native UPI intent checkout flows.
            
            3. Data Retention:
            Verified payment transaction identifiers and entitlement periods are retained securely to fulfill subscription renewals, receipt lookup, and customer support inquiries.
        """.trimIndent()
        else -> """
            APPSC Prep Portal - Refund & Cancellation Policy
            
            1. Digital Content Policy:
            Subscriptions provide immediate access to digital study materials, test series, and analytical tools. 
            
            2. Google Play Purchases:
            For subscriptions purchased through Google Play Billing, refund requests can be submitted directly through the Google Play Store order history within 48 hours of transaction in accordance with Google Play refund policies.
            
            3. Razorpay Alternative Billing:
            If a payment was debited but the transaction failed or duplicate charges occurred due to network interruption, refunds are processed automatically to the original payment source (UPI/Bank/Card) within 5 to 7 business days.
            
            4. Support Inquiries:
            For refund queries, reach out to academic support at support@appscprep.com with your verified Transaction ID.
        """.trimIndent()
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.80f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = content,
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
