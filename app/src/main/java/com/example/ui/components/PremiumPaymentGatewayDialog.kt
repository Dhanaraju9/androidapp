package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PaymentGatewayConfigRepository

/**
 * Premium Payment Gateway Selection Modal
 * Renders 100% pixel-to-pixel match with reference image PAYMENT GATE.png
 * utilizing SelectPaymentGatewayContent.
 */
@Composable
fun PremiumPaymentGatewayDialog(
    planTitle: String,
    planSubtitle: String,
    originalPrice: Int,
    finalPrice: Int,
    couponCode: String? = null,
    discountAmount: Int = 0,
    selectedGateway: String,
    onGatewaySelected: (String) -> Unit,
    selectedPaymentMode: String = "Razorpay Gateway",
    onPaymentModeSelected: (String) -> Unit = {},
    upiIdInput: String = "",
    onUpiIdChanged: (String) -> Unit = {},
    merchantName: String = "",
    merchantId: String = "",
    accountId: String = "",
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onRefundPolicyClick: () -> Unit = {},
    onProceedPayment: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activeConfig by PaymentGatewayConfigRepository.config.collectAsState()
    val detectedUpiApps = remember(context) { com.example.model.UpiAppDetector.getInstalledUpiApps(context) }

    // Map internal gateway keys: "play_billing" or "razorpay"
    val mappedGateway = if (selectedGateway == "play_billing" || selectedGateway == "gplay") "play_billing" else "razorpay"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            SelectPaymentGatewayContent(
                config = activeConfig,
                selectedGateway = mappedGateway,
                onGatewaySelected = { gateway ->
                    onGatewaySelected(if (gateway == "play_billing") "play_billing" else "razorpay")
                },
                customPlanTitle = planTitle,
                customPlanSubtitle = planSubtitle,
                customOriginalPrice = "₹$originalPrice",
                customCurrentPrice = "₹$finalPrice",
                customSavingsText = if (discountAmount > 0) "Coupon Savings ₹$discountAmount" else "You Save ₹${originalPrice - finalPrice}",
                detectedUpiApps = detectedUpiApps,
                onBackClick = onDismiss,
                onProceedClick = onProceedPayment,
                onTermsClick = onTermsClick,
                onPrivacyClick = onPrivacyClick,
                onRefundPolicyClick = onRefundPolicyClick
            )
        }
    }
}
