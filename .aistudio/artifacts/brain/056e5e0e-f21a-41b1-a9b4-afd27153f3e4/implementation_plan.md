# Build Premium Downloaded Materials Screen with Hierarchy Grouping, Offline MCQs & Typography Options

Transform the empty Downloaded Materials screen into a luxurious, feature-rich offline vault. Downloaded MCQs will be structured hierarchically by Subject, Unit, Topic, and Practice Sets using expandable cards, styled with premium serif typography (Cinzel and Playfair Display), and equipped with direct download icons on subtopic and set cards.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following design decisions were confirmed by the user and govern this implementation:

- **Confirmed Grouping**: MCQs and materials are hierarchically structured by **Subject -> Unit -> Topic -> Set** inside interactive expandable cards.
- **Confirmed Download Behavior**: Clicking the Download icon on Subtopic or Set cards saves MCQs locally for offline access with instant status badge updates (`✓ Downloaded`).
- **Confirmed Typography**: Styled with **Cinzel** and **Playfair Display** premium serif headers paired with clean body text, plus a font styling panel for custom sizing and font selection.

---

## 1. Overview & Core Concept

- **What It Does**: Provides a dedicated, offline-capable vault for APPSC / competitive exam aspirants to browse, search, and practice downloaded MCQs grouped strictly by Subject, Unit, Topic, and Practice Sets without an internet connection.
- **Target Audience**: Students preparing on the go or in low-connectivity environments who need reliable offline practice access.
- **Key Value**: Replaces the empty screen with a luxurious visual experience, instant offline practice access, encrypted local MCQ storage, and real-time download badges on all subtopic and set cards.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Viewing Downloaded Materials**:
   - User opens **Downloaded Materials** from the home screen or side menu.
   - Screen displays an offline status header, total downloaded question count, local storage usage, search bar, and typography selector.
   - Expandable **Subject Cards** display subject titles, total questions, and size (e.g., *History • 320 MCQs • 450 MB*).

2. **Exploring Units, Topics, and Sets**:
   - Tapping a **Subject Card** expands to show its **Units**.
   - Tapping a **Unit Card** expands to reveal **Topic / Subtopic Cards**.
   - Each **Topic / Subtopic Card** and **Practice Set Card** features a download badge, MCQ count, and a direct **"Practice Offline"** button.

3. **Downloading Subtopics & Sets**:
   - Tapping the **Download Icon** on any Subtopic Card or Practice Set Card in the Syllabus or Topic Sets views triggers an instant local download, updating the badge to `✓ Downloaded` and making it immediately visible in the Downloaded Materials vault.

4. **Offline MCQ Practice & Inspection**:
   - User can tap any downloaded topic or set to launch offline practice mode or view the list of downloaded questions with answer explanations.

### Visual Identity & Theme

- **Header Typography**: **Cinzel** (`res/font/cinzel.ttf`) and **Playfair Display** (`res/font/playfair_display.ttf`) serif headers.
- **Color Palette**: Deep Obsidian background (`#0B0F19`), Gold/Amber primary accents (`#F59E0B`), Emerald download badges (`#10B981`), and M3 dark surface elevation cards.
- **Cards & Icons**: Glassmorphism translucent borders, rounded corners (16.dp), glowing download icons, and subtle elevation drop shadows.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dynamic Expandable Hierarchy**:
  - *Chosen Approach*: Use nested animated `Column` containers (`AnimatedVisibility`) inside `LazyColumn` items for Subject -> Unit -> Topic -> Set drilldown.
  - *Why*: Keeps the screen compact while letting users drill down cleanly without leaving the vault view.

- **Decision 2: Direct Offline Persistence via `OfflineSyncManager` & Room Database**:
  - *Chosen Approach*: Synchronize state between `OfflineSyncManager.downloadedPacks` state list, `AppDatabase.downloadedMaterialDao()`, and encrypted `SecureMcqVault`.
  - *Why*: Ensures instant state reflection across the app when downloading subtopics or sets from any screen.

- **Decision 3: Local Google Font Installation**:
  - *Chosen Approach*: Downloaded `cinzel.ttf` via `font-util` into `res/font/` alongside existing `playfair_display.ttf`.
  - *Why*: Complies with applet constraints (no `ui-text-google-fonts`) while delivering a true luxury serif aesthetic.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                      DownloadedMaterialsScreen                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ Vault Header (Cinzel Serif Title, Storage Stats, Font Picker)    │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ Search & Filter Bar (Subject Filter, Search Query)                │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ LazyColumn Hierarchy List                                        │  │
│  │  ├─ SubjectCard (Expandable)                                     │  │
│  │  │   └─ UnitCard (Expandable)                                    │  │
│  │  │       └─ Topic / SubtopicCard (With Download Icon & Badge)    │  │
│  │  │           └─ PracticeSetCard (With Download Icon & Practice)  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          OfflineSyncManager                            │
│  - downloadedPacks StateList (SUBJECT, UNIT, TOPIC, SET)              │
│  - isTopicDownloaded() / isSetDownloaded()                             │
│  - downloadTopic() / downloadPracticeSet()                             │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   Room Database & Secure Encryption                    │
│  - DownloadedMaterialDao (DownloadedMaterialEntity)                    │
│  - SecureMcqVault (AES-256 Encrypted Offline MCQs)                     │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Implementation Checklist

1. **Typography Setup**:
   - Configure `Cinzel` serif font (`R.font.cinzel`) and `Playfair Display` (`R.font.playfair_display`) in Compose theme typography for headers and titles.

2. **Subtopics & Sets Download Icons**:
   - Ensure subtopics cards and practice sets cards in `TopicSetsScreen.kt`, `OfflineSubjectDetailScreen.kt`, and `DownloadedMaterialsScreen.kt` display prominent download icons (`Icons.Default.CloudDownload` / `Icons.Default.CheckCircle`) with click handlers calling `OfflineSyncManager`.

3. **Downloaded Materials Screen Implementation**:
   - Build `DownloadedMaterialsScreen.kt` with header stats, luxury serif styling, expandable Subject -> Unit -> Topic -> Set hierarchy, and search filtering.
   - Implement expandable cards with smooth transitions, question count badges, size indicators, and offline practice buttons.
   - Include empty state handling and font customization controls.

4. **Verification**:
   - Verify build compilation via `compile_applet`.
