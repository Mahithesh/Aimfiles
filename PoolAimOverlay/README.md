# Pool Aim Overlay

Native Android Kotlin project for a manual visual practice overlay. It does not read game memory, automate input, or modify another app.

## Build
- Android Studio with JDK 17+
- Android SDK Platform 36 / Build Tools 36.0.0
- Android Gradle Plugin 8.9.2, Gradle 8.11.1

Open this directory in Android Studio and run `:app:assembleDebug`. The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

A GitHub Actions workflow is included at `.github/workflows/android-build.yml`. Push this project to a GitHub repository and run the **Build Android APK** workflow; it uploads debug and unsigned release APK artifacts.

## Implemented in this source snapshot
- Compose dark-themed home screen and overlay-permission flow.
- User-started foreground overlay service (special-use FGS declaration).
- Independent WindowManager drawing window and floating control panel.
- Custom Canvas rectangle, transparent fill, point circles and full-width/full-height crosshair guides.
- Multiple point creation, point hit-selection/drag, undo, delete selected, clear, pan mode and corner resizing.
- Marker size, line width, border width and fill-opacity controls.
- Local JSON layout persistence using SharedPreferences; save and load most recently stored layout.

## Known incomplete items
This is not yet a feature-complete release. Layout CRUD (rename/duplicate/delete/select), edge resize handles, per-point editing/label UI, independent rectangle width/height sliders, reset controls, minimize/collapse behavior, full gesture arbitration and robust service-recovery testing still need work. The included panel currently offers a basic save/load flow, not the full requested layout manager.

## Install and use
1. Install the debug APK on Android 8.0+.
2. Grant **Display over other apps** permission.
3. Tap **Start Floating Mode**.
4. Tap inside the rectangle to place a point. Drag a point to move it. Enable Pan Mode to move the whole rectangle.
5. Use the panel to adjust opacity and drawing dimensions, save/load, undo, delete or clear points.

## Verification status
The source and XML files were created, and XML parsing passed. **No Gradle build, APK generation, emulator test or Android-device test has been completed.** This execution environment has Java 21 but no Gradle installation, Android SDK, Android platform JAR, or Gradle wrapper JAR. DNS resolution for Google's Android SDK host fails, so the required build tools cannot be downloaded here. Do not treat this archive as a tested APK release.
