# Le Salon français

The original Parcours citoyen launch screen, dark palette, navigation and board game
have been restored at the user’s request. New question groups are integrated into
the original menu and board game; civic revision, timed tests and photo quizzes
use the original dark visual theme.

- 20 grammar, 20 literature, 20 regional geography and 20 gastronomy questions.
- Six visual questions using three bundled photographs; no network needed to play.
- 121 exact knowledge prompts selected from the Ministry of the Interior's CSP list.
- Four independently authored answer options per question, shuffled per session.
- Civic revision: 20 randomly selected questions with immediate feedback.
- Civic knowledge test: 40 questions, 45 minutes, 32/40 target; corrections at the end.
  This is a knowledge-only practice test, not a complete official mock exam.
- Quiz position, selected answer, score, random seed, errors and deadline survive
  activity recreation. A deadline continues to elapse while the app is backgrounded.
- Existing board game, progress, piano and Google Play features remain accessible.

Official source checked 2026-10-06:
https://formation-civique.interieur.gouv.fr/examen-civique/liste-officielle-des-questions-de-connaissance-csp/
The ministry publishes knowledge prompts, not the actual answer choices or confidential
situational questions. The app explicitly distinguishes independently authored corrections.

## Photo credits

Bundled photographs downloaded from Unsplash, under the Unsplash license
(https://unsplash.com/license); these are photographs, not generated images.

- Paris: Chris Karidis, https://unsplash.com/photos/nnzkZNYWHaU
  Image: https://images.unsplash.com/photo-1502602898657-3e91760cbb34
- Mont-Saint-Michel: Okamatsu Fujikawa, https://unsplash.com/photos/rmTNrIcsmNs
  Image: https://images.unsplash.com/photo-1451055959797-14c0cfba1a43
- Croissant: https://images.unsplash.com/photo-1619540158579-1b4fd3529849

## Verification

Build with `./gradlew :app:assembleDebug :app:lintDebug`.
Data validation checks exact CSP prompt membership, unique questions and four distinct
answer options. Existing files in docs/ and fastlane/play-games/ predate this work.

Verified: debug APK compilation and Android lint both pass (2026-10-06).
Emulator smoke checks: home rendering, grammar selection and explanatory feedback,
back navigation, dedicated CSP menu and 40-question countdown (44:58 observed).
Corrected an existing LocalContext-to-Activity cast in the piano wrapper using LocalActivity.

The new board modes also record crowns and export best scores to cloud saves.
