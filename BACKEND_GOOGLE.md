# Backend Google — Play Games Services v2

Le jeu reste **100 % hors-ligne** tant que l'APP_ID vaut `"0"`.
Tout le code Google est isolé dans `PlayBackend.kt` + `mergeCloud/toCloudJson`
dans `Progress.kt` : aucun crash si non configuré.

## Configuration locale automatique
Après avoir créé les ressources dans Play Console, appliquez leurs IDs et
validez la compilation avec :
```bash
tools/setup_google_backend.sh \
  --app-id 123456789012 \
  --leaderboard-xp Cg...EAQ \
  --leaderboard-streak Cg...EAQ \
  --achievements Cg...1,Cg...2,Cg...3,Cg...4,Cg...5,Cg...6,Cg...7,Cg...8,Cg...9,Cg...10,Cg...11,Cg...12
```
Le script crée `google-play.properties` (ignoré par Git), affiche le SHA-1
debug disponible et lance `assembleDebug`. Ajoutez `--no-build` pour ne faire
que la configuration. Les étapes Play Console restent manuelles, car elles
nécessitent l'accès au compte propriétaire du jeu.

Une fois le projet Play Games créé et son ID numérique connu, les deux
classements et les douze succès peuvent être créés automatiquement :
```bash
python3 -m venv .venv-play-games
.venv-play-games/bin/pip install google-auth google-api-python-client
.venv-play-games/bin/python tools/setup_play_games.py --app-id 123456789012
```
Le script réutilise les ressources portant déjà les mêmes noms, crée celles qui
manquent et écrit automatiquement tous leurs IDs dans `google-play.properties`.

## 1. Créer le jeu Play Console
1. https://play.google.com/console → Tous les jeux → Créer,
   package `fr.audeladedonnees.examen.civique`, nom « Parcours citoyen ».
2. Lier le projet : version → récupérer l'**ID d'application**
   (12+ chiffres, ex `123456789012`).
3. Ajouter votre SHA-1 debug :
   `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android`
4. Ajouter des testeurs (section Play Games → Testeurs) avec vos comptes Gmail.

## 2. Classements (2)
- « XP total » et « Meilleure série » → copier les IDs (ex `Cg...EAQ`).
- Les transmettre à `tools/setup_google_backend.sh` avec
  `--leaderboard-xp` et `--leaderboard-streak`.

## 3. Succès (12)
Créer un succès par badge puis passer les IDs Google générés à
`--achievements`, exactement dans cet ordre :
`first, correct10, correct50, correct250, xp150, level5, level10,
streak3, streak7, perfect, runs10, crown3`.
Le code associe cet ordre aux badges locaux ; les IDs Play Console commencent
généralement par `Cg` et ne peuvent pas être remplacés par les noms locaux.

## 4. Sauvegardes cloud
- Play Console → Play Games Services → Configuration → activer
  « Saved games » (instantanés).
- Rien d'autre : le snapshot s'appelle `quest-save` et contient
  le JSON de `ProgressStore.toCloudJson()` (xp, gems, streak,
  badges, best0..5…). La fusion garde le max (`mergeCloud`).

## 5. Renseigner l'APP_ID
Passez l'ID à `tools/setup_google_backend.sh --app-id 123456789012`.
Le script le place dans `google-play.properties`, puis compile l'application.
Réinstallez-la : la carte « Google Play Games » apparaît
dans le menu (connexion, classements, succès).

## 6. Test sur émulateur
- L'émulateur doit avoir les Play Services + un compte testeur connecté.
- Logcat : `adb logcat | grep -i -E "games|playbackend"`.
- Vérifier : connexion silencieuse au démarrage, bouton
  « Se connecter », envoi XP après une partie, succès débloqué,
  snapshot `quest-save` synchronisé entre 2 appareils.

## 7. Publication Google Play

1. Créez une fois l'application `fr.audeladedonnees.examen.civique` dans Play Console, acceptez
   les déclarations et envoyez manuellement le premier binaire.
2. Placez la clé du compte de service dans
   `.secrets/google-play-service-account.json`.
3. Ajoutez l'icône, la bannière et les captures selon
   `fastlane/metadata/IMAGES.md`.
4. Validez, puis envoyez un AAB signé sur la piste interne :
```bash
tools/publish_google_play.sh --aab app-release.aab --validate-only
tools/publish_google_play.sh --aab app-release.aab
```

Le statut par défaut est `draft`; aucune publication en production n'est faite
implicitement.

Le workflow peut aussi enregistrer les coordonnées, envoyer le CSV officiel
Data safety, vérifier la disponibilité mondiale et enchaîner Play Games :
```bash
tools/publish_google_play.sh \
  --aab app-release.aab \
  --contact-email admin@audeladedonnees.fr \
  --contact-website https://audeladedonnees.fr \
  --data-safety-csv play-data-safety.csv \
  --verify-all-countries \
  --play-games-app-id 123456789012
```
Politique de confidentialité :
`https://audeladedonnees.fr/legal/privacy` (champ dédié à renseigner dans Play
Console).

La sélection initiale de tous les pays, la politique de confidentialité, la
classification du contenu et les déclarations légales restent à compléter dans
Play Console : l'API Publishing standard ne permet pas de les écrire. Le script
ne fabrique jamais de réponses de conformité.
