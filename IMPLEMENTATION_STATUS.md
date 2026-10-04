# IMPLEMENTATION_STATUS.md — GeoTag Photo Generator Android

**Document type:** Living implementation ledger  
**Rule:** Update this file after every verified task.  
**Source of Truth:** `GeoTag-Photo-Generator-Source-of-Truth.md`

---

## Status Definitions

| Status | Meaning |
|---|---|
| NOT_STARTED | Work has not begun |
| IN_PROGRESS | Work is actively being implemented |
| VERIFIED | Implemented and validated with evidence |
| BLOCKED | Cannot be validated/completed because of an external dependency |
| FAILED | Attempted but validation failed and requires correction |

**Important:** `VERIFIED` requires evidence. Code existence alone is not evidence.

---

## Current Phase

**Android migration / production implementation**

The web prototype is complete and remains a behavioral/reference implementation.

---

## Global Decisions

- [x] Native Android is the production target.
- [x] Kotlin.
- [x] Jetpack Compose.
- [x] Material 3.
- [x] Material 3 Adaptive where appropriate.
- [x] Google Maps SDK for Android as the production map provider.
- [x] osmdroid permitted only as a temporary development fallback when
      Google Maps configuration is unavailable.
- [x] Device location, camera position, and selected coordinate are
      separate; location is a one-time initial-camera aid only.
- [x] Interactive styles are provider-gated; final mini-map must
      explicitly use SATELLITE independently.
- [x] Address resolution uses only the selected coordinate.
- [x] Android Photo Picker.
- [x] Bitmap + Canvas image composition.
- [x] Local QR generation.
- [x] MediaStore.
- [x] Android Sharesheet.
- [x] Simple MVVM/state-driven architecture.
- [x] Firebase Remote Config for remote operational control.
- [x] R8/release optimization.
- [x] Baseline Profile where practical.
- [x] NO CAMERA.
- [x] No application database.
- [x] No cloud photo processing.
- [x] No custom billing-monitor system.

### Map Provider Policy

Production provider:

```text
Google Maps SDK for Android
```

Development-only fallback:

```text
osmdroid + standard OpenStreetMap tiles
```

Provider selection:

```text
Valid Google Maps configuration
    -> Google Maps

No valid Google Maps configuration
    -> osmdroid development fallback
```

Rules:

- Google Maps remains the production provider.
- Do not delete Google Maps code/dependencies/configuration when the key is missing.
- Never initialize both providers simultaneously.
- Both providers use the same `latitude: Double` / `longitude: Double` model.
- osmdroid is development-only and must never become the production map provider.
- osmdroid must never be used as a Google Maps billing bypass.
- No Google tile scraping or unofficial Google map endpoints.
- osmdroid runtime success does not equal Google Maps runtime success.
- Final release validation must verify real Google Maps SDK behavior.

---

## Task Ledger

