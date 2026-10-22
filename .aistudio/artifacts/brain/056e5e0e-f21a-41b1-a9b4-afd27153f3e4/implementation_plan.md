# Premium Solid Colors for Sets & Subtopics Action Buttons

Update the action buttons ("Start", "Continue", and "Review") on Sets and Subtopics cards using the 10 exact premium solid colors specified by the user, dynamically chosen to maximize contrast against each card's background color, paired with bold white text and standard 12dp rounded corners.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> - **Confirmed Color Palette (10 Exact Hex Values)**:
>   - Deep Emerald Green: `#005B35`
>   - Dark Navy Blue: `#082B70`
>   - Rich Burgundy: `#800020`
>   - Deep Teal: `#005F60`
>   - Dark Chocolate Brown: `#4A2608`
>   - Midnight Purple: `#3B087A`
>   - Deep Forest Green: `#07552E`
>   - Royal Blue: `#172FA8`
>   - Deep Plum: `#650044`
>   - Deep Maroon: `#720018`
> - **Scope & States**: Confirmed by user to apply across Start, Continue, and Review action buttons on both Sets (`CompactGridSetCard`) and Subtopics (`CompactGridSubtopicCard`) cards.
> - **Typography & Shape**: Bold white (`#FFFFFF`) text, standard `12.dp` rounded corners (`RoundedCornerShape(12.dp)`), consistent height (`38.dp`), and full-width alignment within the card layout.
> - **Contrast Strategy**: Dynamically map button colors against card backgrounds to avoid low-contrast combinations (e.g. deep navy or midnight purple on warm orange cards; rich burgundy or deep maroon on green/mint cards; deep emerald or teal on ruby/violet cards).

---

### 1. Overview & Core Concept

- **Component Focus**: Sets and Subtopics cards in `TopicSetsScreen.kt`.
- **Primary Change**: Replace default button backgrounds with the 10 premium solid colors, guaranteeing strong visual punch, high contrast against the card body, and clean readability of the bold white "Start" / "Continue" / "Review" text.
- **Strict Boundary**: No changes to card backgrounds, status indicator colors, card padding, heights, icons, navigation, or other screens.

---

### 2. Dynamic Contrast Mapping Strategy

```
┌─────────────────────────────────┬───────────────────────────────────────────┐
│ Card Background Color           │ Recommended Contrasting Button Color(s)   │
├─────────────────────────────────┼───────────────────────────────────────────┤
│ Orange / Amber (#EA580C, #FDBA) │ Dark Navy Blue (#082B70) / Midnight Purple│
│ Royal Blue (#2563EB)            │ Rich Burgundy (#800020) / Deep Maroon     │
│ Ruby Red (#E11D48)              │ Deep Emerald Green (#005B35) / Deep Teal  │
│ Teal / Emerald (#0D9488, #0596) │ Rich Burgundy (#800020) / Deep Plum       │
│ Mint Green (#6EE7B7 - Completed)│ Deep Maroon (#720018) / Deep Plum         │
│ Violet (#7C3AED)                │ Deep Emerald Green (#005B35) / Forest Green│
└─────────────────────────────────┴───────────────────────────────────────────┘
```

A dedicated helper function `getContrastingActionButtonColor(cardBg: Color, index: Int)` will evaluate the card background and select the optimal contrasting color from the 10 requested hex colors.

---

### 3. Implementation Steps

1. **Define Palette & Contrast Engine in `TopicSetsScreen.kt`**:
   - Register the 10 exact premium colors as constant definitions.
   - Implement `getContrastingActionButtonColor(cardBg: Color, fallbackIndex: Int): Color`.

2. **Update `CompactGridSetCard`**:
   - Compute `actionButtonBg = getContrastingActionButtonColor(cardBg, setNumInt)`.
   - Ensure the button surface uses `color = actionButtonBg` with `RoundedCornerShape(12.dp)`.
   - Ensure the text label has `color = Color.White` and `fontWeight = FontWeight.ExtraBold`.

3. **Update `CompactGridSubtopicCard`**:
   - Compute `actionButtonBg = getContrastingActionButtonColor(cardBg, codeInt)`.
   - Ensure the button surface uses `color = actionButtonBg` with `RoundedCornerShape(12.dp)`.
   - Ensure the text label has `color = Color.White` and `fontWeight = FontWeight.ExtraBold`.

4. **Preserve All Other Elements**:
   - Keep status badges, progress bars, serial number badges, card dimensions (195.dp), padding, and click handling untouched.

5. **Compilation & Verification**:
   - Run `compile_applet` to confirm a clean build with zero regression.
