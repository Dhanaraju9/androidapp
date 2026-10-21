# Real SMS OTP Authentication via Firebase Phone Auth

Integrating real SMS OTP verification into the Android application's phone login flow using Firebase Authentication (`PhoneAuthProvider`), with pre-configured bypass logic for instant test logins.

> [!IMPORTANT]
> **Confirmed Choices from Interview**:
> - **Authentication Engine**: Real SMS OTP via Firebase Authentication (`PhoneAuthProvider`).
> - **Test Number Strategy**: Pre-configured test mobile numbers (e.g., `9493512768`, `9876543210`, `9999999999`) bypass SMS gateway delay for instant developer/reviewer testing, while all other mobile numbers trigger real SMS delivery via Firebase Auth.

---

## 1. Overview & Core Concept

Currently, entering a phone number in `LoginScreen.kt` simulates OTP verification locally. This update replaces the mock flow with a production-grade Firebase Phone Authentication integration.

When a user enters a mobile number:
1. **Test Number Check**: If the number is a pre-configured test/demo number, the app immediately transitions to the OTP entry screen with instant verification (`123456`).
2. **Real SMS Delivery**: For all other 10-digit mobile numbers, Firebase Auth dispatches a real SMS OTP via `PhoneAuthProvider.verifyPhoneNumber`.
3. **Verification**: Entering the received 6-digit code constructs a `PhoneAuthCredential` and signs the user in via `Firebase.auth.signInWithCredential(credential)`, syncing their profile to Cloud Firestore.

---

## 2. User Experience & Visual Design

```
┌─────────────────────────────────────────┐
│              Login Screen               │
│                                         │
│  [ Enter 10-digit Mobile Number ]       │
│  ┌───────────────────────────────────┐  │
│  │ +91 | 9876543210                  │  │
│  └───────────────────────────────────┘  │
│  [ Send OTP Button ]                    │
│                                         │
│  ─── OR ───                             │
│  [ Sign in with Google Button ]         │
└────────────────────┬────────────────────┘
                     │ (Triggers PhoneAuthProvider)
                     ▼
┌─────────────────────────────────────────┐
│          OTP Verification Screen        │
│                                         │
│  OTP sent to +91 9876543210             │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐    │
│  │ 5 │ │ 8 │ │ 2 │ │ 1 │ │ 9 │ │ 0 │    │
│  └───┘ └───┘ └───┘ └───┘ └───┘ └───┘    │
│  [ Resend OTP in 0:58 ]                 │
│  [ Verify OTP Button ]                  │
└────────────────────┬────────────────────┘
                     │ (signInWithCredential)
                     ▼
┌─────────────────────────────────────────┐
│    Authenticated Session (Firestore)   │
└─────────────────────────────────────────┘
```

- **Clear Progress Feedback**: Shows loading indicators during `PhoneAuthProvider` phone verification and credential sign-in.
- **Resend Countdown**: 60-second timer before enabling the "Resend OTP" button.
- **Error Handling**: Displays friendly Toasts and error messages for invalid phone numbers, incorrect OTP codes, or network timeouts.

---

## 3. Key Product Decisions & Trade-Offs

- **Firebase Phone Auth Integration**:
  - *Chosen Approach*: Use `PhoneAuthProvider.verifyPhoneNumber` with `PhoneAuthOptions` and `OnVerificationStateChangedCallbacks`.
  - *Why*: Standard, secure Android SDK method for phone authentication that handles SMS dispatch, reCAPTCHA / Play Integrity verification, and credential generation automatically.

- **Pre-Configured Test Numbers**:
  - *Chosen Approach*: Maintain a list of test mobile numbers (e.g. `9493512768`, `9876543210`, `9999999999`).
  - *Why*: Allows instant testing without draining Firebase SMS quotas or waiting for SMS delivery delays during app development and reviews.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Call Sequence

```
+------------------+         +----------------------------+         +---------------------+
|   LoginScreen    | ------> |  Firebase PhoneAuthProvider| ------> | Firebase Auth SDK   |
+------------------+         +----------------------------+         +---------------------+
         |                                |                                   |
         | 1. verifyPhoneNumber()         | 2. Sends SMS via carrier          |
         | -----------------------------> | --------------------------------> |
         |                                |                                   |
         | 3. onCodeSent(verificationId)  |                                   |
         | <----------------------------- |                                   |
         |                                                                    |
         | 4. User inputs 6-digit OTP                                         |
         | -----------------------------------------------------------------> |
         |                                                                    | 5. signInWithCredential()
         | 6. User Authenticated & Profile Synced to Firestore                |
         | <----------------------------------------------------------------- |
```

### Key Source Updates

1. **`LoginScreen.kt`**:
   - Add state for `verificationId`, `resendingToken`, `isSendingOtp`, and `isVerifyingOtp`.
   - Update `onContinueWithOtp` to trigger `PhoneAuthProvider.verifyPhoneNumber` for non-test numbers.
   - Update `onVerifyOtp` to execute `Firebase.auth.signInWithCredential(PhoneAuthProvider.getCredential(verificationId, enteredOtp))`.
   - Integrate proper error listeners (`onVerificationFailed`) and resend logic (`onResendOtp`).

2. **`FirebaseSyncManager.kt`**:
   - Ensure authenticated Firebase User session details (UID, phone number) correctly map and persist to Cloud Firestore profile records upon successful OTP login.

---

## Verification Plan

1. **Compilation**: Run `compile_applet` to confirm zero build errors.
2. **Test Number Login**: Test entering a pre-configured test number (`9493512768`) and verify instant OTP validation (`123456`).
3. **Real SMS OTP Flow**: Test entering a real 10-digit number, confirm `PhoneAuthProvider` dispatch, code entry, and authentication completion.