| ID | Task | Status | Evidence / Notes |
|---|---|---|---|
| A01 | Bootstrap Android project | VERIFIED | Commit `e8be3ee`; debug APK built, installed and launched on `emulator-5554`; no camera permission. |
| A02 | Compose + Material 3 foundation | VERIFIED | Commit `5fcea7c`; build/test and emulator launch validation passed. |
| A03 | Material 3 Adaptive foundation | VERIFIED | Responsive compact/wide layout and navigation rail behavior validated. |
| A04 | Photo Picker / existing-photo flow | VERIFIED | Commit `d3dd048`; image-only Photo Picker, URI/MIME saveable state, validation, cancellation and unavailable-image handling verified. |
| A05 | Photo preview + safe bitmap handling | VERIFIED | Commit `8666127`; bounded decode/orientation, off-main-thread processing and bitmap cleanup verified; cancellation ownership was fixed. |
| A06 | Google Maps SDK integration | BLOCKED | Google Maps production implementation, dependency, and manifest configuration remain present. Real Google Maps runtime verification is pending because no valid local API key/project/billing configuration is available. The debug build reports `MAPS_API_KEY_CONFIGURED = false`; no `local.properties` is present. |
| A07 | Coordinate selection/state | VERIFIED | Provider-independent nullable `MapCoordinate(latitude: Double, longitude: Double)` is saveable across recreation and shared by both providers. `test` and `assembleDebug` pass. On `emulator-5554`, two distinct fallback map taps updated the displayed coordinate and single marker; a drag/pan retained the selected coordinate. No crash/ANR; no camera permission/API. Verified 2026-10-04. |
| A08 | Address resolution | VERIFIED | Android Geocoder resolves only the manually selected coordinate; one-time coarse location centers the initial camera without auto-selecting a point; provider-specific interactive styles expose only supported fallback Normal style. Follow-up lint audit added explicit coarse-permission guards and API-level annotations to the location/Geocoder helpers; all A08 lint errors are cleared. Fresh debug APK built and installed on `emulator-5554`; permission-denial fallback, granted-location centering, map taps, coordinate updates, and address rendering were checked. Google Maps runtime/styles remain blocked under A06. Verified 2026-10-04. |
| A09 | Manual date/time | VERIFIED | Independent saveable date/time state and Material 3 pickers implemented. Final debug APK passed test/build and emulator checks for date/time confirmation, independence, cancellation, A08 map/address regression, and Photo Picker cancellation. A08 lint findings were corrected. Whole-project lint remains failed on six A05 `PhotoPreview.kt` API-level errors; no A05 code was changed. Verified 2026-10-04. |
| A10 | Local QR generation | VERIFIED | Local ZXing Core 3.5.3 encoder builds the exact `https://maps.google.com/?q=LATITUDE,LONGITUDE&t=h&z=18` payload only from `selectedCoordinate`. Three unit tests decoded exact reference/example/full-precision payloads; the QR on the final emulator APK screenshot was independently decoded to its exact selected-coordinate URL. Map reselection regenerated a changed URL/QR; date/time changes left it unchanged. `test` and `assembleDebug` PASS; lint remains blocked only by six existing A05 `PhotoPreview.kt` API errors. No A11+ implementation. Verified 2026-10-04. |
| A11 | Google Maps mini-map snapshot | BLOCKED | Google Maps SDK snapshot path is implemented as satellite-only and selected-coordinate-driven, but production rendering/snapshot/attribution cannot be runtime-verified while `MAPS_API_KEY_CONFIGURED = false`. The emulator correctly reports the missing configuration instead of presenting osmdroid as satellite. |
| A12 | Bitmap + Canvas compositor | NOT_STARTED | |
| A13 | Final image preview | NOT_STARTED | |
| A14 | MediaStore Save | NOT_STARTED | |
| A15 | Android Sharesheet Share | NOT_STARTED | |
| A16 | Firebase Remote Config foundation | NOT_STARTED | |
| A17 | Maintenance mode | NOT_STARTED | |
| A18 | Remote emergency disable (`app_enabled`) | NOT_STARTED | |
| A19 | Minimum supported version | NOT_STARTED | |
| A20 | Error handling | NOT_STARTED | |
| A21 | Accessibility | NOT_STARTED | |
| A22 | Performance hardening | NOT_STARTED | |
| A23 | R8 + release optimization | NOT_STARTED | |
| A24 | Baseline Profile | NOT_STARTED | |
| A25 | README + setup documentation | NOT_STARTED | Agent-owned |
| A26 | Final integration testing | NOT_STARTED | |
| A27 | Final dead-code/security audit | NOT_STARTED | |
| A28 | Release/AAB verification | NOT_STARTED | |
| A29 | Play Store readiness audit | NOT_STARTED | |

---

## Mandatory Per-Task Record

For every task completed, append/update a record:

```text
### Axx — <Task Name>

Status: VERIFIED

Implemented:
- ...

Files changed:
- ...

Validation:
- Command:
- Result:

Functional verification:
- ...

Self-audit:
- Dead code checked: PASS/FAIL
- Unused imports: PASS/FAIL
- Unused dependencies: PASS/FAIL
- Debug code/logging: PASS/FAIL
- Security/secrets: PASS/FAIL
- Main-thread blocking: PASS/FAIL
- Memory/resource issues: PASS/FAIL
- Camera requirement: PASS/FAIL
- Regression check: PASS/FAIL

Known limitations:
- ...

Date:
- YYYY-MM-DD
```

Do not mark `VERIFIED` until all applicable checks pass.

---

## Self-Audit Log

### Rule

After each task:

1. run validation,
2. inspect changed code,
3. inspect nearby affected code,
4. remove dead/unnecessary code,
5. fix findings,
6. run validation again,
7. record the result.

Example:

```text
### Self-Audit — Axx

Initial validation:
- PASS/FAIL

Findings:
- ...

Corrections:
- ...

Final validation:
- PASS/FAIL

Final audit:
- PASS
```

---

## Blocked Items

Record external blockers here.

### BLOCKED — A06 Google Maps runtime

Reason:
No valid Google Maps API key/project/billing/API restriction configuration
is available in the current development environment. The latest debug
build has `MAPS_API_KEY_CONFIGURED = false`, and `local.properties` is
absent.

What was verified:
- Google Maps dependency/configuration path exists;
- Google Maps code remains in the application;
- osmdroid development fallback is implemented;
- the production Google Maps code and fallback are selected by a deterministic build configuration gate;
- only the configured provider is composed for the map surface.

What remains:
- Google Maps rendering;
- Google Maps pan/zoom;
- runtime verification with a valid restricted Google Maps configuration.

