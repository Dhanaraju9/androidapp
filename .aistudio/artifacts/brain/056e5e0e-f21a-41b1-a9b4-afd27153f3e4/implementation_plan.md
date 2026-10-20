# Integrating Official Razorpay Android Standard Checkout Facility

This plan details how to replace the local Razorpay stub with the official **Razorpay Android Standard Checkout SDK** and configure all requested payment channels (**UPI Intent & UPI ID**, **Dynamic UPI QR Code**, **Credit & Debit Cards**, and **NetBanking & Wallets**).

---

## 1. Gradle Dependency & Manifest Configuration

1. **Add Official Razorpay Checkout SDK**:
   - Add `com.razorpay:checkout:1.6.40` to `gradle/libs.versions.toml` and `app/build.gradle.kts`.
   - Delete the local mock stub file `app/src/main/java/com/razorpay/Checkout.kt` so the app uses the real `com.razorpay.Checkout` and `com.razorpay.PaymentResultWithDataListener` classes from the official SDK.
2. **Android 11+ (API 30+) UPI Intent `<queries>` in `AndroidManifest.xml`**:
   - Declare `<queries>` for `upi://pay` intents and major Indian UPI packages (`com.google.android.apps.nbu.paisa.user` for Google Pay, `com.phonepe.app` for PhonePe, `net.one97.paytm` for Paytm, `in.org.npci.upiapp` for BHIM, `com.dreamplug.androidapp` for CRED, and `in.amazon.mShop.android.shopping` for Amazon Pay) so Razorpay and `UpiAppDetector` can detect and launch installed UPI apps directly.

---

## 2. Activity Callback Wiring (`MainActivity.kt`)

1. **Implement `PaymentResultWithDataListener` in `MainActivity`**:
   - Implement `com.razorpay.PaymentResultWithDataListener` on `MainActivity`.
   - Preload Razorpay Checkout in `onCreate` via `Checkout.preload(applicationContext)` and initialize saved merchant credentials in `BillingPaymentManager`.
   - Route `onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?)` to `BillingPaymentManager.onRazorpaySuccess(paymentId, paymentData?.orderId, paymentData?.signature)` so the real payment ID, order ID, and cryptographic signature are passed to `SubscriptionPaymentVerificationService`.
   - Route `onPaymentError(code: Int, response: String?, paymentData: PaymentData?)` to `BillingPaymentManager.onRazorpayError(code, response)`.

---

## 3. Razorpay Checkout & Payment Channels (`BillingPaymentManager.kt`)

1. **Configure Multi-Channel Checkout Options**:
   - Support all 4 prioritized payment modes in `launchRazorpayCheckout(...)`:
     - **UPI Intent & UPI ID (`"upi"`)**: Enables native UPI app intent flow (`_[flow] = "intent"`) and pre-fills `vpa` when a user enters their UPI ID.
     - **Dynamic UPI QR Code (`"upi_qr"`)**: Configures Razorpay display blocks to highlight UPI QR + UPI options for quick scan-and-pay.
     - **Credit & Debit Cards (`"card"`)**: Highlights Visa, Mastercard, and RuPay card checkout.
     - **NetBanking & Wallets (`"netbanking_wallet"`)**: Highlights 50+ Indian banks and major digital wallets.
   - Note: Razorpay Standard Checkout only accepts a server-generated `order_id` if created via the Razorpay Orders REST API; passing a client-generated `order_...` string causes the live Razorpay SDK to reject the checkout payload. We will omit synthetic `order_id` values from the client `JSONObject` unless a real server-issued Razorpay Order ID is provided, while still tracking the internal transaction reference for local ledger and entitlement verification.
2. **Persistent Merchant & Key Configuration**:
   - Persist `merchantName`, `merchantId`, `accountId`, `razorpayKeyId`, and `isLiveMode` in `SharedPreferences` (`razorpay_gateway_prefs`) so updates made in **Admin Dashboard → Subscription Management → Razorpay Gateway** survive app restarts.

---

## 4. Subscription Screen Razorpay Mode Selector (`SubscriptionScreen.kt`)

1. **Interactive Channel Selector inside the Razorpay Card**:
   - In the **Choose Payment Method** card on `SubscriptionScreen.kt`, when **Razorpay / Alternative Billing** is selected, allow the user to pick their preferred Razorpay payment channel before tapping **Continue with Razorpay / UPI →**:
     - **UPI App / UPI ID** (with an optional UPI ID / VPA input field and detected UPI app chips)
     - **UPI QR Code**
     - **Debit / Credit Cards**
     - **NetBanking & Wallets**
   - Tapping **Continue with Razorpay / UPI →** (or **Proceed to Secure Checkout →** / **Choose Plan →**) launches the official Razorpay Standard Checkout bottom sheet pre-configured with the chosen channel, plan amount (in paise), candidate email/phone prefill, and merchant branding.
