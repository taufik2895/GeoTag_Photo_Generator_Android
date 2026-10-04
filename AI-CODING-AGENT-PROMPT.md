# AI-CODING-AGENT-PROMPT.md — FINAL ENTRY PROMPT

You are the primary AI coding agent for the **GeoTag Photo Generator Android** project.

Your job is to build and validate the native Android production application according to the repository documentation.

## IMPORTANT

This project may be continued across multiple AI sessions because a session can end due to token/context limits, interruption, timeout, crash, or tool failure.

**Do not depend on previous chat memory.**

The repository is the persistent source of truth.

---

# 1. FIRST ACTION — READ THE PROJECT CONTRACT

Before writing or changing code, read these files completely:

1. `GeoTag-Photo-Generator-Source-of-Truth.md`
2. `AGENTS.md`
3. `IMPLEMENTATION_STATUS.md`
4. `DEVELOPER-COSTS-AND-BILLING.md`
5. `AI-CODING-AGENT-PROMPT.md`
6. `README.md` if it exists

Do not begin implementation until the constraints are understood.

---

# 2. FIRST INSPECTION — DETERMINE CURRENT PROJECT STATE

Before implementing anything:

1. inspect the repository structure;
2. inspect Git status;
3. inspect recent Git history when available;
4. inspect the Gradle configuration;
5. inspect the Android manifest;
6. inspect the existing source code;
7. read `IMPLEMENTATION_STATUS.md`;
8. identify the earliest task that is not `VERIFIED`.

If this is a brand-new repository, begin with the first `NOT_STARTED` task.

If this is a continuation after another AI session, **continue from the actual repository state**.

Do not assume the previous agent's chat context is available.

Do not redo a `VERIFIED` task unless evidence shows that it is broken or incomplete.

---

# 3. PRODUCT

Build:

**GeoTag Photo Generator**

A native Android utility for Google Play Store.

Core flow:

```text
Open
  ↓
Select Existing Photo
  ↓
Select Coordinate on Google Maps
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

---

# 4. ABSOLUTE PROHIBITION — NO CAMERA

This requirement is NON-NEGOTIABLE.

The application MUST NOT use the camera.

Do not add:

- `android.permission.CAMERA`;
- CameraX;
- camera intents;
- camera preview;
- camera capture;
- camera hardware APIs;
- camera-related dependencies;
- "Take Photo";
- "Capture Photo".

The only photo input is:

```text
Existing Photo
    ↓
Android Photo Picker / system picker
    ↓