Important:
- osmdroid fallback validation, if performed, is development-only evidence.
- It must never be reported as Google Maps validation.
- A07 was independently developed and runtime-validated using the approved development fallback; this does not change A06's status.

### VERIFIED — A07 Coordinate selection/state

Implemented:
- Provider-independent `MapCoordinate` state (`latitude: Double`,
  `longitude: Double`) with a saveable state saver.
- Manual map selection, selected-coordinate display, and one updating
  marker for both Google Maps Compose and the osmdroid development
  fallback.
- osmdroid taps are distinguished from drag/pan gestures; its map view is
  contained within a native frame to avoid drawing over neighboring UI.
- Google Maps implementation and configuration remain intact; the
  fallback is active only because the key is not configured.

Files changed for A07:
- `app/src/main/java/com/geotagphotogenerator/GoogleMapCard.kt`
- `app/src/main/java/com/geotagphotogenerator/MainActivity.kt`
- `README.md`
- `IMPLEMENTATION_STATUS.md`

Validation:
- `.\gradlew.bat --no-daemon test --console=plain` — PASS (test source
  sets are currently `NO-SOURCE`).
- `.\gradlew.bat --no-daemon assembleDebug --console=plain` — PASS.
- Installed the freshly built debug APK on `emulator-5554`; app launched
  with `MainActivity` resumed and remained alive.
- UI hierarchy showed the development map, initial "Tap the map to choose
  a location" state, and then both latitude and longitude after selection.
- Two taps produced distinct coordinates and updated the existing marker;
  a subsequent map drag/pan left the selected coordinate unchanged.
- Latest logcat scan found no `FATAL EXCEPTION`, app ANR, or input
  dispatch timeout.
- Installed permissions include INTERNET only; no CAMERA permission.
- Source/dependency camera scan found no camera API/dependency.
- Provider audit: Google Maps implementation/dependency/configuration are
  present; Google Maps is inactive because the API key is not configured;
  osmdroid is active as the development fallback; simultaneous provider
  initialization was not observed.
- `git diff --check` — no whitespace errors in A07 changes. Existing
  repository edits still produce a pre-existing blank-line-at-EOF notice
  in `IMPLEMENTATION_STATUS.md`.
- Lint was not run in this pass; no lint PASS is claimed.

Self-audit:
- Dead code / unused imports in the changed map code: PASS.
- Repeated provider initialization / simultaneous providers: PASS.
- Main-thread blocking calls added: none.
- Marker accumulation: PASS; one remembered osmdroid marker is moved.
- Camera/privacy audit: PASS.
- A08+ implementation: not started.

Known limitations:
- Google Maps production rendering and interaction remain unverified until
  a valid restricted API key/configuration is supplied (A06 remains
  BLOCKED).
- Runtime fallback evidence does not establish Google Maps behavior.

Date:
- 2026-10-04

---

### A08 — Selected-coordinate address resolution and approved map behavior

Status: VERIFIED

Implemented:
- Reverse-resolves the user's selected map coordinate with Android
  `Geocoder`. Uses the asynchronous API on Android 13+ and performs the
  legacy synchronous API on `Dispatchers.IO` on older Android versions.
- Represents loading, resolved, unavailable, not-found, error, and
  timeout states. A missing address never invalidates the selected
  latitude/longitude.
- Adds optional one-time `ACCESS_COARSE_LOCATION` for initial map
  centering. The selected GeoTag coordinate, device location, and map
  camera remain separate. Permission denial/unavailability retains the
  deterministic Jakarta map fallback; explicit action can retry.
- Adds provider-gated interactive styles. Google Maps maps its four
  supported choices to native map types; osmdroid exposes Normal only.
- Documents that future generated mini-map rendering must explicitly use
  Satellite independently of interactive appearance. Mini-map
  generation itself is not implemented by this task.

