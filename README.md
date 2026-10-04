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
- Without a configured key, the app deterministically uses an osmdroid development fallback backed by OpenStreetMap MAPNIK tiles. It supports map rendering, pan, and zoom for development only; it is not Google Maps and does not validate the production Google Maps path.
- A07 lets the user select a map point, displays one marker at the selected coordinate, and shows its latitude/longitude. The same saveable `MapCoordinate` state is used for either map provider; the initial Jakarta camera position is not treated as a user selection.
- If coarse location permission is already granted, the app makes one location request to center the initial map only. Otherwise it keeps the deterministic default center; the user can explicitly request approximate device location from the map. Device location never selects the GeoTag coordinate.
- Interactive map appearance offers Normal, Satellite, Terrain, and Hybrid with Google Maps. The osmdroid development fallback supports Normal only and marks other styles unavailable.
- A08 resolves an address from the manually selected coordinate using Android `Geocoder`; the coordinate remains valid when address lookup is unavailable or fails.
- A09 lets the user select a date and a 24-hour time independently using Material 3 pickers. Confirmed selections are saveable and authoritative for future image generation; cancel preserves the previous value. Defaults use the current date/time in Asia/Jakarta (WIB) until selected by the user.
- A10 generates a local QR from the currently selected coordinate using `https://maps.google.com/?q=LATITUDE,LONGITUDE&t=h&z=18`. The `q` value contains only latitude and longitude; `t=h` and `z=18` are separate parameters. The QR is regenerated when the selected coordinate changes and is absent before a location is selected.
- ZXing Core encodes QR codes on-device; encoding does not require internet, backend, database, Place ID, or Places API. Internet is needed only when opening the scanned Google Maps URL.
- A11 adds a separate Google Maps SDK mini-map snapshot path. It explicitly uses satellite imagery, is centered on `selectedCoordinate` at deterministic zoom `16.0`, and marks that exact coordinate. Its camera/style are independent of the interactive map. The whole Maps snapshot is retained for attribution/branding; Static Maps API is not used.
- A11's production snapshot is blocked until valid Google Maps configuration is supplied. With no key, the app states that the development osmdroid map cannot provide this product snapshot; osmdroid is not used as a satellite substitute. Satellite rendering and snapshot capture still require production Google Maps runtime validation.
- No camera permission or camera-related code is present.

Final image composition, save/share flow, and Firebase Remote Config are not yet implemented. A11's snapshot is not yet verified against a live Google Maps configuration. Task verification status is recorded in `IMPLEMENTATION_STATUS.md`.

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

The current app uses Android Photo Picker’s per-item URI grant; it does not request broad storage access. Photo access validation and preview decoding remain on-device. The optional one-time approximate device-location permission is foreground-only and is used only for initial map centering; reverse geocoding uses the selected coordinate. Google Maps and the development fallback retrieve map content over the network; photos are not uploaded and there is no custom backend. Camera support remains prohibited and absent.

## Known limitations

The app previews selected images, retains a manually selected map coordinate, and displays an address when Android Geocoder is available; it does not generate or save a final GeoTag image. Preview decoding is bounded in size; metadata, full-resolution composition, save, and share workflow features remain unimplemented. Live Google Maps rendering still requires a valid restricted local API key and has not been verified in this environment.
