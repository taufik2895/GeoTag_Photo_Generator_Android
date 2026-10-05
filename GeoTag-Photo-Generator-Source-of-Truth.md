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
- Google Maps SDK for Android for the production interactive coordinate picker
- Mapbox Static Images API for the final exported satellite mini-map
- Mapbox Reverse Geocoding for the final exported address
- osmdroid only as a temporary development fallback for the interactive map when Google Maps configuration is unavailable
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

The user-selected date and time are separate, authoritative values for the
future generated GeoTag image.

Never silently replace either value with:

- EXIF date/time
- file creation or modification time
- current device time
- server time
- network time
- GPS acquisition time
- map selection time

Use the device date/time only to initialize the pickers before the user
has made a choice. Confirming a picker makes that user choice
authoritative; canceling must leave the previous value unchanged.
Changing the date must not change time, and changing time must not change
date.

Keep the selected calendar date and wall-clock time separate. The
project's default timezone is `Asia/Jakarta` (WIB); do not silently
convert the manually selected time to UTC or another timezone. Display
and edit time in 24-hour `HH:mm` format. Future image generation must
consume the saved date and time without deriving or overriding them from
photo metadata or another clock.

---

## 3. Location, Interactive Google Maps, and Final Map Pipeline

### 3.1 Interactive coordinate picker

Use the official Google Maps SDK for Android for the interactive coordinate
picker. `selectedCoordinate` remains the authoritative user-selected
latitude/longitude.

Internal model:

```kotlin
latitude: Double
longitude: Double
```

### 3.2 Production vs development map provider

The **interactive production provider remains Google Maps SDK for Android**.
A temporary **osmdroid development fallback** is permitted only when valid
Google Maps configuration is unavailable.

Rules:

1. Google Maps remains the production interactive provider.
2. Missing Maps configuration must not delete Google Maps code/dependencies.
3. Valid configuration -> Google Maps; missing configuration -> osmdroid only
   for development/runtime testing.
4. Never initialize both providers simultaneously.
5. Both use the same `latitude: Double` / `longitude: Double` model.
6. osmdroid is never production and never a billing bypass.
7. Do not scrape Google tiles or use unofficial endpoints.
8. Final release validation must include real Google Maps SDK validation.

### 3.3 Initial device location and interactive appearance

`deviceLocation` may center the initial camera only. `mapCameraPosition` is
the viewport. `selectedCoordinate` is set only by a user map tap and is the
authoritative GeoTag coordinate.

Device location must never automatically set the selected coordinate or
marker, and must never be used for final image coordinates, QR content, or
final address. Request only contextual `ACCESS_COARSE_LOCATION`. If denied,
keep the map usable at `(-6.2088, 106.8456)`. No background tracking or
location history.

Interactive Google Maps may be NORMAL, SATELLITE, TERRAIN, or HYBRID. The
osmdroid fallback supports only NORMAL.

### 3.4 Final exported satellite mini-map

The final generated mini-map is **not a Google Maps snapshot**.

Use **Mapbox Static Images API** with:

```text
mapbox/satellite-v9
```

Do not use Mapbox Standard or Mapbox Standard Satellite for Static Images.

The final static image:

- is generated only from `selectedCoordinate`,
- is centered on that exact coordinate,
- includes a marker at that exact coordinate,
- uses a deterministic export zoom defined by implementation,
- is independent of interactive camera/style,
- preserves required Mapbox attribution/branding,
- is decoded locally for Canvas composition.

Do NOT use `GoogleMap.snapshot()`, Google Static Maps, scraped tiles, or
osmdroid tiles for the final exported satellite image.

### 3.5 Final exported address

The final address shown in the exported JPG/PNG is produced by **Mapbox
Reverse Geocoding**, using only `selectedCoordinate`.

The existing Android `Geocoder` from A08 may remain for interactive/on-screen
address behavior, but it is not authoritative for the final exported
address.

Rules:

- never use device location or camera center;
- never fabricate address data;
- handle loading/not-found/error/timeout safely;
- use a Mapbox usage mode/contract that permits the address result to be
  retained in the generated user image;
- verify current Mapbox terms, attribution, and pricing before release.

### 3.6 Provider credentials

Never commit Google API keys or Mapbox tokens. Use local/environment
configuration during development and least-privilege token/scopes for the
client-side Mapbox integration.

If Mapbox configuration is unavailable, final-image generation must show an
explicit blocked/error state rather than silently substituting another map
provider.

## 4. Address Resolution

For the final exported image, use **Mapbox Reverse Geocoding** from
`selectedCoordinate`.

The existing Android `Geocoder` from A08 may remain for interactive/on-screen
address resolution and regression compatibility, but final image generation
must use the approved Mapbox address result.

If the address is unavailable, keep latitude/longitude authoritative, show a
clear fallback, and never fabricate address data.

Verify Mapbox geocoding retention/storage rights, attribution, and current
pricing against official documentation before release.

## 5. QR

QR generation must be local.

Required local payload, built only from the user's current
`selectedCoordinate`:

```text
https://maps.google.com/?q={latitude},{longitude}&t=h&z=18
```

Rules:

- `q` contains only `latitude,longitude`; do not append zoom or distance.
- `t=h` and `z=18` are separate URL parameters, not coordinate data.
- preserve the selected `Double` coordinate precision without rounding.
- if no coordinate has been selected, do not generate a QR or use a default.
- regenerate the URL and QR whenever `selectedCoordinate` changes; never
  show a previous QR as if it represents the new selection.
- generate and render the QR locally on-device; no internet, backend,
  database, Places API, or server is needed to encode it.
- do not shorten the URL,
- do not redirect through this application,
- do not require a database,
- do not parse Google Maps page URLs or use Place IDs/Places APIs.
- QR must open Google Maps at the selected coordinate. Internet is needed
  only when the scanned URL is opened for map navigation.

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
Mapbox Static Satellite Bitmap + Mapbox Address + Coordinates + Date/Time + Local QR
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
- Google Static Maps API for final exported images,
- GoogleMap.snapshot() for final exported images,
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
- osmdroid as a production map provider

Development-only osmdroid fallback is permitted only under the Map Provider Policy in Section 3.1 and is not considered a production architecture change.

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
- If Google Maps configuration is unavailable during development, the approved osmdroid fallback may be used for development-only map rendering and pan/zoom validation.
- The fallback must never be treated as production Google Maps validation.
- Google Maps and osmdroid are never initialized simultaneously.
- The same `Double` latitude/longitude model can be consumed by either provider.
- Final release validation verifies the real Google Maps provider.

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

For maps, Google Maps SDK for Android remains the production provider. osmdroid is permitted only as a temporary development fallback when Google Maps configuration is unavailable; it must never replace or obscure the production Google Maps architecture.

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
