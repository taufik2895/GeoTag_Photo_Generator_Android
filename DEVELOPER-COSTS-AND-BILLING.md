# DEVELOPER-COSTS-AND-BILLING.md — FINAL AUDIT TEMPLATE

**Project:** GeoTag Photo Generator  
**Target:** Native Android application / Google Play Store  
**Document status:** FINAL TEMPLATE — to be completed by the AI coding agent **after the Android project is implemented and can run locally**  
**Authority:** This document records the actual cost/billing configuration of the finished implementation. It is **not** a place to estimate costs before implementation.

---

## 1. Purpose

This document exists to provide a factual audit of the **services, APIs, SDKs, dependencies, and distribution requirements actually used by the finished Android project**.

The AI coding agent must complete this document only after:

1. the main Android implementation is substantially complete;
2. the application can build and run locally;
3. the actual Gradle dependencies and implementation are known;
4. the actual Google/Firebase services used by the app are known;
5. the initial integration validation has passed;
6. the agent has inspected the final source/configuration for unused or accidentally included services.

This document must be based on the **actual repository implementation**, not on assumptions from the Source of Truth.

---

## 2. Important Rule: Do Not Fill This Document Prematurely

Before the implementation is complete, keep this document as a template.

The agent MUST NOT:

- invent prices;
- assume an API is used merely because it appears in the Source of Truth;
- claim a Google/Firebase service is used when it is not present in the implementation;
- claim a dependency is required when it is unused;
- copy pricing information from old web-prototype documentation;
- estimate monthly cost from an imagined number of users;
- add a service solely because it may be useful later;
- treat a theoretical capability as an implemented dependency.

If a service was considered but ultimately not used, record it under **"Considered but Not Used"** rather than treating it as an active cost.

---

## 3. Approved Architecture Constraints

The following are product decisions already approved and must be preserved unless explicitly changed later.

### 3.1 Local-first processing

The application should process user photos and generated images locally whenever practical.

Do not add:

- custom backend;
- cloud photo upload;
- cloud image processing;
- cloud photo storage;
- user database;
- photo history database.

### 3.2 No camera

The application must not use:

- camera permission;
- CameraX;
- camera intents;
- camera preview;
- camera capture;
- camera hardware APIs.

Photo input is through Android Photo Picker / system picker.

### 3.3 Google Maps

Use the official Google Maps SDK for Android.

The project should avoid adding unnecessary Google Maps Platform APIs.

Do not add without an explicit approved requirement:

- Places SDK;
- Routes API;
- Static Maps API;
- other Google Maps Platform APIs.

For the mini-map, prefer the native Android Maps snapshot capability such as `GoogleMap.snapshot()` when it satisfies the requirement and complies with the applicable Google Maps terms/attribution requirements.

### 3.4 Address resolution

Prefer Android `Geocoder` when it is sufficient.

Do not add a paid Google geocoding service merely to replace a working platform-level solution.

If the final implementation uses a Google geocoding service instead, document the concrete reason and the actual API.

### 3.5 QR

QR generation is local.

Required payload:

```text
https://www.google.com/maps?q={latitude},{longitude}
```

No proprietary redirect service or database is required.

### 3.6 Image composition

Final GeoTag output is generated locally with Android Bitmap + Canvas or the final equivalent actually implemented.

Do not introduce cloud image processing.

### 3.7 Save / Share

Use Android MediaStore and Android Sharesheet/share intent as appropriate.

No custom upload backend is required.

### 3.8 Remote operational control

Firebase Remote Config is approved **only** for lightweight operational configuration:

```text
app_enabled
maintenance_mode
maintenance_title
maintenance_message
minimum_supported_version
```

Do not add Firebase Auth, Firestore, Realtime Database, Storage, or Functions for this requirement.

Remote Config is not a security boundary and must not contain secrets.

### 3.9 Developer billing monitoring

Developer billing monitoring is infrastructure/configuration, not an Android application feature.

Use Google's native **Budget & Alerts** / notification facilities where applicable.

Do not build a custom billing-monitor backend.

The Android application must not:

- access developer billing APIs;
- contain billing credentials;
- contain a service account;
- monitor developer billing;
- send developer billing alerts.

---

# 4. FINAL IMPLEMENTATION AUDIT

> **Agent: complete this section after the project runs locally.**

## 4.1 Actual Android / Build Stack

