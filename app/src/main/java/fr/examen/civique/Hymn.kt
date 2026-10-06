package fr.audeladedonnees.examen.civique

import android.content.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.activity.ComponentActivity

val LocalPiano = staticCompositionLocalOf<PianoController> { error("Piano unavailable") }

class PianoController(private val context: Context) {
    private val prefs = context.getSharedPreferences("quest", 0)
    var enabled by mutableStateOf(prefs.getBoolean("music", true)); private set
    var volume by mutableFloatStateOf(prefs.getFloat("musicVolume", .12f).coerceIn(.01f, .3f)); private set
    var available by mutableStateOf(true); private set
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var foreground = false
    private var focused = false
    private var player = runCatching { MediaPlayer.create(context, R.raw.marseillaise_piano)?.apply { isLooping = true; setVolume(volume, volume) } }.getOrNull()
    private val listener = AudioManager.OnAudioFocusChangeListener { change ->
        focused = change == AudioManager.AUDIOFOCUS_GAIN
        if (focused && foreground && enabled) player?.start() else player?.pause()
    }
    init { available = player != null }
    fun enable(value: Boolean) { enabled = value; prefs.edit().putBoolean("music", value).apply(); sync() }
    fun setLevel(value: Float) { volume = value.coerceIn(.01f, .3f); player?.setVolume(volume, volume); prefs.edit().putFloat("musicVolume", volume).apply() }
    fun active(value: Boolean) { foreground = value; sync() }
    @Suppress("DEPRECATION")
    private fun sync() {
        if (foreground && enabled && player != null) {
            if (!focused) focused = audio.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            if (focused) player?.start()
        } else {
            player?.pause(); audio.abandonAudioFocus(listener); focused = false
        }
    }
    fun restart() { player?.seekTo(0); enable(true) }
    fun release() { active(false); player?.release(); player = null }
}

@Composable fun WithPiano(content: @Composable () -> Unit) {
    val activity = androidx.activity.compose.LocalActivity.current as? ComponentActivity
        ?: error("Le piano nécessite une ComponentActivity")
    val piano = remember { PianoController(activity.applicationContext) }
    DisposableEffect(piano) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) piano.active(true)
            if (event == Lifecycle.Event.ON_PAUSE) piano.active(false)
        }
        val noisy = object : BroadcastReceiver() { override fun onReceive(context: Context?, intent: Intent?) { piano.enable(false) } }
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        if (Build.VERSION.SDK_INT >= 33) activity.registerReceiver(noisy, filter, Context.RECEIVER_NOT_EXPORTED) else activity.registerReceiver(noisy, filter)
        activity.lifecycle.addObserver(observer)
        piano.active(activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        onDispose { activity.lifecycle.removeObserver(observer); activity.unregisterReceiver(noisy); piano.release() }
    }
    CompositionLocalProvider(LocalPiano provides piano, content = content)
}

@Composable fun PianoSwitch() {
    val piano = LocalPiano.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) { Text("♪ La Marseillaise au piano", fontWeight = FontWeight.Bold); Text(if (piano.available) "Fond sonore doux • hors ligne" else "Audio indisponible", fontSize = 12.sp) }
        Switch(checked = piano.enabled, enabled = piano.available, onCheckedChange = piano::enable)
    }
}

@Composable fun HymnScreen(exit: () -> Unit, quiz: () -> Unit) {
    BackHandler(onBack = exit)
    val piano = LocalPiano.current
    val context = LocalContext.current
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            TextButton(onClick = exit) { Text("‹ Menu principal") }
            Text("🇫🇷 La Marseillaise", fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text("L’hymne national français", fontSize = 20.sp)
            PianoSwitch()
            Text("Volume de la musique : ${(piano.volume * 100).toInt()} %")
            Slider(value = piano.volume, onValueChange = piano::setLevel, valueRange = .01f.. .3f)
            Text("Le volume général reste celui de votre téléphone.", fontSize = 12.sp)
            OutlinedButton(onClick = piano::restart, enabled = piano.available) { Text("Écouter depuis le début") }
            Text("Du chant révolutionnaire à l’hymne", fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text("Rouget de Lisle compose ce chant à Strasbourg, dans la nuit du 25 au 26 avril 1792. Son premier titre est « Chant de guerre pour l’armée du Rhin ».")
            Text("Les volontaires marseillais le font connaître en rejoignant Paris : il prend alors le nom de La Marseillaise. Il est adopté comme chant national en 1795 et retrouve officiellement son rôle d’hymne national en 1879.")
            Text("Un symbole à connaître", fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text("L’hymne accompagne notamment des cérémonies officielles et des rencontres sportives. Ses paroles s’inscrivent dans le contexte des guerres révolutionnaires de 1792.")
            Button(onClick = quiz, modifier = Modifier.fillMaxWidth()) { Text("Quiz spécial hymne • ${hymnQuestions.size} questions") }
            OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.elysee.fr/la-presidence/la-marseillaise-de-rouget-de-lisle"))) }) { Text("Histoire et paroles sur le site de l’Élysée ↗") }
            Text("Version instrumentale au piano d’après la partition de 1792, édition Mutopia/Coyau placée dans le domaine public. Rendu réalisé pour ce jeu avec FluidSynth et TimGM6mb (Tim Brechbill, David Bolton).", fontSize = 12.sp)
        }
    }
}
