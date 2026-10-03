# GeoTag Photo Generator Android

Native Android application for producing locally generated photos with selected map coordinates and related GeoTag details. The project is currently at its Android bootstrap stage.

## Current implementation

- Kotlin Android application module with application ID `com.geotagphotogenerator`.
- Minimum Android SDK 23; compile and target SDK 36.
- A Compose launcher activity renders through a Material 3 theme.
- No permissions are declared, including no camera permission.

Google Maps, Photo Picker, Firebase Remote Config, image generation, save, and share are not implemented yet. Their status is recorded in `IMPLEMENTATION_STATUS.md`.

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

The bootstrap has no runtime permissions and no network-facing product services. Camera support is prohibited and absent.

## Known limitations

This is only task A01. It is not yet a functional GeoTag generator.
