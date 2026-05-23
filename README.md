# 💧 ackwatraq

An Android app for tracking your daily water intake with gamification elements.

## Features

- **Quick-add logging**: 250mL, 500mL, 1L buttons + custom input
- **Goal calculation**: Based on weight, activity level, and climate
- **Progress dashboard**: Visual progress with anime mascot "Aqua-chan"
- **Reminders**: Configurable notifications to stay hydrated
- **Streaks**: Track consecutive days meeting your hydration goal
- **Achievements**: Unlock badges for milestones (volumes, streaks, consistency)
- **Gamified UI**: Pastel anime palette, mascot reactions, celebration animations
- **Local-first**: All data stored locally with Room + DataStore

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3 (anime-themed)
- **Storage**: Room Database + DataStore Preferences
- **Animations**: Lottie Compose
- **Charts**: MPAndroidChart
- **Architecture**: MVVM with Repository pattern

## Getting Started

1. Clone the repo
2. Open in Android Studio (Hedgehog or newer)
3. Sync Gradle
4. Run on device/emulator (min SDK 26)

## Project Structure

```
app/src/main/java/com/ackwatraq/
├── data/
│   ├── db/          # Room entities, DAO, database
│   ├── repository/  # Data layer
│   └── store/       # DataStore preferences
├── domain/
│   ├── model/       # Intake, Achievement, UserPrefs
│   └── usecase/     # Goal calc, streak logic, XP calc
├── ui/
│   ├── home/        # Dashboard + quick-add
│   ├── history/     # Past logs + charts
│   ├── achievements/# Badges + progress
│   └── settings/    # Goal, reminders, units
└── worker/          # Reminder notifications
```

## License

MIT