Files changed for this task:
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/geotagphotogenerator/AddressResolver.kt`
- `app/src/main/java/com/geotagphotogenerator/DeviceLocationProvider.kt`
- `app/src/main/java/com/geotagphotogenerator/GoogleMapCard.kt`
- `app/src/main/java/com/geotagphotogenerator/MainActivity.kt`
- `README.md`
- `GeoTag-Photo-Generator-Source-of-Truth.md`
- `AGENTS.md`
- `AI-CODING-AGENT-PROMPT.md`
- `DEVELOPER-COSTS-AND-BILLING.md`
- `IMPLEMENTATION_STATUS.md`

Validation:
- `.\gradlew.bat --no-daemon -I .\validation-build-dir.gradle test --console=plain` — PASS; test source sets are `NO-SOURCE`.
- `.\gradlew.bat --no-daemon -I .\validation-build-dir.gradle assembleDebug --console=plain` — PASS.
- The regular Gradle build directory was locked by the VS Code Kotlin
  language-server process (`java.exe`, PID 31468). Left that unrelated
  process untouched and used an isolated build output directory.
- Installed the newly produced `validation-build\app\outputs\apk\debug\app-debug.apk`
  on `emulator-5554`, cleared app state, and cold-launched the app.
- With coarse location granted, the emulator's simulated location
  centered the map while the UI remained at “Tap the map to choose a
  location”; no user-selected coordinate was created automatically.
- Tapping the map produced latitude/longitude and a Geocoder address.
  A second tap changed both coordinate values. Address lookup followed
  the new selected coordinate.
- Denying coarse location displayed the default-center fallback message
  and left manual map selection available.
- Development map showed Normal enabled and Satellite/Terrain/Hybrid
  unavailable. Google Maps style runtime was not exercised because no
  valid Maps API key/configuration is present.
- The app process and `MainActivity` remained alive/resumed. Last-500-line
  logcat scan contained no fatal exception, fatal signal, or ANR.
- Manifest contains INTERNET and ACCESS_COARSE_LOCATION only; no camera,
  fine-location, or background-location permission. Source/dependency
  scan found no camera feature/API/dependency.
- Source/scope scan found no A09+ implementation (date/time, QR, mini-map
  snapshot, compositor, MediaStore, Sharesheet, or Firebase).
- `git diff --check` — PASS; only Git line-ending normalization notices.
- Lint was not run; no lint PASS is claimed.

Self-audit:
- Address resolution is keyed only to `selectedCoordinate`: PASS.
- Device location does not assign the selected coordinate or marker: PASS.
- Location request is coarse, foreground-only, bounded, and cancellable:
  PASS.
- Recomposition does not initialize both map providers: PASS by
  deterministic provider gate; osmdroid fallback observed at runtime.
- Blocking legacy Geocoder call is dispatched to IO: PASS by inspection.
- Camera/privacy and out-of-scope audits: PASS.

Known limitations:
- Location fix on the emulator is simulated and is not evidence of
  physical-device GPS behavior.
- No forced Geocoder timeout/error was induced; those UI outcomes are
  implemented but only normal address resolution was observed.
- A06 remains BLOCKED: production Google Maps rendering and its style
  behavior need a valid restricted API key/configuration and real runtime
  verification.
- The future generated mini-map's Satellite behavior remains a
  documented requirement, not an implemented/verified feature.

Date:
- 2026-10-04

---

### A09 — Manual date/time selection

Status: VERIFIED

Implemented:
- Adds independently saveable `selectedDateMillis` (UTC-midnight
  calendar-day value for DatePicker) and `selectedTimeMinutes`
  (24-hour wall-clock minutes) in the app's Compose state.
- Initializes picker defaults from the current date and time in
  `Asia/Jakarta` (WIB); defaults do not replace user-confirmed values.
- Adds responsive Material 3 date and time picker dialogs. Date/time are
  committed only by confirmation; Cancel and dialog dismissal discard the
  draft. Time is displayed and selected in 24-hour `HH:mm`.
- Changes to date and time update only their own field; date/address/map
  remain independent.
- Keeps the wide layout scrollable after adding the picker card.

Files changed:
- `app/src/main/java/com/geotagphotogenerator/ManualDateTimeCard.kt`
- `app/src/main/java/com/geotagphotogenerator/MainActivity.kt`
- `app/src/main/java/com/geotagphotogenerator/DeviceLocationProvider.kt`
- `app/src/main/java/com/geotagphotogenerator/AddressResolver.kt`
- `GeoTag-Photo-Generator-Source-of-Truth.md`
- `AGENTS.md`
- `AI-CODING-AGENT-PROMPT.md`
- `README.md`
- `IMPLEMENTATION_STATUS.md`

Validation:
- Standard-output Gradle validation is blocked by the existing VS Code
  Kotlin language-server lock on `app/build/.../R.jar`; the unrelated
  process was not stopped.
- `.\gradlew.bat --no-daemon -I .\validation-build-dir.gradle test --console=plain` — PASS after the final A08 permission guard; test source sets are `NO-SOURCE`.
- `.\gradlew.bat --no-daemon -I .\validation-build-dir.gradle assembleDebug --console=plain` — PASS after the final A08 permission guard.
- `.\gradlew.bat --no-daemon -I .\validation-build-dir.gradle lint --console=plain` — FAIL: 6 `NewApi` errors remain in the existing A05 `PhotoPreview.kt` ImageDecoder helper, plus 9 warnings and 2 hints. The A08 permission/API findings are cleared; no unrelated A05 fix was made.
- The newly built APK was installed and cold-launched on `emulator-5554`; process and top-resumed MainActivity were confirmed.
- A fallback map tap changed the selected coordinate to
  `37.72402166460627, -122.15560913085938`; Geocoder displayed
  `159 W Joaquin Ave, San Leandro, CA 94577, USA`.
- Confirming October 15, 2026 changed only the date; time remained
  `20:29`. Date cancellation preserved the confirmed date.
- Confirming time `21:45` changed only the time; date remained
  `15 October 2026`. Time cancellation preserved `20:29` before the
  confirmation check.
- The existing-photo Photo Picker opened and canceled; coordinate,
  address, date, and time remained unchanged.
- The app process remained alive. Logcat scan found no fatal exception,
  ANR, `SecurityException`, or permission-denial event.
- Camera audit: no camera permission, API, dependency, intent, or UI.
- A10+ scope audit: no QR generation, mini-map snapshot, compositor,
  MediaStore save, Sharesheet, or Firebase implementation.
- `git diff --check` — PASS; only Git line-ending normalization notices.

Self-audit:
- Saveable date and time state are separate: PASS.
- Picker draft commits only on confirmation: PASS.
- Date changes do not modify time; time changes do not modify date: PASS.
- Device time is used only for initial WIB defaults: PASS.
- Date/time do not feed back into coordinate, device-location, or
  address state: PASS.
- A08 permission checks are explicit and newer location/Geocoder APIs
  are version-annotated: PASS.
- No new dependency, permission, camera feature, or paid service: PASS.
- Temporary Gradle init script and generated validation output cleaned:
  PASS.

Known limitations:
- Google Maps production runtime remains BLOCKED under A06; style
  interaction on this emulator is limited to the osmdroid development
  fallback's Normal style.
- No final image generation exists yet; selected date/time are available
  as app state for future milestones.

Date:
- 2026-10-04

---

### A10 — Local QR generation

Status: VERIFIED

A09 audit before A10:
- `selectedCoordinate` is only updated by user map-tap callbacks. The
  one-time optional coarse device fix updates `deviceLocation` and camera
  viewport only; it does not select a coordinate or move the marker.
- Google Maps and osmdroid each receive the same provider-independent
  coordinate. Provider selection is build-config gated, so only one map
  provider is composed. The production Google Maps dependency/configuration
  remains present; A06 remains BLOCKED without a valid key.
- Address resolution is keyed to `selectedCoordinate`; resolver output
  updates address state and does not write to the coordinate.
- Location is coarse, foreground-only and one-shot with a timeout; no
  background tracking or location history is present. Permission
  unavailability leaves the map usable at its default center.
- Date and time are separate saveable values. Picker draft state is local
  to each picker and writes only after confirmation; cancellation/dismissal
  does not call the parent setter. The current clock initializes defaults
  only and confirmed values are not overwritten by coordinate/address
  changes or QR generation.
- The manifest and dependencies contain no camera permission or camera
  capability.

Implemented:
- Added ZXing Core 3.5.3 as the local QR encoder; no network service,
  backend, database, Places API, Place ID, or Google Maps API call is used
  to encode the QR.
- Builds `https://maps.google.com/?q=${latitude},${longitude}&t=h&z=18`
  only from the current selected `MapCoordinate`. The `q` value has only
  the two coordinates; `t=h` and `z=18` are separate parameters.
