package fr.audeladedonnees.examen.civique

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlin.random.Random

internal const val REVIEW_COOLDOWN = 30L * 24 * 60 * 60 * 1000
internal fun reviewEligible(disabled: Boolean, runs: Int, answered: Int, now: Long, last: Long): Boolean =
    !disabled && runs >= 3 && answered >= 20 && (last == 0L || now - last >= REVIEW_COOLDOWN)

/** Optional invitation at the menu after playing, never during a question. */
@Composable fun ReviewPrompt() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("review", 0) }
    val progress = LocalProgress.current.progress
    var show by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val now = System.currentTimeMillis()
        val eligible = reviewEligible(prefs.getBoolean("disabled", false), progress.totalRuns + prefs.getInt("quizRuns", 0),
            progress.totalAnswered + prefs.getInt("quizAnswers", 0), now, prefs.getLong("lastPrompt", 0))
        if (eligible && Random.nextInt(4) == 0) {
            prefs.edit().putLong("lastPrompt", now).apply()
            show = true
        }
    }
    fun disable() { prefs.edit().putBoolean("disabled", true).apply(); show = false }
    if (show) AlertDialog(onDismissRequest = { show = false },
        title = { Text("Votre avis compte") },
        text = { Text(error ?: "Partagez votre avis sur Parcours citoyen pour nous aider à améliorer le jeu.") },
        confirmButton = { TextButton(onClick = {
            val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
            try {
                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")).setPackage("com.android.vending")) }
                catch (_: ActivityNotFoundException) { context.startActivity(web) }
                // Stop invitations after opening the store, without claiming a review was submitted.
                disable()
            } catch (_: ActivityNotFoundException) { error = "Aucune application ne peut ouvrir la boutique. Vous pouvez réessayer plus tard." }
        }) { Text("Donner mon avis") } },
        dismissButton = {
            androidx.compose.foundation.layout.Column {
                TextButton(onClick = { show = false }) { Text("Plus tard") }
                TextButton(onClick = { disable() }) { Text("J’ai déjà donné mon avis") }
                TextButton(onClick = { disable() }) { Text("Ne plus me demander") }
            }
        })
}
