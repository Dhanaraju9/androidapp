# Move Current Affairs Card Below Subject Cards on Home Screen

Re-order the Home Screen layout so that the Monthly Current Affairs card is repositioned from its current top position to immediately after the last Subject Card, while preserving all existing designs, styling, dimensions, and functionality.

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Card Position**: The Current Affairs card will move from above the Live Content Dashboard / Subject Cards to directly below the final Subject Card.
> - **Fidelity**: All existing card designs, widths, heights, colors, background images, text, spacing, and navigation click listeners will be precisely preserved without duplication.

- **Confirmed Decision**: Move Current Affairs card below Subject Cards.
- **Open Question**: None.

## 1. Overview & Core Concept

- **What It Does**: Re-orders components in `HomeScreen.kt` so users see the Live Content Dashboard and Subject Cards first, followed by the Current Affairs card below the Subject Cards list.
- **Key Value**: Cleaner visual hierarchy that prioritizes core subject browsing before supplementary monthly current affairs.

## 2. User Experience & Visual Design

- **Current Flow**: Top bar -> Current Affairs card -> Live Content Dashboard -> Exam categories -> Subject Cards list -> Other elements.
- **New Flow**: Top bar -> Live Content Dashboard -> Exam categories -> Subject Cards list -> **Monthly Current Affairs card** -> Other elements.
- **Styling**: Zero visual changes to the Current Affairs card itself; only its vertical position within the `LazyColumn` or parent container in `HomeScreen.kt` is updated.

## 3. Technical Architecture & Data Strategy

- **Component Hierarchy**:
  ```
  HomeScreen.kt (LazyColumn)
  ├── Top App Bar / Banner
  ├── Live Content Dashboard
  ├── Exam Categories Row
  ├── Exam Syllabus & Papers Button
  ├── Subject Cards (LazyVerticalGrid or Column list)
  ├── [NEW POSITION] Monthly Current Affairs Card
  └── Other footer elements
  ```
- **Files to Modify**: `app/src/main/java/com/example/app/ui/screens/HomeScreen.kt` (or corresponding home screen composable file).
