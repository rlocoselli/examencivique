package fr.audeladedonnees.examen.civique

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OfficialTrackMenu(track: String, open: (String) -> Unit) {
    BackHandler { open("home") }
    val resident = track == "resident"
    val title = if (resident) "Carte de résident" else "Naturalisation"
    val prompts = if (resident) residentPrompts else naturalisationPrompts
    val quizCount = if (resident) residentBank.size else naturalisationBank.size
    val source = if (resident) RESIDENT_SOURCE else NATURALISATION_SOURCE
    val context = androidx.compose.ui.platform.LocalContext.current
    Surface(Modifier.fillMaxSize(), color = Color(0xFF12233F), contentColor = Color.White) {
        Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            TextButton(onClick = { open("home") }) { Text("‹ Retour") }
            Text("PARCOURS CITOYEN · EXAMEN CIVIQUE", color = Color(0xFFFFCE68), fontSize = 12.sp)
            Text(title, style = MaterialTheme.typography.headlineLarge)
            Text("${prompts.size} questions de connaissance publiées par le ministère.", fontSize = 17.sp)
            Text("$quizCount QCM avec réponses et corrections pédagogiques. Les choix officiels et les mises en situation ne sont pas publiés.", color = Color(0xFFBAC9DF))
            Button(onClick = { open("$track-practice") }, modifier = Modifier.fillMaxWidth()) { Text("Réviser les QCM") }
            Button(onClick = { open("$track-test") }, modifier = Modifier.fillMaxWidth()) { Text("Test de connaissances · 45 min") }
            Text("Ce test porte sur ${minOf(40, quizCount)} questions disponibles et ne reproduit pas l'examen officiel complet.", color = Color(0xFFBAC9DF), fontSize = 12.sp)
            OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(source))) }, modifier = Modifier.fillMaxWidth()) { Text("Liste officielle ↗") }
            Text("Toutes les questions publiées", style = MaterialTheme.typography.headlineSmall)
            prompts.forEachIndexed { index, prompt ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF203653)), modifier = Modifier.fillMaxWidth()) {
                    Text("${index + 1}. $prompt", modifier = Modifier.padding(16.dp), fontSize = 14.sp)
                }
            }
        }
    }
}
