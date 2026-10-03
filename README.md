# GeoTag Photo Generator Android

Native Android application for generating locally processed GeoTag images from an existing device photo and a selected map coordinate.

## Current implementation

- Kotlin Android app module with package ID `com.geotagphotogenerator`.
- Android SDK configuration targets SDK 36 with min SDK 23.
- Jetpack Compose and Material 3 foundation are in place.
- Adaptive workflow shell switches between mobile and wide layouts using responsive Compose layout logic.
- App theme and typography define the project’s shared design foundation.
- A04 opens Android’s image-only Photo Picker, validates selected image access off the main thread, and retains the selected URI in saveable Compose state.
- A05 decodes the selected image off the main thread into a bounded-size preview, applies image orientation, and displays loading, empty, and error states.
- Replaced and disposed preview bitmaps are released; invalid, unavailable, and too-large images show an in-app error instead of crashing.
- Picker cancellation preserves any previously selected photo and preview.
- A06 integrates the Google Maps Compose SDK with a real map surface, default Jakarta camera position, and SDK pan/zoom controls when a valid local Maps API key is configured.
- Without a configured key, the app shows an explicit Maps configuration message instead of a fake map. Map runtime access is not yet verified in the current environment.
- No camera permission or camera-related code is present.

Coordinate selection, address resolution, date/time selection, QR generation, final image composition, save/share flow, and Firebase Remote Config are not yet implemented. Their status is recorded in `IMPLEMENTATION_STATUS.md`.

## Local prerequisites

- Android Studio with JDK 17.
- Android SDK Platform 36 and Build-Tools 36.1.0.
- Network access on the first Gradle invocation to download the Gradle distribution and declared plugins.

## Build

Install JDK 17 and Android SDK Platform 36 with Build-Tools 36.1.0. Configure `JAVA_HOME` to your JDK 17 installation, then run:

```powershell
.\gradlew.bat assembleDebug
```

## Google Maps local configuration

To load the real map, create a Google Cloud API key with Maps SDK for Android enabled and appropriate Android-app/API restrictions. Supply the key locally using `local.properties`:

```properties
MAPS_API_KEY=replace_with_your_restricted_key
```

`local.properties` is ignored by Git. The build also accepts the `MAPS_API_KEY` Gradle property or the `GOOGLE_MAPS_API_KEY` environment variable. Do not put a real key in tracked source or documentation; restrict it to this Android package/signing certificate and the Maps SDK for Android. The API key is included in the installed app manifest as required by the SDK, so it must be restricted. Google Cloud project, billing, key restrictions, and live map rendering have not been verified in this environment.

## Privacy and permissions

The current app uses Android Photo Picker’s per-item URI grant; it does not request broad storage access. Photo access validation and preview decoding remain on-device. When configured, Google Maps SDK retrieves map content over the network; photos are not uploaded and there is no custom backend. Camera support remains prohibited and absent.

## Known limitations

The app previews selected images but does not generate or save a final GeoTag image. Preview decoding is bounded in size; the full-resolution compositor and later map, metadata, generation, save, and share workflow features remain unimplemented.
