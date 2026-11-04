package fr.audeladedonnees.examen.civique

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

private val Navy = Color(0xFF12233F)
private val Gold = Color(0xFFFFCE68)
private val Mint = Color(0xFF64D9BA)
private val Blue = Color(0xFF456CEA)
private val Red = Color(0xFFE86472)
private val chapters = listOf("La côte Atlantique", "Des Pyrénées aux Alpes", "Cap sur Paris")
val modes = listOf("Tour de France", "République", "Histoire & géographie", "Institutions", "Vie quotidienne", "La Marseillaise", "Grammaire", "Littérature", "Régions & paysages", "Gastronomie", "Examen civique")
private fun pool(mode: Int): List<Question> = (questions + extraQuestions + moreQuestions + hymnQuestions + (cultureBank + civicBank).map { Question(it.category, it.text, it.answers, 0) }).filter {
    when (mode) {
        1 -> it.kind in listOf("ESSENTIEL", "RÉPUBLIQUE", "PIÈGE")
        2 -> it.kind in listOf("HISTOIRE", "GÉOGRAPHIE")
        3 -> it.kind in listOf("INSTITUTIONS", "DROITS / DEVOIRS")
        4 -> it.kind in listOf("SITUATION", "VIE PRATIQUE", "BONUS")
        5 -> it.kind == "HYMNE"
        in 6..10 -> it.kind == modes[mode]
        else -> true
    }
}.distinctBy { it.text }

@Composable fun QuestApp() = WithPiano { QuestContent() }

@Composable private fun QuestContent() {
    var mode by remember { mutableIntStateOf(-1) }
    var hubTab by remember { mutableIntStateOf(0) }
    var session by remember { mutableIntStateOf(0) }
    var splash by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val store = remember { ProgressStore(context.applicationContext) }
    LaunchedEffect(Unit) { delay(1000); splash = false }
    MaterialTheme(colorScheme = darkColorScheme(primary = Gold, background = Navy, surface = Color(0xFF203653))) {
        CompositionLocalProvider(LocalProgress provides store) {
            HeartTicker()
            if (splash) Surface(Modifier.fillMaxSize(), color = Navy) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🇫🇷", fontSize = 64.sp)
                    Text("PARCOURS", fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Text("CITOYEN", fontSize = 36.sp, color = Gold, fontWeight = FontWeight.Black)
                    Text("L’aventure commence ici", Modifier.padding(16.dp))
                }
            } else if (mode == -2) HymnScreen({ mode = -1 }, { mode = 5; session++ })
            else if (mode == -3) HubScreen(hubTab) { mode = -1 }
            else if (mode == -4) CivicMenu { route -> mode = when (route) { "civic-practice" -> -5; "civic-test" -> -6; else -> -1 }; session++ }
            else if (mode == -8 || mode == -9) OfficialTrackMenu(if (mode == -8) "resident" else "naturalisation") { route ->
                mode = when (route) { "resident-practice" -> -10; "resident-test" -> -11; "naturalisation-practice" -> -12; "naturalisation-test" -> -13; else -> -1 }
                session++
            }
            else if (mode in -13..-10) key(session) {
                val route = when (mode) { -10 -> "resident-practice"; -11 -> "resident-test"; -12 -> "naturalisation-practice"; else -> "naturalisation-test" }
                val parent = if (mode == -10 || mode == -11) -8 else -9
                BackHandler { mode = parent }
                AdditionalQuiz(route) { mode = parent }
            }
            else if (mode in -7..-5) key(session) {
                BackHandler { mode = if (mode == -7) -1 else -4 }
                AdditionalQuiz(when (mode) { -5 -> "civic-practice"; -6 -> "civic-test"; else -> "photos" }) { mode = if (mode == -7) -1 else -4 }
            }
            else if (mode < 0) QuestMenu({ mode = it; session++ }, { hubTab = it; mode = -3 })
            else key(session) { Adventure(mode, { mode = -1 }, { session++ }) }
        }
    }
}

