# Play Console — dossier de conformité

Ce document sépare les constats techniques vérifiés des déclarations qui doivent
être confirmées par le propriétaire avant soumission.

## Constats vérifiés dans l'application

- Package : `fr.audeladedonnees.examen.civique`
- SDK cible : Android 35 ; SDK minimal : Android 24.
- Aucune permission caméra, microphone, localisation, contacts, SMS ou stockage.
- Aucune permission `com.google.android.gms.permission.AD_ID`.
- Aucun SDK publicitaire ou de facturation intégré.
- Google Play Games est facultatif ; le jeu principal fonctionne hors ligne.
- Stockage local : progression dans les SharedPreferences `quest`.
- Synchronisation facultative : snapshot Play Games `quest-save`, classements et succès.
- Aucun compte propre à l'application et aucun accès restreint requis pour jouer.
- Aucun service de santé ou financier.
- L'application est indépendante et n'est pas une application gouvernementale.

## Réponses proposées à confirmer dans Play Console

- Contient des publicités : **Non**.
- Utilise l'identifiant publicitaire : **Non**.
- Accès à l'application : **Toutes les fonctionnalités principales sont accessibles
  sans identifiant ; la connexion Play Games est facultative**.
- Fonctionnalités santé : **Aucune**.
- Fonctionnalités financières : **Aucune**.
- Application gouvernementale : **Non**.
- Catégorie : **Éducation** ; type : **Jeu**.
- Public cible proposé : **18 ans et plus**. Ce choix doit être confirmé par le
  propriétaire ; choisir des mineurs déclenche des obligations Families supplémentaires.
- Classification indicative : aucun contenu violent, sexuel, grossier, lié aux
  drogues ou aux jeux d'argent ; un dé sert uniquement au déplacement sur un plateau,
  sans mise ni achat.

## Informations encore obligatoires

- Adresse e-mail publique de support.
- URL publique de politique de confidentialité, également accessible depuis l'app
  si Google l'exige pour les données Play Games.
- CSV Data safety officiel exporté depuis Play Console, relu et approuvé par le
  propriétaire avant envoi avec `--data-safety-csv`.
- Confirmation du public cible et des réponses de classification du contenu.
- Sélection initiale des pays/régions dans Play Console.

Le script de publication peut envoyer le CSV Data safety et vérifier les pays,
mais il ne soumet pas automatiquement les attestations non confirmées.
