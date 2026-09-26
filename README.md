# Parcours Citoyen

Jeu Android Kotlin / Jetpack Compose. L’entrée active est `QuestApp.kt`.

## Jouer

- 159 questions d’entraînement originales ou reprises de la première version du jeu.
- Six parcours : toutes les questions, République, histoire/géographie, institutions/droits, vie quotidienne et quiz de l’hymne.
- Rubrique La Marseillaise : histoire, paroles sur le site officiel et piano hors ligne (92 secondes en boucle).
- Musique douce à 12 %, interrupteur dans le menu et dans le jeu ; volume réglable et mémorisé. Pause automatique hors de l’application.
- Questions et réponses mélangées à chaque partie, sans répétition d’une question pendant la partie.
- Trois cartes stylisées de 20 cases. Le dé anime ses faces, sa rotation et son rebond.
- Une réponse correcte rapporte 10 points et permet d’avancer du résultat du dé ; une destination multiple de 7 donne deux cases bonus.
- Une erreur fait reculer de deux cases, sans descendre sous zéro. Un popup permet de reprendre dans les deux cas.
- Le bilan intervient à l’arrivée après au moins 15 réponses (ou le nombre disponible pour un petit thème), ou à l’épuisement de toutes les questions du thème. Réussite du quiz à partir de 80 %.
- Les étapes découvertes et meilleurs pourcentages sont conservés localement. Quitter une partie revient au menu ; une nouvelle partie recommence le parcours.

Les cartes sont des décors de jeu, pas des cartes géographiques de navigation. Le résultat ne vaut pas attestation officielle. Les liens vers les centres ouvrent le navigateur.

## Gamification

Une couche de jeu inspirée de Duolingo, entièrement hors ligne dans `Progress.kt` (règles et sauvegarde) et `Gamification.kt` (interface).

- **XP et niveaux** : une bonne réponse rapporte 10 XP, multipliés par 2 dès 3 bonnes réponses d’affilée puis par 3 dès 5 (+5 XP sur une case bonus). Les niveaux demandent 25, 40, 55, 70… XP. Un passage de niveau affiche son écran de célébration.
- **Cœurs** : 5 cœurs, −1 par erreur, +1 toutes les 30 minutes. À zéro cœur la partie s’arrête : recharge des 5 cœurs pour 50 💎 ou fin de partie.
- **Jetons 💎** : +1 par bonne réponse, +10 pour une partie sans faute, +5 par quête, +20 pour le coffre du jour, +10 par succès. Ils servent à recharger les cœurs (50) et à acheter un gel de série (30).
- **Série 🔥** : nombre de jours consécutifs joués, avec record. Un gel de série (2 maximum) sauve automatiquement une journée manquée.
- **Objectif du jour** : anneau de progression sur le menu, palier au choix (10, 20, 30 ou 50 XP) et compteur de journées réussies.
- **Quêtes quotidiennes** : trois missions renouvelées chaque jour à minuit, récompensées automatiquement, plus un coffre de 20 💎 quand les trois sont terminées.
- **Succès** : douze badges (premier pas, séries, niveaux, sans faute, couronnes…) visibles dans l’onglet Succès.
- **Ligue de la semaine** : Bronze, Argent, Or, Diamant puis Rubis. Le classement compte huit participants : le joueur et sept rivaux tirés au sort en début de semaine. Les trois premiers montent d’une ligue, les deux derniers descendent.
- **Couronnes** : jusqu’à trois 👑 par mode selon le meilleur pourcentage et le nombre de parties terminées.
- Écran « Ma progression » (Succès / Quêtes / Ligue) accessible depuis le menu ; la flamme, les jetons et les cœurs restent affichés en haut du menu.

Tout est enregistré dans les préférences locales « quest » : aucune donnée ne quitte l’appareil.

## Construction

SDK Android 35, Java 17, Gradle installé : `gradle assembleDebug`.

APK : `app/build/outputs/apk/debug/app-debug.apk`.

## Publication Android

Le workflow GitHub `Build and publish production` construit un AAB signé et
l'envoie sur la piste Google Play `production` lors d'un tag `v*`, ou depuis
`workflow_dispatch` après saisie de `production`.

La clé de release n'est jamais versionnée. Son certificat possède le SHA-1 :
`80:01:AD:B8:C5:4F:2A:98:DD:DD:FF:18:91:88:AF:81:E6:2B:B4:0A`.

Secrets GitHub configurés : keystore encodé, mots de passe et alias de
signature, compte de service Google Play. Le secret public
`PLAY_CONTACT_EMAIL` reste obligatoire avant le premier lancement. Les secrets
Play Games (`PLAY_GAMES_APP_ID`, `LEADERBOARD_XP`, `LEADERBOARD_STREAK`,
`ACHIEVEMENTS_CSV`) deviennent nécessaires pour activer le backend en release.

## Vérification manuelle

Tester une bonne réponse, confirmer l’avancée, vérifier le déplacement puis le retour du dé. Refaire avec une mauvaise réponse pour le recul. Vérifier le bouton Android Retour, chaque catégorie, le passage de case 20/40, l’arrivée avec moins de 15 réponses et le bilan en fin de banque.

Côté gamification : vérifier que l’XP, les jetons et les cœurs augmentent ou diminuent après chaque réponse, que le compteur de série passe à 1 au premier jour de jeu, que les quêtes du jour se valident et se paient, qu’un passage de niveau ouvre sa fenêtre et que l’épuisement des cœurs propose la recharge à 50 💎. Dans le menu, contrôler l’anneau d’objectif, les couronnes par mode et les onglets Succès / Quêtes / Ligue de « Ma progression ». Pour rejouer une journée différente sans attendre, effacer les données de l’application ou modifier la date du téléphone.
