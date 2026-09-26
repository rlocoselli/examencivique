package fr.audeladedonnees.examen.civique

private fun parsePractice(text: String) = text.trimIndent().lines().map {
    val parts = it.split('|')
    Question(parts[0], parts[1], parts.drop(2), 0)
}

val hymnQuestions = parsePractice("""
HYMNE|Qui a composé La Marseillaise ?|Rouget de Lisle|Victor Hugo|Claude Monet
HYMNE|Dans quelle ville La Marseillaise a-t-elle été composée ?|Strasbourg|Marseille|Bordeaux
HYMNE|En quelle année La Marseillaise a-t-elle été composée ?|1792|1889|1945
HYMNE|Quel était le premier titre de La Marseillaise ?|Chant de guerre pour l’armée du Rhin|Le Chant des partisans|La Marche de Paris
HYMNE|Quels volontaires ont contribué à populariser le chant en rejoignant Paris ?|Les volontaires marseillais|Les volontaires romains|Les volontaires londoniens
HYMNE|Quel contexte historique accompagne la création de La Marseillaise ?|Les guerres révolutionnaires|La conquête spatiale|La Renaissance italienne
HYMNE|En quelle année La Marseillaise est-elle adoptée comme chant national pour la première fois ?|1795|1900|1968
HYMNE|En quelle année La Marseillaise redevient-elle officiellement l’hymne national ?|1879|1815|2002
HYMNE|Quel métier exerçait Rouget de Lisle lorsqu’il composa l’hymne ?|Officier|Astronaute|Cinéaste
HYMNE|À quelle occasion peut-on entendre l’hymne national ?|Une cérémonie officielle|Uniquement un anniversaire privé|Uniquement dans les cinémas
HYMNE|Une version de l’hymne jouée au piano sans chant est une version…|Instrumentale|Muette|Sans mélodie
HYMNE|Quel symbole sonore représente la République française ?|La Marseillaise|La sonnerie d’un téléphone|Une annonce publicitaire
""")

