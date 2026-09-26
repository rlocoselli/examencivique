package fr.audeladedonnees.examen.civique

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Panel = Color(0xFF203653)
private val Track = Color(0xFF2C4568)
private val Gold = Color(0xFFFFCE68)
private val Mint = Color(0xFF64D9BA)
private val Blue = Color(0xFF456CEA)
private val Red = Color(0xFFE86472)
private val Muted = Color(0xFFBAC9DF)

fun heartDelay(ms: Long): String = when {
    ms <= 0 -> "prêt"
    ms < 60_000 -> "moins d’1 min"
    else -> "${ms / 60_000} min"
}

/** Rafraîchit la progression (cœurs régénérés, nouveau jour) toutes les [interval] millisecondes. */
@Composable fun HeartTicker(interval: Long = 15_000L) {
    val store = LocalProgress.current
    LaunchedEffect(store) { while (true) { delay(interval); store.refresh() } }
}

@Composable fun StatChip(icon: String, text: String, tint: Color) {
    Surface(color = Panel, shape = RoundedCornerShape(14.dp)) {
        Text("$icon $text", Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = tint, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

/** En-tête façon Duolingo : flamme, jetons et cœurs toujours visibles. */
@Composable fun GameBar() {
    val p = LocalProgress.current.progress
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatChip("🔥", if (p.freezes > 0) "${p.streak} ❄${p.freezes}" else "${p.streak}", Gold)
        StatChip("💎", "${p.gems}", Mint)
        StatChip("❤️", "${p.hearts}/${Rules.MAX_HEARTS}", Red)
    }
}

@Composable fun Bar(ratio: Float, color: Color, height: Dp = 10.dp, track: Color = Track) {
    Box(Modifier.fillMaxWidth().height(height).background(track, RoundedCornerShape(height / 2))) {
        Box(Modifier.fillMaxWidth(ratio.coerceIn(0f, 1f)).fillMaxHeight().background(color, RoundedCornerShape(height / 2)))
    }
}

@Composable fun LevelBar() {
    val p = LocalProgress.current.progress
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Niveau ${p.level.number}", color = Gold, fontWeight = FontWeight.Black)
            Text("${p.level.into} / ${p.level.needed} XP avant le niveau ${p.level.number + 1}", fontSize = 12.sp, color = Muted)
        }
        Bar(p.level.ratio, Gold)
    }
}

/** Cœurs restants et compte à rebours du prochain cœur. */
@Composable fun HeartsIndicator() {
    val p = LocalProgress.current.progress
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("❤️".repeat(p.hearts) + "🤍".repeat(Rules.MAX_HEARTS - p.hearts), fontSize = 15.sp)
        Text(if (p.fullHearts) "Cœurs pleins" else "Prochain cœur dans ${heartDelay(p.nextHeartMs)}", fontSize = 12.sp, color = Muted)
    }
}

/** Anneau d’objectif quotidien, comme le compteur du jour de Duolingo. */
@Composable fun GoalRing(diameter: Dp = 104.dp, ratio: Float, done: Boolean, xpToday: Int, goal: Int) {
    Box(Modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = this.size.minDimension * .13f
            val inset = stroke / 2
            val arc = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(Track, 0f, 360f, false, Offset(inset, inset), arc, style = Stroke(stroke))
            drawArc(if (done) Mint else Gold, -90f, 360f * ratio.coerceIn(0f, 1f), false, Offset(inset, inset), arc, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$xpToday", fontSize = 24.sp, fontWeight = FontWeight.Black, color = if (done) Mint else Gold)
            Text("/ $goal XP", fontSize = 12.sp, color = Muted)
        }
    }
}

/** Objectif du jour : anneau, palier à choisir et achat d’un gel de série. */
@Composable fun DailyGoalCard() {
    val store = LocalProgress.current
    val p = store.progress
    Card(colors = CardDefaults.cardColors(containerColor = Panel), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Objectif du jour", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GoalRing(ratio = p.goalRatio, done = p.goalDone, xpToday = p.xpToday, goal = p.goal)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        if (p.goalDone) "Objectif atteint, bravo ! 🎉" else "Encore ${(p.goal - p.xpToday).coerceAtLeast(0)} XP pour aujourd’hui",
                        fontWeight = FontWeight.Bold, color = if (p.goalDone) Mint else Gold
                    )
                    Text("Objectifs atteints : ${p.goalDays} jour(s)", fontSize = 12.sp, color = Muted)
                    Text("Série : ${p.streak} jour(s) 🔥 • record ${p.bestStreak}", fontSize = 12.sp, color = Muted)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Rules.goals.forEach { goal -> FilterChip(selected = p.goal == goal, onClick = { store.setGoal(goal) }, label = { Text("$goal XP") }) }
            }
            OutlinedButton(onClick = { store.buyFreeze() }, enabled = p.gems >= Rules.FREEZE_COST && p.freezes < 2, modifier = Modifier.fillMaxWidth()) {
                Text("❄️ Gel de série • ${Rules.FREEZE_COST} 💎  (${p.freezes}/2)")
            }
        }
    }
}