| Item | Actually used? | Version | Purpose | Billing impact | Evidence |
|---|---|---|---|---|---|
| Kotlin | `[ ]` | `[fill]` | `[fill]` | None | `[file/config]` |
| Jetpack Compose | `[ ]` | `[fill]` | `[fill]` | None | `[file/config]` |
| Material 3 | `[ ]` | `[fill]` | `[fill]` | None | `[file/config]` |
| Material 3 Adaptive | `[ ]` | `[fill]` | `[fill]` | None | `[file/config]` |
| Google Maps SDK for Android | `[ ]` | `[fill]` | `[fill]` | `[verify current terms/pricing]` | `[file/config]` |
| Firebase Remote Config | `[ ]` | `[fill]` | `[fill]` | `[verify current terms/pricing]` | `[file/config]` |
| QR library | `[ ]` | `[fill]` | `[fill]` | `[fill]` | `[Gradle dependency]` |
| Other dependency | `[ ]` | `[fill]` | `[fill]` | `[fill]` | `[Gradle dependency]` |

Remove rows that were not actually used and add rows for actual dependencies that materially affect the cost audit.

---

# 5. GOOGLE SERVICES ACTUALLY USED

Complete this from the final implementation.

| Service / API | Used? | Why used | Required configuration | Billing required? | Current free allowance / pricing | Cost risk | Official source checked |
|---|---|---|---|---|---|---|---|
| Google Maps SDK for Android | `[ ]` | `[fill]` | `[fill]` | `[verify]` | `[verify]` | `[fill]` | `[official URL/title]` |
| Geocoder | `[ ]` | `[fill]` | `[fill]` | `[verify]` | `[verify]` | `[fill]` | `[official source]` |
| Static Maps API | `[ ]` | `[fill]` | `[fill]` | `[verify]` | `[verify]` | `[fill]` | `[official source]` |
| Places SDK | `[ ]` | `[fill]` | `[fill]` | `[verify]` | `[verify]` | `[fill]` | `[official source]` |
| Routes API | `[ ]` | `[fill]` | `[fill]` | `[verify]` | `[verify]` | `[fill]` | `[official source]` |
| Other Google API | `[ ]` | `[fill]` | `[fill]` | `[verify]` | `[verify]` | `[fill]` | `[official source]` |

### Audit rule

Only list a service as **Used = Yes** when there is evidence in the final project/configuration that the application actually uses it.

---

# 6. FIREBASE AUDIT

## 6.1 Approved Firebase scope

The approved Firebase scope is:

**Firebase Remote Config only**, and only for remote operational configuration.

Expected parameters:

```text
app_enabled
maintenance_mode
maintenance_title
maintenance_message
minimum_supported_version
```

## 6.2 Actual Firebase services

| Firebase service | Actually used? | Purpose | Configuration evidence | Billing impact | Official source checked |
|---|---|---|---|---|---|
| Remote Config | `[ ]` | `[fill]` | `[fill]` | `[verify]` | `[official source]` |
| Authentication | `[ ]` | Must remain unused unless explicitly approved | `[fill]` | `[N/A if unused]` | `[official source if needed]` |
| Firestore | `[ ]` | Must remain unused unless explicitly approved | `[fill]` | `[N/A if unused]` | `[official source if needed]` |
| Realtime Database | `[ ]` | Must remain unused unless explicitly approved | `[fill]` | `[N/A if unused]` | `[official source if needed]` |
| Storage | `[ ]` | Must remain unused unless explicitly approved | `[fill]` | `[N/A if unused]` | `[official source if needed]` |
| Cloud Functions | `[ ]` | Must remain unused unless explicitly approved | `[fill]` | `[N/A if unused]` | `[official source if needed]` |

If an unapproved Firebase service is found in the project, do not silently accept it. Investigate why it exists, remove it if unnecessary, or document the explicit product decision that approved it.

---

# 7. GOOGLE CLOUD PROJECT / BILLING CONFIGURATION

> Complete this from the actual project configuration used for development/release.

Record:

- Google Cloud project ID: `[fill]`
- Project purpose: `[fill]`
- Billing account configured: `[yes/no]`
- APIs enabled: `[list actual APIs]`
- API key(s) used: `[describe identifiers without exposing secrets]`
- API key restrictions: `[fill]`
- Application/package restrictions: `[fill]`
- API restrictions: `[fill]`
- Debug/release key separation: `[fill]`
- Credentials stored outside source control: `[yes/no]`

### Security rule

