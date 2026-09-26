# Images Google Play

Ajoutez avant publication les fichiers suivants dans
`fastlane/metadata/android/fr-FR/images/` :

- `icon.png` : icône Play Store, PNG 512 × 512 ;
- `featureGraphic.png` : bannière, PNG ou JPEG 1024 × 500 ;
- `phoneScreenshots/01.png`, `02.png`, etc. : au moins deux captures d'écran téléphone.

Le script refuse l'envoi si ces trois catégories d'images sont absentes. Les
fichiers présents sont envoyés automatiquement lors de chaque publication.
Ne placez pas ici de fichiers contenant des secrets.
