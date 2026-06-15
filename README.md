# Reshare Cleaner 🛡️

Reshare Cleaner is a privacy-first, highly intelligent native Android application designed to help users declutter their galleries. By distinguishing between **social media reshares** (memes, promos, screenshots, receipts) and **personal photos** (keepsakes, family portraits, custom captures), the app streamlines storage management while ensuring absolute user data privacy.

---

## 🎨 Core Architectural Principles

1. **Privacy-First Design**: Media files and structural metadata are processed entirely on-device by default.
2. **Hybrid Dual-Core Classification**: Flexibly toggles between localized, network-independent on-device rules and deep cloud-based semantic intelligence.
3. **Adaptive Habit Learning**: Learns directly from manual keep/discard actions to auto-tailor recommendations to user-specific clean up preferences.
4. **Modern Android Stack**: Fully built with Jetpack Compose, Material Design 3, Room, Kotlin Coroutines, and strict Unidirectional Data Flow (UDF).

---

## 🚀 Key Features and Modules

### 1. Dual-Core AI Classifier Engine
Users can configure their intelligence profile under the **Settings & Insights** tab based on their connectivity and privacy preferences:

*   **🛡️ On-Device Gemini Nano AI (Recommended)**:
    *   **Absolute Privacy**: Processes image characteristics completely on the device chip. Zero data packets are offloaded, and zero metrics leave your physical storage.
    *   **Network Independent**: Works flawlessly on airplane flights, underground metros, or remote locations with poor cellular coverage.
    *   **Dynamic Reinforcement**: Utilizes a combination of neural metadata signature analysis and real-time user-habit adjustment.
*   **🌐 Cloud Gemini 3.5 Flash AI**:
    *   **Advanced Semantic Intelligence**: Leverages deep multi-modal reasoning models via the Google Gen AI SDK for high-fidelity scene understanding.
    *   **High Recall**: Best suited for complex visual details, textual memes, or fine contrast analysis (requires secure internet connectivity).

### 2. Live Preference Engine (Adaptive Learning)
The app doesn't just scan; it **learns**:
*   Every swipe—whether to **Keep** or **Discard**—is logged locally into a Room database database structure (`UserActionLog`).
*   During scans, the classification engine aggregates your custom discard ratios per category.
*   **Decline-Heavy Adjustments**: If the analyzer detects you delete $\ge 75\%$ of items in a category (e.g., *Receipts*), it boosts deletion priority and raises recommendation confidence.
*   **Preservation Protection**: If you save/keep $\ge 70\%$ of items in a category (e.g., *Memes*), the engine dramatically reduces deletion recommendations or safe-lists corresponding items under *Personal* to protect your favorites.

### 3. Smart Swiping Deck
The core triage interaction presents pending reshares inside a fluid, gestures-driven swiping interface:
*   **Swipe Left (Discard)**: Declutter and queue item for removal recommendations.
*   **Swipe Right (Keep)**: Transition item directly to the Whitelist.
*   **In-View Protection Bar**: Real-time shield banner ("🛡️ On-Device Gemini Nano active") indicates when 100% cloud-free local processing protects active scans.

### 4. Interactive Insights & Settings Panel
Allows total control over the cleaning mechanisms:
*   **Compare Engines**: Visual pros/cons matrix matching offline performance, privacy parameters, and semantic accuracy.
*   **Habit Dashboard**: Showcases learned action rates and logs.

---

## 🛠️ Codebase Architecture & File Structure

The project strictly complies with the MVVM (Model-View-ViewModel) design pattern:

```
├── app
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── example
│           │           ├── data
│           │           │   ├── ai
│           │           │   │   └── OnDeviceGeminiNanoClassifier.kt   <-- Private local heuristic & preference engine
│           │           │   ├── api
│           │           │   │   ├── GeminiApiModels.kt                  <-- Cloud REST schemas
│           │           │   │   └── GeminiRetrofitClient.kt             <-- Cloud API Retrofit wrapper
│           │           │   ├── local
│           │           │   │   ├── AppDatabase.kt                      <-- Room Database configuration
│           │           │   │   ├── ScannedMedia.kt                     <-- Scanned image entity 
│           │           │   │   └── UserActionLog.kt                    <-- Swipes behavior history log
│           │           │   └── repository
│           │           │       └── MediaRepository.kt                  <-- Coordinates DB operations and AI fetching
│           │           ├── ui
│           │           │   ├── components
│           │           │   │   └── MediaPreview.kt                     <-- Media rendering views
│           │           │   ├── screens
│           │           │   │   ├── DashboardScreen.kt                  <-- Home screen (Stats, Quick Clean, Dynamic Lists)
│           │           │   │   ├── InsightsScreen.kt                   <-- Rebranded Settings tab & Habit parameters
│           │           │   │   ├── SwipeScreen.kt                      <-- Immersive Tinder-style swipe cards
│           │           │   │   └── WhitelistScreen.kt                  <-- Safe-listed protected images explorer
│           │           │   └── viewmodel
│           │           │       └── AppViewModel.kt                     <-- Controls App states & AI Toggle Actions
│           │           └── MainActivity.kt                              <-- App scaffold & bottom navigation rail
```

---

## ⚡ How the Local Learning Algorithm Calibrates

The local `OnDeviceGeminiNanoClassifier` runs a two-step formula to calculate safe-list and delete-confidence scores on-device:

$$\text{Confidence}_{\text{Final}} = \text{Confidence}_{\text{Base}} \pm \Delta_{\text{Preference}}$$

1. **Heuristic Discovery**: Evaluates local header characteristics, naming patterns, downloaded keywords, and social-app indicators (WhatsApp, Instagram, TikTok) to resolve a base recommendation confidence.
2. **Reinforcement Weights calculation**:
   $$R_{\text{discard}} = \frac{\text{Deletes}_{\text{Category}}}{\text{Total Decisions}_{\text{Category}}}$$
   *   If $R_{\text{discard}} \ge 0.75$: Confidence is boosted by $+50\%$ of remaining headroom.
   *   If $R_{\text{discard}} \le 0.30$: Confidence is penalised by $-40\%$. If adjusted value falls below $0.50$, the item is automatically reclassified to **Personal** to safeguard user keepsakes.

---

## 🔒 Security & Privacy Commitments

*   **Zero Leakage**: All images analyzed during `On-Device Gemini Nano` mode are processed locally. No pixel matrices or filenames leave external boundaries.
*   **Secure API Handling**: Native Gemini 3.5 Cloud fallback processes secure metadata tokens only via HTTPS with secure Google AI Studio keys.