@Composable fun Crowns(count: Int, slots: Int = 3) {
    Text(List(slots) { if (it < count) "👑" else "•" }.joinToString(" "), fontSize = 13.sp, color = if (count > 0) Gold else Muted)
}

@Composable fun StatTile(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), modifier = modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("$icon $value", fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(label, fontSize = 11.sp, color = Muted)
        }
    }
}

@Composable fun StatTiles(p: PlayerProgress) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("⭐", "XP au total", "${p.xp}", Modifier.weight(1f))
            StatTile("✅", "Bonnes réponses", "${p.totalCorrect}", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("🚩", "Parties terminées", "${p.totalRuns}", Modifier.weight(1f))
            StatTile("💯", "Sans faute", "${p.perfectRuns}", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("🔥", "Meilleure série", "${p.bestStreak} j", Modifier.weight(1f))
            StatTile("🎯", "Réponses données", "${p.totalAnswered}", Modifier.weight(1f))
        }
    }
}

@Composable fun BadgeTile(badge: Badge, earned: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = if (earned) Panel else Color(0xFF1A2E4B)), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (earned) badge.icon else "🔒", fontSize = 26.sp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(badge.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (earned) Gold else Muted)
                Text(badge.detail, fontSize = 11.sp, color = Muted)
            }
        }
    }
}

@Composable fun BadgeGrid(p: PlayerProgress) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${p.badges.size} / ${allBadges.size} succès débloqués", color = Mint, fontSize = 13.sp)
        allBadges.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { badge -> Box(Modifier.weight(1f)) { BadgeTile(badge, badge.id in p.badges) } }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable fun QuestCard(quest: DailyQuest, claimed: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${quest.icon}  ${quest.title}", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(if (claimed) "✅" else "${quest.gems} 💎", fontSize = 13.sp, color = Mint)
            }
            Bar(quest.ratio, if (quest.done) Mint else Gold, 9.dp)
            Text("${quest.progress} / ${quest.target}", fontSize = 12.sp, color = Muted)
        }
    }
}

@Composable fun QuestsTab(p: PlayerProgress) {
    val quests = dailyQuests(p)
    val allDone = quests.all { it.done }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Quêtes du jour", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Trois missions renouvelées chaque jour à minuit. Les récompenses sont créditées automatiquement.", fontSize = 12.sp, color = Muted)
        quests.forEach { QuestCard(it, it.id in p.claimed) }
        Card(colors = CardDefaults.cardColors(containerColor = if (allDone) Color(0xFF18574F) else Panel), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (allDone) "🎁 Coffre du jour ouvert (+${Rules.CHEST_GEMS} 💎)" else "🎁 Coffre du jour : ${Rules.CHEST_GEMS} 💎", fontWeight = FontWeight.Bold)
                Text(if (allDone) "Toutes les quêtes sont terminées, revenez demain !" else "Terminez les trois quêtes pour l’ouvrir.", fontSize = 12.sp, color = Muted)
            }
        }
    }
}



@Composable fun Leaderboard(p: PlayerProgress) {
    val scores = weekScores(p)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        scores.forEachIndexed { i, s ->
            val rank = i + 1
            Surface(
                color = if (s.me) Blue.copy(alpha = .35f) else Panel,
                shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("$rank", fontWeight = FontWeight.Black, color = if (rank <= 3) Gold else Muted, modifier = Modifier.width(22.dp))
                    Text(if (s.me) "🧑‍🎓  Vous" else "🙂  ${s.name}", modifier = Modifier.weight(1f), fontWeight = if (s.me) FontWeight.Black else FontWeight.Normal)
                    Text("${s.xp} XP", fontWeight = FontWeight.Bold, color = if (s.me) Gold else Muted)
                }
            }
        }
    }
}

