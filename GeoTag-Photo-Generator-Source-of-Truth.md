# GeoTag Photo Generator — Source of Truth

**Status:** FINAL / Approved for Native Android + Google Play Store  
**Product:** GeoTag Photo Generator  
**Previous reference:** Web prototype (completed)  
**Target:** Native Android application  
**Audience:** AI coding agents, Android developers, reviewers

---

## 1. Product Goal

GeoTag Photo Generator is a mobile utility for creating a final JPG/PNG from:

- an existing photo selected from the device,
- a precisely selected Google Maps coordinate,
- manually selected date,
- manually selected time,
- human-readable location/address when available,
- mini map with marker,
- latitude/longitude,
- QR code that opens the exact coordinate in Google Maps.

Core flow:

```text
Open App
  ↓
Select Existing Photo
  ↓
Select Location on Google Maps
  ↓
Confirm Coordinate
  ↓
Select Date
  ↓
Select Time
  ↓
Generate GeoTag
  ↓
Preview
  ↓
Save / Share
```

The web prototype is a behavioral/reference implementation only. The production target is native Android.

---

## 2. NON-NEGOTIABLE PRODUCT DECISIONS

### 2.1 Native Android

Use:

- Kotlin
- Jetpack Compose
- Material 3
- Material 3 Adaptive where appropriate
- Google Maps SDK for Android
- Android Photo Picker / system picker
- Android Bitmap + Canvas
- local QR generation
- MediaStore
- Android Sharesheet
- simple MVVM/state-driven architecture

Do not create another web application.

### 2.2 NO CAMERA

This is an absolute requirement.

Do NOT use:

- `android.permission.CAMERA`
- CameraX
- camera intents
- camera preview
- camera capture
- camera hardware APIs
- "Take Photo"
- "Capture Photo"

The only photo input is an existing photo selected from the device.

```text
Select Existing Photo
        ↓
Android Photo Picker
        ↓
Selected URI
```

### 2.3 No Account / No Database

Do not add:

- login
- registration
- profile
- user history
- saved projects
- Supabase
- Firebase database
- Room database
- PostgreSQL
- MongoDB

unless explicitly requested in a future product decision.

### 2.4 No Cloud Photo Processing

Source photos and generated images are processed locally whenever practical.

Do not upload user photos to a custom backend.

Temporary cache/files are acceptable when required by Android APIs.

### 2.5 Manual Date/Time Is Authoritative

The user-selected date/time is the source of truth.

Never silently replace it with:

- EXIF date/time
- current device time
- server time
- GPS acquisition time
- map selection time

Default timezone: `Asia/Jakarta` (WIB).

---

## 3. Location and Google Maps

Use the official Google Maps SDK for Android.

Required behavior:

- map is visible,
- pan/zoom works,
- user can select a point,
- marker is visible,
- latitude/longitude update immediately,
- coordinate precision is preserved internally.

Internal model:

```kotlin
latitude: Double
longitude: Double
```

Do not use Places SDK unless a future requirement explicitly needs Places functionality.

Do not use:

- scraped tiles,
- unofficial map endpoints,
- billing bypasses,
- fake map images,
- unsupported workarounds.

Google Cloud billing/API configuration is required for Google Maps SDK usage. Keep API keys restricted and never commit secrets.

### Mini-map

Prefer an officially supported Android Google Maps rendering/snapshot approach such as `GoogleMap.snapshot()` when suitable and compliant.

Do not use Static Maps API merely because it is familiar if the native SDK snapshot can satisfy the requirement.

Always preserve required Google Maps attribution/branding.

---

## 4. Address Resolution

Use an appropriate Android-supported approach.

Prefer Android `Geocoder` when sufficient.

For modern Android versions, use its asynchronous API rather than blocking/deprecated patterns.

If an address is unavailable:

- keep the coordinate usable,
- show a clear fallback,
- never fabricate address data.

Do not add Places API merely for reverse geocoding unless technically justified and explicitly approved.

---

## 5. QR

QR generation must be local.

Required payload:

```text
https://www.google.com/maps?q={latitude},{longitude}
```

Rules:

- preserve exact useful coordinate precision,
- do not shorten the URL,
- do not redirect through this application,
- do not require a database,
- QR must open Google Maps at the selected coordinate.

---

## 6. Final Image

The output must be a real JPG/PNG, not a screenshot of the Android UI.

It must contain:

1. user-selected photo,
2. mini map,
3. selected marker,
4. location/address when available,
5. latitude,
6. longitude,
7. manually selected date,
8. manually selected time,
9. QR code,
10. required map attribution/branding,
11. styling consistent with the approved reference/web prototype.

