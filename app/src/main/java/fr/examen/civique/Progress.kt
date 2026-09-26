package fr.audeladedonnees.examen.civique

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.TimeZone
import kotlin.random.Random

/**
 * Règles de gamification inspirées de Duolingo : XP, niveaux, cœurs, flamme (série),
 * ligues hebdomadaires, quêtes quotidiennes, jetons et succès. Tout reste hors ligne
 * dans les SharedPreferences « quest » déjà utilisées par le jeu.
 */
object Rules {
    const val MAX_HEARTS = 5
    const val HEART_REFILL_MS = 30 * 60 * 1000L
    const val HEART_COST = 50
    const val FREEZE_COST = 30
    const val BASE_XP = 10
    const val BONUS_CASE_XP = 5
    const val QUEST_GEMS = 5
    const val CHEST_GEMS = 20
    const val PERFECT_GEMS = 10
    const val BADGE_GEMS = 10
    val goals = listOf(10, 20, 30, 50)
    val leagues = listOf("Bronze", "Argent", "Or", "Diamant", "Rubis")
    val leagueIcons = listOf("🥉", "🥈", "🥇", "💠", "🔴")

    /** XP nécessaires pour passer du niveau [level] au suivant. */
    fun step(level: Int): Int = 25 + (level - 1) * 15

    /** Duolingo récompense les séries de bonnes réponses : ×2 dès 3, ×3 dès 5. */
    fun multiplier(combo: Int): Int = when {
        combo >= 5 -> 3
        combo >= 3 -> 2
        else -> 1
    }
}

data class Level(val number: Int, val into: Int, val needed: Int) {
    val ratio: Float get() = (into / needed.toFloat()).coerceIn(0f, 1f)
}

fun levelOf(xp: Int): Level {
    var number = 1
    var left = xp.coerceAtLeast(0)
    while (left >= Rules.step(number)) { left -= Rules.step(number); number++ }
    return Level(number, left, Rules.step(number))
}

/** Photographie complète de la progression, observée par Compose. */
data class PlayerProgress(
    val dayIndex: Long, val weekIndex: Long,
    val xp: Int, val gems: Int, val hearts: Int, val nextHeartMs: Long,
    val streak: Int, val bestStreak: Int, val freezes: Int,
    val xpToday: Int, val correctToday: Int, val answeredToday: Int, val bestComboToday: Int, val runsToday: Int,
    val goal: Int, val goalDays: Int,
    val league: Int, val weekXp: Int, val weekRank: Int, val bestRank: Int,
    val totalCorrect: Int, val totalAnswered: Int, val totalRuns: Int, val perfectRuns: Int,
    val crowns: List<Int>, val badges: Set<String>, val claimed: Set<String>
) {
    val level: Level get() = levelOf(xp)
    val goalRatio: Float get() = (xpToday / goal.toFloat()).coerceIn(0f, 1f)
    val goalDone: Boolean get() = xpToday >= goal
    val leagueName: String get() = Rules.leagues[league]
    val leagueIcon: String get() = Rules.leagueIcons[league]
    val fullHearts: Boolean get() = hearts >= Rules.MAX_HEARTS
    val bestCrowns: Int get() = crowns.maxOrNull() ?: 0
}

data class Badge(val id: String, val icon: String, val title: String, val detail: String)

