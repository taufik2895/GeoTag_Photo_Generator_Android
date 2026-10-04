# AGENTS.md — GeoTag Photo Generator Android

## Mission

You are an AI coding agent implementing **GeoTag Photo Generator**, a native Android app for Google Play Store.

Before changing code, read:

1. `GeoTag-Photo-Generator-Source-of-Truth.md`
2. `AGENTS.md`
3. `IMPLEMENTATION_STATUS.md`
4. `DEVELOPER-COSTS-AND-BILLING.md`

`README.md` is created/maintained by you based on the actual implementation.

---

## 1. Absolute Rules

### NO CAMERA

Never add camera support.

Forbidden:

- `android.permission.CAMERA`
- CameraX
- camera intents
- camera preview/capture
- camera UI
- "Take Photo"
- "Capture Photo"

Photo input means existing-photo selection only.

### NO UNAPPROVED SCOPE

Do not add:

- authentication,
- database,
- user history,
- cloud photo storage,
- cloud image processing,
- social features,
- unrelated APIs,
- unnecessary third-party UI frameworks.

### NO SECRETS

Never commit:

- API keys,
- Firebase service-account keys,
- signing keys,
- passwords,
- tokens.

### MAP PROVIDER POLICY

Google Maps SDK for Android is the production map provider.

osmdroid is permitted only as a temporary development fallback when
Google Maps configuration is unavailable.

Rules:

- Never remove Google Maps implementation because a key is missing.
- Never replace Google Maps production architecture with osmdroid.
- When a valid Google Maps key/configuration exists, use Google Maps.
- When the key/configuration is unavailable, osmdroid may be used only
  for development/runtime fallback testing.
- Never initialize Google Maps and osmdroid simultaneously.
- Both providers must consume the same `latitude: Double` and
  `longitude: Double` coordinate model.
- Keep `deviceLocation`, camera position, and user-selected coordinate
  separate. A one-time device location may center the initial map only;
  it must never select or move the GeoTag marker.
- Request only contextual `ACCESS_COARSE_LOCATION`; denial/unavailability
  must leave the map usable at the deterministic default
  `(-6.2088, 106.8456)`. No background tracking or location history.
- Interactive Google Maps appearance may be NORMAL, SATELLITE, TERRAIN,
  or HYBRID. The current osmdroid fallback supports only NORMAL; mark
  other styles unavailable instead of simulating them.
- The final generated mini-map must explicitly use SATELLITE regardless
  of interactive appearance. Do not use Static Maps API, scrape tiles,
  or bypass billing.
- Reverse geocoding must use `selectedCoordinate`, never device location
  or camera center.
- Keep manually selected date and time as separate saveable values.
  Device date/time may initialize the pickers only; confirmed user values
  are authoritative and must not be replaced by EXIF, file timestamps,
  GPS, server/network time, or map/address events.
- Canceling a date/time picker must preserve its previous value.
  Changing date must not change time and vice versa.
- Use Asia/Jakarta (WIB) as the project timezone without silently
  converting manually selected wall time; display time as 24-hour `HH:mm`.
- Future image generation must use the selected date and time unchanged.
- Never use osmdroid to bypass Google Maps billing or requirements.
- Never scrape Google map tiles or use unofficial Google map endpoints.
- A passing osmdroid test is not evidence that Google Maps runtime works.
- Final production/release validation must verify real Google Maps.

---

## 2. Required Technology

Use:

- Kotlin
- Jetpack Compose
- Material 3
- Material 3 Adaptive where appropriate
- Google Maps SDK for Android as the production map provider
- osmdroid only as a temporary development fallback when Google Maps configuration is unavailable
- Android Photo Picker
- Bitmap + Canvas
- local QR generation
- MediaStore
- Android Sharesheet
- simple MVVM/state-driven architecture
- Firebase Remote Config for operational remote control only

Optimize release builds with R8 and use Baseline Profiles where practical.

---

## 3. Remote Control

Implement Remote Config parameters:

```text
app_enabled
maintenance_mode
maintenance_title
maintenance_message
minimum_supported_version
```

Rules:

- `app_enabled=false` => block core workflow.
- `maintenance_mode=true` => show maintenance UI.
- `minimum_supported_version` => require update when current version is below it.
- Network failure alone must not automatically disable the app.
- Remote Config values are not secrets.
- Do not add Firebase Auth/Firestore/Storage/Functions for this requirement.

---

## 4. Task Workflow

Every task must use:

```text
1. Inspect
2. Plan
3. Implement
4. Build/test
5. Self-audit
6. Fix
7. Build/test again
8. Final self-audit
9. Update IMPLEMENTATION_STATUS.md
```

Never skip self-audit.

---

## 5. Definition of Done

A task is not done because code exists.

A task is `VERIFIED` only if:

- implementation exists,
- relevant build/tests pass,
- expected behavior was actually checked,
- self-audit was performed,
- dead code and obvious regressions were addressed,
- `IMPLEMENTATION_STATUS.md` is updated with evidence.

If verification cannot be performed, use `BLOCKED` or `FAILED`; do not pretend it is verified.

---

## 6. Self-Audit Checklist

After every task inspect:

