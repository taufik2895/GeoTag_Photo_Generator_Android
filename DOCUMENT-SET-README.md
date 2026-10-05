# GeoTag Photo Generator — Final Android Source-of-Truth Document Set

These five synchronized Markdown files are intended to be copied together
into the Android project.

## Authoritative map-provider decision

- **Production:** Google Maps SDK for Android.
- **Development fallback:** osmdroid + standard OpenStreetMap tiles only
  when Google Maps configuration is unavailable.
- Missing Google Maps configuration must never cause Google Maps code,
  dependencies, or architecture to be deleted.
- Google Maps and osmdroid must never be initialized simultaneously.
- Both providers use the same provider-independent `Double` coordinate model.
- osmdroid is never the production provider and is never a billing bypass.
- Final release validation must verify real Google Maps SDK behavior.

## Files

1. `GeoTag-Photo-Generator-Source-of-Truth.md`
2. `AGENTS.md`
3. `IMPLEMENTATION_STATUS.md`
4. `DEVELOPER-COSTS-AND-BILLING.md`
5. `AI-CODING-AGENT-PROMPT.md`

`DOCUMENT-SET-README.md` is only a handoff index; it is not a sixth
source-of-truth document and does not replace the five files above.