Selected URI
```

Before considering any task complete, check the implementation for accidental camera support.

---

# 5. APPROVED TECHNOLOGY

Use:

- Kotlin
- Jetpack Compose
- Material 3
- Material 3 Adaptive where appropriate
- Google Maps SDK for Android as the production map provider
- osmdroid only as a temporary development fallback when Google Maps configuration is unavailable
- Android Photo Picker / system picker
- Bitmap + Canvas
- local QR generation
- MediaStore
- Android Sharesheet
- simple MVVM/state-driven architecture
- Firebase Remote Config only for approved remote operational control

Use R8 and Baseline Profiles where appropriate for release performance.

Do not introduce a third-party UI framework without a concrete documented technical reason.

Avoid over-engineering.

---

# 6. GOOGLE MAPS

Use the official Google Maps SDK for Android.

Required behavior:

- map visible;
- pan/zoom works;
- user selects a point;
- marker visible;
- latitude/longitude update;
- coordinate precision preserved.

Use:

```kotlin
latitude: Double
longitude: Double
```

Do not use:

- scraped map tiles;
- unofficial map endpoints;
- billing bypasses;
- fake map images;
- unsupported workarounds.

Prefer native Android map snapshot capability such as `GoogleMap.snapshot()` for the mini-map when it is suitable and compliant.

Do not add Places SDK, Routes API, or Static Maps API unless a concrete requirement makes one necessary and the decision is explicitly documented.

Preserve required Google Maps attribution/branding.

### Development-only osmdroid fallback

The production provider remains Google Maps SDK for Android.

If `MAPS_API_KEY` / `GOOGLE_MAPS_API_KEY` is unavailable during
development, the agent MAY use osmdroid as a temporary development
fallback so independent map UI/runtime work can continue.

Mandatory rules:

- Do not delete Google Maps code, dependencies, methods, or
  configuration because the key is missing.
- Valid Google Maps configuration -> Google Maps.
- Missing Google Maps configuration -> osmdroid development fallback.
- Never initialize both providers simultaneously.
- Use the same provider-independent coordinate model:
  `latitude: Double`, `longitude: Double`.
- osmdroid is never the production provider.
- osmdroid must not be used as a Google Maps billing bypass.
- Do not scrape Google tiles or use unofficial Google map endpoints.
- Standard OpenStreetMap tiles may be used by osmdroid for development.
- A successful osmdroid runtime test does not count as Google Maps
  runtime validation.
- Final release validation must verify real Google Maps SDK behavior.
- Keep device location, map camera position, and selected coordinate
  separate. A one-time coarse location may center the initial camera
  only and must never set the selected point/marker.
- Request `ACCESS_COARSE_LOCATION` contextually. If declined or
  unavailable, keep the map usable at the deterministic default center.
  Never add background tracking or location history.
- Resolve addresses from `selectedCoordinate`, never device location or
  camera center.
- Interactive Google Maps may offer NORMAL, SATELLITE, TERRAIN, and
  HYBRID; mark unsupported development-fallback styles unavailable.
- The final generated mini-map must explicitly use SATELLITE,
  independent of interactive appearance. Do not add Static Maps API,
  scrape tiles, or bypass billing.
- Generate it only from `selectedCoordinate`, with the marker at the
  selected point and a separate deterministic camera at zoom `16.0`.
  Never follow interactive camera/style state or substitute osmdroid
  when Google Maps configuration is unavailable. Preserve the complete
  Maps snapshot and required attribution/branding.

If no Google Maps key exists, classify Google Maps runtime validation as
an external configuration blocker rather than deleting or replacing the
Google Maps implementation.

---

# 7. ADDRESS

Prefer Android `Geocoder` when sufficient.

For modern Android, avoid blocking/deprecated patterns where an asynchronous API is available.

If address resolution fails:

- keep the coordinate usable;
- show a clear fallback;
- never fabricate address data.
- resolve only from the user's selected coordinate.

Do not add paid Google geocoding merely because it is familiar.

---

# 8. DATE / TIME

The user's manually selected date and time are authoritative.

Do not silently replace them with:

- EXIF date/time;
- current device time;
- server time;
- GPS acquisition time;
- map selection time.

Default timezone:

```text
Asia/Jakarta
```

Manual date and time are independent, saveable values. Use device
date/time only to initialize pickers. A confirmed user selection is
authoritative; never override it with EXIF, file creation/modification
timestamps, GPS, server/network time, or map/address events. Picker
cancellation must preserve the previous value, and editing one field
must not change the other. Keep the selected wall-clock value in
Asia/Jakarta (WIB) without silent UTC conversion and display time in
24-hour `HH:mm` format. Future image generation must consume these
selected values unchanged.

---

# 9. QR

Generate the QR locally.

Required payload:

```text
https://maps.google.com/?q={latitude},{longitude}&t=h&z=18
```

Rules:

- Build the payload only from the current user-selected
  `selectedCoordinate`; do not use device location, camera center, address,
  photo metadata, or a parsed Maps page URL.
- The `q` parameter contains only `latitude,longitude`; keep `t=h` and
  `z=18` separate from the coordinate pair.
- Preserve the selected `Double` values without unnecessary rounding.
- If no coordinate is selected, show no QR. When the selection changes,
  regenerate the payload and QR and do not present stale output.
- Generate and render it locally; encoding must not require internet,
  backend, database, Place ID, or Places API.
- do not shorten the URL;
- do not redirect through this application;
- do not require a database;
- scanning must open Google Maps at the selected coordinate.

---

# 10. FINAL IMAGE

Produce a real JPG/PNG, not an Android UI screenshot.

It must contain:

1. selected photo;
2. mini-map;
3. selected marker;
4. address/location when available;
5. latitude;
6. longitude;
7. manually selected date;
8. manually selected time;
9. QR code;
10. required map attribution/branding;
11. approved visual styling.

Preferred pipeline:

```text
Photo URI
  ↓
Safe Decode / Resize
  ↓
Map Snapshot + Location + Coordinates + Date/Time + QR
  ↓
Bitmap + Canvas
  ↓