- Preserves the selected `Double` string precision without rounding.
  There is no QR when the coordinate is null. Payload-keyed UI state
  immediately removes the old QR on selection change.
- Generates a high-contrast 512x512 bitmap off the main thread and
  releases replaced/disposed bitmaps. The UI shows the QR and exact URL.
- Added exact payload + ZXing decode unit cases for the supplied reference
  coordinate, user example coordinate, and additional full-precision
  coordinate.

Files changed:
- `app/src/main/java/com/geotagphotogenerator/LocationQrCode.kt`
- `app/src/test/java/com/geotagphotogenerator/LocationQrCodeTest.kt`
- `app/src/main/java/com/geotagphotogenerator/MainActivity.kt`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `GeoTag-Photo-Generator-Source-of-Truth.md`
- `AGENTS.md`
- `AI-CODING-AGENT-PROMPT.md`
- `DEVELOPER-COSTS-AND-BILLING.md` (QR policy text only; cost audit remains a template)
- `README.md`
- `IMPLEMENTATION_STATUS.md`

Validation:
- `.\gradlew.bat --no-daemon test --rerun-tasks --console=plain` — PASS;
  three QR unit tests ran, with no failures or skips.
- `.\gradlew.bat --no-daemon test --console=plain` after the final QR
  bitmap API adjustment — PASS.
- `.\gradlew.bat --no-daemon assembleDebug --console=plain` after all
  production source changes — PASS.
- Installed the fresh debug APK on `emulator-5554`, cold-launched
  `MainActivity`, and confirmed the process remained alive.
