# Google Play Games connection corrections — 2026-10-06

The APK previously lacked INTERNET permission and declared APP_ID as a literal
numeric manifest attribute. The app now requests INTERNET and ACCESS_NETWORK_STATE,
and uses a generated @string/play_games_app_id resource matching Google’s documented
metadata format. Local Play Games IDs are present (their values are not copied here).

Authentication now times out after 30 seconds, resets stale connection state on
failure, gives actionable status messages, and retains a successful authentication
if the optional player display-name lookup fails. A cloud sync failure does not
prevent gameplay or leave the connection button spinning. Cloud snapshot reads
close their handles and merge progress on the main thread.

The APK is a debug build. Its public signing certificate SHA-1 is:
45:AB:39:CD:C8:D4:BC:A2:E3:43:FD:37:E8:6A:39:7E:22:E1:3A:60
This signature and package fr.audeladedonnees.examen.civique must be authorized in
the linked Android OAuth client in Play Games configuration. Release builds served
by Google Play use the Play App Signing certificate and require their own matching
client. A published app does not imply published Play Games settings. Test accounts
must be explicitly allowed when the game configuration is unpublished.

No successful authentication with the user’s Google account has been verified.
The local changes fix identified client defects; OAuth registration, tester access
and publication state still require verification in Play Console if login fails.

Official reference:
https://developer.android.com/games/pgs/android/troubleshooting

# Review invitation

The app cannot query whether a user has already rated it. It uses a neutral,
optional popup with a 25% chance per eligible menu visit, after three runs and twenty
answers, at most once per thirty days. Completed direct quizzes count as runs too.
No prompts interrupt a question. “Already reviewed” and “Never ask again” permanently
suppress prompts on this install. Opening the Store also suppresses future requests,
without recording a submitted review. There are no rewards or positive-rating filters.
The Store link falls back to a browser if the Play Store is unavailable.

Reference for the platform limitation:
https://developer.android.com/guide/playcore/in-app-review/kotlin-java

Validation completed: assembleDebug, testDebugUnitTest (4 tests, zero failures),
and lintDebug pass. APK inspection confirms both network permissions and the
launcher activity. Emulator smoke checks confirm the featured civic card and
its direct 20-question revision shortcut. The local download endpoint returns
HTTP 200 with the rebuilt APK. End-to-end Google-account authentication remains
unverified, as noted above.
