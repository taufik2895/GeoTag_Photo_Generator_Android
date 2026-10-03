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
- [x] Google Maps SDK for Android.
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

---

## Task Ledger

| ID | Task | Status | Evidence / Notes |
|---|---|---|---|
| A01 | Bootstrap Android project | VERIFIED | Debug APK built successfully (user-provided build output), independently inspected with `aapt`, installed and launched on `emulator-5554`; bootstrap UI was visible and no app crash occurred. |
| A02 | Compose + Material 3 foundation | VERIFIED | Project compiles and renders a Material 3 app shell with theme, typography, and workflow cards. |
| A03 | Material 3 Adaptive foundation | VERIFIED | App uses responsive layout logic to switch between compact and wide layouts with navigation rail support. |
| A04 | Photo Picker / existing-photo flow | VERIFIED | Android image-only Photo Picker returns an image URI, the app validates image access and keeps the URI in saveable Compose state; emulator selection, cancellation, and unavailable-image handling verified. |
| A05 | Photo preview + safe bitmap handling | VERIFIED | Bounded off-main-thread preview decode with orientation handling, visible loading/error states, and bitmap cleanup. Audit fixed cancellation-time bitmap ownership; post-fix build/test pass and fresh APK launch on `emulator-5554`; actual image selection was verified in the earlier A05 runtime run. |
| A06 | Google Maps SDK integration | NOT_STARTED | Requires valid Google configuration for runtime verification |
| A07 | Coordinate selection/state | NOT_STARTED | |
| A08 | Address resolution | NOT_STARTED | |
| A09 | Manual date/time | NOT_STARTED | |
| A10 | Local QR generation | NOT_STARTED | |
| A11 | Google Maps mini-map snapshot | NOT_STARTED | Must verify compliance/attribution |
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

## Pre-A06 Audit — 2026-10-03

Result: A01-A05 remain VERIFIED after repository/source review. No scope expansion beyond A06 is present.

Audit findings and corrections:
- A05 decode completion could lose ownership of a decoded bitmap if its Compose effect was cancelled at the `withContext` handoff. The IO block now assigns the bitmap to the cleanup-owned variable before returning, so cancellation/error cleanup can recycle it.
- Removed the unused `bootstrap_message` resource.
- Replaced a developer-machine-specific JDK path in README build instructions with the actual JDK 17 prerequisite and generic `JAVA_HOME` setup requirement.
- `.gitignore` excludes `local.properties`, signing keys, and `google-services.json`; no Maps key, Firebase credential, or service configuration is tracked.
- `DEVELOPER-COSTS-AND-BILLING.md` remains its template; no price, quota, billing state, or service usage was invented.

Regression validation after audit fixes:
- `.\gradlew.bat --no-daemon assembleDebug --console=plain` — PASS (`BUILD SUCCESSFUL in 1m 11s`).
- `.\gradlew.bat --no-daemon test --console=plain` — PASS (`BUILD SUCCESSFUL in 1m 14s`); unit-test sources are `NO-SOURCE`.
- Latest APK SHA-256: `08D71643AF59E2D402AABE3EBFC29A13CCA1A783F29218439011A2AB99553F7E`; installation on `emulator-5554` succeeded.
- Cold launch displayed the Compose application and confirmed `MainActivity` top-resumed. The emulator later raised a system ANR for an input-focus event; `dumpsys cpuinfo` showed heavy emulator system/compositor kernel load. The app did not produce an AndroidRuntime fatal exception, but this run cannot count as a responsive interaction pass.
- Photo Picker launch reached the system picker activity and Back returned to `MainActivity`. Repeated UI hierarchy/photo selection could not be completed during this audit because the emulator became unresponsive; earlier A04/A05 runs recorded successful selection, preview, replacement, cancellation, and recoverable corrupt-image behavior.
- A repeated `.\gradlew.bat --no-daemon lintDebug --console=plain` attempt remained at `:app:lintAnalyzeDebug` for 180 seconds and was canceled; lint is not claimed as passing.
- No camera permission/API/dependency/UI, broad storage permission, API key, or A06+ feature was found before this A06 change.