- With no selected coordinate, the QR card displayed its empty-state
  prompt and no QR. A manual map tap selected
  `37.74574303801686, -122.1295166015625`; the address resolved and the
  UI showed the exact corresponding URL and QR.
- A second map tap changed the coordinate to
  `37.72402166460627, -122.1295166015625`; the displayed URL changed
  immediately and the refreshed QR screenshot decoded to that exact URL.
- A final screenshot from the final APK was decoded locally using the
  ZXing Core 3.5.3 JVM decoder. Decoded result:
  `https://maps.google.com/?q=37.74574303801686,-122.1295166015625&t=h&z=18`.
- The exact reference and user-example URLs were each generated and
  decoded in unit tests:
  `https://maps.google.com/?q=-7.19005,107.90158&t=h&z=18` and
  `https://maps.google.com/?q=-6.9705992,107.7648696&t=h&z=18`.
- The exact user-example URL was opened with Android's VIEW intent and
  launched the installed Google Maps app. Maps rendered the destination;
  its visible coordinate text rounded to seven decimal places.
- A09 regression on the A10 APK: map tap changed coordinate and address;
  date confirmation changed only date; time confirmation changed only
  time; date/time cancellation preserved confirmed values; date/time
  edits left the QR URL unchanged; the Photo Picker opened and canceled
  without losing app state.
- Final app log/process checks found no fatal exception, ANR,
  `OutOfMemoryError`, or QR-generation failure; MainActivity process
  remained alive.
- Lint — FAIL: six existing `NewApi` errors remain in A05
  `PhotoPreview.kt`, with nine warnings and two hints. No lint finding
  points to the A10 QR implementation.
- Camera audit — no CAMERA permission, CameraX/Camera2/API, capture
  intent, or camera UI/dependency found in manifest, Gradle config, or
  main source.
- A11+ audit — this task adds no map snapshot, final image compositor,
  final image preview, MediaStore save, Sharesheet, or Firebase feature.
- `git diff --check` — PASS; only existing line-ending normalization
  notices.

Self-audit:
- Payload source is only `selectedCoordinate`; no parallel coordinate
  state, device location, camera center, address, metadata, or URL parsing:
  PASS.
- URL separators, exact coordinate precision, no-selection state, and
  stale-QR replacement: PASS.
- QR encoding and bitmap rendering are local; encoding runs off the main
  thread; old bitmaps are recycled: PASS.
- Added dependency is the only production QR encoder and is used; JUnit
  is test-only: PASS.
- No QR changes to selected coordinate, map style, date, or time: PASS.
- Camera/scope/secrets/debug logging audit: PASS.

Known limitations:
- Google Maps production map runtime remains BLOCKED under A06 because no
  valid restricted Maps API key/configuration is available; the app's
  interactive map runtime used the approved osmdroid development fallback.
- QR encoding is implemented without network APIs and unit-tested locally;
  a dedicated airplane-mode emulator run was not performed.
- Lint remains failed by the existing unrelated A05 `PhotoPreview.kt`
  API-level diagnostics.

Date:
- 2026-10-04

---

### A11 — Google Maps mini-map snapshot

Status: BLOCKED

A10 audit before A11:
- The committed A10 QR builder uses only the supplied `MapCoordinate`
  and produces
  `https://maps.google.com/?q=LATITUDE,LONGITUDE&t=h&z=18`; the `q`
  parameter remains only the full-precision coordinate pair.
- The three ZXing unit tests independently encode/decode the reference,
  user-example, and full-precision URLs.
- The A10 UI receives `selectedCoordinate` as an immutable input and
  does not update coordinate, address, date, or time state.
- No Places, Routes, Static Maps, cloud QR, or camera capability is
  present.

Implemented:
- Added a mini-map card to compact and wide layouts. The configured
  production path uses a separate Google Maps Compose surface and the
  official `GoogleMap.snapshot()` SDK callback.
- The snapshot is keyed to the current selected coordinate, moves an
  independent camera to that exact latitude/longitude at deterministic
  zoom `16.0`, and includes a marker at that point.
- Both the Compose map properties and the SDK map instance explicitly
  set Satellite. Snapshot state records its source coordinate; a new
  selection cancels the prior effect, hides the old snapshot, and
  recycles its bitmap when replaced or disposed.
- Map-load readiness and snapshot callbacks have bounded timeouts and
  explicit failure states rather than indefinite loading.
- The preview displays the full snapshot without cropping; native map
  attribution/branding is not deliberately removed or covered.
- Without configured Google Maps, the app shows an explicit production
  snapshot blocker. It does not substitute osmdroid or claim that
  fallback tiles are satellite imagery.
- The mini-map is independent of interactive camera/style and does not
  use device location, address, QR payload, or hard-coded coordinates.
- No Static Maps API or new dependency/service was added.