val allBadges = listOf(
    Badge("first", "🐣", "Premier pas", "Répondre à sa première question"),
    Badge("correct10", "✅", "Dix bonnes réponses", "10 bonnes réponses au total"),
    Badge("correct50", "🧠", "Cinquante justes", "50 bonnes réponses au total"),
    Badge("correct250", "🎓", "Deux cent cinquante justes", "250 bonnes réponses au total"),
    Badge("xp150", "✨", "Cent cinquante XP", "Cumuler 150 XP"),
    Badge("level5", "🎖️", "Citoyen engagé", "Atteindre le niveau 5"),
    Badge("level10", "🏅", "Citoyen expert", "Atteindre le niveau 10"),
    Badge("streak3", "🔥", "Trois jours d’affilée", "Jouer 3 jours de suite"),
    Badge("streak7", "🗓️", "Une semaine entière", "Jouer 7 jours de suite"),
    Badge("perfect", "💯", "Sans faute", "Terminer une partie à 100 %"),
    Badge("runs10", "🚩", "Dix parcours", "Terminer 10 parties"),
    Badge("crown3", "👑", "Première couronne d’or", "Obtenir 3 couronnes dans un mode")
)

fun badgeEarned(id: String, p: PlayerProgress): Boolean = when (id) {
    "first" -> p.totalAnswered >= 1
    "correct10" -> p.totalCorrect >= 10
    "correct50" -> p.totalCorrect >= 50
    "correct250" -> p.totalCorrect >= 250
    "xp150" -> p.xp >= 150
    "level5" -> p.level.number >= 5
    "level10" -> p.level.number >= 10
    "streak3" -> p.bestStreak >= 3
    "streak7" -> p.bestStreak >= 7
    "perfect" -> p.perfectRuns >= 1
    "runs10" -> p.totalRuns >= 10
    "crown3" -> p.bestCrowns >= 3
    else -> false
}

private data class QuestTemplate(val id: String, val icon: String, val title: String, val target: Int, val value: (PlayerProgress) -> Int)

private val questTemplates = listOf(
    QuestTemplate("xp30", "✨", "Gagne 30 XP aujourd’hui", 30) { it.xpToday },
    QuestTemplate("correct5", "✅", "Donne 5 bonnes réponses", 5) { it.correctToday },
    QuestTemplate("combo4", "🔥", "Enchaîne 4 bonnes réponses", 4) { it.bestComboToday },
    QuestTemplate("answer10", "🎲", "Réponds à 10 questions", 10) { it.answeredToday },
    QuestTemplate("run1", "🏁", "Termine une partie", 1) { it.runsToday },
    QuestTemplate("xp60", "🌟", "Gagne 60 XP aujourd’hui", 60) { it.xpToday }
)

data class DailyQuest(val id: String, val icon: String, val title: String, val target: Int, val progress: Int, val gems: Int) {
    val done: Boolean get() = progress >= target
    val ratio: Float get() = (progress / target.toFloat()).coerceIn(0f, 1f)
}

/** Trois quêtes tirées du pool, renouvelées chaque jour et stables pendant toute la journée. */
fun dailyQuests(p: PlayerProgress): List<DailyQuest> {
    val size = questTemplates.size
    val offset = ((p.dayIndex % size) + size).toInt()
    return (0..2).map { i ->
        val t = questTemplates[(offset + i) % size]
        DailyQuest(t.id, t.icon, t.title, t.target, t.value(p).coerceAtMost(t.target), Rules.QUEST_GEMS)
    }
}

data class Score(val name: String, val xp: Int, val me: Boolean)

private val rivalNames = listOf("Camille", "Yanis", "Sofia", "Malik", "Élodie", "Tarek", "Louise")

/** Sept rivaux déterministes : le classement reste stable pendant toute la semaine, sans réseau. */
private fun rivals(league: Int, weekIndex: Long): List<Score> {
    val seed = weekIndex * 7919L + league * 131L
    return rivalNames.mapIndexed { i, name ->
        val r = Random(seed + i)
        Score(name, 25 + league * 25 + (6 - i) * 7 + r.nextInt(0, 45), false)
    }
}

fun weekScores(p: PlayerProgress): List<Score> =
    (rivals(p.league, p.weekIndex) + Score("Vous", p.weekXp, true))
        .sortedWith(compareByDescending<Score> { it.xp }.thenByDescending { it.me })