val moreQuestions = parsePractice("""
GÉOGRAPHIE|Quel fleuve est le plus long de France ?|La Loire|La Seine|La Somme
GÉOGRAPHIE|Quelle chaîne montagneuse se situe entre la France et la Suisse ?|Le Jura|Les Pyrénées|L’Himalaya
GÉOGRAPHIE|Quelle étendue d’eau sépare la France du sud de l’Angleterre ?|La Manche|La mer Rouge|Le lac Léman
GÉOGRAPHIE|Dans quelle région se situe Rennes ?|La Bretagne|La Corse|La Provence-Alpes-Côte d’Azur
GÉOGRAPHIE|Dans quelle région se situe Lille ?|Les Hauts-de-France|L’Occitanie|La Bretagne
GÉOGRAPHIE|Quelle ville est située sur la Côte d’Azur ?|Nice|Rouen|Amiens
GÉOGRAPHIE|Quelle ville est connue pour sa cathédrale où étaient sacrés les rois de France ?|Reims|Brest|Toulon
GÉOGRAPHIE|Quel territoire français est un archipel des Antilles ?|La Guadeloupe|La Corse|La Réunion
GÉOGRAPHIE|Quel territoire français est situé dans l’océan Indien ?|Mayotte|La Martinique|Saint-Pierre-et-Miquelon
GÉOGRAPHIE|Quel pays est voisin de la Guyane française ?|Le Brésil|L’Espagne|L’Allemagne
GÉOGRAPHIE|Quel fleuve traverse Strasbourg et sa région à la frontière allemande ?|Le Rhin|Le Nil|Le Tage
GÉOGRAPHIE|Dans quelle région se trouve le Mont-Saint-Michel ?|La Normandie|La Bourgogne-Franche-Comté|La Corse
GÉOGRAPHIE|Quel massif se trouve au centre de la France ?|Le Massif central|Les Andes|Les Carpates
GÉOGRAPHIE|Quelle ville est réputée pour son activité aéronautique ?|Toulouse|Chartres|Avignon
GÉOGRAPHIE|Quelle ville française se trouve en Alsace ?|Colmar|Biarritz|Limoges
HISTOIRE|Quel roi est surnommé le Roi-Soleil ?|Louis XIV|Louis XVI|François II
HISTOIRE|Quel château est particulièrement associé à Louis XIV ?|Versailles|Le château d’If|Le château de Chambord uniquement
HISTOIRE|En quelle année Napoléon Ier est-il sacré empereur ?|1804|1789|1914
HISTOIRE|À quelle bataille Napoléon est-il vaincu en 1815 ?|Waterloo|Verdun|Valmy
HISTOIRE|Quel écrivain a publié la lettre « J’accuse…! » ?|Émile Zola|Jules Verne|Molière
HISTOIRE|À quelle affaire cette lettre de Zola est-elle liée ?|L’affaire Dreyfus|La prise de la Bastille|La bataille de Waterloo
HISTOIRE|Quel écrivain a imaginé Vingt mille lieues sous les mers ?|Jules Verne|Jean Racine|Paul Verlaine
HISTOIRE|Quel peintre est associé aux Nymphéas ?|Claude Monet|Auguste Rodin|Louis Pasteur
HISTOIRE|Auguste Rodin était principalement…|Sculpteur|Navigateur|Astronome
HISTOIRE|Louis Pasteur est notamment connu pour ses travaux sur…|Les microbes et la vaccination|La peinture impressionniste|La construction de ponts
HISTOIRE|En quelle année la tour Eiffel est-elle inaugurée ?|1889|1789|1989
HISTOIRE|Pour quelle exposition la tour Eiffel a-t-elle été construite ?|L’Exposition universelle de 1889|Une exposition en 1945|L’exposition coloniale de 1931
HISTOIRE|Quel nom est associé aux débuts du cinéma en France ?|Les frères Lumière|Les frères Montgolfier|Les frères Grimm
HISTOIRE|Les frères Montgolfier sont associés à…|La montgolfière|La locomotive électrique|La photographie numérique
HISTOIRE|Quel monument parisien accueille les sépultures de grandes personnalités ?|Le Panthéon|L’Opéra Garnier|Le Centre Pompidou
RÉPUBLIQUE|La devise républicaine comporte combien de mots principaux ?|Trois|Cinq|Un
RÉPUBLIQUE|La fraternité encourage notamment…|L’entraide entre les personnes|Le rejet des autres|La violence dans les débats
RÉPUBLIQUE|Quel symbole républicain retrouve-t-on sur des timbres français ?|Marianne|Une couronne impériale obligatoire|Un drapeau pirate
SITUATION|Lors d’un débat, une information vous paraît douteuse. Que faire ?|Vérifier sa source avant de la partager|La partager immédiatement|Ajouter une rumeur
SITUATION|Vous voulez connaître les horaires de votre mairie. Où chercher d’abord ?|Sur le site officiel de la commune|Dans une publicité sans rapport|Sur un message anonyme
SITUATION|Vous préparez un rendez-vous administratif. Quelle habitude aide ?|Lire la liste des justificatifs demandés|Venir avec des documents inventés|Confier son identité à un inconnu
SITUATION|Vous avez rendez-vous mais ne pouvez plus venir. Que faire ?|Prévenir et demander un nouveau créneau|Ne jamais prévenir|Envoyer un inconnu avec vos papiers
SITUATION|Vous participez à un projet associatif. Quelle attitude facilite le travail collectif ?|Écouter les autres et partager les tâches|Refuser tout échange|Garder toutes les informations pour soi
SITUATION|Un piéton âgé semble perdu. Quelle première approche est respectueuse ?|Lui demander s’il souhaite de l’aide|Le saisir sans lui parler|Photographier ses papiers
SITUATION|Un document administratif contient une erreur sur votre nom. Que faire ?|Contacter le service pour demander une correction|Modifier le document officiel soi-même|Utiliser une autre identité
SITUATION|Quel comportement aide à préserver un parc public ?|Utiliser les poubelles et respecter les équipements|Abîmer les bancs|Laisser les déchets sur place
SITUATION|Vous organisez un repas entre voisins aux habitudes différentes. Que faire ?|Demander leurs préférences et respecter leurs choix|Imposer un menu pour les humilier|Exclure ceux qui mangent autrement
SITUATION|Vous voulez apprendre le français dans votre quartier. Quelle démarche peut aider ?|Se renseigner auprès des associations et de la mairie|Éviter tout échange|Acheter un faux certificat
SITUATION|Une personne ne sait pas utiliser un service en ligne. Comment l’aider ?|L’accompagner avec son accord sans conserver ses mots de passe|Publier son mot de passe|Faire les démarches à son insu
SITUATION|Vous voulez donner des vêtements à une association. Que faire ?|Vérifier ses besoins et modalités de collecte|Déposer des sacs devant une porte fermée sans consigne|Exiger des données personnelles des bénéficiaires
SITUATION|Vous voyez une information d’alerte locale sur un réseau social. Que faire ?|Consulter les messages des autorités compétentes|Créer une fausse alerte|Diffuser une ancienne photo comme actuelle
SITUATION|Un nouvel élève arrive dans votre groupe. Quelle attitude favorise son accueil ?|L’inclure et lui expliquer le fonctionnement du groupe|Se moquer de son accent|L’isoler
SITUATION|Vous voulez visiter un musée avec peu de moyens. Que faire ?|Consulter ses tarifs et les éventuelles gratuités|Entrer de force|Utiliser un billet falsifié
SITUATION|Lors d’une réunion, plusieurs personnes parlent en même temps. Que proposer ?|Organiser des tours de parole|Parler toujours plus fort|Empêcher les autres de participer
SITUATION|Une personne vous confie une difficulté personnelle. Quelle attitude est respectueuse ?|L’écouter et respecter sa vie privée|Raconter son histoire à tout le quartier|Publier son adresse
""")
