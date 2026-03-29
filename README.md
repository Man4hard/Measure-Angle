# Inclinometer - Android Tilt Measurement App

A professional Android inclinometer app built with Kotlin and MVVM architecture.

## Features

- **Bubble Level Mode** — Visual bubble level with real-time pitch & roll display
- **Digital Mode** — High-precision angle readouts for Pitch, Roll, and Yaw
- **Camera Overlay Mode** — Live camera feed with horizon line and angle overlay
- **Sensor Fusion** — Combines accelerometer + gyroscope with complementary filter
- **Smooth Filtering** — Exponential moving average for stable, jitter-free readings
- **Calibration** — Set any position as zero reference, persisted across sessions
- **Sound Feedback** — Audible beep when level, click when saving
- **Measurement History** — Save, label, and review past measurements (Room DB)
- **Dark / Light Theme** — Toggle via toolbar button
- **MVVM Architecture** — ViewModel + StateFlow + Repository pattern
- **Dependency Injection** — Hilt for clean, testable code

## Requirements

- Android Studio Hedgehog or newer
- Android SDK 24+ (Android 7.0)
- Physical device recommended for sensor data

## Getting Started

1. Clone: `git clone https://github.com/Man4hard/Measure-Angle.git`
2. Open in Android Studio
3. Sync Gradle
4. Run on a physical Android device

## Architecture

```
app/
├── data/
│   ├── model/         # Measurement, SensorData, CalibrationOffset
│   ├── local/         # Room DB (AppDatabase, MeasurementDao)
│   └── repository/    # MeasurementRepository
├── di/                # Hilt AppModule
├── ui/
│   ├── bubble/        # BubbleLevelFragment + custom BubbleLevelView
│   ├── digital/       # DigitalFragment with angle cards
│   ├── camera/        # CameraFragment + CameraOverlayView
│   ├── history/       # HistoryFragment + MeasurementAdapter
│   ├── calibration/   # CalibrationDialogFragment
│   └── main/          # MainFragment (ViewPager2 + Toolbar)
├── util/
│   ├── SensorManagerHelper.kt   # Sensor fusion + filtering
│   ├── SoundManager.kt          # Web Audio feedback
│   ├── ThemeManager.kt          # Dark/light theme
│   └── CalibrationManager.kt   # Persist calibration offsets
└── viewmodel/
    └── InclinometerViewModel.kt # Central state (Hilt + StateFlow)
```

## Tech Stack

| Library | Purpose |
|---|---|
| Room | Local measurement history |
| Hilt | Dependency injection |
| CameraX | Camera overlay preview |
| Navigation Component | Fragment navigation |
| ViewPager2 | Mode tabs |
| Material 3 | UI components |
| Kotlin Coroutines + Flow | Async sensor data |