Preferred pipeline:

```text
Photo URI
  ↓
Decode / Resize Safely
  ↓
Map Snapshot + Location + Coordinates + Date/Time + QR
  ↓
Bitmap + Canvas
  ↓
Final JPG/PNG
```

Large image work must not block the UI thread.

---

## 7. UI / UX

Use modern native Android UI:

- Jetpack Compose
- Material 3
- Material 3 Adaptive
- mobile-first
- touch-friendly controls
- accessible labels/content descriptions
- adequate contrast
- clear loading/error states
- restrained animations
- professional visual hierarchy
- responsive behavior for different window sizes

Do not introduce a third-party UI framework unless a concrete technical reason is documented.

The visual flow should remain faithful to the approved product/reference rather than becoming an unrelated redesign.

---

## 8. Performance

Performance is a first-class requirement.

Use:

- efficient bitmap decoding,
- downsampling for very large images,
- background/off-main-thread image composition,
- careful resource release,
- minimal unnecessary network requests,
- lazy initialization where appropriate,
- R8 for release builds,
- Baseline Profile where practical,
- Compose performance best practices.

Do not make numerical performance promises unless actually measured.

Potential generation states:

```text
Preparing photo...
Preparing map...
Generating QR...
Composing image...
Almost done...
```

---

## 9. Remote App Control

### Firebase Remote Config

Use **Firebase Remote Config only as a lightweight remote configuration/operational control layer**.

Do NOT add Firebase Authentication, Firestore, Realtime Database, Storage, or Functions for this requirement.

Required parameters:

```text
app_enabled
maintenance_mode
maintenance_title
maintenance_message
minimum_supported_version
```

### Normal

```text
app_enabled = true
maintenance_mode = false
```

### Maintenance

```text
app_enabled = true
maintenance_mode = true
```

The app displays the configured maintenance screen and disables the core workflow.

### Emergency Disable

```text
app_enabled = false
```

The app displays a locked/disabled screen and prevents the core workflow.

### Minimum Version

Example:

```text
minimum_supported_version = 1.3.0
```

Older versions should instruct the user to update.

### Offline behavior

Do NOT lock the application merely because the network is temporarily unavailable.

Use the last successfully fetched configuration according to Firebase Remote Config behavior.

Remote Config is not a security boundary. Treat its client-visible values as non-secret configuration. A determined user can potentially modify a client application. The purpose here is operational control, not DRM.

---

## 10. Save / Share

Save:

- use Android MediaStore or the appropriate modern Android media-saving mechanism.

Share:

- use Android Sharesheet/share intent.

No server upload is required.

---

## 11. Privacy

The application should operate locally whenever practical.

Do not:

- upload photos to a custom backend,
- store user photo history,
- store generated images remotely,
- create user accounts,
- create a user database,
- send photos to cloud image-processing services.

---

## 12. Billing and Cost Control

The project must use the least expensive compliant architecture.

Avoid unnecessary Google services.

Do NOT add:

- Places SDK without a requirement,
- Routes API,
- Static Maps API if native map snapshot is sufficient,
- cloud image processing,
- cloud photo storage,
- custom backend,
- database.

Use Google Cloud Billing Budget & Alerts for developer cost monitoring.

Do not build a custom billing-monitor system.

---

## 13. Developer Cost Monitoring

Developer billing alerts are infrastructure/configuration, not an Android feature.

Use Google Cloud's native Budget & Alerts / notification facilities.

The Android application must not:

- access billing APIs,
- contain billing credentials,
- contain a service account,
- monitor developer costs,
- send developer billing emails.

See `DEVELOPER-COSTS-AND-BILLING.md`.

---

## 14. Security

Never commit:

- Google API keys,
- Firebase service account credentials,
- signing keys,
- keystores,
- passwords,
- tokens.

Use appropriate API key restrictions and release configuration.

Do not put privileged Google Cloud credentials in the APK.

---

## 15. Architecture

Suggested separation:

```text
ui/
viewmodel/
map/
photo/
location/
qr/
composer/
storage/
remoteconfig/
```

The exact package structure may differ if the agent documents a better choice.

Avoid over-engineering.

---

## 16. Explicitly Out of Scope

Do not add:

- camera
- camera permission
- login
- registration
- profile
- user history
- database
- cloud photo storage
- cloud image processing
- social features
- filters
- AI image generation
- automatic EXIF Date/Time override
- proprietary QR redirects
- subscriptions
- advertisements
- Places search/autocomplete
- Routes
- custom billing-monitor backend

