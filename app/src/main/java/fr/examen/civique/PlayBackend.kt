package fr.audeladedonnees.examen.civique

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Backend Google Play Games (v2) branche sur la progression locale existante.
 * Sans APP_ID Play Console configure, tout est desactive proprement :
 * le jeu reste 100 % hors-ligne comme avant.
 */
object PlayBackend {
    private const val SNAPSHOT_NAME = "quest-save"
    private val localAchievementIds = listOf(
        "first", "correct10", "correct50", "correct250", "xp150", "level5",
        "level10", "streak3", "streak7", "perfect", "runs10", "crown3"
    )

    var connected by mutableStateOf(false); private set
    var playerName by mutableStateOf<String?>(null); private set
    var syncing by mutableStateOf(false); private set
    var lastError by mutableStateOf<String?>(null); private set

    fun configured(): Boolean = try {
        val id = BuildConfig.PLAY_GAMES_APP_ID.trim()
        id.isNotEmpty() && id != "0"
    } catch (_: Throwable) { false }

    private fun achievements(): Map<String, String> = try {
        val remoteIds = BuildConfig.ACHIEVEMENTS_CSV.split(",").map { it.trim() }
            .filter { it.isNotEmpty() }
        if (remoteIds.size == localAchievementIds.size)
            localAchievementIds.zip(remoteIds).toMap()
        else emptyMap()
    } catch (_: Throwable) { emptyMap() }

    fun init(context: Context) {
        if (!configured()) return
        try { PlayGamesSdk.initialize(context.applicationContext) }
        catch (t: Throwable) { lastError = t.message }
    }

    suspend fun silentSignIn(activity: Activity): Boolean = withContext(Dispatchers.Main) {
        if (!configured()) return@withContext false
        return@withContext try {
            val auth = PlayGames.getGamesSignInClient(activity).isAuthenticated.await()
            connected = auth.isAuthenticated
            if (connected) playerName =
                PlayGames.getPlayersClient(activity).currentPlayer.await()?.displayName
            connected
        } catch (t: Throwable) { lastError = t.message; false }
    }

    suspend fun signIn(activity: Activity): Boolean = withContext(Dispatchers.Main) {
        if (!configured()) return@withContext false
        return@withContext try {
            val result = PlayGames.getGamesSignInClient(activity).signIn().await()
            connected = result.isAuthenticated
            if (connected) {
                playerName = PlayGames.getPlayersClient(activity).currentPlayer.await()?.displayName
                pushScores(activity)
            }
            connected
        } catch (t: Throwable) { lastError = t.message; false }
    }

    fun signOut(done: () -> Unit = {}) {
        connected = false; playerName = null; done()
    }

    fun pushScores(activity: Activity, xp: Int = -1, streak: Int = -1) {
        if (!configured() || !connected) return
        val prefs = activity.getSharedPreferences("quest", 0)
        val totalXp = if (xp >= 0) xp else prefs.getInt("xp", 0)
        val best = if (streak >= 0) streak else prefs.getInt("bestStreak", 0)
        try {
            val boards = PlayGames.getLeaderboardsClient(activity)
            if (BuildConfig.LEADERBOARD_XP.isNotEmpty()) boards.submitScore(BuildConfig.LEADERBOARD_XP, totalXp.toLong())
            if (BuildConfig.LEADERBOARD_STREAK.isNotEmpty()) boards.submitScore(BuildConfig.LEADERBOARD_STREAK, best.toLong())
        } catch (t: Throwable) { lastError = t.message }
    }

    fun unlock(activity: Activity, badgeId: String) {
        if (!configured() || !connected) return
        val remoteId = achievements()[badgeId] ?: return
        try { PlayGames.getAchievementsClient(activity).unlock(remoteId) }
        catch (t: Throwable) { lastError = t.message }
    }

    fun showLeaderboards(activity: Activity) {
        if (!configured()) return
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val intent: Intent = PlayGames.getLeaderboardsClient(activity).allLeaderboardsIntent.await()
                activity.startActivityForResult(intent, 9001)
            } catch (t: Throwable) { lastError = t.message }
        }
    }

    fun showAchievements(activity: Activity) {
        if (!configured()) return
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val intent: Intent = PlayGames.getAchievementsClient(activity).achievementsIntent.await()
                activity.startActivityForResult(intent, 9002)
            } catch (t: Throwable) { lastError = t.message }
        }
    }

    fun pushCloud(activity: Activity, store: ProgressStore) {
        if (!configured() || !connected) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                syncing = true
                val snaps = PlayGames.getSnapshotsClient(activity)
                val opened = snaps.open(SNAPSHOT_NAME, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED).await()
                val snap = opened.data ?: return@launch
                snap.snapshotContents.writeBytes(store.toCloudJson().toString().toByteArray())
                val change = SnapshotMetadataChange.Builder()
                    .setDescription("Parcours citoyen - ${store.progress.xp} XP").build()
                snaps.commitAndClose(snap, change).await()
            } catch (t: Throwable) { lastError = t.message } finally { syncing = false }
        }
    }

    suspend fun pullCloud(activity: Activity, store: ProgressStore): Boolean = withContext(Dispatchers.IO) {
        if (!configured() || !connected) return@withContext false
        syncing = true
        try {
            val snaps = PlayGames.getSnapshotsClient(activity)
            val opened = snaps.open(SNAPSHOT_NAME, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED).await()
            val data = opened.data?.let { runCatching { it.snapshotContents.readFully() }.getOrNull() }
            if (data != null && data.isNotEmpty()) store.mergeCloud(JSONObject(String(data)))
            withContext(Dispatchers.Main) { syncing = false }
            true
        } catch (t: Throwable) { withContext(Dispatchers.Main) { syncing = false; lastError = t.message }; false }
    }
}