@Composable fun LeagueTab(p: PlayerProgress) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Ligue ${p.leagueName} ${p.leagueIcon}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Gold)
        Text("Les XP gagnés cette semaine font monter votre classement. À la fin de la semaine, les 3 premiers montent d’une ligue et les 2 derniers descendent.", fontSize = 12.sp, color = Muted)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("🏆", "Rang actuel", "#${p.weekRank}", Modifier.weight(1f))
            StatTile("🔥", "Meilleur rang", if (p.bestRank > 0) "#${p.bestRank}" else "—", Modifier.weight(1f))
            StatTile("⭐", "XP de la semaine", "${p.weekXp}", Modifier.weight(1f))
        }
        Leaderboard(p)
        if (p.league < Rules.leagues.size - 1) {
            val next = Rules.leagues[p.league + 1]
            Text("Zone de promotion : restez dans les 3 premiers pour rejoindre la ligue $next ${Rules.leagueIcons[p.league + 1]}.", fontSize = 12.sp, color = Mint)
        }
        Text("Classement simulé hors ligne : les sept adversaires sont tirés au sort en début de semaine et restent les mêmes jusqu’au dimanche.", fontSize = 11.sp, color = Muted)
    }
}

/** Écran de célébration du passage de niveau, comme Duolingo. */
@Composable fun LevelUpDialog(level: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("✨ Niveau $level !", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Gold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Bravo, votre parcours citoyen progresse !", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Plus vous répondez juste, plus les niveaux montent vite. Continuez comme ça 🚀", fontSize = 13.sp, color = Muted)
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Continuer") } }
    )
}

/** Récompenses gagnées pendant la partie : succès, quêtes et coffre. */
@Composable fun RewardDialog(events: List<GameEvent>, onDismiss: () -> Unit) {
    val badges = events.filterIsInstance<GameEvent.NewBadge>().map { it.badge }
    val quests = events.filterIsInstance<GameEvent.QuestDone>().map { it.quest }
    val chest = events.filterIsInstance<GameEvent.Chest>().firstOrNull()
    if (badges.isEmpty() && quests.isEmpty() && chest == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🎁 Récompenses", fontWeight = FontWeight.Black) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                badges.forEach { Text("🏅 Succès « ${it.title} » • +${Rules.BADGE_GEMS} 💎", fontWeight = FontWeight.Bold, color = Gold) }
                quests.forEach { Text("${it.icon} Quête « ${it.title} » • +${it.gems} 💎", color = Mint) }
                chest?.let { Text("🎁 Coffre du jour • +${it.gems} 💎", color = Mint) }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Super !") } }
    )
}

/** Plus de cœurs : recharger avec des jetons ou arrêter la partie, comme Duolingo. */
@Composable fun HeartsDialog(p: PlayerProgress, onRefill: () -> Unit, onEnd: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("💔 Plus de cœurs", fontWeight = FontWeight.Black, color = Red) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Vous avez utilisé toutes vos chances pour cette session.", fontWeight = FontWeight.Bold)
                Text("Rechargez vos ${Rules.MAX_HEARTS} cœurs pour ${Rules.HEART_COST} 💎 (vous avez ${p.gems} 💎) ou terminez la partie : un cœur revient toutes les 30 minutes.", fontSize = 13.sp, color = Muted)
            }
        },
        confirmButton = { Button(onClick = onRefill, enabled = p.gems >= Rules.HEART_COST) { Text("Recharger • ${Rules.HEART_COST} 💎") } },
        dismissButton = { OutlinedButton(onClick = onEnd) { Text("Terminer la partie") } }
    )
}

/** Espace de progression : succès, quêtes du jour et ligue de la semaine. */
@Composable fun HubScreen(initialTab: Int, back: () -> Unit) {
    BackHandler(onBack = back)
    val p = LocalProgress.current.progress
    var tab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    val titles = listOf("Succès", "Quêtes", "Ligue")
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = back) { Text("‹ Menu principal") }
            Text("🏅 Ma progression", fontSize = 28.sp, fontWeight = FontWeight.Black)
            GameBar()
            LevelBar()
            TabRow(selectedTabIndex = tab) {
                titles.forEachIndexed { i, title -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) }) }
            }
            when (tab) {
                0 -> {
                    DailyGoalCard()
                    HeartsIndicator()
                    StatTiles(p)
                    BadgeGrid(p)
                }
                1 -> QuestsTab(p)
                else -> LeagueTab(p)
            }
        }
    }
}
