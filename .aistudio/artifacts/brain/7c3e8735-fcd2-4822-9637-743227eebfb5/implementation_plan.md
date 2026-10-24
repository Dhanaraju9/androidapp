# Implementation Plan: Restoring Screen & MCQ Transition Styles

Restore the signature **Hero Expand with spring depth** transition for top-level app screens and the **Directional Horizontal Slide** transition for MCQ question navigation in the Practice Arena.

---

## 1. Top-Level App Screens Transition: Hero Expand with Spring Depth

### Objective
Configure screen-to-screen navigation across the entire app to use the **Hero Expand** transition profile with natural spring physics.

### Implementation Details
- **Configuration Default**:
  - Update `GlobalAnimationMasterConfig` and preset defaults in `AnimationEngineModels.kt` / `AnimationConfigManager.kt` to default `screenTransitionType` to `ScreenTransitionAnimationType.HERO_EXPAND`.
- **Motion Characteristics (`PremiumAnimationEngine.kt`)**:
  - **Forward Entrance**: Vertical expansion (`expandVertically`) combined with spring scale (`scaleIn`, initialScale `0.92f`, dampingRatio `0.72f`, stiffness `Spring.StiffnessMediumLow`) and alpha fade-in (`fadeIn`).
  - **Backward Entrance**: Smooth scale-in from `1.04f` to `1.00f` with deceleration fade-in.
  - **Exit**: Emphasized accelerate scale-out (`scaleOut`, targetScale `1.04f` forward, `0.94f` backward) with alpha fade-out (`fadeOut`).
  - **Master Controls**: Respect `speedMultiplier`, `slowMoFactor`, and `reducedMotionMode` without dropping frames.

---

## 2. MCQ Practice Arena: Directional Horizontal Slide Transition

### Objective
Smoothly animate question cards when moving between questions (via Next, Previous, Swipe, or Question Palette clicks) using directional sliding based on forward/backward movement.

### Implementation Details in `PracticeArenaScreen.kt`
- **Dynamic Direction Detection**:
  - Compare `targetState` with `initialState` in `AnimatedContent` transition spec:
    - Moving to next question (`targetState > initialState`): Slide in from right (`+fullWidth / 3`), slide out to left (`-fullWidth / 4`).
    - Moving to previous question (`targetState < initialState`): Slide in from left (`-fullWidth / 3`), slide out to right (`+fullWidth / 4`).
- **Smooth Easing & Performance**:
  - Easing: `FastOutSlowInEasing` / `EmphasizedDecelerate` with `240ms` enter duration and `200ms` exit duration.
  - Paired with subtle fade (`fadeIn` 200ms / `fadeOut` 180ms) and GPU-backed rendering.
- **Both Practice & Mock Test Modes**:
  - Apply the `AnimatedContent` transition wrapper around `QuestionDetailPanel` in both standard practice mode and mock test mode.
  - Keep scroll position reset (`scrollState.scrollTo(0)`) tied cleanly to question change.
  - Ensure zero regressions in answer selection, bookmarking, review flagging, and TTS speech.

---

## 3. Verification & Build Integrity
1. Compile the app using `compile_applet` to verify clean Kotlin compilation.
2. Verify screen switching (Splash -> Home -> Practice -> Results -> Admin) applies Hero Expand.
3. Verify MCQ next/previous navigation slides directionally and respects accessibility/reduced motion settings.