Files changed:
- `app/src/main/java/com/geotagphotogenerator/GoogleMapsMiniMap.kt`
- `app/src/main/java/com/geotagphotogenerator/MainActivity.kt`
- `GeoTag-Photo-Generator-Source-of-Truth.md`
- `AGENTS.md`
- `AI-CODING-AGENT-PROMPT.md`
- `README.md`
- `IMPLEMENTATION_STATUS.md`

Validation:
- `.\gradlew.bat --no-daemon test --console=plain` — PASS; A10 QR
  suite ran 3 tests, 0 failures/errors/skips.
- `.\gradlew.bat --no-daemon assembleDebug --console=plain` — PASS.
- Installed fresh debug APK on `emulator-5554`, cold-launched
  `MainActivity`, and confirmed the process remained alive and resumed.
- The final post-install cold launch took about 23 seconds to first render
  and logged 899 skipped frames. No ANR was recorded for
  `com.geotagphotogenerator`; this startup jank is observed, unexplained,
  and remains for performance follow-up.
- Runtime selected a coordinate on the osmdroid development fallback.
  The UI explicitly displayed that Google Maps satellite snapshot is
  blocked until `MAPS_API_KEY` is configured; no false satellite preview
  was shown.
- A10 regression on the same app session: selected coordinate changes
  updated the visible full-precision QR URL; address followed the selected
  coordinate; selected date `04 October 2026` and time `22:50` remained
  unchanged across selection changes. Panning the interactive map left
  selected coordinate and QR unchanged.
- Android Photo Picker opened; cancel returned to the app and preserved
  coordinate/QR/date/time state.
- Last-2000-line logcat check found no fatal exception, ANR, OOM, or fatal
  signal for `com.geotagphotogenerator`. Logcat did contain an ANR in the
  separate `com.google.android.apps.maps` process; it is not evidence of
  the A11 path, which was not initialized without the key. `MainActivity`
  remained alive and top-resumed.
- Lint — FAIL: six known `NewApi` errors in A05 `PhotoPreview.kt`, plus
  nine warnings and two hints. No A11 lint finding was reported.
- Camera audit: no camera permission/API/dependency/intent/UI found.
- A12+ scope audit: no compositor, final-image preview, MediaStore save,
  Sharesheet, or Firebase implementation was added.
- `git diff --check` — PASS.

Self-audit:
- Snapshot path is only instantiated when the Google Maps config gate is
  true; when false, the existing app uses only its osmdroid interactive
  fallback and the A11 production snapshot reports blocked: PASS by code
  and emulator UI.
- Snapshot style is explicitly SATELLITE, independent of interactive
  `mapDisplayType`: PASS by code inspection.
- Snapshot coordinate/camera/marker derive only from selectedCoordinate:
  PASS by code inspection.
- Rapid selection cancellation and bitmap cleanup are handled; no
  activity/map reference is retained beyond the composable: PASS by code
  inspection.
- Google Maps snapshot generation, satellite imagery, marker rendering,
  and branding could not be exercised because the Maps API key is not
  configured: BLOCKED.
- The emulator's final cold startup was slow and skipped frames; the
  cause was not established in this A11 pass.

Known limitations / blockers:
- `MAPS_API_KEY_CONFIGURED = false`; `local.properties` contains no
  configured Maps key. Real Google Maps rendering and `GoogleMap.snapshot`
  behavior—including satellite tiles, marker, and included attribution—
  have NOT been runtime-verified. osmdroid fallback does not satisfy this
  verification.
- A06 remains BLOCKED. A11 remains BLOCKED until valid restricted Google
  Maps configuration is supplied and the production snapshot path is
  verified on-device.
- Interactive style independence could not be exercised with Google Maps;
  the active osmdroid fallback only supports Normal. The snapshot path
  does not read interactive style state.
- Whole-project lint remains failed on existing A05 API-level findings.

Date:
- 2026-10-04

---

## Regression Checklist

Before final release, verify:

- [ ] no camera permission
- [ ] no camera APIs
- [ ] existing photo selection works
- [ ] map works with valid configuration
- [ ] coordinate precision preserved
- [ ] address fallback works
- [ ] manual date/time preserved
- [ ] QR opens exact coordinate
- [ ] mini-map is compliant
- [ ] final JPG/PNG is valid
- [ ] Save works
- [ ] Share works
- [ ] Remote Config works
- [ ] maintenance mode works
- [ ] emergency disable works
- [ ] minimum version works
- [ ] offline/network failure behavior is safe
- [ ] no secrets committed
- [ ] no dead code
- [ ] no unnecessary dependencies
- [ ] no obvious memory leaks/bitmap pressure
- [ ] release build works
- [ ] AAB works
- [ ] README reflects reality

---

## Final Release Gate