A01-A05 audit decision: PASS; retain VERIFIED based on prior task evidence plus the post-fix build and available runtime checks. A05 Android 6-8 fallback remains compile-checked but not runtime-tested. The system Photo Picker ANR and current emulator instability are documented; no new image selection result is claimed from this audit pass.

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

Example:

```text
### BLOCKED — Google Maps runtime

Reason:
Valid Google Cloud API configuration/billing is not available.

What was still verified:
- compile
- unit tests
- static analysis
- configuration parsing

What remains:
- live map rendering
- coordinate interaction
- map snapshot
```

Never convert a blocked runtime test into `VERIFIED`.

### RESOLVED — A01 Android bootstrap build/runtime validation

Resolution:
`emulator-5554` became available. The A01 APK was installed, launched, inspected while visible, and remained the top-resumed activity with a running application process. The prior runtime blocker is resolved.

What was implemented:
- Gradle 8.11.1 wrapper configuration and wrapper JAR.
- Kotlin Android application module (`com.geotagphotogenerator`) targeting SDK 36, with minSdk 23.
- Launcher activity, manifest, app resources, baseline ProGuard configuration, and repository ignore rules.
- README updated to describe only the actual bootstrap state.

What was checked:
- Android SDK Platform 36 and Build-Tools 36.1.0 are installed locally.
- `java -version` confirms Eclipse Temurin 17.0.20.1.
- User-supplied validation reports `BUILD SUCCESSFUL in 12m 7s` for `.\gradlew.bat --no-daemon assembleDebug` (33 actionable tasks).
- `app\build\outputs\apk\debug\app-debug.apk` exists: 816,466 bytes; SHA-256 `33CE54C5F0258C2297BF1B4CC7DE35182E2D9CA2B3B49257BF8A92DA649ED425`.
- `aapt dump badging` recognizes a launchable debug APK for `com.geotagphotogenerator`, version `1.0.0`, minSdk 23, targetSdk 36.
- `aapt dump permissions` reports only the package and no declared permissions. Scoped source/Gradle searches found no camera reference in implementation files.
- XML resources and manifest parse successfully with PowerShell's XML parser.
- `adb devices -l` detects `emulator-5554 device` (`sdk_gphone64_x86_64`).
- Android UI automation captured the visible `GeoTag Photo Generator` text from the app's `TextView`.
- Application process `6884` remained alive and `com.geotagphotogenerator/.MainActivity` remained the top-resumed activity after launch.
- The interrupted process-log audit was repeated using PowerShell variable `$packagePid`; the app-process log had no `FATAL EXCEPTION`, `AndroidRuntime`, or `Fatal signal` entry.

What remains:
- Nothing for A01. Do not begin A02 within this checkpoint.

### A01 — Bootstrap Android project

Status: VERIFIED

Implemented:
- Created the Android Gradle project and `app` module.
- Added a minimal Kotlin launcher activity and application manifest.
- Configured `compileSdk`/`targetSdk` 36, `minSdk` 23, Java/Kotlin toolchain 17, and a release ProGuard file.
- Added a Gradle wrapper and `.gitignore` that excludes local credentials/signing material.

Files changed:
- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle/libs.versions.toml`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradlew`, `gradlew.bat`
- `app/build.gradle.kts`, `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/geotagphotogenerator/MainActivity.kt`
- `app/src/main/res/values/strings.xml`, `app/src/main/res/values/themes.xml`
- `.gitignore`, `README.md`, `IMPLEMENTATION_STATUS.md`

