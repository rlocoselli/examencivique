package fr.audeladedonnees.examen.civique

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlin.random.Random
import kotlinx.coroutines.delay

data class Question(val kind: String, val text: String, val answers: List<String>, val good: Int)
private const val FINAL_CASE = 20
private val diceFaces = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")
val questions = listOf(
    Question("ESSENTIEL", "Quelle est la devise de la République française ?", listOf("Liberté, Égalité, Fraternité", "Unité, Travail, Justice", "Paix, Force, Progrès"), 0),
    Question("SITUATION", "À la mairie, vous voulez vous inscrire sur les listes électorales. Que devez-vous faire ?", listOf("Présenter une pièce d’identité et un justificatif de domicile", "Attendre d’être tiré au sort", "Demander l’autorisation d’un voisin"), 0),
    Question("ESSENTIEL", "Quelle est la date de la fête nationale française ?", listOf("1er mai", "14 juillet", "11 novembre"), 1),
    Question("SITUATION", "Votre voisin a une religion différente de la vôtre. Quel comportement respecte la laïcité ?", listOf("Respecter sa liberté de conscience", "Lui interdire d’en parler", "L’obliger à changer de religion"), 0),
    Question("ESSENTIEL", "Qui vote les lois en France ?", listOf("Le Parlement", "Le Conseil constitutionnel", "La mairie"), 0),
    Question("SITUATION", "Vous êtes témoin d’un accident avec une personne blessée. Quel numéro appeler ?", listOf("112 ou 15", "3637", "Le 118 000"), 0),
    Question("ESSENTIEL", "Quel est le principe de la laïcité ?", listOf("Une religion officielle", "La liberté de conscience et la neutralité de l’État", "L’interdiction des religions"), 1),
    Question("SITUATION", "Une fille et un garçon veulent pratiquer le même métier. Que dit le principe d’égalité ?", listOf("Ils ont les mêmes droits et les mêmes chances", "Seul le garçon peut choisir", "Cela dépend de leur quartier"), 0),
    Question("ESSENTIEL", "Combien de couleurs composent le drapeau français ?", listOf("Deux", "Trois", "Quatre"), 1),
    Question("SITUATION", "Vous déménagez : quelle démarche est importante auprès de l’administration ?", listOf("Mettre à jour votre adresse auprès des organismes concernés", "Ne rien déclarer", "Changer de nom"), 0),
    Question("ESSENTIEL", "Quel document fixe les droits et libertés fondamentaux ?", listOf("La Constitution", "Le code de la route", "Le budget de l’État"), 0),
    Question("SITUATION", "Dans une file d’attente, quelle attitude correspond aux règles de vie collective ?", listOf("Respecter les autres et attendre son tour", "Passer devant tout le monde", "Crier pour être servi"), 0),
    Question("HISTOIRE", "En quelle année commence la Révolution française ?", listOf("1789", "1815", "1905"), 0),
    Question("RÉPUBLIQUE", "Quel personnage symbolise la République française ?", listOf("Marianne", "Jeanne d’Arc", "Napoléon"), 0),
    Question("INSTITUTIONS", "Qui représente l’État dans un département ?", listOf("Le préfet", "Le maire", "Le sénateur"), 0),
    Question("INSTITUTIONS", "Combien y a-t-il de chambres au Parlement français ?", listOf("Une", "Deux", "Trois"), 1),
    Question("VIE PRATIQUE", "À quoi sert la carte Vitale ?", listOf("À faciliter le remboursement des soins", "À voter", "À conduire"), 0),
    Question("DROITS / DEVOIRS", "L’instruction est obligatoire en France jusqu’à quel âge minimum ?", listOf("16 ans", "12 ans", "21 ans"), 0),
    Question("SITUATION", "Vous trouvez un portefeuille dans la rue. Que faire ?", listOf("Le remettre à la police ou à la mairie", "Garder l’argent", "Le jeter"), 0),
    Question("GÉOGRAPHIE", "Quelle est la capitale de la France ?", listOf("Paris", "Lyon", "Marseille"), 0),
    Question("HISTOIRE", "Quel est l’hymne national français ?", listOf("La Marseillaise", "Le Chant du départ", "Le Déserteur"), 0),
    Question("DROITS / DEVOIRS", "Payer ses impôts sert notamment à financer :", listOf("Les services publics", "Uniquement les fêtes", "Les voyages privés"), 0),
    Question("SITUATION", "Vous êtes victime de discrimination au travail. Que pouvez-vous faire ?", listOf("Demander de l’aide et signaler les faits", "Ne jamais en parler", "Discriminer à votre tour"), 0),
    Question("RÉPUBLIQUE", "Que signifie l’égalité devant la loi ?", listOf("Les mêmes règles s’appliquent à tous", "Tout le monde gagne le même salaire", "Personne ne peut être jugé"), 0),
    Question("VIE PRATIQUE", "Quel service accompagne les demandeurs d’emploi ?", listOf("France Travail", "La préfecture uniquement", "La bibliothèque"), 0),
    Question("SITUATION", "Un enfant est en danger immédiat. Quel numéro d’urgence peut-on appeler ?", listOf("112", "3999", "0800 00 00 00"), 0),
    Question("RÉPUBLIQUE", "Quelles sont les couleurs du drapeau français ?", listOf("Bleu, blanc, rouge", "Vert, blanc, rouge", "Bleu, jaune, rouge"), 0),
    Question("INSTITUTIONS", "Quel est le rôle principal du président de la République ?", listOf("Être le chef de l’État", "Diriger chaque mairie", "Voter toutes les lois seul"), 0),
    Question("INSTITUTIONS", "Qui dirige une commune ?", listOf("Le maire", "Le préfet", "Le président du Sénat"), 0),
    Question("INSTITUTIONS", "Les députés sont élus par :", listOf("Les citoyens", "Les préfets", "Les juges uniquement"), 0),
    Question("HISTOIRE", "Que célèbre-t-on le 11 novembre ?", listOf("L’armistice de 1918", "La fête nationale", "La rentrée scolaire"), 0),
    Question("HISTOIRE", "Quel événement est associé au 14 juillet 1789 ?", listOf("La prise de la Bastille", "La signature de l’Euro", "La création de l’école"), 0),
    Question("HISTOIRE", "La Déclaration des droits de l’homme et du citoyen date de :", listOf("1789", "1945", "2002"), 0),
    Question("GÉOGRAPHIE", "La France métropolitaine se trouve sur quel continent ?", listOf("L’Europe", "L’Afrique", "L’Asie"), 0),
    Question("DROITS / DEVOIRS", "Voter en France est :", listOf("Un droit et une responsabilité citoyenne", "Interdit aux citoyens", "Réservé aux élus"), 0),
    Question("DROITS / DEVOIRS", "La liberté d’expression permet-elle de menacer quelqu’un ?", listOf("Non, elle a des limites prévues par la loi", "Oui, toujours", "Seulement sur internet"), 0),
    Question("DROITS / DEVOIRS", "Les femmes et les hommes ont-ils les mêmes droits ?", listOf("Oui", "Non", "Seulement au travail"), 0),
    Question("VIE PRATIQUE", "Où demander une copie d’acte de naissance ?", listOf("À la mairie compétente", "À la banque", "À la bibliothèque"), 0),
    Question("VIE PRATIQUE", "À quoi sert une carte d’identité ?", listOf("À prouver son identité", "À remplacer un permis", "À payer ses impôts"), 0),
    Question("VIE PRATIQUE", "Quel organisme gère notamment les remboursements de santé ?", listOf("L’Assurance Maladie", "La mairie uniquement", "Le Parlement"), 0),
    Question("SITUATION", "Votre enfant est malade et ne peut pas aller à l’école. Que faire ?", listOf("Prévenir l’école", "Ne rien dire", "Changer son identité"), 0),
    Question("SITUATION", "Vous recevez un message demandant votre mot de passe bancaire. Que faire ?", listOf("Ne pas le transmettre et signaler la fraude", "L’envoyer immédiatement", "Le publier en ligne"), 0),
    Question("SITUATION", "Vous avez un conflit avec un voisin. Quelle attitude est préférable ?", listOf("Dialoguer calmement ou demander une médiation", "Menacer le voisin", "Appeler les secours sans raison"), 0),
    Question("SITUATION", "Vous voyez un départ de feu. Que faire ?", listOf("Vous mettre en sécurité et appeler le 18 ou le 112", "Entrer dans le feu", "Attendre plusieurs heures"), 0),
    Question("PIÈGE", "La laïcité signifie-t-elle que les citoyens n’ont pas le droit d’avoir une religion ?", listOf("Non, elle garantit la liberté de conscience", "Oui, toute religion est interdite", "Seulement pour les adultes"), 0),
    Question("PIÈGE", "Peut-on refuser une personne à cause de son origine ?", listOf("Non, la discrimination est interdite", "Oui, si elle est polie", "Oui, dans les services publics"), 0),
    Question("BONUS", "Une association peut-elle réunir des citoyens autour d’un projet commun ?", listOf("Oui", "Non, jamais", "Seulement dans une mairie"), 0)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        // Backend Google : init SDK (no-op si APP_ID non configuré → reste hors-ligne).
        PlayBackend.init(this)
        // Connexion silencieuse + fusion cloud au démarrage.
        lifecycleScope.launch {
            try {
                if (PlayBackend.silentSignIn(this@MainActivity)) {
                    val store = ProgressStore(applicationContext)
                    PlayBackend.pullCloud(this@MainActivity, store)
                }
            } catch (_: Throwable) { /* hors-ligne : on ignore */ }
        }
        setContent { QuestApp() }
    }

    override fun onResume() {
        super.onResume()
        // Re-soumission opportuniste des scores si on est connecté.
        if (PlayBackend.connected) PlayBackend.pushScores(this)
    }
}