unless explicitly approved later.

---

## 17. Acceptance Criteria

### Build

- Android project builds.
- Debug APK builds.
- Release/AAB configuration is valid.
- No unnecessary permissions.
- `CAMERA` permission is absent.

### Photo

- Existing photo can be selected.
- Android Photo Picker/system picker works.
- Camera is never invoked.
- Photo preview works.

### Map

- Google Maps loads with valid configuration.
- User can pan/zoom.
- User can select a coordinate.
- Marker is visible.
- Latitude/longitude update correctly.

### Date/Time

- Manual date works.
- Manual time works.
- Values reach the final image.
- EXIF does not override them.

### Address

- Address shown when available.
- Missing address handled gracefully.
- No fabricated data.

### QR

- QR contains the selected coordinate.
- QR scans.
- QR opens Google Maps at that coordinate.

### Image

- Valid JPG/PNG is produced.
- Required components are present.
- Image is not merely a UI screenshot.

### Save/Share

- Save works.
- Share works.

### Remote Control

- Remote Config is fetched correctly.
- `app_enabled=false` locks the app.
- maintenance mode works.
- minimum supported version works.
- temporary network failure does not automatically lock the app.

### Privacy

- No login.
- No database.
- No custom backend for photos.
- No permanent remote photo storage.
- No camera permission.

---

## 18. AI Coding Agent Execution Protocol

The agent must follow this order:

```text
INSPECT
  ↓
PLAN
  ↓
IMPLEMENT ONE TASK
  ↓
BUILD / TEST
  ↓
SELF-AUDIT
  ↓
FIX
  ↓
BUILD / TEST AGAIN
  ↓
FINAL SELF-AUDIT
  ↓
UPDATE IMPLEMENTATION_STATUS.md
  ↓
NEXT TASK
```

A task may only be marked `VERIFIED` when there is evidence that it works.

Never mark work as complete merely because:

- code compiles,
- a screen exists,
- a function was written,
- a test was imagined,
- or a TODO was added.

### Self-audit must check

- dead code,
- unused imports,
- unused dependencies,
- unused state,
- unused functions/classes,
- duplicate code,
- unnecessary abstractions,
- TODO/FIXME leftovers,
- debug logging,
- `println`,
- hard-coded test data,
- fake credentials,
- secrets,
- unnecessary permissions,
- main-thread blocking,
- bitmap memory risks,
- lifecycle problems,
- Compose performance issues,
- camera-related code,
- regression against previous tasks.

### Status rule

Update `IMPLEMENTATION_STATUS.md` only after validation.

Allowed statuses:

```text
NOT_STARTED
IN_PROGRESS
VERIFIED
BLOCKED
FAILED
```

Every `VERIFIED` task must include:

- what changed,
- validation commands,
- actual results,
- self-audit result,
- known limitations if any.

---

## 19. README

The coding agent must create/update `README.md`.

README must describe the actual implementation, not assumptions.

Include:

- project purpose,
- requirements,
- Android/Gradle setup,
- build commands,
- Google Maps configuration,
- Firebase Remote Config configuration,
- API key restrictions,
- billing requirements,
- permissions,
- testing,
- release/AAB build,
- Play Store preparation,
- privacy,
- known limitations.

Never document camera support.

---

## 20. Final Rule

This document is authoritative.

The web prototype is reference material only.

Prefer official Android/Google APIs, local processing, minimal dependencies, privacy-preserving architecture, low recurring infrastructure cost, maintainable Kotlin, and Play Store compatibility.

---

## 21. Session Continuity / Token Exhaustion

The project must remain recoverable across separate AI coding sessions.

AI agents must not rely on previous chat memory. Persistent project state is stored in:

- source code,
- Git history/status when available,
- `IMPLEMENTATION_STATUS.md`,
- this Source of Truth,
- `AGENTS.md`,
- `AI-CODING-AGENT-PROMPT.md`,
- `DEVELOPER-COSTS-AND-BILLING.md`,
- `README.md` after it is created.

If an AI session ends because of token/context limits or another interruption, the next session must inspect the repository and resume from the earliest task that is not `VERIFIED`.

An incomplete task must remain `IN_PROGRESS`, `BLOCKED`, or `FAILED` as appropriate. It must never be marked `VERIFIED` merely because code exists.

Before a substantial session ends, the agent must update `IMPLEMENTATION_STATUS.md` with completed work, remaining work, validation, self-audit, blockers, and the exact next action.