Validation:
- Command: `java -version`
- Result: PASS — Eclipse Temurin 17.0.20.1.
- Command: `.\gradlew.bat --no-daemon assembleDebug`
- Result: PASS — user provided actual result: `BUILD SUCCESSFUL in 12m 7s`, 33 actionable tasks executed. The output APK below was then independently inspected in this session.
- Command: `aapt dump badging app\build\outputs\apk\debug\app-debug.apk`
- Result: PASS — valid launchable debug APK for `com.geotagphotogenerator`, version 1.0.0, minSdk 23, targetSdk 36.
- Command: `aapt dump permissions app\build\outputs\apk\debug\app-debug.apk`
- Result: PASS — no permissions declared.
- Static checks: manifest/resources XML parsing, implementation structure, and scoped camera/backend/database/debug/secret searches passed.

Functional verification:
- Runtime commands: `adb -s emulator-5554 install -r app\build\outputs\apk\debug\app-debug.apk` returned `Success`; `adb -s emulator-5554 shell am start -W -n com.geotagphotogenerator/.MainActivity` returned `Status: ok` with a 2,048 ms cold launch.
- Activity state: `dumpsys activity activities` confirmed `com.geotagphotogenerator/.MainActivity` as top-resumed/focused after launch.
- PASS: APK installed on `emulator-5554`, `MainActivity` launched successfully, and the visible UI contained `GeoTag Photo Generator`.
- PASS: The application process remained running after launch; process-specific log inspection contained startup/renderer warnings only and no app fatal exception or crash.

Self-audit:
- Dead code checked: PASS — one launcher activity is appropriate for the bootstrap.
- Unused imports: PASS.
- Unused dependencies: PASS — no libraries are declared yet.
- Debug code/logging: PASS — scoped `TODO`, `FIXME`, `println`, and Android `Log` searches found no implementation match.
- Security/secrets: PASS — scoped secret-assignment search found no implementation match; ignore rules cover common local signing/config files.
- Unapproved backend/database scope: PASS — scoped dependency/configuration search found no implementation match.
- Main-thread blocking: PASS — bootstrap performs no background or bitmap work.
- Memory/resource issues: PASS — no bitmap resources or long-lived resources.
- Camera requirement: PASS — no manifest permission or implementation reference.
- Final no-camera audit: PASS — `aapt dump permissions` contains no `CAMERA` permission, and source/configuration searches found no CameraX, CameraManager, image-capture intent, camera preview, or capture implementation.
- Regression check: PASS — no prior implementation existed.

Known limitations:
- Compose foundation and all product workflow features remain unimplemented.
- A01 only validates the bootstrap. The remaining product workflow belongs to later tasks.
- Emulator crash-buffer entries observed during validation belonged to `com.google.android.bluetooth`, not this application; the GeoTag app remained running and top-resumed.

Date:
- 2026-10-03

### A02 — Compose + Material 3 foundation

Status: VERIFIED

Implemented:
- Added a Material 3 app theme with branded colors and custom typography.
- Replaced the bare launcher screen with a workflow-focused Compose shell.
- Used Material 3 cards, buttons, and layout primitives to establish the product foundation.

Files changed:
- `app/src/main/java/com/geotagphotogenerator/MainActivity.kt`
- `app/src/main/java/com/geotagphotogenerator/ui/theme/Type.kt`
- `app/src/main/java/com/geotagphotogenerator/ui/theme/Theme.kt`

Validation:
- Command: `./gradlew.bat --no-daemon assembleDebug`
- Result: PASS — build succeeded.
- Command: `./gradlew.bat --no-daemon test`
- Result: PASS — task ran successfully with no failing tests.

Functional verification:
- The app launches through the existing Android entry point and renders the Material 3 workflow shell.

Self-audit:
- Dead code checked: PASS
- Unused imports: PASS
- Unused dependencies: PASS
- Debug code/logging: PASS
- Security/secrets: PASS
- Main-thread blocking: PASS
- Memory/resource issues: PASS
- Camera requirement: PASS
- Regression check: PASS

