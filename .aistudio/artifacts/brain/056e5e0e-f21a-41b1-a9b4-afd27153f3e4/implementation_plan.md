# Implementation Plan - Logout & Offline MCQ Subscription Access

## Proposed Changes

### 1. Logout and Session Clearing
- Update the logout handler to immediately clear user session data, auth tokens, and cached user states.
- Redirect navigation to `LoginScreen` and clear the backstack so users cannot press back into authenticated screens.

### 2. Offline Subscription Management & Validation
- Ensure the subscription model supports the ₹99 offline subscription plan.
- Implement subscription validation logic to check both active status and validity expiration date.

### 3. Online vs Offline MCQ Access Rules
- **Online Access**: Allow users without a ₹99 offline subscription to practice MCQs online through Subjects -> Units -> Topics -> Sets/Subtopics.
- **Offline Access / Downloaded Materials**: Require an active and valid ₹99 offline subscription to access downloaded MCQs offline. If offline and subscription is missing/expired, block access and display a subscription prompt dialog.

### 4. Verification
- Compile app and verify navigation and subscription checks work seamlessly.
