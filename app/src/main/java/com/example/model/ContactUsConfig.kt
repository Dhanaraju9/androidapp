package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Model representing the configurable Contact Us & Support screen configuration.
 * All properties are manageable via the Admin Panel and persisted persistently.
 */
data class ContactUsConfig(
    // Contact Info (defaults match the pixel reference image)
    val supportEmail: String = "support@infinitylabs.in",
    val emailTitle: String = "Email Us",
    val emailSubtext: String = "Get detailed support via email",
    
    val whatsappNumber: String = "+91 98765 43210",
    val whatsappTitle: String = "Chat on WhatsApp",
    val whatsappSubtext: String = "Quick replies during support hours",
    val whatsappLink: String = "https://wa.me/919876543210",
    
    val telegramLink: String = "t.me/infinitylabs",
    val telegramTitle: String = "Join our Telegram",
    val telegramSubtext: String = "Updates, Notifications & Study Resources",
    
    val phoneNumber: String = "+91 98765 43210",
    
    // Support Timings & Quick Response
    val supportTimingsTitle: String = "Support Timings",
    val supportTimingsLine1: String = "Mon - Sat : 9:00 AM - 6:00 PM",
    val supportTimingsLine2: String = "Sunday : Holiday",
    
    val quickSupportTitle: String = "Quick Support",
    val quickSupportSubtext: String = "We usually respond within 2-4 hours",
    
    // Header & Banner
    val headerTitle: String = "Contact Us",
    val headerSubtitle: String = "We're here to help you",
    val bannerTitle: String = "Your Success Matters",
    val bannerSubtitle: String = "Reach out to us anytime. We're always with you.",
    val fastSupportBadge: String = "Fast Support",
    
    // Quick-Links Grid
    val faqTitle: String = "FAQ",
    val faqSubtitle: String = "Find Answers",
    val faqUrl: String = "",
    
    val helpCenterTitle: String = "Help Center",
    val helpCenterSubtitle: String = "Study Guides",
    val helpCenterUrl: String = "",
    
    val callUsTitle: String = "Call Us",
    
    val communityTitle: String = "Community",
    val communitySubtitle: String = "Learn Together",
    val communityUrl: String = "https://t.me/infinitylabs",
    
    // Bottom Motivational Strip
    val motivationalQuote: String = "“We are here to support your journey. Keep Learning, Keep Growing.”",
    
    // Color Styling Config (Defaults match reference image)
    val screenBgHex: String = "#FFFFFF",
    
    val bannerBgStartHex: String = "#FFFBEB",
    val bannerBgEndHex: String = "#FEF3C7",
    val bannerBorderHex: String = "#FDE68A",
    val bannerShieldBgHex: String = "#FEF3C7",
    val bannerShieldIconHex: String = "#D97706",
    val bannerBadgeBgHex: String = "#FEF3C7",
    val bannerBadgeBorderHex: String = "#FCD34D",
    val bannerBadgeTextHex: String = "#92400E",
    
    val emailBgStartHex: String = "#F0F9FF",
    val emailBgEndHex: String = "#E0F2FE",
    val emailBorderHex: String = "#BAE6FD",
    val emailIconContainerHex: String = "#0284C7",
    val emailTextAccentHex: String = "#0284C7",
    
    val whatsappBgStartHex: String = "#F0FDF4",
    val whatsappBgEndHex: String = "#DCFCE7",
    val whatsappBorderHex: String = "#BBF7D0",
    val whatsappIconContainerHex: String = "#22C55E",
    val whatsappTextAccentHex: String = "#16A34A",
    
    val telegramBgStartHex: String = "#F0F9FF",
    val telegramBgEndHex: String = "#E0F2FE",
    val telegramBorderHex: String = "#BAE6FD",
    val telegramIconContainerHex: String = "#0088CC",
    val telegramTextAccentHex: String = "#0284C7",
    
    val timingsBgStartHex: String = "#FFFBEB",
    val timingsBgEndHex: String = "#FEF3C7",
    val timingsBorderHex: String = "#FDE68A",
    val timingsIconContainerHex: String = "#FDE68A",
    val timingsIconHex: String = "#B45309",
    
    val quickSupportBgStartHex: String = "#F5F3FF",
    val quickSupportBgEndHex: String = "#EDE9FE",
    val quickSupportBorderHex: String = "#DDD6FE",
    val quickSupportIconContainerHex: String = "#DDD6FE",
    val quickSupportIconHex: String = "#7C3AED",
    
    val quickLinksCardBgHex: String = "#FFFFFF",
    val quickLinksBorderHex: String = "#F1F5F9",
    
    val motivationalBgHex: String = "#F0FDF4",
    val motivationalBorderHex: String = "#DCFCE7",
    val motivationalHeartHex: String = "#15803D",
    val motivationalTextHex: String = "#1E293B",
    
    val primaryTextHex: String = "#0F172A",
    val secondaryTextHex: String = "#64748B",
    
    val cardCornerRadiusDp: Int = 18
) {
    companion object {
        fun parseHexColor(hex: String, defaultColor: Color): Color {
            return try {
                val clean = hex.removePrefix("#")
                val longVal = clean.toLong(16)
                if (clean.length == 6) {
                    Color(0xFF000000L or longVal)
                } else if (clean.length == 8) {
                    Color(longVal)
                } else {
                    defaultColor
                }
            } catch (e: Exception) {
                defaultColor
            }
        }
    }
}
