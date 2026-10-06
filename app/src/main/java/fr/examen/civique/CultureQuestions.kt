package fr.audeladedonnees.examen.civique

const val CIVIC_SOURCE = "https://formation-civique.interieur.gouv.fr/examen-civique/liste-officielle-des-questions-de-connaissance-csp/"
data class CultureQuestion(val category: String, val text: String, val answers: List<String>, val detail: String, val photo: String? = null)
private fun bank(category: String, rows: String) = rows.trimIndent().lines().filter { it.isNotBlank() }.map {
    val p = it.split('|')
    CultureQuestion(category, p[0], p.subList(1, 5), p[5], p.getOrNull(6))
}
val cultureBank = bank("Grammaire", """
Quelle phrase est correctement accordée ?|Les fleurs sont belles.|Les fleurs sont belle.|Les fleur sont belles.|Les fleurs est belles.|L’adjectif « belles » s’accorde au féminin pluriel avec « fleurs ».
Complétez : « Les lettres que j’ai … »|écrites|écrit|écrite|écrire|Avec avoir, le participe s’accorde avec le complément d’objet direct placé avant : « les lettres ».
Complétez : « Elles se sont … »|lavées|lavé|laver|lavée|Le pronom « se » est ici complément d’objet direct : le participe s’accorde.
Complétez : « Elles se sont … les mains. »|lavé|lavées|lavée|laver|Le COD « les mains » suit le verbe : « se » est indirect, donc pas d’accord.
Quelle forme est au subjonctif ?|que nous soyons|nous sommes|nous serons|nous étions|« Que nous soyons » est le présent du subjonctif du verbe être.
Complétez : « Si j’avais le temps, je … »|viendrais|viendrai|viens|venir|Après si + imparfait, la proposition principale emploie le conditionnel présent.
Complétez : « Il faut que tu … »|viennes|viens|viendras|venais|« Il faut que » appelle le subjonctif.
Quelle orthographe est correcte ?|Ils ont leurs livres.|Ils ont leur livres.|Ils on leurs livres.|Ils ont leurs livre.|« Ont » est le verbe avoir ; « leurs » s’accorde avec les livres possédés.
Complétez : « Elle … Paris demain. »|va à|va a|vas à|vas a|« À » est une préposition ; « a » est le verbe avoir.
Quel mot est un adverbe ?|doucement|douce|douceur|adoucir|Un adverbe précise notamment le sens d’un verbe : parler doucement.
Quel est le pluriel de « cheval » ?|chevaux|chevals|chevaus|chevales|Le pluriel de cheval est chevaux.
Quel est le pluriel de « bijou » ?|bijoux|bijous|bijou|bijoues|Bijou fait partie des sept noms en -ou dont le pluriel prend un x.
Complétez : « … sont mes amis. »|Ce|Se|Ces|Ses|« Ce » est un pronom démonstratif dans « ce sont ».
Complétez : « Je … ai parlé. »|leur|leurs|leures|leurent|Le pronom personnel « leur » est invariable.
Quel temps est « nous avions fini » ?|Le plus-que-parfait|Le passé composé|Le futur antérieur|Le passé simple|Le plus-que-parfait associe un auxiliaire à l’imparfait et un participe passé.
Quelle phrase emploie le futur antérieur ?|Quand tu auras terminé, appelle-moi.|Tu termineras demain.|Tu terminais hier.|Tu as terminé.|« Auras terminé » exprime une action future accomplie avant une autre.
Quel mot est féminin ?|Une échappatoire|Un échappatoire|Un intervalle|Un pétale|Échappatoire est féminin ; intervalle et pétale sont masculins.
Complétez : « … soit votre choix, je le respecte. »|Quel que|Quelque|Qu’elle que|Quelques|« Quel que » suivi du subjonctif s’accorde avec le sujet « choix ».
Quel est le sujet dans « Dans le jardin chantent les oiseaux » ?|Les oiseaux|Le jardin|Dans|Chantent|Le sujet peut être placé après le verbe : ce sont les oiseaux qui chantent.
Quelle phrase est à la voix passive ?|Le livre est lu par Léa.|Léa lit le livre.|Léa a lu le livre.|Léa lira le livre.|À la voix passive, le sujet subit l’action.
""") + bank("Littérature", """
Qui a écrit Les Misérables ?|Victor Hugo|Émile Zola|Gustave Flaubert|Marcel Proust|Victor Hugo publie Les Misérables en 1862.
Qui a écrit Madame Bovary ?|Gustave Flaubert|Honoré de Balzac|Stendhal|Albert Camus|Le roman de Flaubert paraît en 1857.
Qui a écrit Le Petit Prince ?|Antoine de Saint-Exupéry|Jules Verne|Jean Giono|Jacques Prévert|Ce conte philosophique paraît en 1943.
Qui a écrit Le Malade imaginaire ?|Molière|Racine|Corneille|Beaumarchais|La dernière comédie de Molière est créée en 1673.
À qui doit-on Les Fleurs du mal ?|Charles Baudelaire|Paul Verlaine|Arthur Rimbaud|Paul Éluard|Le recueil de Baudelaire paraît en 1857.
Qui a écrit L’Étranger ?|Albert Camus|Jean-Paul Sartre|André Gide|André Malraux|Meursault est le personnage principal de ce roman publié en 1942.
Quel auteur a créé le capitaine Nemo ?|Jules Verne|Victor Hugo|Alexandre Dumas|Maupassant|Nemo apparaît notamment dans Vingt mille lieues sous les mers.
Qui a écrit Le Rouge et le Noir ?|Stendhal|Balzac|Flaubert|Zola|Julien Sorel est le héros du roman paru en 1830.
Quel auteur a écrit À la recherche du temps perdu ?|Marcel Proust|Louis Aragon|André Breton|Paul Valéry|La madeleine est associée à la mémoire involontaire dans cette œuvre.
Qui a écrit les Fables ?|Jean de La Fontaine|Molière|Racine|Voltaire|Les Fables de La Fontaine mettent souvent en scène des animaux.
Qui a écrit Candide ?|Voltaire|Rousseau|Diderot|Montesquieu|Ce conte philosophique de 1759 critique notamment l’optimisme aveugle.
Qui a écrit Les Trois Mousquetaires ?|Alexandre Dumas|Jules Verne|Victor Hugo|Prosper Mérimée|Athos, Porthos et Aramis accueillent d’Artagnan dans ce roman.
Quel est le vrai nom de George Sand ?|Aurore Dupin|Colette Willy|Simone de Beauvoir|Marguerite Duras|Aurore Dupin adopte le pseudonyme George Sand.
Qui a écrit Le Deuxième Sexe ?|Simone de Beauvoir|Simone Veil|Colette|George Sand|Cet essai de 1949 est une œuvre majeure de la pensée féministe.
Qui a écrit Cyrano de Bergerac ?|Edmond Rostand|Victor Hugo|Alfred de Musset|Jean Anouilh|La pièce est créée en 1897.
Quel mouvement est associé à André Breton ?|Le surréalisme|Le classicisme|Le naturalisme|Le romantisme|Breton publie le Manifeste du surréalisme en 1924.
Quel écrivain est associé au naturalisme ?|Émile Zola|Jean Racine|Chrétien de Troyes|Pierre Corneille|Zola est l’auteur du cycle des Rougon-Macquart.
Combien de vers comporte un sonnet traditionnel ?|14|12|16|10|Un sonnet comporte deux quatrains et deux tercets.
Combien de syllabes compte un alexandrin ?|12|10|8|14|L’alexandrin est un vers de douze syllabes.
Qui a écrit La Princesse de Clèves ?|Madame de Lafayette|Madame de Sévigné|George Sand|Colette|Ce roman paraît anonymement en 1678.
""") + bank("Régions & paysages", """
Dans quelle région se trouve le Mont-Saint-Michel ?|Normandie|Bretagne|Pays de la Loire|Hauts-de-France|Le Mont-Saint-Michel se trouve dans le département de la Manche.|mont
Dans quelle région se trouve Strasbourg ?|Grand Est|Bourgogne-Franche-Comté|Hauts-de-France|Île-de-France|Strasbourg est la préfecture du Grand Est.
Quelle ville est la préfecture de la Bretagne ?|Rennes|Brest|Nantes|Vannes|Rennes est la préfecture de la région Bretagne.
Dans quelle région se trouve Bordeaux ?|Nouvelle-Aquitaine|Occitanie|Centre-Val de Loire|Normandie|Bordeaux est la préfecture de Nouvelle-Aquitaine.
Dans quelle région se trouve Lyon ?|Auvergne-Rhône-Alpes|Grand Est|Occitanie|Provence-Alpes-Côte d’Azur|Lyon est la préfecture d’Auvergne-Rhône-Alpes.
Quelle mer borde la Corse ?|La Méditerranée|La mer du Nord|La mer Baltique|La mer Noire|La Corse est une île de Méditerranée.
Quelle région abrite les châteaux de Chambord et Chenonceau ?|Centre-Val de Loire|Bretagne|Grand Est|Occitanie|Ces châteaux se trouvent dans le Val de Loire.
Quelle ville est surnommée la Ville rose ?|Toulouse|Lille|Rouen|Dijon|Les briques de terre cuite donnent à Toulouse sa couleur caractéristique.
Quelle chaîne sépare la France de l’Espagne ?|Les Pyrénées|Les Alpes|Le Jura|Les Vosges|Les Pyrénées forment une frontière naturelle entre les deux pays.
Quel océan borde Biarritz ?|L’Atlantique|Le Pacifique|L’Indien|L’Arctique|Biarritz se trouve sur la côte basque, dans le golfe de Gascogne.
Dans quelle région se trouve Lille ?|Hauts-de-France|Grand Est|Normandie|Bretagne|Lille est la préfecture des Hauts-de-France.
Quelle région abrite les calanques de Marseille ?|Provence-Alpes-Côte d’Azur|Occitanie|Corse|Nouvelle-Aquitaine|Les calanques s’étendent sur le littoral entre Marseille et Cassis.
Quel fleuve traverse Paris ?|La Seine|La Loire|Le Rhône|La Garonne|La Seine traverse la capitale.|paris
Quel territoire français se trouve en Amérique du Sud ?|La Guyane|La Guadeloupe|Mayotte|La Réunion|La Guyane est frontalière du Brésil et du Suriname.
Quelle île française se trouve dans l’océan Indien ?|La Réunion|La Martinique|La Corse|Belle-Île|La Réunion se trouve à l’est de Madagascar.
Combien de régions compte la France métropolitaine ?|13|18|22|10|La métropole compte 13 régions ; cinq autres régions sont ultramarines.
Quelle région est associée aux volcans de la chaîne des Puys ?|Auvergne-Rhône-Alpes|Bretagne|Île-de-France|Grand Est|La chaîne des Puys se trouve dans le Puy-de-Dôme.
Quelle ville est célèbre pour les Hospices de Bourgogne ?|Beaune|Nice|Amiens|Bayonne|Les Hospices de Beaune sont célèbres pour leurs toits de tuiles vernissées.
Quelle ville abrite le palais des Papes ?|Avignon|Arles|Nîmes|Aix-en-Provence|Le palais des Papes rappelle la présence de la papauté à Avignon au XIVe siècle.
Dans quel département se trouve Étretat ?|Seine-Maritime|Calvados|Finistère|Somme|Les falaises d’Étretat bordent la Manche en Normandie.
""") + bank("Gastronomie", """
De quelle ville la bouillabaisse est-elle une spécialité ?|Marseille|Lille|Strasbourg|Rennes|La bouillabaisse est une préparation provençale de poissons.
À quelle région associe-t-on la choucroute ?|L’Alsace|La Bretagne|Le Pays basque|La Provence|La choucroute repose sur du chou fermenté.
Quel fromage est traditionnellement utilisé dans la tartiflette ?|Le reblochon|Le roquefort|Le camembert|Le brie|La tartiflette associe pommes de terre, reblochon, oignons et souvent lardons.
Quelle farine utilise-t-on traditionnellement pour une galette bretonne salée ?|Le sarrasin|Le maïs|Le riz|Le pois chiche|Le sarrasin, appelé aussi blé noir, est caractéristique des galettes bretonnes.
Quel plat est associé à Castelnaudary ?|Le cassoulet|La quiche|La bouillabaisse|La ratatouille|Le cassoulet est préparé avec des haricots blancs et des viandes.
Quel dessert est une tarte aux pommes renversée ?|La tarte Tatin|Le clafoutis|Le far breton|Le Paris-Brest|Les pommes caramélisées cuisent sous la pâte avant le démoulage.
Quel dessert est traditionnellement préparé avec des cerises ?|Le clafoutis|Le canelé|Le kouign-amann|Le financier|Le clafoutis aux cerises est associé au Limousin.
De quelle ville le canelé est-il une spécialité ?|Bordeaux|Dijon|Nancy|Reims|Le canelé a une croûte caramélisée et un cœur tendre.
Quel gâteau breton contient beaucoup de beurre ?|Le kouign-amann|Le baba|Le fraisier|Le saint-honoré|Son nom breton signifie gâteau au beurre.
Quelle ville est associée à la moutarde ?|Dijon|Toulon|Brest|Orléans|Dijon est historiquement célèbre pour sa moutarde.
Quel fromage est affiné dans les caves de Roquefort-sur-Soulzon ?|Le roquefort|Le comté|Le cantal|Le munster|Le roquefort est un fromage persillé au lait de brebis.
Quel fromage est associé à la Normandie ?|Le camembert|Le reblochon|Le beaufort|Le brocciu|Le camembert est un fromage à pâte molle et croûte fleurie.
Quel fromage corse est fabriqué à partir de lactosérum ?|Le brocciu|Le comté|Le brie|Le maroilles|Le brocciu est notamment utilisé dans le fiadone.
Quelle spécialité niçoise est faite de farine de pois chiche ?|La socca|La crêpe|La gaufre|La fougasse|La socca est cuite en fine couche, traditionnellement au four à bois.
Quel ingrédient caractérise la ratatouille ?|Les légumes d’été|Le riz|Les pommes|Les haricots secs|Aubergines, courgettes, tomates et poivrons composent ce plat provençal.
Quelle pâtisserie a la forme d’une roue ?|Le Paris-Brest|L’éclair|Le millefeuille|Le canelé|La forme du Paris-Brest évoque la course cycliste du même nom.
Qu’est-ce qu’une AOP ?|Une appellation d’origine protégée|Une association de pâtissiers|Une méthode de cuisson|Un label de prix|L’AOP lie un produit à son origine géographique et à un savoir-faire.
Quel plat mijote traditionnellement dans du vin rouge de Bourgogne ?|Le bœuf bourguignon|La blanquette|La choucroute|Le gratin dauphinois|Le bœuf bourguignon est un plat de viande mijotée au vin rouge.
Que contient traditionnellement une quiche lorraine ?|Des œufs, de la crème et des lardons|Du thon et du riz|Des pommes et du sucre|Des pois chiches|La recette traditionnelle ne comporte pas de fromage.
Quel dessert est associé à la Lorraine et à Commercy ?|La madeleine|Le canelé|Le far|La tarte tropézienne|La madeleine est un petit gâteau reconnaissable à sa forme de coquillage.
""")