@Composable fun App() {
    var splash by remember { mutableStateOf(true) }; var menu by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(1400); splash = false }
    if (splash) SplashScreen() else if (menu) MainMenu(onStart = { menu = false }) else Game(onExit = { menu = true })
}

@Composable private fun SplashScreen() {
    Surface(color = Color(0xFFF7F4EF), modifier = Modifier.fillMaxSize()) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
        Row { Box(Modifier.size(28.dp).background(Color(0xFF172B5C))); Box(Modifier.size(28.dp).background(Color.White)); Box(Modifier.size(28.dp).background(Color(0xFFD64545))) }
        Text("PARCOURS", color = Color(0xFF17233C), fontWeight = FontWeight.Bold, fontSize = 34.sp, letterSpacing = 3.sp)
        Text("CITOYEN", color = Color(0xFFD64545), fontWeight = FontWeight.Bold, fontSize = 34.sp, letterSpacing = 3.sp)
        Text("Apprendre • Jouer • Devenir citoyen", color = Color(0xFF657087), modifier = Modifier.padding(top = 12.dp))
    } }
}

@Composable private fun MainMenu(onStart: () -> Unit) {
    val context = LocalContext.current
    Surface(color = Color(0xFFF7F4EF), modifier = Modifier.fillMaxSize()) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🇫🇷 PARCOURS CITOYEN", color = Color(0xFFD64545), fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.padding(top = 22.dp))
        Text("Choisissez votre aventure", color = Color(0xFF17233C), fontWeight = FontWeight.Bold, fontSize = 28.sp, modifier = Modifier.padding(vertical = 10.dp))
        Text("Préparez l’examen civique en vous amusant.", color = Color(0xFF657087), modifier = Modifier.padding(bottom = 22.dp))
        MenuButton("🎲  Parcours complet", "Plateau, dé et toutes les catégories", onStart)
        MenuButton("🏛️  Quiz République", "Valeurs, laïcité et symboles", onStart)
        MenuButton("📜  Quiz Histoire & géographie", "Dates, personnages et territoires", onStart)
        MenuButton("⚖️  Quiz droits & vie pratique", "Institutions, école, santé et travail", onStart)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.service-public.gouv.fr/particuliers/vosdroits/R75684"))) }, modifier = Modifier.fillMaxWidth().height(58.dp)) { Text("📍 Trouver un centre d’examen officiel") }
        Text("Recherche via Service-Public.fr", color = Color(0xFF657087), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
    } }
}