@Composable private fun QuestMenu(start: (Int) -> Unit, openHub: (Int) -> Unit) {
    val context = LocalContext.current
    val activity = androidx.activity.compose.LocalActivity.current
    val store = LocalProgress.current
    val scope = rememberCoroutineScope()
    var signingIn by remember { mutableStateOf(false) }
    val prefs = remember { context.getSharedPreferences("quest", 0) }
    val progress = store.progress
    ReviewPrompt()
    Surface(Modifier.fillMaxSize(), color = Navy) {
        Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth().height(3.dp)) {
                listOf(Blue, Color.White, Red).forEach { Box(Modifier.weight(1f).fillMaxHeight().background(it)) }
            }
            Text("🇫🇷  PARCOURS CITOYEN", color = Gold, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("La France vous\nattend !", fontSize = 38.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
            Text("${pool(0).size} questions • 3 cartes • 60 cases", color = Mint)
            Text("Un voyage pour apprendre, une case à la fois.")
            Card(onClick = { start(-4) }, shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Gold.copy(alpha = .6f), RoundedCornerShape(24.dp))) {
                Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF234676), Navy))).padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("À LA UNE  •  EXAMEN CIVIQUE CSP", color = Gold, fontSize = 11.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Text("Examen civique", fontSize = 30.sp, fontFamily = FontFamily.Serif, color = Color.White)
                    Text("${civicBank.size} questions de connaissance publiées par le ministère. Choix et corrections pédagogiques.", color = Color(0xFFD6E2F3), lineHeight = 22.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(onClick = { start(-5) }, label = { Text("Réviser", color = Color.White) })
                        SuggestionChip(onClick = { start(-6) }, label = { Text("40 questions · 45 min", color = Color.White) })
                    }
                    Text("Découvrir la préparation →", color = Gold, fontWeight = FontWeight.Bold)
                }
            }
            Card(onClick = { start(-8) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("CARTE DE RÉSIDENT", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${residentPrompts.size} questions publiées · ${residentBank.size} QCM pédagogiques", fontSize = 15.sp)
                }
            }
            Card(onClick = { start(-9) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("NATURALISATION", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${naturalisationPrompts.size} questions publiées · ${naturalisationBank.size} QCM pédagogiques", fontSize = 15.sp)
                }
            }
            GameBar()
            LevelBar()
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF203653)), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    GoalRing(84.dp, progress.goalRatio, progress.goalDone, progress.xpToday, progress.goal)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Objectif du jour : ${progress.xpToday} / ${progress.goal} XP", fontWeight = FontWeight.Bold)
                        HeartsIndicator()
                        Text("Ligue ${progress.leagueName} ${progress.leagueIcon} • rang #${progress.weekRank} sur 8", fontSize = 12.sp, color = Color(0xFFBAC9DF))
                    }
                }
            }
            PianoSwitch()
            if (PlayBackend.configured()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (PlayBackend.connected) "● ${PlayBackend.playerName ?: "Connecté à Play Games"}" else "Google Play Games", fontWeight = FontWeight.Bold)
                        Text(if (PlayBackend.syncing) "Synchronisation en cours…" else if (PlayBackend.connected) "Vos succès et classements vous accompagnent." else "Connectez votre profil pour retrouver vos succès et classements.", fontSize = 12.sp, color = Color(0xFFBAC9DF))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!PlayBackend.connected) {
                                Button(onClick = {
                                    val a = activity ?: return@Button
                                    signingIn = true
                                    scope.launch {
                                        try {
                                            if (PlayBackend.signIn(a)) {
                                                signingIn = false
                                                withTimeoutOrNull(15_000) { PlayBackend.pullCloud(a, store) }
                                            }
                                        }
                                        finally { signingIn = false }
                                    }
                                }, enabled = !signingIn) { Text(if (signingIn) "Connexion…" else "Se connecter") }
                            } else {
                                OutlinedButton(onClick = { activity?.let { PlayBackend.showLeaderboards(it) } }) { Text("Classements") }
                                OutlinedButton(onClick = { activity?.let { PlayBackend.showAchievements(it) } }) { Text("Succès") }

                            }
                        }
                        PlayBackend.lastError?.let { Text(it, fontSize = 11.sp) }
                    }
                }
            }
            Card(onClick = { start(-2) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("♫  L’hymne de la France", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("Piano, histoire, paroles officielles et quiz →")
                }
            }
            Text("Choisissez votre aventure", fontFamily = FontFamily.Serif, fontSize = 25.sp, color = Color.White)
            modes.forEachIndexed { i, title ->
                if (i == 5) return@forEachIndexed
                Card(onClick = { start(i) }, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().border(1.dp, Gold.copy(alpha = .12f), RoundedCornerShape(20.dp)), colors = CardDefaults.cardColors(containerColor = if (i == 0) Blue else Color(0xFF203653))) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${listOf("🎲", "🇫🇷", "🧭", "🏛", "🤝", "♫", "✍️", "📚", "🗺️", "🥐", "🏛️")[i]}  $title   →", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text(if (i == 0) "Grande aventure • 15 réponses minimum" else "Quiz dédié • ${pool(i).size} questions disponibles")
                        val best = prefs.getInt("best$i", -1)
                        if (best >= 0) Text("Votre record : $best %", color = Gold)
                        Crowns(progress.crowns[i])
                    }
                }
            }
            Card(onClick = { start(-7) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("📷  La France en images", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("Quiz photo • lieux et spécialités →")
                }
            }
            Card(onClick = { openHub(0) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("🏅  Succès et quêtes", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("${progress.badges.size} / ${allBadges.size} succès • objectif atteint ${progress.goalDays} jour(s) • ${progress.gems} 💎")
                }
            }
            Card(onClick = { openHub(2) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("🏆  Ligue ${progress.leagueName}", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("${progress.weekXp} XP cette semaine • rang #${progress.weekRank} sur 8 →")
                }
            }
            Text("Vos étapes découvertes", fontWeight = FontWeight.Bold)
            val unlocked = prefs.getInt("chapter", 0)
            chapters.forEachIndexed { i, title -> Text("${if (i <= unlocked) "🗺" else "🔒"}  $title") }
            OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.service-public.gouv.fr/particuliers/vosdroits/R75684"))) }, modifier = Modifier.fillMaxWidth()) { Text("Trouver un centre d’examen ↗") }
            Text("Jeu d’entraînement indépendant. Le résultat du jeu n’est pas une décision de naturalisation.", fontSize = 12.sp, color = Color(0xFFBAC9DF))
        }
    }
}

