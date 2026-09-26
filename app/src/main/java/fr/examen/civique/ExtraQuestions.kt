package fr.audeladedonnees.examen.civique

// Original practice questions: these are not presented as official examination items.
val extraQuestions = """
HISTOIRE|En quelle année la Première République est-elle proclamée ?|1792|1789|1815
HISTOIRE|Quel roi est exécuté en 1793 ?|Louis XVI|Louis XIV|Henri IV
HISTOIRE|À quel siècle appartient le siècle des Lumières ?|XVIIIe siècle|XVe siècle|XXe siècle
HISTOIRE|Quel penseur est associé à la séparation des pouvoirs ?|Montesquieu|Louis XIV|Jules César
HISTOIRE|Qui a écrit Les Misérables ?|Victor Hugo|Molière|Albert Camus
HISTOIRE|Qui a écrit Le Malade imaginaire ?|Molière|Victor Hugo|Émile Zola
HISTOIRE|En quelle année commence la Première Guerre mondiale ?|1914|1918|1939
HISTOIRE|En quelle année commence la Seconde Guerre mondiale en Europe ?|1939|1914|1945
HISTOIRE|Que commémore le 8 mai 1945 ?|La victoire contre l’Allemagne nazie|Le début de la Révolution|La création de l’euro
HISTOIRE|Qui lance l’appel du 18 juin 1940 ?|Charles de Gaulle|Jean Jaurès|Jules Ferry
HISTOIRE|Jean Moulin est une figure de quel mouvement ?|La Résistance|La monarchie absolue|Les Lumières
HISTOIRE|En quelle année les Françaises obtiennent-elles le droit de vote ?|1944|1789|1968
HISTOIRE|En quelle année débute la Ve République ?|1958|1945|1905
HISTOIRE|Qui est le premier président de la Ve République ?|Charles de Gaulle|François Mitterrand|Georges Pompidou
HISTOIRE|En quelle année la peine de mort est-elle abolie en France ?|1981|1958|2000
HISTOIRE|Quelle femme a porté la loi de 1975 sur l’IVG ?|Simone Veil|Marie Curie|George Sand
HISTOIRE|Quelle scientifique a reçu deux prix Nobel ?|Marie Curie|Simone de Beauvoir|Olympe de Gouges
HISTOIRE|Qui rédige la Déclaration des droits de la femme et de la citoyenne ?|Olympe de Gouges|Marie Curie|Simone Veil
GÉOGRAPHIE|Quel fleuve traverse Paris ?|La Seine|La Loire|La Garonne
GÉOGRAPHIE|Quel fleuve traverse Bordeaux ?|La Garonne|La Seine|Le Rhin
GÉOGRAPHIE|Quel fleuve traverse Lyon avec la Saône ?|Le Rhône|La Loire|La Somme
GÉOGRAPHIE|Quel océan borde l’ouest de la France métropolitaine ?|L’Atlantique|Le Pacifique|L’océan Indien
GÉOGRAPHIE|Quelle mer borde Marseille ?|La Méditerranée|La Baltique|La mer Noire
GÉOGRAPHIE|Quelle chaîne de montagnes sépare la France de l’Espagne ?|Les Pyrénées|Les Vosges|Le Jura
GÉOGRAPHIE|Dans quel massif se trouve le Mont Blanc ?|Les Alpes|Les Pyrénées|Le Massif central
GÉOGRAPHIE|Quelle île française se trouve en Méditerranée ?|La Corse|La Réunion|La Martinique
GÉOGRAPHIE|Dans quel océan se situe La Réunion ?|L’océan Indien|L’Atlantique|L’océan Arctique
GÉOGRAPHIE|Sur quel continent se trouve la Guyane ?|L’Amérique du Sud|L’Europe|L’Asie
GÉOGRAPHIE|Quelle ville française accueille le siège du Parlement européen ?|Strasbourg|Nantes|Toulouse
GÉOGRAPHIE|Quel pays partage une frontière avec la France métropolitaine ?|La Belgique|Le Portugal|La Grèce
GÉOGRAPHIE|Quelle ville est un grand port méditerranéen ?|Marseille|Lille|Reims
GÉOGRAPHIE|Quelle ville est surnommée la Ville rose ?|Toulouse|Paris|Nice
RÉPUBLIQUE|Quelle figure représente la République dans les mairies ?|Marianne|Une reine|Un empereur
RÉPUBLIQUE|Quel mot de la devise évoque la solidarité ?|Fraternité|Monarchie|Privilège
RÉPUBLIQUE|Quel symbole républicain porte traditionnellement Marianne ?|Le bonnet phrygien|Une couronne royale|Un casque de chevalier
RÉPUBLIQUE|Quel animal est un symbole traditionnel de la France ?|Le coq|Le lion|Le panda
RÉPUBLIQUE|Quel texte de 1789 proclame que les hommes naissent libres et égaux en droits ?|La Déclaration des droits de l’homme et du citoyen|Le Code de la route|Un règlement scolaire
INSTITUTIONS|Quelles sont les deux chambres du Parlement français ?|L’Assemblée nationale et le Sénat|La mairie et la préfecture|Le gouvernement et la police
INSTITUTIONS|Où siègent les députés ?|À l’Assemblée nationale|Au Sénat|À la Cour de cassation
INSTITUTIONS|Quel mot désigne une consultation directe des électeurs sur une question ?|Un référendum|Un recensement|Un sondage privé
INSTITUTIONS|Quel pouvoir est exercé par les tribunaux ?|Le pouvoir judiciaire|Le pouvoir législatif|Le pouvoir municipal
SITUATION|Une personne ne partage pas votre avis. Comment débattre ?|Écouter et argumenter sans violence|La menacer|L’empêcher de parler
SITUATION|Vous souhaitez aider des personnes isolées. Que pouvez-vous faire ?|Devenir bénévole dans une association|Publier leurs données privées|Exiger une récompense
SITUATION|Un camarade subit des moqueries répétées. Quelle réaction est utile ?|Le soutenir et prévenir un adulte de confiance|Participer aux moqueries|Partager les insultes
SITUATION|Vous devez trier un emballage et hésitez sur la poubelle. Que faire ?|Consulter les consignes locales de tri|Le jeter dans la rivière|Le laisser dans la rue
SITUATION|Un voisin utilise un fauteuil roulant. Comment l’aider à accéder à l’immeuble ?|Garder le passage accessible|Bloquer la rampe|Déplacer son fauteuil sans lui demander
SITUATION|Vous empruntez un livre à la bibliothèque. Que faire ensuite ?|Le rendre selon les conditions du prêt|Le revendre|Arracher les pages utiles
SITUATION|Un inconnu vous demande un code reçu par SMS. Que faire ?|Ne pas communiquer le code|Le transmettre immédiatement|Le publier sur les réseaux
SITUATION|Vous ne comprenez pas une démarche administrative. Que faire ?|Demander une explication au service concerné|Signer sans lire|Inventer des informations
SITUATION|Une réunion de quartier est organisée. Pourquoi y participer ?|Échanger sur la vie locale|Décider seul pour tous|Remplacer les élections
""".trimIndent().lines().map { line ->
    val fields = line.split('|')
    Question(fields[0], fields[1], fields.drop(2), 0)
}