- dead code,
- unused imports,
- unused dependencies,
- unused state,
- unused functions/classes,
- duplicate logic,
- unnecessary abstractions,
- TODO/FIXME leftovers,
- debug logs,
- `println`,
- hard-coded fake data,
- secrets,
- unnecessary permissions,
- main-thread blocking,
- bitmap memory leaks/pressure,
- lifecycle issues,
- Compose recomposition/performance issues,
- camera-related references,
- regressions from earlier tasks.

Use appropriate static analysis and tests.

---

## 7. Validation

Use the actual project's Gradle wrapper.

Typical commands:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew bundleRelease
```

On Windows, use:

```powershell
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
.\gradlew.bat bundleRelease
```

Do not claim a command passed unless it actually ran.

If an emulator/device is available, verify:

- app launch,
- no camera permission,
- Photo Picker,
- photo selection,
- map;
- if Google Maps is configured, real Google Maps rendering;
- if Google Maps is not configured, development-only osmdroid fallback;
- pan/zoom for the active provider;
- no simultaneous provider initialization;
- one-time coarse location grant/denial and deterministic fallback;
- style changes preserve the selected coordinate and marker;
- address resolution uses the selected coordinate and handles failures;
- coordinate selection,
- date/time,
- image generation,
- Save,
- Share,
- QR scan,
- Remote Config states,
- obvious crashes/memory problems.

---

## 8. Implementation Order

Prefer this order unless dependency constraints require otherwise:

1. Android project bootstrap
2. Compose + Material 3 foundation
3. Adaptive layout foundation
4. Photo Picker
5. Map screen
6. Coordinate state
7. Address resolution
8. Date/time
9. QR
10. Map snapshot
11. Bitmap compositor
12. Preview
13. Save
14. Share
15. Firebase Remote Config
16. minimum-version/maintenance/disable flows
17. performance hardening
18. accessibility
19. release/AAB configuration
20. final audit

Do not jump ahead merely to create UI demos.

---

## 9. Cost Discipline

Use the least expensive compliant Google architecture.

The development-only osmdroid fallback is NOT a billing workaround.
It must never be used to avoid, bypass, or misrepresent Google Maps
production requirements.

Avoid:

- Places SDK unless required,
- Routes API,
- Static Maps API if native snapshot is sufficient,
- cloud image processing,
- cloud photo storage,
- custom backend,
- database.

Billing monitoring belongs to Google Cloud's native Budget & Alerts system, not to the Android application.

---

## 10. Final Reporting

At the end of each task report:

- task ID,
- implementation summary,
- changed files,
- commands executed,
- PASS/FAIL/BLOCKED results,
- self-audit result,
- device/emulator result if available,
- Google/Firebase configuration status,
- known limitations,
- next task.

Never claim a verification that did not happen.

---

## 11. Session / Token Exhaustion Recovery

Assume that the current AI session may end at any time because of token/context limits, interruption, crash, timeout, or tool failure.

The repository must always remain recoverable without relying on previous chat memory.

### Checkpoint rule

Before ending a substantial task/session, the agent must:

1. save all valid code changes;
2. run the relevant validation available at that point;
3. inspect the actual source tree;
4. update `IMPLEMENTATION_STATUS.md`;
5. record the current task status accurately;
6. record what has been completed;
7. record what remains;
8. record the exact recommended next action;
9. record validation results;
10. record known blockers or limitations;
11. never mark incomplete work as `VERIFIED`.

### Repository is the memory

A future AI session must reconstruct state from:

- actual source code;
- Git status/history when available;
- `IMPLEMENTATION_STATUS.md`;
- `GeoTag-Photo-Generator-Source-of-Truth.md`;
- `AGENTS.md`;
- `DEVELOPER-COSTS-AND-BILLING.md`;
- `AI-CODING-AGENT-PROMPT.md`;
- `README.md` when it exists.

Do not assume that previous chat context is available.

### Resuming after interruption

When starting a new session:

1. inspect Git status;
2. inspect recent Git history when available;
3. read the project documentation;
4. read `IMPLEMENTATION_STATUS.md`;
5. identify the earliest task that is not `VERIFIED`;
6. inspect the actual implementation of that task;
7. determine what is already complete;
8. continue from the existing implementation;
9. do not redo verified work without evidence that it is broken;
10. follow the normal task workflow again.

### Git checkpoint guidance

Prefer a Git commit after each meaningful verified task.

Recommended commit format:

```text
A01: bootstrap Android project
A02: add Compose and Material 3 foundation
A03: add adaptive layout foundation
...
```

If a task is incomplete, a checkpoint commit may be used when appropriate, but the task must remain `IN_PROGRESS` in `IMPLEMENTATION_STATUS.md`.

Never use a Git commit message as a substitute for the status ledger.

### Do not fake continuity

Do not claim:

- "I remember the previous session";
- "the previous agent completed this";
- "this was already tested";

unless the repository contains evidence.

Use the source tree, tests, build output, Git history, and status ledger as evidence.

---

## 12. End-of-Session Report

Before a session ends, report:

- current task ID;
- current status;
- completed work;
- remaining work;
- files changed;
- validation performed;
- validation results;
- self-audit findings;
- blockers;
- exact next action.

Then update `IMPLEMENTATION_STATUS.md`.

---

## 13. Cost Document Timing

Do not populate `DEVELOPER-COSTS-AND-BILLING.md` with guessed implementation details.

It remains a template until:

- the Android implementation is substantially complete;
- the app can run locally;
- final dependencies/services can be inspected;
- initial validation passes.

Only then perform the actual cost/billing audit using current official provider documentation.
