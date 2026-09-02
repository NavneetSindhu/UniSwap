# UniSwap: The Campus Marketplace

UniSwap is a modern, peer-to-peer campus marketplace Android application built with Kotlin and Jetpack Compose. Engineered specifically for university environments, UniSwap enables students to buy, sell, exchange, and donate textbooks, dorm essentials, electronics, engineering tools, and course notes locally within verified campus networks.

---

## Screenshots

| Campus Feed | Item Details | Create Listing | Profile & Eco Metrics |
| :---: | :---: | :---: | :---: |
| ![Campus Feed Placeholder](docs/screenshots/feed_screen_placeholder.png) | ![Item Details Placeholder](docs/screenshots/details_screen_placeholder.png) | ![Create Listing Placeholder](docs/screenshots/create_listing_placeholder.png) | ![Profile & Eco Metrics Placeholder](docs/screenshots/profile_screen_placeholder.png) |

| Direct Messaging | Safety & Reporting | Student Verification | Developer Suite |
| :---: | :---: | :---: | :---: |
| ![Chat Screen Placeholder](docs/screenshots/chat_screen_placeholder.png) | ![Safety Report Placeholder](docs/screenshots/report_screen_placeholder.png) | ![Verification Placeholder](docs/screenshots/verification_placeholder.png) | ![Developer Suite Placeholder](docs/screenshots/developer_suite_placeholder.png) |

---

## Core Features

### 1. Real-Time Campus Feed & Discovery
- **Smart Campus Scoping:** Switch between local campus listings (`MY_CAMPUS`) and multi-university discovery (`ALL_CAMPUSES`).
- **Dynamic Category Filtering:** Filter items by categories (Architecture, Dorm Essentials, Engineering, Electronics, Digital Notes, and Custom Categories).
- **Multi-Faceted Search & Sorting:** Real-time query search, condition selection (Brand New, Like New, Good, Fair), price ranges, free items, and verified seller filters.
- **Safety Filters:** Automatically excludes listings from blocked users in real time.

### 2. Multi-Image Listing Creation & Moderation
- **Parallel Image Processing:** Multi-image picker with asynchronous parallel Cloudinary uploads via Kotlin Coroutines.
- **Content Safety Checks:** Automated image moderation screening before publishing to ensure safe, campus-compliant content.
- **Responsive Layout:** Adaptive card inputs and dropdowns structured with `IntrinsicSize` measurements to scale across diverse screen dimensions.

### 3. Real-Time Pickup Chat & Messaging
- **Contextual In-App Chat:** Chat conversations directly attached to marketplace listings for seamless negotiation and pickup scheduling.
- **Live Streams:** Real-time messaging powered by Cloud Firestore snapshot listeners with unread message badges.
- **Push Notifications:** Instant message delivery alerts powered by Firebase Cloud Messaging (FCM).

### 4. Trust, Safety & Student Verification
- **College Email Verification:** In-app verification workflow linking official university email addresses (`.edu` / college domains) with live countdown timers and recovery mechanisms.
- **Trust Badges:** Verified student status badges displayed across feeds, listings, and user profiles.
- **UGC Moderation & Reporting:** In-app reporting flow for listing violations and one-tap user blocking to maintain campus community standards.

### 5. Sustainability & Circular Economy Metrics
- **Automated Impact Tracking:** Quantifies environmental impact by computing kilograms of waste diverted and estimated kilograms of CO2 saved per rehomed item.
- **Eco Tiers:** Visual progress tracking and milestone badges celebrating student sustainability contributions.

### 6. Personalization & Design System
- **Figma-Aligned Design System:** Custom `UniSwapTheme` tokens (`ExtendedColors`) supporting dynamic colors, dark/light themes, and custom Matter typography.
- **Fluid Micro-Interactions:** Smooth AnimatedContent transitions, pull-to-refresh feeds, shimmer skeleton loaders, and physics-based modal bottom sheets.
- **Guest Mode Support:** Low-friction onboarding allowing users to explore the marketplace before authenticating, with contextual nudge bottom sheets for restricted actions.

---

## Architecture & Technology Stack

The project follows Clean Architecture principles with MVVM (Model-View-ViewModel) pattern and unidirectional data flow (UDF).

```
app/src/main/java/com/minimize/uniswap/
├── data/
│   ├── model/             # Domain models (CampusItem, User, Report, Message)
│   ├── preferences/       # Jetpack DataStore user preferences manager
│   ├── prompt/            # Global prompt cooldown and nudge manager
│   └── repository/        # Repository interfaces and Firebase implementations
│       └── firebase/      # Auth, Firestore items, reports, and messages
├── di/                    # Hilt dependency injection modules
├── ui/
│   ├── components/        # Reusable Compose components (dialogs, bottom sheets, cards)
│   ├── screens/           # Feature screens (auth, feed, details, list, chat, profile, settings)
│   └── theme/             # Theme tokens, extended colors, typography, and shapes
└── util/                  # Helpers (Cloudinary, GoogleAuth, ShareUtils, Formatters)
```

### Technical Specifications
- **Language:** Kotlin 2.0+
- **UI Toolkit:** Jetpack Compose & Material 3
- **Dependency Injection:** Dagger Hilt
- **Asynchronous & Reactive Streams:** Kotlin Coroutines & Flow (`StateFlow`, `callbackFlow`)
- **Backend & Cloud Infrastructure:**
  - **Authentication:** Firebase Auth (Email/Password, Google Sign-In with Credential Manager)
  - **Database:** Cloud Firestore (Real-time collections for items, users, messages, reports)
  - **Media Storage:** Cloudinary Android SDK
  - **Push Notifications:** Firebase Cloud Messaging (FCM)
  - **Monitoring:** Firebase Crashlytics & Google Analytics
- **Local Persistence:** Jetpack DataStore Preferences
- **Image Loading:** Coil Compose
- **Animations:** Jetpack Compose Animation APIs & Lottie

---

## Testing & Quality Assurance

The codebase includes comprehensive unit test suites validating ViewModel business logic, input validation, math computations, and state flows using **Turbine** and **MockK**.

### Test Suites Included
- `MainViewModelTest`
- `LoginViewModelTest`
- `FeedViewModelTest`
- `DetailsViewModelTest`
- `ListViewModelTest`
- `ChatViewModelTest`
- `MessagesViewModelTest`
- `ProfileViewModelTest`
- `SettingsViewModelTest`
- `GlobalPromptManagerTest`

To run all unit tests:
```bash
./gradlew testDebugUnitTest
```

---

## Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17 or higher
- Android SDK API 35+
- Firebase project with Authentication, Firestore, and FCM enabled
- Cloudinary account for media hosting

### Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/NavneetSindhu/UniSwap.git
   cd UniSwap
   ```

2. **Add Firebase Configuration:**
   - Download `google-services.json` from your Firebase Console.
   - Place `google-services.json` into the `app/` directory.

3. **Configure Environment Keys:**
   Add the following properties to your `local.properties` or build configuration:
   ```properties
   CLOUDINARY_CLOUD_NAME=your_cloud_name
   CLOUDINARY_API_KEY=your_api_key
   CLOUDINARY_API_SECRET=your_api_secret
   WEB_CLIENT_ID=your_google_web_client_id.apps.googleusercontent.com
   ```

4. **Build and Run:**
   - Sync Gradle in Android Studio.
   - Select an emulator (API 26+) or a connected physical Android device.
   - Click **Run** or execute:
     ```bash
     ./gradlew assembleDebug
     ```

---

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