Never place actual API key secrets, service-account JSON, private keys, signing keys, passwords, or tokens in this Markdown document or source control.

---

# 8. BILLING / COST CONTROL

After the implementation audit, document the actual controls.

## 8.1 Native Google budget controls

- Google Cloud Budget configured: `[yes/no/not yet]`
- Budget amount: `[fill if configured]`
- Alert thresholds: `[fill]`
- Notification recipients: `[describe without exposing private information]`
- Pub/Sub/custom billing automation: `[yes/no]`
- Custom billing-monitor application code: `[must be no unless explicitly approved]`

## 8.2 Important distinction

A Google Cloud **Budget** is a monitoring/alerting mechanism. It does not by itself guarantee that API usage will stop at the budget amount.

The final project documentation should clearly distinguish:

- billing visibility;
- budget alerts;
- actual API/service usage;
- quotas;
- hard usage restrictions, if any.

Do not claim that a budget alert is a hard spending cap unless the official provider documentation explicitly supports that behavior for the configured service.

---

# 9. GOOGLE PLAY CONSOLE / DISTRIBUTION

Complete this at the release-readiness stage.

Record:

- Google Play Console developer account: `[required / configured / not yet configured]`
- App package/application ID: `[fill]`
- Target Android SDK: `[fill]`
- Minimum supported Android version: `[fill]`
- Release format: `[AAB]`
- Signing configuration: `[fill without exposing private keys]`
- Play App Signing: `[fill]`
- Internal testing configured: `[fill]`
- Closed/open testing status: `[fill if applicable]`
- Production release status: `[fill]`

### Cost audit rule

Do not invent or hard-code a Play Console fee in this document.

If a monetary fee or policy requirement is relevant, verify the current official Google Play documentation at the time of the final audit and record:

- what the fee/policy is;
- what date it was verified;
- the official source.

---

# 10. ACTUAL DEPENDENCY AUDIT

The agent must inspect the final Gradle configuration and identify:

### Keep

Dependencies that are:

- actually imported;
- actually used;
- required by the implementation;
- justified by the approved architecture.

### Remove

Dependencies that are:

- unused;
- duplicate;
- transitively unnecessary where practical;
- added only for abandoned experiments;
- related to prohibited camera functionality;
- related to unapproved backend/database/cloud functionality.

Record findings:

| Dependency | Used? | Why retained/removed | Version | Notes |
|---|---|---|---|---|
| `[fill]` | `[yes/no]` | `[fill]` | `[fill]` | `[fill]` |

---

# 11. CONSIDERED BUT NOT USED

Record services that were deliberately evaluated but were **not** included in the final implementation.

Examples:

- Places SDK — not needed if coordinate selection/address requirements are satisfied without it.
- Routes API — not required.
- Static Maps API — not required if native map snapshot is sufficient.
- Cloud image processing — not required.
- Cloud photo storage — not required.
- Custom backend — not required.
- Database — not required.
- Firebase Auth — not required.
- Firestore — not required.
- Firebase Storage — not required.
- Cloud Functions — not required.

The final agent should replace this list with the actual findings.

---

# 12. ACTUAL COST RISK SUMMARY

After the final audit, summarize only the real cost-producing or potentially cost-producing components.

Use this structure:

| Component | Actual use | Cost mechanism | Free allowance/limit | What can increase cost | Control |
|---|---|---|---|---|---|
| `[fill]` | `[fill]` | `[fill]` | `[verify]` | `[fill]` | `[fill]` |

Do not create a hypothetical monthly bill unless the user explicitly requests a forecast.

If a forecast is requested later, base it on:

- verified current provider pricing;
- actual services used;
- explicit usage assumptions.

---

# 13. OFFICIAL SOURCES TO VERIFY AT FINAL AUDIT

The coding agent should prefer current official provider documentation.

At minimum, check the official documentation relevant to the services actually used.

Potential sources include:

- Google Maps Platform pricing and billing documentation
- Google Maps SDK for Android usage/billing documentation
- Google Maps Platform cost-management documentation
- GoogleMap Android SDK reference, especially snapshot behavior where applicable
- Android Geocoder documentation
- Android Photo Picker documentation
- Firebase Remote Config documentation and pricing documentation
- Google Play Android Developer / Play Console documentation

Do not rely on third-party pricing articles when an official provider source is available.

Record the verification date for every pricing-sensitive item.

---

# 14. FINAL COST AUDIT CHECKLIST

The AI coding agent must check all of the following before declaring this document complete.