private enum class Phase { READY, ROLLING, QUESTION, FEEDBACK, MOVING, END }

@Composable private fun Adventure(mode: Int, exit: () -> Unit, replay: () -> Unit) {
    BackHandler { exit() }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("quest", 0) }
    val deck = remember { pool(mode).shuffled().map { q ->
        val order = q.answers.indices.shuffled()
        q.copy(answers = order.map { q.answers[it] }, good = order.indexOf(q.good))
    } }
    var phase by remember { mutableStateOf(Phase.READY) }
    var index by remember { mutableIntStateOf(0) }
    var position by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var answered by remember { mutableIntStateOf(0) }
    var die by remember { mutableIntStateOf(1) }
    var chosen by remember { mutableIntStateOf(-1) }
    var viewed by remember { mutableIntStateOf(0) }
    var notice by remember { mutableStateOf("Lancez le dé pour commencer votre voyage.") }
    var stepInfo by remember { mutableStateOf<Int?>(null) }
    var runXp by remember { mutableIntStateOf(0) }
    var pendingXp by remember { mutableStateOf(0) }
    var pendingMultiplier by remember { mutableIntStateOf(1) }
    var combo by remember { mutableIntStateOf(0) }
    var rewards by remember { mutableStateOf<List<GameEvent>>(emptyList()) }
    var levelUp by remember { mutableStateOf<Int?>(null) }
    var outOfHearts by remember { mutableStateOf(false) }
    val activity = androidx.activity.compose.LocalActivity.current
    val store = LocalProgress.current
    val progress = store.progress
    val motion = remember { Animatable(0f) }
    val tumble = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val q = deck[index.coerceAtMost(deck.lastIndex)]
    val minimum = minOf(15, deck.size)
    val chapter = (position / 20).coerceAtMost(2)
    val rate = if (answered == 0) 0 else correct * 100 / answered
    val bonus = (position + die).coerceAtMost(60) % 7 == 0
    fun rollDice() {
        if (phase != Phase.READY) return
        if (progress.hearts <= 0) { outOfHearts = true; return }
        phase = Phase.ROLLING
        viewed = chapter
        scope.launch {
            tumble.snapTo(0f)
            die = Random.nextInt(1, 7)
            notice = "Le dé s’envole…"
            tumble.animateTo(1f, tween(1800, easing = CubicBezierEasing(.15f, .0f, .15f, 1f)))
            notice = "Vous avez obtenu $die !"
            delay(550)
            phase = Phase.QUESTION
            notice = "Dé : $die • Répondez pour avancer."
        }
    }
    fun closeRun() {
        val outcome = store.finishRun(mode, correct, answered)
        rewards = rewards + outcome.events
        // Backend Google : succès + classements + cloud après chaque partie.
        activity?.let { a ->
            outcome.events.filterIsInstance<GameEvent.NewBadge>().forEach { PlayBackend.unlock(a, it.badge.id) }
            PlayBackend.pushScores(a, store.progress.xp, store.progress.bestStreak)
            PlayBackend.pushCloud(a, store)
        }
        if (outcome.perfect) notice = "Parcours sans faute ! +${outcome.gems} 💎"
        phase = Phase.END
    }
    fun endTurn() {
        if ((position >= 60 && answered >= minimum) || index == deck.lastIndex) closeRun()
        else { index++; chosen = -1; phase = Phase.READY }
    }
    Scaffold(containerColor = Navy, bottomBar = {
        Surface(color = Color(0xFF203653)) {
            Column(Modifier.navigationBarsPadding().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(notice, fontSize = 13.sp, maxLines = 3)
                if (phase == Phase.END) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = replay, modifier = Modifier.weight(1f)) { Text("Rejouer") }
                    OutlinedButton(onClick = exit, modifier = Modifier.weight(1f)) { Text("Menu") }
                } else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PivotingDice(die, if (phase == Phase.ROLLING) tumble.value else 0f, phase == Phase.READY, ::rollDice)
                    Button(enabled = phase == Phase.READY, modifier = Modifier.weight(1f).height(54.dp), onClick = ::rollDice) { Text(when (phase) { Phase.ROLLING -> "Le dé roule…"; Phase.MOVING -> "En route…"; Phase.QUESTION, Phase.FEEDBACK -> "Question en cours"; else -> "Lancer le dé" }, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }) { padding ->
        Column(Modifier.padding(padding).statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = exit) { Text("‹ Menu") }
                Text(modes[mode], modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, maxLines = 2)
                val piano = LocalPiano.current
                TextButton(onClick = { piano.enable(!piano.enabled) }) { Text(if (piano.enabled) "Son : oui" else "Son : non") }
            }
            Text(chapters[viewed], fontWeight = FontWeight.Black, fontSize = 26.sp)
            QuestMap(viewed, motion.value, phase == Phase.MOVING) { stepInfo = it }
            Text("Case $position / 60   •   $answered réponses   •   $rate %   •   ⭐ $runXp XP", color = Mint)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeartsIndicator()
                if (combo >= 3) Text("Combo ×${Rules.multiplier(combo)} 🔥", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chapters.forEachIndexed { i, _ ->
                    val unlocked = maxOf(chapter, prefs.getInt("chapter", 0))
                    FilterChip(selected = viewed == i, onClick = { viewed = i }, enabled = i <= unlocked, label = { Text("${if (i <= unlocked) "🗺" else "🔒"} ${i + 1}") })
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column { Text("Objectif : 80 %", color = Gold, fontWeight = FontWeight.Bold); Text("$minimum réponses minimum\nBonne réponse : +10 XP et +$die cases\nErreur : −1 cœur ❤️ et −2 cases", fontSize = 13.sp) }
            }
            if (phase == Phase.END) Card(colors = CardDefaults.cardColors(containerColor = if (rate >= 80) Color(0xFF18574F) else Color(0xFF573944))) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (rate >= 80) "🎉 Défi réussi !" else "💪 Défi non réussi", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("$correct bonnes réponses sur $answered • $rate %")
                    Text(if (position >= 60) "Vous êtes arrivé à la cérémonie du jeu." else "Toutes les questions de ce thème ont été jouées.")
                    Text(if (rate >= 80) "Bravo pour ce parcours !" else "Les erreurs font partie de l’apprentissage. Rejouez pour progresser.")
                    Text("Gains de la partie : +$runXp XP", fontWeight = FontWeight.Bold, color = Gold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Couronnes du mode :", fontSize = 13.sp)
                        Crowns(progress.crowns[mode])
                    }
                    Text("🔥 Série : ${progress.streak} jour(s) • objectif du jour ${progress.xpToday}/${progress.goal} XP • ${progress.gems} 💎", fontSize = 13.sp)
                    HeartsIndicator()
                }
            } else Text("Touchez une case pour découvrir son thème. Les cartes suivantes se débloquent à mesure que vous avancez.", color = Color(0xFFBAC9DF), fontSize = 13.sp)
        }
    }
    if (phase == Phase.QUESTION || phase == Phase.FEEDBACK) AlertDialog(
        onDismissRequest = {}, title = { Text(if (phase == Phase.QUESTION) "${index + 1}/${deck.size} • ${q.kind}" else if (chosen == q.good) "🎉 Bien joué !" else "Pas tout à fait…") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(q.text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            if (phase == Phase.QUESTION) q.answers.forEachIndexed { i, answer ->
                OutlinedButton(onClick = {
                    chosen = i; answered++
                    val good = i == q.good
                    if (good) { correct++; combo++ } else combo = 0
                    val outcome = store.answer(good, combo, bonus)
                    runXp += outcome.xp
                    pendingXp = outcome.xp
                    pendingMultiplier = outcome.multiplier
                    rewards = rewards + outcome.events
                    outcome.events.filterIsInstance<GameEvent.LevelUp>().lastOrNull()?.let { levelUp = it.level }
                    outOfHearts = outcome.outOfHearts
                    phase = Phase.FEEDBACK
                }, modifier = Modifier.fillMaxWidth()) { Text(answer, modifier = Modifier.padding(vertical = 5.dp)) }
            } else {
                Text("À retenir : ${q.answers[q.good]}", color = Mint, fontWeight = FontWeight.Bold)
                (cultureBank + civicBank).firstOrNull { it.text == q.text }?.let { Text(it.detail) }
                Text(if (chosen == q.good) "+$pendingXp XP${if (bonus) " • case bonus : +2 cases et +${Rules.BONUS_CASE_XP} XP !" else " • votre compagnon est prêt à repartir."}${if (pendingMultiplier > 1) " • combo ×$pendingMultiplier" else ""}" else "−1 cœur ❤️ : il vous en reste ${progress.hearts} sur ${Rules.MAX_HEARTS}. Petite marche arrière de 2 cases.")
            }
        } }, confirmButton = {
            if (phase == Phase.FEEDBACK) Button(onClick = {
                phase = Phase.MOVING
                val target = (position + if (chosen == q.good) die + if (bonus) 2 else 0 else -2).coerceIn(0, 60)
                scope.launch {
                    while (position != target) {
                        val next = position + if (target > position) 1 else -1
                        viewed = (minOf(position, next) / 20).coerceAtMost(2)
                        motion.animateTo(next.toFloat(), tween(320, easing = LinearEasing)); position = next
                        delay(45)
                    }
                    viewed = (position / 20).coerceAtMost(2)
                    prefs.edit().putInt("chapter", maxOf(viewed, prefs.getInt("chapter", 0))).apply()
                    notice = if (position >= 60 && answered < minimum) "Arrivée atteinte ! Encore ${minimum - answered} réponses pour le bilan." else "Case $position : prêt pour le prochain lancer !"
                    endTurn()
                }
            }) { Text(if (chosen == q.good) "Avancer →" else "Reculer et continuer") }
        })
    stepInfo?.let { number -> AlertDialog(onDismissRequest = { stepInfo = null }, title = { Text("Case $number") }, text = { Text(if (number % 7 == 0) "Bonus : une bonne réponse donne 2 cases supplémentaires." else "${modes[mode]} • ${chapters[(number / 20).coerceAtMost(2)]}\nAvancez en répondant aux questions de votre parcours.") }, confirmButton = { TextButton(onClick = { stepInfo = null }) { Text("Fermer") } }) }
    levelUp?.let { LevelUpDialog(it) { levelUp = null } }
    RewardDialog(rewards) { rewards = emptyList() }
    if (outOfHearts) HeartsDialog(progress, { if (store.refillHearts()) outOfHearts = false }, { outOfHearts = false; closeRun() })
}

