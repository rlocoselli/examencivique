# Automatic Google Play versioning

`.github/workflows/publish-production.yml` publishes to the production track on
pushes to main, or on manual runs confirmed with `production`. Production
environment protections continue to apply. The existing first-release draft
fallback is preserved.

Before building the signed AAB, `tools/next_play_version.py` queries the bundles,
APKs and releases in all Play tracks, then selects the greatest versionCode + 1.
It preserves the historical code floor 1790408057. The visible version follows
the existing sequence `1.0.N`, where N = versionCode - 1790408057.
Both BUILD_NUMBER and VERSION_NAME are exported through GITHUB_ENV for Gradle.

Queries use a temporary edit that is discarded without committing. API failures
abort the workflow rather than guessing a potentially duplicate version code.
Existing production concurrency serializes this workflow's publishing jobs.
Re-running after an uploaded version picks the next code. External/manual
uploads made during a build can still require re-running the job.

The workflow runs the five versioning unit tests before building. Local validation
also queried Google Play: on 2026-10-06 production reported status `completed`,
versionCode 1790408058; the next selected version was 1790408059 / 1.0.2.
No new release was published during this verification.

API references:
https://developers.google.com/android-publisher/api-ref/rest/v3/edits.bundles/list
https://developers.google.com/android-publisher/api-ref/rest/v3/edits.tracks/list