## Implementation

- [ ] Android project builds locally.
- [ ] App launches locally.
- [ ] Final Gradle dependencies inspected.
- [ ] Final manifest inspected.
- [ ] Actual Google services identified.
- [ ] Actual Firebase services identified.
- [ ] Unused dependencies removed where appropriate.
- [ ] No accidental backend/database/storage service exists.
- [ ] No camera dependency exists.
- [ ] No camera permission exists.

## Google Maps

- [ ] Actual Maps SDK usage confirmed.
- [ ] Actual enabled APIs confirmed.
- [ ] API key restrictions reviewed.
- [ ] Mini-map implementation confirmed.
- [ ] Static Maps API is not enabled/used unless actually required.
- [ ] Places is not enabled/used unless actually required.
- [ ] Routes is not enabled/used unless actually required.

## Firebase

- [ ] Remote Config usage confirmed.
- [ ] Only approved Remote Config functionality is used.
- [ ] No Firebase Auth unless explicitly approved.
- [ ] No Firestore unless explicitly approved.
- [ ] No Firebase Storage unless explicitly approved.
- [ ] No Realtime Database unless explicitly approved.
- [ ] No Cloud Functions unless explicitly approved.
- [ ] No secrets stored in Remote Config.

## Billing

- [ ] Billing requirements verified from official sources.
- [ ] Free allowances/quotas verified from official sources.
- [ ] Pricing-sensitive information has a verification date.
- [ ] Google Cloud Budget & Alerts status recorded.
- [ ] No custom billing-monitor system was added.
- [ ] No billing credentials exist in the APK/repository.

## Play Store

- [ ] Play Console requirements checked against current official documentation.
- [ ] Release/AAB requirements recorded.
- [ ] Signing information documented without secrets.
- [ ] Any current monetary fees are verified rather than assumed.

## Document integrity

- [ ] No invented prices.
- [ ] No invented quotas.
- [ ] No service claimed as used without implementation evidence.
- [ ] No old web-prototype cost assumptions copied into this audit.
- [ ] No secrets included.
- [ ] Actual implementation and documentation agree.
- [ ] README agrees with the final implementation.
- [ ] `IMPLEMENTATION_STATUS.md` records the final validation state.

---

# 15. FINAL AUDIT RESULT

Complete only after the application can run locally.

**Audit date:** `[YYYY-MM-DD]`  
**Audited commit/tag:** `[fill]`  
**Android build result:** `[PASS/FAIL]`  
**Local runtime result:** `[PASS/FAIL/BLOCKED]`  
**Release/AAB result:** `[PASS/FAIL/BLOCKED]`  
**Google Maps configuration result:** `[PASS/FAIL/BLOCKED]`  
**Firebase Remote Config result:** `[PASS/FAIL/BLOCKED]`  
**Billing configuration result:** `[fill]`  
**Play Console readiness result:** `[fill]`

### Actual cost-producing services

`[List only services confirmed by the final implementation and current provider documentation.]`

### Unused services removed

`[List actual unused services/dependencies removed.]`

### Remaining blockers

`[List actual blockers, or "None".]`

### Final conclusion

`[Write a factual summary of the actual cost/billing situation based on the completed implementation. Do not provide invented estimates.]`

---

# 16. UPDATE POLICY

This document is intentionally **not** updated after every coding task.

Update it when:

1. the main implementation is complete and runnable locally;
2. the final dependency/service audit is performed;
3. a Google/Firebase service is added or removed;
4. a release configuration materially changes the billing/distribution situation;
5. a final release audit is performed.

If dependencies or services change after the initial audit, update this document again before release.

---

# 17. Relationship to Other Project Documents

### `GeoTag-Photo-Generator-Source-of-Truth.md`

Defines the approved product and architecture.

### `AGENTS.md`

Defines coding-agent rules, workflow, validation, and self-audit requirements.

### `IMPLEMENTATION_STATUS.md`

Records implementation progress and evidence task by task.

### `README.md`

Must describe the actual finished project and setup instructions.

### `DEVELOPER-COSTS-AND-BILLING.md`

This document records the **actual final cost/billing audit** after implementation. It must not be used as a substitute for the Source of Truth and must not be populated with assumptions before the project is runnable.

---

## FINAL RULE

**Build first. Audit the real implementation second. Record verified cost/billing facts third.**

Never let this document drive the architecture by assumption.

The final implementation remains governed by the approved Source of Truth.
