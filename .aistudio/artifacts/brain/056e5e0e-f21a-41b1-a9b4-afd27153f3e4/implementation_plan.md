# Expanding & Customizing Splash Screen Quotes

This plan outlines how new inspirational quotes are added to the splash screen, how you can supply your own custom quotes, and how the session rotation system ensures every quote is displayed smoothly without immediate repetition.

### User Review & Critical Decisions

> [!IMPORTANT]
> **Share Your Custom Quotes Anytime**: Since you indicated you would like to provide your own custom list of quotes, you can paste your list of quotes (along with their authors) directly in the chat before or after clicking **Proceed**, and they will be added directly into the splash screen collection.

- **Confirmed Decision 1 (Custom Quote List)**: The quote provider will be structured to cleanly accept your custom list of quotes while also supporting dynamic runtime additions saved persistently on the device.
- **Confirmed Decision 2 (Selected Quote Themes)**: Any additional built-in quotes will focus on Indian leaders and scholars (Dr. A.P.J. Abdul Kalam, Swami Vivekananda, Dr. B.R. Ambedkar, Chanakya), Civil Services and competitive exam perseverance, and daily study discipline and time management.

---

### 1. Overview & Core Concept

- **What It Does**: Expands the splash screen quote system so it can host an extended library of custom motivational quotes and rotate through them deterministically across app launches, while maintaining the character-by-character typewriter reveal and zero-layout-jump container.
- **Target Audience / Persona**: APPSC Group-2 and competitive exam aspirants who open the app daily for study sessions and benefit from fresh, high-intent motivation during startup.
- **Key Value**: Prevents quote fatigue across repeated daily study sessions and makes adding new custom quotes effortless—either by pasting them in chat for direct inclusion or adding them through the quote manager API.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Adding Custom Quotes in Code / Chat**: Each quote is defined as a `QuoteItem(text = "...", author = "...")` entry inside the central quote provider. You can paste any number of quotes in chat, or add them directly to the list.
  2. **App Launch & Session Rotation**: On every launch, the splash screen fetches the next quote in the sequence from `SplashQuoteProvider`, ensuring two consecutive sessions never show the same quote.
  3. **Zero-Jump Typewriter Reveal**: As the quote types out character by character (`38ms` per character, adapted for long quotes so startup never feels sluggish), the unrevealed characters are pre-allocated as transparent text so the card height and line wrapping remain 100% locked from the first frame.
  4. **Post-Typing Author Reveal**: Once the quote finishes typing, the author attribution (`— Author Name`) gently slides and fades in below the quote.
- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Pastel Rainbow Luxury (`WarmIvory` `#FFF8F0` background, `PastelPeach` `#FFD6B8`, `PastelPink` `#F7B7D2`, and `Lavender` `#DCC6F5` border gradient).
  - *Typography & Hierarchy*: Centered `16.5sp` `SemiBold` Serif typography in `DeepPlum` (`#64205E`) with `25sp` line height for quotation text, paired with `13.5sp` `Bold` Italic author attribution.
- **Interactive Feedback & Motion**:
  - Adaptive typing speed adjustment so longer custom quotes (e.g., 100+ characters) automatically scale their per-character typing interval to keep total splash duration balanced and responsive.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Adaptive Typing Speed for Longer Custom Quotes**
  - *Chosen Approach*: Dynamically adjust the character typing interval between `24ms` and `38ms` based on quote character length.
  - *Why*: Custom quotes vary in length; capping total typing duration ensures longer quotes from scholars or leaders do not overly delay entering the app while keeping short quotes deliberate and readable.
  - *Alternatives Considered*: Fixed `38ms` interval for all quotes, which causes 140-character quotes to take over 5.3 seconds just to type.
- **Decision 2: Combined Built-In + Persistent Custom Quotes Storage**
  - *Chosen Approach*: Merge the static `curatedQuotes` list with any user-supplied custom quotes stored in `SharedPreferences` (`SplashQuoteProvider.addCustomQuote(...)` / `setCustomQuotes(...)`).
  - *Why*: Allows immediate code-level additions when you share quotes in chat while also enabling runtime management without requiring a database migration.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌───────────────────────────────────────────────────────────────────┐
│                      SplashQuoteProvider                          │
│  ┌─────────────────────────────┐  ┌────────────────────────────┐  │
│  │ Curated & User Custom List  │  │ SharedPreferences Store    │  │
│  │ (Kalam, Vivekananda, Exam   │  │ • last_quote_index         │  │
│  │  Perseverance, Discipline)  │  │ • session_count            │  │
│  └──────────────┬──────────────┘  │ • custom_quotes_json       │  │
│                 └────────┬────────┴──────────────┬─────────────┘  │
│                          ▼                       ▼                │
│               getQuoteForNextSession(context)                     │
└──────────────────────────┬────────────────────────────────────────┘
                           │ Returns Pair<Int, QuoteItem>
                           ▼
┌───────────────────────────────────────────────────────────────────┐
│                        AppSplashScreen                            │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ Adaptive Typing Interval Calculator (24ms – 38ms)           │  │
│  └─────────────────────────────┬───────────────────────────────┘  │
│                                ▼                                  │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ PastelInspirationalQuoteSection                             │  │
│  │ • Pre-allocated transparent suffix (Zero layout jump)       │  │
│  │ • Post-typing author slide + fade reveal                    │  │
│  └─────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────┘
```

- **Data Model & State**:
  - `QuoteItem(val text: String, val author: String)`: Immutable model representing each quote and its author.
  - `SplashQuoteProvider`: Singleton managing the combined list of built-in quotes and custom quotes, tracking `last_quote_index` and `session_count` in `SharedPreferences` so every quote is cycled through before repeating.
- **Interactive Component & State Mapping**:
  - `getQuoteForNextSession(context)` reads the merged quote pool, advances the index deterministically with wrap-around protection, persists the new index, and provides the selected `QuoteItem` to `AppSplashScreen`.
  - `addCustomQuote(context, text, author)` and `addCustomQuotes(context, list)` allow adding new quotes at any time.
