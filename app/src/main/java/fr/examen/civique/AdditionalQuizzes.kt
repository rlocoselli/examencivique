package fr.audeladedonnees.examen.civique

import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val Ink = Color.White
private val Cream = Color(0xFF12233F)
private val Brass = Color(0xFFFFCE68)
private val Rouge = Color(0xFFA43D46)
@Composable private fun Tricolore() {
    Row(Modifier.fillMaxWidth().height(3.dp)) { listOf(Color(0xFF456CEA), Color.White, Rouge).forEach { Box(Modifier.weight(1f).fillMaxHeight().background(it)) } }
}
@Composable private fun Page(title: String, subtitle: String, back: (() -> Unit)? = null, resetKey: Int = 0, content: @Composable ColumnScope.() -> Unit) {
    val scroll = rememberScrollState()
    LaunchedEffect(resetKey) { scroll.scrollTo(0) }
    Surface(Modifier.fillMaxSize(), color = Cream, contentColor = Ink) {
    Column(Modifier.safeDrawingPadding().verticalScroll(scroll)) {
        Tricolore()
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            if (back != null) TextButton(onClick = back, contentPadding = PaddingValues(0.dp)) { Text("‹ Retour") }
            Text("🇫🇷  PARCOURS CITOYEN", color = Brass, fontSize = 10.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
            Text(title, fontFamily = FontFamily.Default, fontSize = 36.sp, lineHeight = 40.sp, color = Ink)
            Text(subtitle, color = Color(0xFFBAC9DF), fontSize = 15.sp, lineHeight = 23.sp)
            content()
        }
    }
    }
}
@Composable private fun Photo(resource: Int, description: String, modifier: Modifier = Modifier) {
    Image(painterResource(resource), contentDescription = description, contentScale = ContentScale.Crop, modifier = modifier.fillMaxWidth().height(190.dp))
}
@Composable private fun EditorialCard(number: String, title: String, subtitle: String, click: () -> Unit) {
    Card(onClick = click, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF203653), contentColor = Ink), modifier = Modifier.fillMaxWidth().border(1.dp, Brass.copy(alpha = .22f), RoundedCornerShape(18.dp))) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(number, color = Brass, fontFamily = FontFamily.Default, fontSize = 15.sp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, fontFamily = FontFamily.Default, fontSize = 22.sp)
                Text(subtitle, color = Color(0xFFBAC9DF), fontSize = 12.sp, lineHeight = 18.sp)
            }
            Text("→", color = Brass, fontSize = 23.sp)
        }
    }
}
@Composable fun CivicMenu(open: (String) -> Unit) {
    BackHandler { open("home") }
    val context = LocalContext.current
    Page("Les clés de\nla citoyenneté.", "Examen civique · Carte de séjour pluriannuelle (CSP)", { open("home") }) {
        Text("${civicBank.size} questions officielles de connaissance", fontWeight = FontWeight.Bold)
        Text("Les intitulés proviennent du ministère de l’Intérieur. Les quatre choix et les corrections sont rédigés pour cet entraînement indépendant. Les mises en situation de l’examen ne sont pas publiques.", lineHeight = 23.sp)
        EditorialCard("01", "Réviser sans pression", "20 questions · correction après chaque réponse") { open("civic-practice") }
        EditorialCard("02", "Test de connaissances", "40 questions · 45 minutes · objectif 32/40") { open("civic-test") }
        Text("Ce test porte uniquement sur les connaissances publiées. Il ne reproduit pas l’intégralité de l’examen officiel, qui comporte aussi des mises en situation.", fontSize = 13.sp, color = Color(0xFFBAC9DF), lineHeight = 20.sp)
        OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(CIVIC_SOURCE))) }, modifier = Modifier.fillMaxWidth()) { Text("Liste officielle et fiches de révision ↗") }
        Text("Source vérifiée le 6 octobre 2026. Aucun résultat du jeu ne constitue une attestation officielle.", fontSize = 12.sp, color = Color(0xFFBAC9DF))
    }
}
private val photoBank = listOf(
    CultureQuestion("Photo", "Quel monument reconnaissez-vous ?", listOf("La tour Eiffel", "Le pont du Gard", "Le château de Chambord", "L’Arc de triomphe"), "La tour Eiffel a été inaugurée à Paris pour l’Exposition universelle de 1889.", "paris"),
    CultureQuestion("Photo", "Dans quelle région ce lieu se trouve-t-il ?", listOf("Normandie", "Bretagne", "Occitanie", "Grand Est"), "Le Mont-Saint-Michel se trouve dans la Manche, en Normandie.", "mont"),
    CultureQuestion("Photo", "Quelle viennoiserie voyez-vous ?", listOf("Un croissant", "Un canelé", "Une madeleine", "Un éclair"), "Le croissant est une viennoiserie de pâte levée feuilletée.", "croissant"),
    CultureQuestion("Photo", "Quel fleuve traverse cette ville ?", listOf("La Seine", "La Loire", "La Garonne", "Le Rhône"), "La Seine traverse Paris.", "paris"),
    CultureQuestion("Photo", "Quelle particularité caractérise la baie de ce monument ?", listOf("De très grandes marées", "L’absence de marées", "Une eau douce", "Une mer tropicale"), "La baie du Mont-Saint-Michel connaît des marées de très forte amplitude.", "mont"),
    CultureQuestion("Photo", "Quelle technique donne ses couches à cette viennoiserie ?", listOf("Le tourage de la pâte avec du beurre", "La cuisson à l’eau", "La fermentation du fromage", "Le pochage"), "Le tourage alterne des couches de pâte et de beurre pour produire le feuilletage.", "croissant")
)
@Composable fun AdditionalQuiz(route: String, exit: () -> Unit) {
    val exam = route == "civic-test"
    val civic = route.startsWith("civic")
    val source = if (civic) civicBank else if (route == "photos") photoBank else cultureBank.filter { it.category == route }
    val seed = rememberSaveable { Random.nextInt() }
    val deck = remember(route, seed) { source.shuffled(Random(seed)).take(if (exam) 40 else 20) }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableIntStateOf(-1) }
    var correct by rememberSaveable { mutableIntStateOf(0) }
    var mistakes by rememberSaveable { mutableStateOf("") }
    val deadline = rememberSaveable { SystemClock.elapsedRealtime() + 45 * 60 * 1000L }
    var remaining by remember { mutableLongStateOf((deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0) / 1000) }
    LaunchedEffect(exam) { if (exam) while (remaining > 0) { delay(1000); remaining = (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0) / 1000 } }
    val done = index >= deck.size || (exam && remaining == 0L)
    val context = LocalContext.current
    var reviewRecorded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(done) {
        if (done && !reviewRecorded) {
            val reviews = context.getSharedPreferences("review", 0)
            reviews.edit().putInt("quizRuns", reviews.getInt("quizRuns", 0) + 1)
                .putInt("quizAnswers", reviews.getInt("quizAnswers", 0) + index.coerceAtMost(deck.size)).apply()
            reviewRecorded = true
        }
    }
    Page(if (done) "Votre bilan." else if (civic) "Examen civique" else if (route == "photos") "La France en images" else route,
        if (done) "Chaque découverte compte." else "${index + 1} / ${deck.size}  ·  ${if (exam) "%02d:%02d".format(remaining / 60, remaining % 60) else "À votre rythme"}", exit, index) {
        if (done) {
            Text("$correct / ${deck.size}", fontSize = 54.sp, fontFamily = FontFamily.Default, color = Brass)
            Text(if (exam) { if (correct >= 32) "Objectif de connaissances atteint" else "Poursuivez vos révisions" } else "${correct * 100 / deck.size}% de bonnes réponses", fontSize = 22.sp, fontFamily = FontFamily.Default)
            Text(if (exam && index < deck.size) "Temps écoulé. Les questions sans réponse comptent comme incorrectes." else "Une nouvelle session vous proposera un ordre différent.")
            if (mistakes.isNotEmpty()) {
                Text("À revoir", fontFamily = FontFamily.Default, fontSize = 24.sp)
                mistakes.split('~').filter { it.isNotBlank() }.forEach { entry -> val q = deck[entry.toInt()]; Text(q.text, fontWeight = FontWeight.Bold); Text("${q.answers[0]} — ${q.detail}", lineHeight = 22.sp) }
            }
            Button(onClick = exit, colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Cream), modifier = Modifier.fillMaxWidth()) { Text("Retour à la collection") }
        } else {
            val q = deck[index]
            val order = remember(seed, index) { q.answers.indices.shuffled(Random(seed + index)) }
            LinearProgressIndicator(progress = { index.toFloat() / deck.size }, modifier = Modifier.fillMaxWidth(), color = Brass)
            q.photo?.let { photo -> Card(shape = RoundedCornerShape(18.dp)) { Photo(when(photo) { "mont" -> R.drawable.mont_photo; "croissant" -> R.drawable.croissant_photo; else -> R.drawable.paris_photo }, "Photographie à observer pour répondre au quiz") } }
            Text(q.text, fontFamily = FontFamily.Default, fontSize = 26.sp, lineHeight = 33.sp)
            order.forEachIndexed { letter, answer ->
                val revealed = selected >= 0 && !exam
                val answerColor = if (revealed && answer == 0) Color(0xFF18574F)
                    else if (revealed && selected == answer) Color(0xFF573944)
                    else if (selected == answer) Color(0xFF345B80) else Color(0xFF203653)
                OutlinedButton(onClick = {
                    if (selected < 0 && index < deck.size) { selected = answer; if (answer == 0) correct++ else mistakes += "$index~" }
                }, enabled = selected < 0, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = answerColor, disabledContainerColor = answerColor,
                    contentColor = Ink, disabledContentColor = Ink), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(('A' + letter).toString(), color = Brass, fontWeight = FontWeight.Bold)
                        Text(q.answers[answer], modifier = Modifier.weight(1f), lineHeight = 22.sp)
                        if (revealed && answer == 0) Text("✓")
                    }
                }
            }
            if (selected >= 0) {
                if (!exam) {
                    Text(if (selected == 0) "Très bien !" else "À retenir", color = if (selected == 0) Color(0xFF64D9BA) else Rouge, fontWeight = FontWeight.Bold)
                    Text(q.detail, lineHeight = 23.sp)
                }
                Button(onClick = { index++; selected = -1 }, colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Cream), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(if (index == deck.lastIndex) "Voir mon bilan →" else "Question suivante →") }
            }
            if (civic) TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(CIVIC_SOURCE))) }) { Text("Source : ministère de l’Intérieur ↗", fontSize = 12.sp) }
        }
    }
}