The project is not considered complete until:

1. all applicable tasks are `VERIFIED`,
2. external blockers are resolved or explicitly documented,
3. final integration tests pass,
4. final dead-code/security audit passes,
5. release AAB is validated,
6. Play Store readiness is reviewed,
7. README is accurate,
8. this document contains evidence for the final validation.

---

# Session Recovery / Checkpoint Rules

This file is a persistent project-state ledger. It must allow a new AI coding session to continue without relying on previous chat context.

## Required checkpoint information

For every task marked `IN_PROGRESS`, record:

- current objective;
- completed work;
- remaining work;
- files changed;
- validation already performed;
- validation results;
- self-audit already performed;
- known blockers;
- exact next action.

For every task marked `VERIFIED`, record evidence that the implementation was actually validated.

## If a session ends unexpectedly

The next agent must:

1. read this file;
2. inspect the actual source tree;
3. inspect Git status/history when available;
4. find the earliest task that is not `VERIFIED`;
5. continue from the actual implementation state;
6. never assume previous chat memory;
7. never mark a task verified without fresh evidence when necessary.

## Session checkpoint

At the end of every substantial session, update:

```text
Current Task:
Status:
Completed:
Remaining:
Files Changed:
Validation:
Self-Audit:
Blockers:
Next Action:
Last Updated:
```

Do not erase useful historical evidence merely to make the file shorter.

## Current Session Checkpoint

Current Task: A11 — Google Maps mini-map snapshot

Status: BLOCKED

Completed:
- Audited A10 only; verified the exact local QR URL, full-precision
  selectedCoordinate source, passing local encode/decode tests, and
  absence of side effects on address/date/time.
- Implemented a separate Google Maps SDK snapshot path that explicitly
  enforces Satellite, selectedCoordinate camera/marker, deterministic
  zoom 16.0, and coordinate-keyed bitmap cleanup. Added an explicit
  missing-key blocked state instead of representing osmdroid as satellite.
- Fresh debug APK passed build/test, installed and ran on
  `emulator-5554`. A10 selection/QR/address/date/time/pan and Photo Picker
  open/cancel regressions passed.
- Google Maps production snapshot remains unverified because no Maps API
  key is configured; A11 is therefore BLOCKED, not VERIFIED.

Files Changed:
- `app/src/main/java/com/geotagphotogenerator/GoogleMapsMiniMap.kt`
- `app/src/main/java/com/geotagphotogenerator/MainActivity.kt`
- `GeoTag-Photo-Generator-Source-of-Truth.md`
- `AGENTS.md`
- `AI-CODING-AGENT-PROMPT.md`
- `README.md`
- `IMPLEMENTATION_STATUS.md`

Validation:
- `.\gradlew.bat --no-daemon test --console=plain` — PASS (3 A10 QR
  tests, 0 failures/errors/skips).
- `.\gradlew.bat --no-daemon assembleDebug --console=plain` — PASS.
- APK installed/launched on `emulator-5554`; process remained alive.
- Final cold launch rendered after about 23 seconds and reported 899
  skipped frames; no ANR for this app, but startup jank needs follow-up.
- A11 UI showed snapshot blocked until `MAPS_API_KEY` configuration.
- A10 coordinate/QR/address/date/time/pan and Photo Picker cancellation
  regression checks passed.
- Lint FAIL: six existing A05 `PhotoPreview.kt` API errors, nine warnings,
  two hints; no A11 lint finding.
- Logcat also contained an ANR in the separate `com.google.android.apps.maps`
  process; the A11 Google Maps path was not initialized without a key.
- `git diff --check` — PASS.

Self-Audit:
- Snapshot uses selectedCoordinate only, has independent satellite camera,
  fixed zoom, exact marker, and does not follow interactive style/camera:
  PASS by code inspection.
- Snapshot capture preserves the full bitmap/branding and disposes
  replaced bitmaps; no fake fallback: PASS by code inspection.
- Camera and A12+ scope audits: PASS.

Blockers:
- Google Maps production key/configuration is missing; real satellite
  snapshot path is not runtime-verified. A06 and A11 remain BLOCKED until
  valid restricted configuration and the relevant real Google Maps
  runtime checks are supplied.
- Whole-project lint remains failed on existing A05 ImageDecoder API
  findings.

Next Action:
- Configure a valid restricted Maps SDK key, then validate the real Google
  Maps satellite snapshot, marker, attribution, coordinate changes, and
  interactive-style independence. Keep A06 BLOCKED until separately
  validated; do not start A12.

Last Updated:
- 2026-10-04

---

## Git

Git commits are recommended after meaningful verified tasks.

Recommended format:

```text
A01: bootstrap Android project
A02: add Compose and Material 3 foundation
...
```

Git history supplements this status file; it does not replace it.
