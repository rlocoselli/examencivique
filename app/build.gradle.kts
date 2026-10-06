import java.util.Properties

plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }

val playGamesProperties = Properties().apply {
    val configFile = rootProject.file("google-play.properties")
    if (configFile.isFile) configFile.inputStream().use(::load)
}

fun playGamesProperty(name: String, default: String = ""): String =
    providers.gradleProperty(name).orNull?.takeIf { it.isNotEmpty() }
        ?: System.getenv(name)?.takeIf { it.isNotEmpty() }
        ?: playGamesProperties.getProperty(name)?.takeIf { it.isNotEmpty() }
        ?: default

val releaseKeystorePath = System.getenv("ANDROID_SIGNING_KEYSTORE_PATH").orEmpty()
val releaseStorePassword = System.getenv("ANDROID_SIGNING_STORE_PASSWORD").orEmpty()
val releaseKeyAlias = System.getenv("ANDROID_SIGNING_KEY_ALIAS").orEmpty()
val releaseKeyPassword = System.getenv("ANDROID_SIGNING_KEY_PASSWORD").orEmpty()
val releaseSigningConfigured = listOf(
    releaseKeystorePath, releaseStorePassword, releaseKeyAlias, releaseKeyPassword
).all { it.isNotEmpty() }
val ciVersionCode = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: 1
val ciVersionName = System.getenv("VERSION_NAME") ?: "1.0"

android { namespace = "fr.audeladedonnees.examen.civique"; compileSdk = 36
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }
    defaultConfig {
        applicationId = "fr.audeladedonnees.examen.civique"; minSdk = 24; targetSdk = 36; versionCode = ciVersionCode; versionName = ciVersionName
        // Remplacez par l'ID de votre jeu Play Console (12+ chiffres). En attendant,
        // la valeur "0" laisse l'app 100% hors-ligne sans jamais crasher.
        val playGamesAppId = playGamesProperty("PLAY_GAMES_APP_ID", "0")
        resValue("string", "play_games_app_id", playGamesAppId)
        buildConfigField("String", "PLAY_GAMES_APP_ID", "\"$playGamesAppId\"")
        // IDs Play Console (classements / succès) : remplacez par les vôtres.
        // Valeurs vides = fonctionnalité désactivée proprement (jeu local conservé).
        buildConfigField("String", "LEADERBOARD_XP", "\"${playGamesProperty("LEADERBOARD_XP")}\"")
        buildConfigField("String", "LEADERBOARD_STREAK", "\"${playGamesProperty("LEADERBOARD_STREAK")}\"")
        buildConfigField(
            "String", "ACHIEVEMENTS_CSV",
            "\"${playGamesProperty("ACHIEVEMENTS_CSV")}\""
        )
    }
    buildTypes {
        getByName("release") {
            if (releaseSigningConfigured) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }
    buildFeatures { buildConfig = true }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation(platform("androidx.compose:compose-bom:2025.08.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")
    // Backend Google : connexion + classements + succès + sauvegardes cloud (optionnel hors-ligne).
    implementation("com.google.android.gms:play-services-games-v2:20.1.2")
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
}
