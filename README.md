# ML Counters

> Mobile Legends Counter Pick Assistant with Real-Time Hero Detection

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)

## 🎮 Features

- **Real-Time Hero Detection** — Detects enemy heroes from your screen during draft using OpenCV template matching (works down to 35px portraits)
- **Floating Overlay** — Counter suggestions displayed as a draggable overlay while you play
- **Manual Input** — Search and select heroes manually when auto-detection isn't needed
- **Counter Matrix** — Comprehensive "who counters who" database with 834+ matchup relationships
- **Counter Items** — Recommended item builds against specific enemy heroes
- **Role Filtering** — Filter by Tank, Fighter, Assassin, Mage, Marksman, Support
- **Mastery Tracker** — Track your hero mastery (Beginner → Master) to prioritize counters you can actually play
- **Dark Gaming Theme** — Full Material 3 dark theme optimized for gaming
- **Weekly Data Updates** — Counter data auto-syncs from GitHub every week
- **Multi-Resolution Support** — Works on lowered resolution devices (35px, 48px, 64px, 96px templates)

## 📊 Data Coverage

| Data | Count |
|---|---|
| Heroes | 130 |
| Counter Relationships | 834 |
| Items | 67 |
| Item Recommendations | 390 |
| Roles | 6 |

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Detection:** OpenCV 4.x (template matching with NMS)
- **Screen Capture:** Android MediaProjection API
- **Database:** Room (SQLite)
- **DI:** Hilt
- **Networking:** Retrofit + kotlinx.serialization
- **Sync:** WorkManager (weekly)
- **Image Loading:** Coil

## 📱 Screenshots

*Coming soon*

## 🚀 Building

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/MLCounters.git
cd MLCounters

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease
```

### Requirements
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 35
- Min SDK 26 (Android 8.0)

## 📂 Project Structure

```
app/src/main/java/com/mlcounters/app/
├── MLCountersApp.kt              # Application class
├── MainActivity.kt               # Main activity with navigation
├── data/
│   ├── local/                    # Room database, entities, DAOs
│   ├── remote/                   # Retrofit API, WorkManager sync
│   └── repository/               # Data repositories
├── detection/
│   ├── HeroDetectionEngine.kt    # OpenCV template matching
│   ├── ScreenCaptureService.kt   # MediaProjection service
│   └── TemplateManager.kt        # Multi-resolution template management
├── overlay/
│   ├── OverlayService.kt         # Floating window service
│   ├── OverlayContent.kt         # Compose overlay UI
│   └── OverlayViewModel.kt       # Overlay state management
├── ui/
│   ├── theme/                    # Dark gaming theme
│   ├── navigation/               # Navigation graph
│   ├── components/               # Reusable composables
│   └── screens/                  # Home, Heroes, Counter, Items, Mastery, Settings
├── di/                           # Hilt modules
└── util/                         # Permission helpers
```

## 🔄 Data Updates

Counter data is hosted in a separate GitHub repository and auto-synced weekly:

```
github-data-repo/
├── manifest.json         # Version check
├── heroes.json           # Hero metadata
├── counters.json         # Counter relationships
├── items.json            # Item data
├── counter_items.json    # Item recommendations
└── portraits/            # Hero portrait templates
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a Pull Request

### Updating Counter Data
Edit the JSON files in the `github-data-repo/` folder and run:
```bash
cd github-data-repo
python3 update_data.py
```

## ⚖️ Disclaimer

This is a community tool. All game content, hero names, and item data are property of Moonton/ByteDance. This project is not affiliated with or endorsed by Moonton.

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.