/** Rang de 1 à 8 : 1 + nombre de rivaux qui ont plus d’XP cette semaine. */
fun weekRank(league: Int, weekIndex: Long, weekXp: Int): Int =
    1 + rivals(league, weekIndex).count { it.xp > weekXp }


sealed interface GameEvent {
    data class LevelUp(val level: Int) : GameEvent
    data class NewBadge(val badge: Badge) : GameEvent
    data class QuestDone(val quest: DailyQuest) : GameEvent
    data class Chest(val gems: Int) : GameEvent
}

data class AnswerOutcome(val xp: Int, val multiplier: Int, val gems: Int, val hearts: Int, val events: List<GameEvent>) {
    val outOfHearts: Boolean get() = hearts <= 0
}

data class RunOutcome(val rate: Int, val perfect: Boolean, val gems: Int, val crowns: Int, val events: List<GameEvent>)

/**
 * Conserve la progression dans les SharedPreferences « quest » et l’expose à Compose
 * via [progress]. Chaque action écrit son résultat puis distribue quêtes, coffre et succès.
 */
class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("quest", 0)
    var progress by mutableStateOf(read())
        private set

    init { rollover(); progress = read() }

    // ---- temps : jour et semaine locales, sans dépendance à java.time (minSdk 24) ----
    private fun now() = System.currentTimeMillis()
    private fun today(): Long { val n = now(); return (n + TimeZone.getDefault().getOffset(n)) / 86_400_000L }
    private fun week(): Long = today() / 7L

    private fun currentHearts(): Int {
        val stored = prefs.getInt("hearts", Rules.MAX_HEARTS)
        if (stored >= Rules.MAX_HEARTS) return Rules.MAX_HEARTS
        val gained = ((now() - prefs.getLong("heartsAt", now())) / Rules.HEART_REFILL_MS).toInt()
        return (stored + gained).coerceIn(0, Rules.MAX_HEARTS)
    }

    private fun crownsOf(i: Int): Int {
        val runs = prefs.getInt("runs$i", 0)
        val best = prefs.getInt("best$i", -1)
        return when {
            runs == 0 || best < 0 -> 0
            best >= 90 && runs >= 3 -> 3
            best >= 80 -> 2
            best >= 50 -> 1
            else -> 0
        }
    }

    private fun read(): PlayerProgress {
        val hearts = currentHearts()
        val sinceHeart = (now() - prefs.getLong("heartsAt", now())) % Rules.HEART_REFILL_MS
        val league = prefs.getInt("league", 0)
        val weekXp = prefs.getInt("weekXp", 0)
        return PlayerProgress(
            dayIndex = today(), weekIndex = week(),
            xp = prefs.getInt("xp", 0), gems = prefs.getInt("gems", 50), hearts = hearts,
            nextHeartMs = if (hearts >= Rules.MAX_HEARTS) 0 else Rules.HEART_REFILL_MS - sinceHeart,
            streak = prefs.getInt("streak", 0), bestStreak = prefs.getInt("bestStreak", 0), freezes = prefs.getInt("freezes", 0),
            xpToday = prefs.getInt("xpToday", 0), correctToday = prefs.getInt("correctToday", 0),
            answeredToday = prefs.getInt("answeredToday", 0), bestComboToday = prefs.getInt("bestComboToday", 0),
            runsToday = prefs.getInt("runsToday", 0),
            goal = prefs.getInt("goal", 30), goalDays = prefs.getInt("goalDays", 0),
            league = league, weekXp = weekXp, weekRank = weekRank(league, week(), weekXp),
            bestRank = prefs.getInt("weekBestRank", 0),
            totalCorrect = prefs.getInt("totalCorrect", 0), totalAnswered = prefs.getInt("totalAnswered", 0),
            totalRuns = prefs.getInt("runs", 0), perfectRuns = prefs.getInt("perfectRuns", 0),
            crowns = (0 until 6).map { crownsOf(it) },
            badges = prefs.getStringSet("badges", emptySet()).orEmpty(),
            claimed = prefs.getStringSet("claimed", emptySet()).orEmpty()
        )
    }

    /** Recharge l’état : régénération des cœurs, nouveau jour, nouvelle semaine. */
    fun refresh() { rollover(); progress = read() }

    // ---- changements de jour et de semaine ----
    private fun rollover() {
        val day = today()
        if (prefs.getLong("day", -1L) != day) {
            val last = prefs.getLong("lastDay", -1L)
            if (last in 1..(day - 2)) {
                val missed = day - last - 1
                val freezes = prefs.getInt("freezes", 0)
                if (freezes >= missed) prefs.edit().putInt("freezes", (freezes - missed).toInt()).putLong("lastDay", day - 1).apply()
                else prefs.edit().putInt("streak", 0).putLong("lastDay", -1L).apply()
            }
            prefs.edit().putLong("day", day).putStringSet("claimed", emptySet())
                .putInt("xpToday", 0).putInt("correctToday", 0).putInt("answeredToday", 0)
                .putInt("bestComboToday", 0).putInt("runsToday", 0).apply()
        }
        val week = week()
        if (prefs.getLong("week", -1L) != week) {
            if (prefs.getLong("week", -1L) >= 0) settleLeague()
            prefs.edit().putLong("week", week).putInt("weekXp", 0).putInt("weekBestRank", 0).apply()
        }
    }

    /** À la fin d’une semaine : top 3 montent d’une ligue, les deux derniers descendent. */
    private fun settleLeague() {
        val league = prefs.getInt("league", 0)
        val rank = weekRank(league, prefs.getLong("week", 0L), prefs.getInt("weekXp", 0))
        val previous = prefs.getInt("weekBestRank", 0)
        val edit = prefs.edit().putInt("weekBestRank", minOf(rank, if (previous > 0) previous else 99))
        when {
            rank <= 3 -> edit.putInt("league", (league + 1).coerceAtMost(Rules.leagues.size - 1))
            rank >= 7 -> edit.putInt("league", (league - 1).coerceAtLeast(0))
        }
        edit.apply()
    }

    /** Première action du jour : entretient ou relance la flamme. */
    private fun startDay() {
        val day = today()
        val last = prefs.getLong("lastDay", -1L)
        if (last == day) return
        val streak = if (last == day - 1) prefs.getInt("streak", 0) + 1 else 1
        prefs.edit().putLong("lastDay", day).putInt("streak", streak)
            .putInt("bestStreak", maxOf(streak, prefs.getInt("bestStreak", 0))).apply()
    }

    // ---- actions de jeu ----
    /** Applique la conséquence d’une réponse : XP (avec multiplicateur de série), jetons et cœurs. */
    fun answer(correct: Boolean, combo: Int, bonusCase: Boolean = false): AnswerOutcome {
        rollover(); startDay()
        val multiplier = if (correct) Rules.multiplier(combo) else 1
        val bonusXp = if (correct && bonusCase) Rules.BONUS_CASE_XP else 0
        val xp = if (correct) Rules.BASE_XP * multiplier + bonusXp else 0
        val gems = if (correct) 1 else 0
        val before = levelOf(prefs.getInt("xp", 0)).number
        val edit = prefs.edit()
        if (correct) {
            edit.putInt("xp", prefs.getInt("xp", 0) + xp)
                .putInt("xpToday", prefs.getInt("xpToday", 0) + xp)
                .putInt("weekXp", prefs.getInt("weekXp", 0) + xp)
                .putInt("correctToday", prefs.getInt("correctToday", 0) + 1)
                .putInt("totalCorrect", prefs.getInt("totalCorrect", 0) + 1)
                .putInt("gems", prefs.getInt("gems", 50) + gems)
                .putInt("bestComboToday", maxOf(combo, prefs.getInt("bestComboToday", 0)))
        } else {
            edit.putInt("hearts", (currentHearts() - 1).coerceAtLeast(0)).putLong("heartsAt", now())
        }
        edit.putInt("answeredToday", prefs.getInt("answeredToday", 0) + 1)
            .putInt("totalAnswered", prefs.getInt("totalAnswered", 0) + 1).apply()
        val events = mutableListOf<GameEvent>()
        val after = levelOf(prefs.getInt("xp", 0)).number
        if (after > before) events += GameEvent.LevelUp(after)
        val hearts = currentHearts()
        commit(events)
        return AnswerOutcome(xp, multiplier, gems, hearts, events)
    }

    /** Fin de partie : couronnes du mode, bonus sans faute et compteurs de série. */
    fun finishRun(mode: Int, correct: Int, answered: Int): RunOutcome {
        rollover(); startDay()
        val rate = if (answered == 0) 0 else correct * 100 / answered
        val perfect = answered > 0 && correct == answered
        val gems = if (perfect) Rules.PERFECT_GEMS else 0
        val edit = prefs.edit()
            .putInt("runs", prefs.getInt("runs", 0) + 1)
            .putInt("runsToday", prefs.getInt("runsToday", 0) + 1)
            .putInt("runs$mode", prefs.getInt("runs$mode", 0) + 1)
            .putInt("best$mode", maxOf(rate, prefs.getInt("best$mode", 0)))
            .putInt("gems", prefs.getInt("gems", 50) + gems)
        if (perfect) edit.putInt("perfectRuns", prefs.getInt("perfectRuns", 0) + 1)
        edit.apply()
        val events = mutableListOf<GameEvent>()
        commit(events)
        return RunOutcome(rate, perfect, gems, crownsOf(mode), events)
    }

    /** Recharge complète des cœurs contre [Rules.HEART_COST] jetons. */
    fun refillHearts(): Boolean {
        if (currentHearts() >= Rules.MAX_HEARTS || prefs.getInt("gems", 50) < Rules.HEART_COST) return false
        prefs.edit().putInt("gems", prefs.getInt("gems", 50) - Rules.HEART_COST)
            .putInt("hearts", Rules.MAX_HEARTS).putLong("heartsAt", now()).apply()
        progress = read()
        return true
    }

    /** Achat d’un gel de série : la flamme survit à une journée manquée. */
    fun buyFreeze(): Boolean {
        if (prefs.getInt("gems", 50) < Rules.FREEZE_COST || prefs.getInt("freezes", 0) >= 2) return false
        prefs.edit().putInt("gems", prefs.getInt("gems", 50) - Rules.FREEZE_COST)
            .putInt("freezes", prefs.getInt("freezes", 0) + 1).apply()
        progress = read()
        return true
    }

    fun setGoal(goal: Int) { prefs.edit().putInt("goal", goal.coerceIn(Rules.goals.first(), Rules.goals.last())).apply(); progress = read() }

    /** Distribue automatiquement quêtes du jour, coffre, objectif quotidien et succès. */
    fun exportCloud(): org.json.JSONObject = toCloudJson()

    /** Fusionne une sauvegarde cloud : on garde le max de chaque compteur. */
    fun mergeCloud(json: org.json.JSONObject) {
        fun i(key: String, def: Int = 0) = json.optInt(key, prefs.getInt(key, def))
        val badges = (prefs.getStringSet("badges", emptySet()).orEmpty() +
            json.optJSONArray("badges")?.let { arr -> (0 until arr.length()).map { arr.optString(it) } }.orEmpty())
        val edit = prefs.edit()
            .putInt("xp", maxOf(prefs.getInt("xp", 0), json.optInt("xp", 0)))
            .putInt("gems", maxOf(prefs.getInt("gems", 50), json.optInt("gems", 50)))
            .putInt("streak", maxOf(prefs.getInt("streak", 0), json.optInt("streak", 0)))
            .putInt("bestStreak", maxOf(prefs.getInt("bestStreak", 0), json.optInt("bestStreak", 0)))
            .putInt("goalDays", maxOf(prefs.getInt("goalDays", 0), json.optInt("goalDays", 0)))
            .putInt("league", maxOf(prefs.getInt("league", 0), json.optInt("league", 0)))
            .putInt("weekXp", maxOf(prefs.getInt("weekXp", 0), json.optInt("weekXp", 0)))
            .putInt("correct", maxOf(prefs.getInt("correct", 0), i("correct")))
            .putInt("answered", maxOf(prefs.getInt("answered", 0), i("answered")))
            .putInt("runs", maxOf(prefs.getInt("runs", 0), i("runs")))
            .putStringSet("badges", badges)
        for (m in 0..5) edit.putInt("best$m", maxOf(prefs.getInt("best$m", 0), json.optInt("best$m", 0)))
        edit.apply()
        progress = read()
    }

    /** Photographie JSON minimale de la progression pour le cloud Play Games. */
    fun toCloudJson(): org.json.JSONObject = org.json.JSONObject()
        .put("xp", prefs.getInt("xp", 0)).put("gems", prefs.getInt("gems", 50))
        .put("streak", prefs.getInt("streak", 0)).put("bestStreak", prefs.getInt("bestStreak", 0))
        .put("goalDays", prefs.getInt("goalDays", 0)).put("league", prefs.getInt("league", 0))
        .put("weekXp", prefs.getInt("weekXp", 0)).put("correct", prefs.getInt("correct", 0))
        .put("answered", prefs.getInt("answered", 0)).put("runs", prefs.getInt("runs", 0))
        .put("badges", org.json.JSONArray(prefs.getStringSet("badges", emptySet()).orEmpty().toList()))
        .put("best0", prefs.getInt("best0", 0)).put("best1", prefs.getInt("best1", 0))
        .put("best2", prefs.getInt("best2", 0)).put("best3", prefs.getInt("best3", 0))
        .put("best4", prefs.getInt("best4", 0)).put("best5", prefs.getInt("best5", 0))

    /** Distribue automatiquement quêtes du jour, coffre, objectif quotidien et succès. */
    private fun commit(events: MutableList<GameEvent>) {
        val p = read()
        val claimed = prefs.getStringSet("claimed", emptySet()).orEmpty().toMutableSet()
        val badges = prefs.getStringSet("badges", emptySet()).orEmpty().toMutableSet()
        var gems = prefs.getInt("gems", 50)
        if (p.goalDone && prefs.getLong("goalDay", -1L) != p.dayIndex)
            prefs.edit().putLong("goalDay", p.dayIndex).putInt("goalDays", prefs.getInt("goalDays", 0) + 1).apply()
        val quests = dailyQuests(p)
        quests.filter { it.done && it.id !in claimed }.forEach { claimed += it.id; gems += it.gems; events += GameEvent.QuestDone(it) }
        if (quests.all { it.done } && "chest" !in claimed) { claimed += "chest"; gems += Rules.CHEST_GEMS; events += GameEvent.Chest(Rules.CHEST_GEMS) }
        val snapshot = p.copy(gems = gems, badges = badges)
        allBadges.forEach { badge ->
            if (badge.id !in badges && badgeEarned(badge.id, snapshot)) { badges += badge.id; gems += Rules.BADGE_GEMS; events += GameEvent.NewBadge(badge) }
        }
        val rank = weekRank(p.league, p.weekIndex, p.weekXp)
        val best = prefs.getInt("weekBestRank", 0)
        prefs.edit().putInt("gems", gems).putStringSet("claimed", claimed).putStringSet("badges", badges)
            .putInt("weekBestRank", if (best > 0) minOf(best, rank) else rank).apply()
        progress = read()
    }
}

val LocalProgress = staticCompositionLocalOf<ProgressStore> { error("Progression indisponible") }
