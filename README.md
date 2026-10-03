# GeoTag Photo Generator Android

Native Android application for generating locally processed GeoTag images from an existing device photo and a selected map coordinate.

## Current implementation

- Kotlin Android app module with package ID `com.geotagphotogenerator`.
- Android SDK configuration targets SDK 36 with min SDK 23.
- Jetpack Compose and Material 3 foundation are in place.
- Adaptive workflow shell switches between mobile and wide layouts using responsive Compose layout logic.
- App theme and typography define the project’s shared design foundation.
- No camera permission or camera-related code is present.

Google Maps integration, Photo Picker, QR generation, final image composition, save/share flow, and Firebase Remote Config are not yet implemented. Their status is recorded in `IMPLEMENTATION_STATUS.md`.

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

The current app foundation has no runtime permissions and no network-facing product services. Camera support remains prohibited and absent.

## Known limitations

The app is still in the foundational stage. It verifies the Compose foundation and adaptive layout shell, but the product workflow features remain to be implemented.