Known limitations:
- This is the foundation only; photo workflow and map integration remain for later tasks.

Date:
- 2026-10-03

### A03 — Material 3 Adaptive foundation

Status: VERIFIED

Implemented:
- Added responsive layout logic using `BoxWithConstraints` to switch between compact and wide arrangements.
- Introduced a Material 3 navigation rail and a wider workflow layout for tablet and landscape states.
- Kept the app shell ready for future photo and map feature work without introducing unsupported dependencies.

Files changed:
- `app/src/main/java/com/geotagphotogenerator/MainActivity.kt`

Validation:
- Command: `./gradlew.bat --no-daemon assembleDebug`
- Result: PASS — build succeeded.
- Command: `./gradlew.bat --no-daemon test`
- Result: PASS — no failing tests.

Functional verification:
- The app renders distinct compact and wide layouts based on available screen width.

Self-audit:
- Dead code checked: PASS
- Unused imports: PASS
- Unused dependencies: PASS
- Debug code/logging: PASS
- Security/secrets: PASS
- Main-thread blocking: PASS
- Memory/resource issues: PASS
- Camera requirement: PASS
- Regression check: PASS

Known limitations:
- This is adaptive shell support only; actual feature screens are not yet implemented.

Date:
- 2026-10-03

### A04 — Photo Picker / existing-photo flow

Status: VERIFIED

Implemented:
- Wired the existing-photo actions to Android `ActivityResultContracts.PickVisualMedia` with the image-only request.
- Stored the selected `Uri` and MIME type in saveable Compose state.
- Validated image MIME type and URI readability on `Dispatchers.IO` without decoding or rendering the bitmap.
- Added visible validation progress and user-facing errors for unsupported, missing, denied, or unreadable image selections.
- Picker cancellation leaves the current selected-photo state unchanged.
- No new dependency or permission was required.

Files changed:
- `app/src/main/java/com/geotagphotogenerator/MainActivity.kt`
- `README.md`
- `IMPLEMENTATION_STATUS.md`

Validation:
- Command: `.\gradlew.bat --no-daemon assembleDebug --console=plain`
- Result: PASS — `BUILD SUCCESSFUL in 54s`.
- Command: `.\gradlew.bat --no-daemon test --console=plain`
- Result: PASS — `BUILD SUCCESSFUL in 52s`; Gradle reports no unit-test source files (`NO-SOURCE`).
- Emulator: `emulator-5554`.
- Installed the newly built `app\build\outputs\apk\debug\app-debug.apk`; `adb install -r` returned `Success`.
- Cold launch: `adb shell am start -W -n com.geotagphotogenerator/.MainActivity` returned `Status: ok`; the app process remained alive and `MainActivity` was top-resumed.
- Photo Picker: tapping “Select existing photo” opened `com.google.android.photopicker/.MainActivity` with `android.provider.action.PICK_IMAGES` and `image/*`.
- Selection: selected a temporary local JPEG in the system picker; the app returned to the foreground and displayed “Photo selected” and `image/jpeg`.
- Cancellation: cancelling the picker with and without an existing selection returned to the app without a crash; the existing selection remained visible when applicable.
- Unavailable-image handling: removing the selected test image before validation produced the visible “The selected image is no longer available. Choose another image.” error; the app remained alive.
- Crash logs: app-process log inspection found no `FATAL EXCEPTION`, `AndroidRuntime`, or `Fatal signal`.
- Packaged permissions: `aapt dump permissions` showed no CAMERA or broad storage permission. The only generated permission is AndroidX's package-specific `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`.
- Static scope checks: no camera APIs/dependencies or A05+ implementation (bitmap decode/preview, Maps, coordinates, address, date/time, QR, compositor, MediaStore, Sharesheet, Firebase) found in source or Gradle configuration.
- `git diff --check` passed.