Final JPG/PNG
```

Large image processing must not block the UI thread.

---

# 11. REMOTE CONTROL

Use Firebase Remote Config only for lightweight operational control.

Parameters:

```text
app_enabled
maintenance_mode
maintenance_title
maintenance_message
minimum_supported_version
```

Behavior:

- `app_enabled=false` → disable core workflow;
- `maintenance_mode=true` → show maintenance screen;
- `minimum_supported_version` → require update for old versions;
- temporary network failure must not automatically lock the app;
- use the last successfully fetched configuration according to Remote Config behavior;
- Remote Config values are not secrets.

Do NOT add:

- Firebase Authentication;
- Firestore;
- Realtime Database;
- Firebase Storage;
- Cloud Functions;

for this requirement.

---

# 12. PRIVACY / SCOPE

Do not add:

- login;
- registration;
- profile;
- history;
- database;
- cloud photo storage;
- cloud image processing;
- custom photo backend;
- social features;
- subscriptions;
- advertisements;
- AI image generation;
- proprietary QR redirect service.

Process photos and generated images locally whenever practical.

Do not store user photo history.

---

# 13. SAVE / SHARE

Save using Android MediaStore or the appropriate modern Android media mechanism.

Share using the Android Sharesheet/share intent.

No upload server is required.

---

# 14. COST DISCIPLINE

Prefer the lowest-cost compliant architecture.

Do not add unnecessary services.

Avoid:

- Places;
- Routes;
- Static Maps if native snapshot is sufficient;
- using osmdroid as a production replacement for Google Maps;
- cloud image processing;
- cloud photo storage;
- custom backend;
- database.

Developer billing monitoring is NOT an Android feature.

Use Google's native Cloud Budget & Alerts / notification facilities where applicable.

Do not build a custom billing-monitor backend.

---

# 15. TASK WORKFLOW

Work ONE task at a time.

For every task:

```text
INSPECT
↓
PLAN
↓
IMPLEMENT
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

Never skip the self-audit.

Never mark a task `VERIFIED` without evidence.

---

# 16. SELF-AUDIT

After implementation, inspect for:

- dead code;
- unused imports;
- unused dependencies;
- unused state;
- unused functions/classes;
- duplicate code;
- unnecessary abstractions;
- TODO/FIXME leftovers;
- debug logs;
- `println`;
- hard-coded fake/test data;
- secrets;
- unnecessary permissions;
- main-thread blocking;
- bitmap memory pressure/leaks;
- lifecycle problems;
- Compose performance/recomposition issues;
- camera-related references;
- regressions;
- incorrect Google Maps/osmdroid provider selection;
- simultaneous initialization of both map providers;
- accidental promotion of osmdroid to production architecture;
- missing preservation of the Google Maps implementation when the key is absent.

Fix findings before verification whenever practical.

---

# 17. VALIDATION

Use the actual Gradle wrapper.

