package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * PaymentGatewayConfig defines the customizable visual and content parameters
 * for the Select Payment Gateway Screen.
 *
 * DEFAULT values reproduce 100% pixel-to-pixel match with the reference image (PAYMENT GATE.png).
 */
data class PaymentGatewayConfig(
    // 1. TOP HEADER BAR
    val topBarTitle: String = "APPSC GROUP-2",
    val topBarSubtitle: String = "Learn • Practice • Succeed",
    val topBarBgColorHex: String = "#F8FAFC",
    
    // 2. PAGE HEADER
    val pageHeading: String = "Choose Your Payment Method",
    val pageSubtitle: String = "Select a secure payment option to continue your subscription",
    
    // 3. TRUST INDICATORS
    val trust1Title: String = "Secure Payments",
    val trust1Subtitle: String = "Your data is protected",
    val trust1IconColorHex: String = "#10B981",
    
    val trust2Title: String = "Fast & Easy",
    val trust2Subtitle: String = "Complete in seconds",
    val trust2IconColorHex: String = "#6366F1",
    
    val trust3Title: String = "Trusted by Students",
    val trust3Subtitle: String = "Safe & Reliable",
    val trust3IconColorHex: String = "#0EA5E9",

    // 4. SELECTED PLAN CARD
    val selectedPlanBadgeText: String = "★ Selected Plan",
    val selectedPlanBadgeBgHex: String = "#3B82F6",
    val planCardTitle: String = "Premium Plan",
    val planCardDurationText: String = "12 Months   |   Full Access",
    val originalPriceText: String = "₹1,999",
    val currentPriceText: String = "₹999",
    val savingsBadgeText: String = "You Save ₹1,000 (50%)",
    val savingsBadgeBgHex: String = "#DCFCE7",
    val savingsBadgeTextHex: String = "#15803D",
    val benefitsList: List<String> = listOf(
        "Full Syllabus Access",
        "All Mock Tests",
        "Current Affairs (MCQs)",
        "Performance Analytics",
        "AI Study Companion"
    ),
    val planCardBgHex: String = "#EFF6FF",
    val planCardBorderHex: String = "#BFDBFE",

    // 5. PAYMENT GATEWAY 1: GOOGLE PLAY BILLING
    val gplayTitle: String = "Google Play Billing",
    val gplaySubtitle: String = "Pay securely through Google Play",
    val gplayBadgeText: String = "★ Recomended",
    val gplayBadgeBgHex: String = "#DBEAFE",
    val gplayBadgeTextHex: String = "#2563EB",
    val gplayBullets: List<String> = listOf(
        "Secure Google Play checkout",
        "Supports Google Play payment methods",
        "Subscription managed through Google Play"
    ),
    val gplaySelectedBgHex: String = "#F0F9FF",
    val gplaySelectedBorderHex: String = "#2563EB",

    // 6. PAYMENT GATEWAY 2: RAZORPAY
    val razorpayTitle: String = "Razorpay",
    val razorpaySubtitle: String = "Pay securely with Razorpay",
    val razorpayBadgeText: String = "💳 More Payment Options",
    val razorpayBadgeBgHex: String = "#DCFCE7",
    val razorpayBadgeTextHex: String = "#15803D",
    val razorpayBullets: List<String> = listOf(
        "UPI, Cards, Net Banking & more",
        "Secure Razorpay checkout",
        "Instant payment confirmation",
        "Supports all major payment methods"
    ),
    val razorpayUnselectedBgHex: String = "#FFFFFF",
    val razorpayUnselectedBorderHex: String = "#E2E8F0",
    val razorpaySelectedBgHex: String = "#F0F9FF",
    val razorpaySelectedBorderHex: String = "#2563EB",

    // 7. PRIMARY CTA
    val ctaGooglePlayText: String = "Continue with Google Play",
    val ctaRazorpayText: String = "Continue with Razorpay",
    val ctaBgHex: String = "#2563EB",

    // 8. TRUST FOOTER
    val footerTrust1Title: String = "Secure Transaction",
    val footerTrust1Sub: String = "Your information is safe",
    val footerTrust2Title: String = "Trusted Platforms",
    val footerTrust2Sub: String = "Google Play  |  Razorpay",
    val footerTrust3Title: String = "Millions of Students",
    val footerTrust3Sub: String = "Already learning with us",
    val footerPolicyText: String = "You can change or cancel your subscription anytime as per our policy.",
    val footerTermsLinkText: String = "View Subscription Terms"
) {
    companion object {
        val DEFAULT = PaymentGatewayConfig()
    }
}