@Composable private fun MenuButton(title: String, subtitle: String, onClick: () -> Unit) { Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF17233C)), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).height(68.dp)) { Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) { Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(subtitle, fontSize = 12.sp, color = Color(0xFF657087)) } } }

@Composable fun Game(onExit: () -> Unit = {}) {
    var position by remember { mutableIntStateOf(0) }; var score by remember { mutableIntStateOf(0) }
    var questionIndex by remember { mutableIntStateOf(0) }; var questionOrder by remember { mutableStateOf(questions.indices.shuffled()) }; var selected by remember { mutableIntStateOf(-1) }; var message by remember { mutableStateOf("Lancez le dé pour commencer ! 🎲") }; var combo by remember { mutableIntStateOf(0) }; var dice by remember { mutableIntStateOf(0) }; var rolled by remember { mutableStateOf(false) }; var rolling by remember { mutableStateOf(false) }; var total by remember { mutableIntStateOf(0) }; var correct by remember { mutableIntStateOf(0) }; var lastAnswerCorrect by remember { mutableStateOf(false) }; var showAdvancePopup by remember { mutableStateOf(false) }
    val q = questions[questionOrder[questionIndex % questionOrder.size]]; val finished = position >= FINAL_CASE
    val animatedProgress by animateFloatAsState((position / FINAL_CASE.toFloat()).coerceIn(0f, 1f), animationSpec = tween(700), label = "progression")
    LaunchedEffect(rolling) { if (rolling) { repeat(12) { dice = Random.nextInt(1, 7); delay(75) }; rolling = false; rolled = true; message = "Vous avez fait $dice ! Direction la case ${position + dice} 🚶" } }
    Surface(color = Color(0xFFF7F4EF), modifier = Modifier.fillMaxSize()) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        TextButton(onClick = onExit, modifier = Modifier.align(Alignment.Start)) { Text("‹ Menu principal", color = Color(0xFF17233C)) }
        Text("PARCOURS CITOYEN", color = Color(0xFFD64545), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text(if (finished) "Félicitations ! 🎉" else "En route vers la naturalisation", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17233C), modifier = Modifier.padding(top = 8.dp))
        Text("Répondez juste, avancez fièrement.", color = Color(0xFF657087), modifier = Modifier.padding(bottom = 18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF17233C)), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("CASE $position / $FINAL_CASE", color = Color(0xFFFFD166), fontWeight = FontWeight.Bold); Text("Score $score", color = Color.White) }
            Spacer(Modifier.height(14.dp)); Box(Modifier.fillMaxWidth().height(22.dp).background(Color(0xFF33415F), RoundedCornerShape(12.dp))) { Box(Modifier.fillMaxWidth(animatedProgress).height(22.dp).background(Color(0xFFFFD166), RoundedCornerShape(12.dp))) }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("🚶", fontSize = 22.sp, modifier = Modifier.offset(x = (animatedProgress * 245).dp)); Spacer(Modifier.weight(1f)); Text("🏁", fontSize = 22.sp) }
            Spacer(Modifier.height(10.dp)); Text(if (finished) "NATURALISATION" else "Départ  →  Citoyen français", color = Color.White, fontWeight = FontWeight.Bold)
        } }
        Spacer(Modifier.height(12.dp))
        Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth().height(210.dp), elevation = CardDefaults.cardElevation(6.dp)) { Box {
            Image(painterResource(R.drawable.board_art), "Illustration du plateau citoyen", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Color(0x440E1B38)))
            Text("🧑‍🎓", fontSize = 38.sp, modifier = Modifier.align(Alignment.BottomStart).offset(x = (animatedProgress * 260).dp, y = (-animatedProgress * 105).dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xDD17233C)), shape = RoundedCornerShape(14.dp), modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) { Text("CASE $position • ${house(position)}", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) }
            Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) { Text("Ton aventure citoyenne", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("Le bonhomme avance à chaque bonne réponse !", color = Color.White) }
        } }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FrenchBadge("🗼", "Eiffel"); FrenchBadge("🥖", "Baguette"); FrenchBadge("🐓", "Coq"); FrenchBadge("🇫🇷", "France")
        }
        Spacer(Modifier.height(18.dp))
        AnimatedVisibility(!finished, enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { it / 2 }), modifier = Modifier.fillMaxWidth()) { Column {
            Text(q.kind, color = Color(0xFFD64545), fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start)); Text(q.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17233C), modifier = Modifier.padding(vertical = 10.dp))
            Text("⭐ $score points   🔥 série : $combo   🎯 ${if (total == 0) 0 else correct * 100 / total}%", color = Color(0xFFD64545), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp)); Text(message, color = Color(0xFF657087), modifier = Modifier.padding(8.dp))
            if (!rolled) Button(onClick = { rolling = true; message = "Le dé roule… roule… roule… 🎲" }, enabled = !rolling, modifier = Modifier.fillMaxWidth()) { Text(if (rolling) "${diceFaces.getOrNull(dice - 1) ?: "🎲"} Lancement…" else "🎲 Lancer le dé") }
            if (rolled) { Text("🎲 $dice  •  CASE : ${house(position)}", color = Color(0xFF17233C), fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)); q.answers.forEachIndexed { i, answer -> Button(onClick = { if (selected < 0) { selected = i; total++; lastAnswerCorrect = i == q.good; if (i == q.good) { correct++; score += 10; combo++; showAdvancePopup = true; message = "Bonne réponse ! Préparez-vous à avancer 🚀\nExplication : ${explanation(q)}" } else { combo = 0; position = (position - 2).coerceAtLeast(0); message = "Oups ! Le bonhomme recule de 2 cases 😅\nExplication : ${explanation(q)}" } } }, colors = ButtonDefaults.buttonColors(containerColor = if (selected == i) Color(0xFFD64545) else Color.White, contentColor = Color(0xFF17233C)), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text(answer) } } }
            if (selected >= 0 && !showAdvancePopup) Button(onClick = { selected = -1; rolled = false; dice = 0; questionIndex = (questionIndex + 1) % questionOrder.size; message = "La prochaine case vous attend ! 🎲" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD64545)), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Continuer vers la prochaine case →", color = Color.White) }
        } }
        if (showAdvancePopup) AlertDialog(onDismissRequest = {}, title = { Text("🎉 Bonne réponse !") }, text = { Text("Votre pion avance de $dice cases.\n\nPrêt pour le départ ? 🧑‍🎓") }, confirmButton = { Button(onClick = { position = (position + dice).coerceAtMost(FINAL_CASE); showAdvancePopup = false; selected = -1; rolled = false; dice = 0; questionIndex = (questionIndex + 1) % questionOrder.size; message = "Le pion est arrivé ! Lancez le dé pour la prochaine case 🎲" }) { Text("🚶 Avancer de $dice cases") } })
        AnimatedVisibility(finished, enter = fadeIn(tween(700)) + slideInVertically(initialOffsetY = { it })) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Vous avez atteint la cérémonie de naturalisation 🇫🇷", fontSize = 20.sp, color = Color(0xFF17233C), fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp)); Text("Résultat : $correct / $total — ${if (total == 0) 0 else correct * 100 / total}%", fontSize = 19.sp, color = Color(0xFFD64545), fontWeight = FontWeight.Bold); Text(if (total > 0 && correct * 100 / total >= 80) "Bravo, vous êtes prêt(e) ! 🎉" else "Encore un petit entraînement et ce sera gagné 💪", color = Color(0xFF657087), modifier = Modifier.padding(10.dp)); Button(onClick = { position = 0; score = 0; combo = 0; correct = 0; total = 0; dice = 0; rolled = false; rolling = false; selected = -1; questionIndex = 0; questionOrder = questions.indices.shuffled(); message = "Nouvelle partie : lancez le dé ! 🎲" }, modifier = Modifier.padding(top = 16.dp)) { Text("Rejouer") } } }
    } }
}

@Composable private fun FrenchBadge(icon: String, label: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.width(80.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 7.dp).fillMaxWidth()) {
            Text(icon, fontSize = 22.sp); Text(label, fontSize = 10.sp, color = Color(0xFF17233C), fontWeight = FontWeight.Bold)
        }
    }
}

private fun house(position: Int): String = when (position % 7) { 0 -> "RÉPUBLIQUE"; 1 -> "INSTITUTIONS"; 2 -> "HISTOIRE"; 3 -> "DROITS / DEVOIRS"; 4 -> "VIE PRATIQUE"; 5 -> "PIÈGE"; else -> "BONUS +2" }
private fun explanation(q: Question): String = when (q.kind) { "SITUATION" -> "La bonne réponse applique les principes républicains et les démarches de la vie quotidienne."; else -> "Cette notion fait partie des connaissances civiques essentielles en France." }