private fun point(n: Float): Offset {
    val a = n.coerceIn(0f, 20f)
    val row = (a / 5).toInt().coerceAtMost(3)
    val fraction = (a - row * 5) / 5f
    return Offset(if (row % 2 == 0) .16f + .68f * fraction else .84f - .68f * fraction, .81f - .19f * (row + fraction))
}

@Composable private fun QuestMap(chapter: Int, progress: Float, moving: Boolean, tap: (Int) -> Unit) {
    val local = (progress - chapter * 20).coerceIn(0f, 20f)
    val mapHeight = (LocalConfiguration.current.screenHeightDp.dp * .36f).coerceIn(160.dp, 260.dp)
    val palette = listOf(Color(0xFFB8E0CB), Color(0xFFE5D6B3), Color(0xFFC2D7ED))
    Card(shape = RoundedCornerShape(26.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(mapHeight).background(Color(0xFF85BCCD))) {
            Canvas(Modifier.fillMaxSize()) {
                val land = Path().apply {
                    moveTo(size.width * .12f, size.height * .21f)
                    listOf(.31f to .23f, .39f to .10f, .57f to .07f, .69f to .20f, .9f to .28f, .84f to .49f, .91f to .69f, .70f to .86f, .49f to .82f, .31f to .93f, .19f to .72f, .22f to .47f, .06f to .32f).forEach { (x,y) -> lineTo(x * size.width, y * size.height) }; close()
                }
                drawPath(land, palette[chapter]); drawPath(land, Color.White.copy(alpha = .5f), style = Stroke(3.dp.toPx()))
                // Landmarks are drawn natively and scale with the map.
                val cx = size.width * .50f
                val cy = size.height * .48f
                val unit = size.width * .13f
                if (chapter == 2) {
                    drawLine(Navy, Offset(cx, cy - unit), Offset(cx - unit * .55f, cy + unit), 5.dp.toPx())
                    drawLine(Navy, Offset(cx, cy - unit), Offset(cx + unit * .55f, cy + unit), 5.dp.toPx())
                    drawLine(Navy, Offset(cx - unit * .3f, cy), Offset(cx + unit * .3f, cy), 4.dp.toPx())
                    drawLine(Navy, Offset(cx - unit * .45f, cy + unit * .6f), Offset(cx + unit * .45f, cy + unit * .6f), 4.dp.toPx())
                } else if (chapter == 1) {
                    val mountain = Path().apply { moveTo(cx - unit, cy + unit); lineTo(cx, cy - unit); lineTo(cx + unit, cy + unit); close() }
                    drawPath(mountain, Color(0xFF6D8B95))
                    val snow = Path().apply { moveTo(cx - unit * .4f, cy - unit * .2f); lineTo(cx, cy - unit); lineTo(cx + unit * .4f, cy - unit * .2f); close() }
                    drawPath(snow, Color.White)
                } else {
                    drawRect(Color.White, Offset(cx - unit * .25f, cy - unit), androidx.compose.ui.geometry.Size(unit * .5f, unit * 2))
                    drawRect(Red, Offset(cx - unit * .25f, cy - unit * .2f), androidx.compose.ui.geometry.Size(unit * .5f, unit * .5f))
                    drawCircle(Gold, unit * .3f, Offset(cx, cy - unit))
                    drawLine(Gold.copy(alpha = .6f), Offset(cx, cy - unit), Offset(cx + unit, cy - unit * 1.3f), 7.dp.toPx())
                }
                val trail = Path()
                (0..20).forEach { i -> val p = point(i.toFloat()); if (i == 0) trail.moveTo(p.x * size.width, p.y * size.height) else trail.lineTo(p.x * size.width, p.y * size.height) }
                drawPath(trail, Color(0xFF608574), style = Stroke(18.dp.toPx(), cap = StrokeCap.Round))
                drawPath(trail, Color(0xFFFFF4D8), style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
            }
            Text("🇫🇷", Modifier.align(Alignment.TopEnd).padding(16.dp), fontSize = 32.sp)
            Text(listOf("ATLANTIQUE", "LES ALPES", "PARIS")[chapter], Modifier.align(Alignment.TopStart).padding(16.dp), color = Navy, fontWeight = FontWeight.Black)
            (0..20).forEach { i ->
                val p = point(i.toFloat()); val global = chapter * 20 + i
                Box(Modifier.offset(maxWidth * p.x - 13.dp, maxHeight * p.y - 13.dp).size(26.dp).background(if (global <= progress) Gold else Color.White, RoundedCornerShape(9.dp)).clickable { tap(global) }, contentAlignment = Alignment.Center) {
                    Text(if (global % 7 == 0 && global > 0) "★" else "$global", color = Navy, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
            if (progress in chapter * 20f..(chapter + 1) * 20f) {
                val p = point(local)
                val bounce = if (moving) abs(sin(local * PI)).toFloat() * 10 else 0f
                Canvas(Modifier.offset(maxWidth * p.x - 16.dp, maxHeight * p.y - 4.dp).size(32.dp, 8.dp)) {
                    drawOval(Navy.copy(alpha = .22f - bounce * .009f))
                }
                Canvas(Modifier.offset((maxWidth * p.x - 22.dp).coerceIn(4.dp, maxWidth - 48.dp), (maxHeight * p.y - 54.dp - bounce.dp).coerceIn(4.dp, maxHeight - 60.dp)).size(44.dp, 56.dp).graphicsLayer {
                    rotationZ = if (moving) sin(local * PI * 2).toFloat() * 7f else 0f
                    scaleY = if (moving) 1f + sin(local * PI).toFloat().let { abs(it) } * .06f else 1f
                }.semantics { contentDescription = "Personnage sur la case ${progress.toInt()}" }) {
                    drawRoundRect(Color.White.copy(alpha = .9f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Fill)
                    val gait = if (moving) sin(local * PI * 2).toFloat() * 6.dp.toPx() else 0f
                    drawLine(Navy, Offset(size.width * .42f, size.height * .66f), Offset(size.width * .3f + gait, size.height * .92f), 5.dp.toPx(), StrokeCap.Round)
                    drawLine(Navy, Offset(size.width * .58f, size.height * .66f), Offset(size.width * .7f - gait, size.height * .92f), 5.dp.toPx(), StrokeCap.Round)
                    drawRoundRect(Blue, Offset(size.width * .22f, size.height * .35f), androidx.compose.ui.geometry.Size(size.width * .56f, size.height * .38f), androidx.compose.ui.geometry.CornerRadius(8f))
                    drawCircle(Color(0xFFF2BD8A), size.width * .26f, Offset(size.width * .5f, size.height * .24f))
                    drawLine(Red, Offset(size.width * .23f, size.height * .39f), Offset(size.width * .82f, size.height * .46f), 4.dp.toPx(), StrokeCap.Round)
                    drawCircle(Navy, 1.3.dp.toPx(), Offset(size.width * .6f, size.height * .23f))
                }
            }
            Text("Carte de jeu stylisée • étape ${chapter + 1}/3", Modifier.align(Alignment.BottomCenter).padding(12.dp), fontSize = 11.sp, color = Navy)
        }
    }
}

@Composable private fun Dice(value: Int, turn: Float) {
    Canvas(Modifier.size(72.dp).graphicsLayer { rotationZ = turn * 720f; translationY = -abs(sin(turn * PI * 3)).toFloat() * 35; scaleX = 1 + sin(turn * PI).toFloat() * .15f; scaleY = scaleX }.semantics { contentDescription = "Dé : $value" }) {
        drawRoundRect(Color.White, cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()))
        val dots = when (value) { 1 -> listOf(1 to 1); 2 -> listOf(0 to 0, 2 to 2); 3 -> listOf(0 to 0, 1 to 1, 2 to 2); 4 -> listOf(0 to 0, 2 to 0, 0 to 2, 2 to 2); 5 -> listOf(0 to 0, 2 to 0, 1 to 1, 0 to 2, 2 to 2); else -> listOf(0 to 0, 2 to 0, 0 to 1, 2 to 1, 0 to 2, 2 to 2) }
        dots.forEach { (x,y) -> drawCircle(Navy, 5.dp.toPx(), Offset(size.width * (.25f + x * .25f), size.height * (.25f + y * .25f))) }
    }
}