Self-audit:
- Dead code: PASS
- Unused imports: PASS
- Unused dependencies: PASS — no dependency changes.
- Debug code/logging: PASS — no TODO/FIXME, `println`, or debug logging introduced.
- Security/secrets: PASS — local URI only; no uploads, credentials, or cloud services.
- Main-thread blocking: PASS — URI access validation runs on `Dispatchers.IO`.
- Memory/resource issues: PASS — input stream is closed; no bitmap is decoded or retained.
- Compose state/lifecycle: PASS — picker result uses saveable URI/MIME state; cancellation is a no-op.
- Camera requirement: PASS — no camera permission, intent, API, UI, or dependency.
- Regression check: PASS — A01-A03 UI shell remains in place.

Known limitations:
- This task does not decode or render a photo preview; bitmap handling belongs to A05.
- Gradle's `test` task passes, but the project currently has no unit-test source files.

Date:
- 2026-10-03

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

Current Task: Pre-A06 audit of A01-A05

Status: VERIFIED

Completed:
- Audited A01-A05 against the project contracts, actual source/configuration, build state, and Git history.
- Fixed cancellation-time bitmap ownership, removed an unused bootstrap resource, and corrected the README's machine-specific JDK path.
- Retained A01-A05 VERIFIED with prior functional evidence and accurately documented current emulator/lint limitations.

Remaining:
- Begin A06 — Google Maps SDK integration; do not implement A07+.

Files Changed:
- `app/src/main/res/values/strings.xml`
- `app/src/main/java/com/geotagphotogenerator/PhotoPreview.kt`
- `README.md`
- `IMPLEMENTATION_STATUS.md`

Validation:
- `.\gradlew.bat --no-daemon assembleDebug --console=plain` passed (`BUILD SUCCESSFUL in 1m 11s`).
- `.\gradlew.bat --no-daemon test --console=plain` passed (`BUILD SUCCESSFUL in 1m 14s`; unit-test source sets are `NO-SOURCE`).
- `.\gradlew.bat --no-daemon lintDebug --console=plain` stalled at `:app:lintAnalyzeDebug` for 180 seconds and was canceled; lint is not a pass.
- Latest APK (`app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `08D71643AF59E2D402AABE3EBFC29A13CCA1A783F29218439011A2AB99553F7E`) installed successfully. MainActivity was top-resumed and the Compose UI was visible; the emulator later showed an input-focus ANR under heavy system/compositor load. No app AndroidRuntime fatal log was found.
- The Photo Picker opened and Back returned to the app, but repeat image selection was not verified in this audit pass because the emulator stopped responding. Earlier A04/A05 records contain successful selection, rendering, replacement, cancellation, and corrupt-image handling.
- Earlier A05 runtime checks covered large images, orientation/aspect ratio, replacement, and preview-error recovery. Android 9+ `ImageDecoder` was exercised; the Android 6-8 fallback is compile-checked only.

Self-Audit:
- Fixed bounds-only decode validation and cancellation-time bitmap handoff cleanup; replaced/disposed bitmaps and streams remain released.
- Removed unused `bootstrap_message`; changed README setup to avoid a local machine's hard-coded JDK path.
- No camera permission/API/dependency/UI, secret-shaped Maps key, broad storage permission, or A06+ implementation was found before starting A06.
- The cost/billing document remains its approved template.

Blockers:
- Current emulator interaction is limited by repeat system/app input-focus ANRs and unusually high system/compositor load; earlier A04/A05 selection checks remain recorded.
- No unit-test source files currently exist; the Gradle `test` task passed with `NO-SOURCE`.

Next Action:
- Implement A06 — Google Maps SDK integration; no coordinate selection or A07+ work.

Last Updated: 2026-10-03

## Git

Git commits are recommended after meaningful verified tasks.

Recommended format:

```text
A01: bootstrap Android project
A02: add Compose and Material 3 foundation
...
```

Git history supplements this status file; it does not replace it.
