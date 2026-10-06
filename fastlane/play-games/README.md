# Play Games Services — Guide d'intégration des ressources graphiques

Tous les visuels requis par Google Play Games Services ont été générés au format exact exigé par Google (PNG 512×512 et 1024×500).

> **Note Google API** : La spécification officielle de l'API Google Play Games (`gamesConfiguration v1configuration`) indique explicitement :  
> `"iconUrl": "The icon url of this achievement. Writes to this field are ignored."`  
> L'API REST ne permet donc pas l'envoi d'images de succès ni les visuels de la fiche Play Games via un compte de service. Ces téléversements doivent se faire via l'interface web de la Google Play Console.

---

## 1. Propriétés du jeu (Fiche Play Games)

Dans la Play Console, allez dans **Play Games Services** > **Configuration** (ou **Configuration et gestion** > **Configuration**) :

1. **Description** :
   Copiez le texte contenu dans `fastlane/play-games/description-fr.txt` :
   > Parcours citoyen — révisez l'examen civique français en jouant : République, histoire-géographie, institutions et droits, vie quotidienne, Marseillaise. Gagnez de l'XP, débloquez 12 succès et grimpez dans les classements !
2. **Icône** :
   Téléversez le fichier : `fastlane/play-games/game-icon-512.png` (512 × 512).
3. **Graphique de fonctionnalité** :
   Téléversez le fichier : `fastlane/play-games/game-feature-graphic-1024x500.png` (1024 × 500).
4. Cliquez sur **Enregistrer**.

---

## 2. Icônes des 12 succès (Achievements)

Dans la Play Console, allez dans **Play Games Services** > **Succès** (Achievements).  
Pour chaque succès de la liste, cliquez sur les trois petits points ou sur le nom pour le modifier, puis chargez l'icône PNG 512×512 correspondante depuis le dossier `fastlane/play-games/achievements/` :

| Nom du succès dans Play Console | Fichier image à téléverser | Badge / Intitulé |
| :--- | :--- | :--- |
| **Premier pas** | `fastlane/play-games/achievements/first.png` | 1 |
| **Dix bonnes réponses** | `fastlane/play-games/achievements/correct10.png` | 10 |
| **Cinquante justes** | `fastlane/play-games/achievements/correct50.png` | 50 |
| **Deux cent cinquante justes** | `fastlane/play-games/achievements/correct250.png` | 250 |
| **Cent cinquante XP** | `fastlane/play-games/achievements/xp150.png` | XP |
| **Citoyen engagé** | `fastlane/play-games/achievements/level5.png` | L5 |
| **Citoyen expert** | `fastlane/play-games/achievements/level10.png` | L10 |
| **Trois jours d’affilée** | `fastlane/play-games/achievements/streak3.png` | 3J |
| **Une semaine entière** | `fastlane/play-games/achievements/streak7.png` | 7J |
| **Sans faute** | `fastlane/play-games/achievements/perfect.png` | 100% |
| **Dix parcours** | `fastlane/play-games/achievements/runs10.png` | 10x |
| **Première couronne d’or** | `fastlane/play-games/achievements/crown3.png` | 3★ |

Après avoir assigné l'image à chaque succès, enregistrez la modification.

---

## 3. Publication de Play Games Services

Une fois ces étapes complétées :
1. Rendez-vous dans **Play Games Services** > **Publication** (Publishing).
2. Toutes les pastilles passeront au vert :
   - *Identifiants* : Prêt à publier
   - *Propriétés* : Prêt à publier
   - *Succès* : Prêt à publier
   - *Classements* : Prêt à publier
3. Cliquez sur **Vérifier et publier** (Review & Publish).
