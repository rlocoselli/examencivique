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

SDK Android 36, Java 17, Gradle installé : `gradle assembleDebug`.

APK : `app/build/outputs/apk/debug/app-debug.apk`.

## Publication Android

À chaque push sur la branche `main`, le workflow GitHub
`Build and publish production` construit un AAB signé avec un nouveau code de
version et l'envoie avec le statut `completed` sur la piste Google Play
`production`. Il peut aussi être lancé depuis `workflow_dispatch` après saisie
de `production`.

Tant que l'application est encore en brouillon global, le workflow place
automatiquement la première release corrigée en brouillon Production, prête à
être envoyée en examen depuis Play Console. Google n'autorise pas l'API à
effectuer cette première soumission. Après son acceptation, les pushes suivants
sont publiés automatiquement avec le statut `completed`.

La clé de release n'est jamais versionnée. Son certificat possède le SHA-1 :
`80:01:AD:B8:C5:4F:2A:98:DD:DD:FF:18:91:88:AF:81:E6:2B:B4:0A`.

Secrets GitHub configurés : keystore encodé, mots de passe et alias de
signature, compte de service Google Play, e-mail public de support
`admin@audeladedonnees.fr`, site de support et politique de confidentialité
`https://audeladedonnees.fr/legal/privacy`. Les secrets Play Games
(`PLAY_GAMES_APP_ID`, `LEADERBOARD_XP`, `LEADERBOARD_STREAK`,
`ACHIEVEMENTS_CSV`) deviennent nécessaires pour activer le backend en release.
L'ID numérique est attribué par Play Console lors de la création du projet Play
Games Services et ne peut pas être généré localement.

## Vérification manuelle

Tester une bonne réponse, confirmer l’avancée, vérifier le déplacement puis le retour du dé. Refaire avec une mauvaise réponse pour le recul. Vérifier le bouton Android Retour, chaque catégorie, le passage de case 20/40, l’arrivée avec moins de 15 réponses et le bilan en fin de banque.

Côté gamification : vérifier que l’XP, les jetons et les cœurs augmentent ou diminuent après chaque réponse, que le compteur de série passe à 1 au premier jour de jeu, que les quêtes du jour se valident et se paient, qu’un passage de niveau ouvre sa fenêtre et que l’épuisement des cœurs propose la recharge à 50 💎. Dans le menu, contrôler l’anneau d’objectif, les couronnes par mode et les onglets Succès / Quêtes / Ligue de « Ma progression ». Pour rejouer une journée différente sans attendre, effacer les données de l’application ou modifier la date du téléphone.
# Site web Flask

Le site compagnon se trouve dans `web/`. Pour le lancer localement :

```bash
python3 -m pip install -r web/requirements.txt
python3 web/app.py
```

Pour AUDELA Deploy, le [Dockerfile](Dockerfile) à la racine construit uniquement
le site Flask et le sert avec Gunicorn sur la porte interne `8000`. Dans
AUDELA Deploy, créer une application à partir de ce dépôt GitHub, choisir le
runtime Dockerfile et conserver la porte `8000`. Aucun volume ni variable
d'environnement n'est nécessaire pour les données incluses. La configuration
`PORT` est prise en charge si le service l'impose. La route `/` sert aussi de
sonde de santé. Pour actualiser les données parlementaires et géographiques
avant un nouveau déploiement :

```bash
python3 -m web.update_open_data
```

Ouvrir ensuite <http://127.0.0.1:5000/>. Le site reprend la palette et les
questions de l'application mobile. Il contient les catalogues des questions de
connaissance publiées pour la carte de séjour pluriannuelle (191), la carte de
résident (209) et la naturalisation (244), des QCM pédagogiques, un test CSP de
40 questions, des exercices de grammaire et de lecture, ainsi que les liens
vers les deux annuaires de centres agréés.

Les intitulés officiels sont dans `web/data/*_official.json`. Les réponses et
choix proposés sont rédigés pour l'entraînement, et seules les questions avec
correction vérifiée sont incluses dans les QCM (121 CSP, 20 résident, 36
naturalisation). Les questions de mise en situation et les choix officiels ne
sont pas publics. Le site n'émet aucune attestation de réussite.

Après avoir modifié les banques de l'application ou les listes officielles,
mettre les exports à jour avec :

```bash
python3 web/build_data.py
python3 web/generate_mobile_banks.py
```

Sources vérifiées le 4 novembre 2026 : [CSP](https://formation-civique.interieur.gouv.fr/examen-civique/liste-officielle-des-questions-de-connaissance-csp/),
[carte de résident](https://formation-civique.interieur.gouv.fr/examen-civique/liste-officielle-des-questions-de-connaissance-cr/),
[naturalisation](https://www.immigration.interieur.gouv.fr/documentation/guides-textes-et-brochures/questions-de-connaissance-pour-lexamen-civique-nationalite-francaise.html),
[centres FEI](https://www.france-education-international.fr/centres-d-examen/carte?type-centre=examen_civique)
et [centres CCI](https://www.lefrancaisdesaffaires.fr/candidat/trouver-un-centre-agree/).

La carte de l'accueil est dessinée à partir du contour de la France de
[Natural Earth](https://www.naturalearthdata.com/), domaine public (échelle
1:50m). Les photos de Paris, du Mont-Saint-Michel et du croissant sont les
visuels déjà présents dans l'application ; leurs crédits figurent dans
`docs/SALON_FRANCAIS.md`. Les animations respectent le réglage système de
réduction des mouvements.

La recherche de centres sur `/centres` utilise une copie locale d'un
[recensement publié sur data.gouv.fr](https://www.data.gouv.fr/datasets/centres-dexamen-civique-agrees-en-france-recensement-au-10-octobre-2026)
(Licence Ouverte 2.0), relevé le 10 octobre 2026 à partir des annuaires CCI
et FEI. Elle cherche par ville, code postal, département, adresse ou nom de
centre et filtre par réseau, sans quitter la page. Les fiches renvoient aux
annuaires officiels pour vérifier l'agrément, les sessions et les coordonnées.
Pour actualiser la copie locale après téléchargement du CSV :

```bash
python3 web/import_centers.py /chemin/vers/centres.csv
python3 -m unittest discover -s web -p 'test_*.py'
```

Les pages `/sujets/politique`, `/sujets/geographie` et `/sujets/histoire`
ajoutent des fiches de révision et des QCM tirés des banques existantes. La
carte de géographie permet de sélectionner dix villes au clic ou au clavier
et propose un défi de repérage. La frise d'histoire se filtre par thème.
Les chiffres du Parlement sont vérifiés auprès de
[l'Assemblée nationale](https://www2.assemblee-nationale.fr/decouvrir-l-assemblee/juniors)
et du [Sénat](https://www.senat.fr/connaitre-le-senat/role-et-fonctionnement/mode-delection-des-senateurs.html).
