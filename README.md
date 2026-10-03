# GeoTag Photo Generator Android

Native Android application for generating locally processed GeoTag images from an existing device photo and a selected map coordinate.

## Current implementation

- Kotlin Android app module with package ID `com.geotagphotogenerator`.
- Android SDK configuration targets SDK 36 with min SDK 23.
- Jetpack Compose and Material 3 foundation are in place.
- Adaptive workflow shell switches between mobile and wide layouts using responsive Compose layout logic.
- App theme and typography define the project’s shared design foundation.
- A04 opens Android’s image-only Photo Picker, validates selected image access off the main thread, and retains the selected URI in saveable Compose state.
- Selection cancellation preserves any prior selected-photo state; invalid or unavailable images show an in-app error.
- No camera permission or camera-related code is present.

Google Maps integration, advanced photo preview/bitmap handling, QR generation, final image composition, save/share flow, and Firebase Remote Config are not yet implemented. Their status is recorded in `IMPLEMENTATION_STATUS.md`.

## Local prerequisites

- Android Studio with JDK 17.
- Android SDK Platform 36 and Build-Tools 36.1.0.
- Network access on the first Gradle invocation to download the Gradle distribution and declared plugins.

## Build

On Windows PowerShell, set `JAVA_HOME` to a JDK 17 installation (the development environment currently uses Eclipse Temurin 17.0.20.1), then run:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot'
.\gradlew.bat assembleDebug
```

## Privacy and permissions

The current app uses Android Photo Picker’s per-item URI grant; it does not request broad storage access. Photo access validation and later processing remain on-device. No network-facing product service is implemented. Camera support remains prohibited and absent.

## Known limitations

The app currently has the Compose/adaptive foundation and existing-photo selection. It does not yet decode or render a photo preview, or implement later map, metadata, generation, save, and share workflow features.