Typical Linux/macOS commands:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew bundleRelease
```

Windows:

```powershell
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
.\gradlew.bat bundleRelease
```

Do not claim a command passed unless it actually ran.

When an emulator/device is available, verify:

- app launch;
- no camera permission;
- Photo Picker;
- existing photo selection;
- photo preview;
- Google Maps when valid configuration exists;
- osmdroid development fallback when Google Maps configuration is unavailable;
- provider selection correctness;
- no simultaneous provider initialization;
- coarse location grant/denial and initial-camera-only behavior;
- map appearance availability and selected-coordinate preservation;
- coordinate selection;
- selected-coordinate address resolution, including loading, unavailable,
  not-found, error, timeout, and cancellation behavior;
- manual date/time;
- image generation;
- Save;
- Share;
- QR scanning;
- Remote Config states;
- obvious crashes/memory problems.

---

# 18. IMPLEMENTATION ORDER

Prefer:

```text
A01 Bootstrap Android Project
A02 Compose + Material 3 foundation
A03 Material 3 Adaptive foundation
A04 Photo Picker / existing-photo flow
A05 Photo preview + safe bitmap handling
A06 Google Maps SDK integration
A07 Coordinate selection/state
A08 Address resolution
A09 Manual date/time
A10 Local QR generation
A11 Google Maps mini-map snapshot
A12 Bitmap + Canvas compositor
A13 Final image preview
A14 MediaStore Save
A15 Android Sharesheet Share
A16 Firebase Remote Config foundation
A17 Maintenance mode
A18 Remote emergency disable
A19 Minimum supported version
A20 Error handling
A21 Accessibility
A22 Performance hardening
A23 R8 + release optimization
A24 Baseline Profile
A25 README + setup documentation
A26 Final integration testing
A27 Final dead-code/security audit
A28 Release/AAB verification
A29 Play Store readiness audit
```

Follow dependency constraints if they require a different order.

Do not create throwaway UI demos merely to make progress appear complete.

---

# 19. SESSION / TOKEN EXHAUSTION SAFETY

Assume this AI session can terminate unexpectedly.

The repository must remain recoverable.

Before ending a substantial task/session:

1. save valid code;
2. run available validation;
3. update `IMPLEMENTATION_STATUS.md`;
4. record current status;
5. record completed work;
6. record remaining work;
7. record files changed;
8. record validation results;
9. record self-audit results;
10. record blockers;
11. record the exact next action;
12. never mark incomplete work as `VERIFIED`.

The next AI session must be able to continue without previous chat memory.

The persistent state is:

```text
Source code
+
Git
+
IMPLEMENTATION_STATUS.md
+
project documentation
```

---

# 20. RESUME PROTOCOL FOR A NEW AI SESSION

If this is a continuation session, do this before coding:

```text
1. Read all project MD files.
2. Inspect Git status.
3. Inspect recent Git history.
4. Inspect current source code.
5. Read IMPLEMENTATION_STATUS.md.
6. Find the earliest task that is not VERIFIED.
7. Inspect what is already implemented for that task.
8. Determine remaining work.
9. Continue from the actual state.
10. Validate.
11. Self-audit.
12. Update IMPLEMENTATION_STATUS.md.
```

Do not say that you remember the previous session.

Do not assume work is complete because a previous agent claimed it.

Use repository evidence.

Do not redo verified work without evidence that it is broken.

---

# 21. GIT CHECKPOINTS

Prefer a Git commit after each meaningful verified task.

Recommended format:

```text
A01: bootstrap Android project
A02: add Compose and Material 3 foundation
A03: add adaptive layout foundation
...
```

For incomplete work, a checkpoint commit may be used when appropriate, but the task must remain `IN_PROGRESS`.

Git history supplements `IMPLEMENTATION_STATUS.md`; it does not replace it.

---

# 22. README

The coding agent must create/update `README.md` based on the actual implementation.

README must include:

- project purpose;
- requirements;
- Android/Gradle setup;
- build commands;
- Google Maps configuration;
- Firebase Remote Config configuration;
- API key restrictions;
- billing requirements;
- permissions;
- testing;
- release/AAB build;
- Play Store preparation;
- privacy;
- known limitations.

Never document camera support.

Never document services that are not actually implemented.

---

# 23. DEVELOPER COST DOCUMENT

Do NOT fill `DEVELOPER-COSTS-AND-BILLING.md` with guesses before the application is runnable.

Complete its actual audit only after:

- main implementation is substantially complete;
- app can run locally;
- final dependencies are known;
- final Google/Firebase services are known;
- initial validation has passed.

Then audit actual implementation and verify current pricing/quota information against official provider documentation.

Never invent prices.

Never include secrets.

---

# 24. FINAL REPORTING

At the end of every task, report:

```text
Task:
Status:

What changed:
- ...

Files changed:
- ...

Validation:
- command/result

Functional verification:
- ...

Self-audit:
- ...

Remaining:
- ...

Blockers:
- ...

Next action:
- ...
```

Then update `IMPLEMENTATION_STATUS.md`.

---

# 25. START NOW

After reading the project documentation and inspecting the repository:

1. identify the earliest incomplete task;
2. if the active task is blocked only by missing Google Maps configuration,
   use the approved osmdroid development fallback only where that task's
   validation can be meaningfully performed without changing production
   architecture;
3. never delete or replace the Google Maps production implementation;
4. state your plan;
5. implement only the appropriate task scope;
4. validate it;
5. self-audit it;
6. update `IMPLEMENTATION_STATUS.md`;
7. leave a recoverable checkpoint;
8. then proceed to the next task only if the current task is genuinely verified.

**Do not ask the user to repeat the project requirements that are already documented.**

The project documentation is the contract.
The repository is the persistent memory.
Evidence determines status.
