#!/usr/bin/env bash
set -Eeuo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"
readonly CONFIG_FILE="$PROJECT_DIR/google-play.properties"

app_id=""
leaderboard_xp=""
leaderboard_streak=""
achievements=""
build_app=true

usage() {
    cat <<'EOF'
Configure le backend Google Play Games v2 de Parcours citoyen.

Usage :
  tools/setup_google_backend.sh --app-id ID --leaderboard-xp ID \
    --leaderboard-streak ID [--achievements ID1,...,ID12] [--no-build]

Options :
  --app-id ID              ID numérique Play Games (12 chiffres minimum)
  --leaderboard-xp ID      ID Play Console du classement « XP total »
  --leaderboard-streak ID  ID Play Console du classement « Meilleure série »
  --achievements CSV       12 IDs Google, dans l'ordre documenté dans BACKEND_GOOGLE.md
  --no-build               Configure sans lancer assembleDebug
  -h, --help               Affiche cette aide

Les mêmes valeurs peuvent être fournies par les variables d'environnement
PLAY_GAMES_APP_ID, LEADERBOARD_XP, LEADERBOARD_STREAK et ACHIEVEMENTS_CSV.
EOF
}

die() {
    printf 'Erreur : %s\n' "$*" >&2
    exit 1
}

while (($#)); do
    case "$1" in
        --app-id) [[ $# -ge 2 ]] || die "valeur absente après --app-id"; app_id=$2; shift 2 ;;
        --leaderboard-xp) [[ $# -ge 2 ]] || die "valeur absente après --leaderboard-xp"; leaderboard_xp=$2; shift 2 ;;
        --leaderboard-streak) [[ $# -ge 2 ]] || die "valeur absente après --leaderboard-streak"; leaderboard_streak=$2; shift 2 ;;
        --achievements) [[ $# -ge 2 ]] || die "valeur absente après --achievements"; achievements=$2; shift 2 ;;
        --no-build) build_app=false; shift ;;
        -h|--help) usage; exit 0 ;;
        *) die "option inconnue : $1 (utilisez --help)" ;;
    esac
done

app_id=${app_id:-${PLAY_GAMES_APP_ID:-}}
leaderboard_xp=${leaderboard_xp:-${LEADERBOARD_XP:-}}
leaderboard_streak=${leaderboard_streak:-${LEADERBOARD_STREAK:-}}
achievements=${ACHIEVEMENTS_CSV:-$achievements}

[[ $app_id =~ ^[0-9]{12,}$ ]] || die "--app-id doit contenir au moins 12 chiffres"
[[ $leaderboard_xp =~ ^[A-Za-z0-9._-]+$ ]] || die "--leaderboard-xp est absent ou invalide"
[[ $leaderboard_streak =~ ^[A-Za-z0-9._-]+$ ]] || die "--leaderboard-streak est absent ou invalide"
[[ $achievements =~ ^[A-Za-z0-9._-]+(,[A-Za-z0-9._-]+){11}$ ]] || \
    die "--achievements doit contenir exactement 12 IDs séparés par des virgules"

umask 077
tmp_file=$(mktemp "$PROJECT_DIR/.google-play.properties.XXXXXX")
trap 'rm -f -- "$tmp_file"' EXIT
{
    printf '# Généré par tools/setup_google_backend.sh — ne pas versionner.\n'
    printf 'PLAY_GAMES_APP_ID=%s\n' "$app_id"
    printf 'LEADERBOARD_XP=%s\n' "$leaderboard_xp"
    printf 'LEADERBOARD_STREAK=%s\n' "$leaderboard_streak"
    printf 'ACHIEVEMENTS_CSV=%s\n' "$achievements"
} > "$tmp_file"
mv -- "$tmp_file" "$CONFIG_FILE"
trap - EXIT
printf 'Configuration écrite dans %s\n' "$CONFIG_FILE"

debug_keystore="${ANDROID_DEBUG_KEYSTORE:-${HOME}/.android/debug.keystore}"
if command -v keytool >/dev/null 2>&1 && [[ -f $debug_keystore ]]; then
    printf '\nSHA-1 debug à enregistrer dans Play Console :\n'
    keytool -list -v -keystore "$debug_keystore" -alias androiddebugkey \
        -storepass android 2>/dev/null | sed -n 's/^[[:space:]]*SHA1: /  /p'
else
    printf '\nAttention : SHA-1 non affiché (keytool ou keystore debug introuvable).\n' >&2
fi

if [[ $build_app == true ]]; then
    if [[ -x "$PROJECT_DIR/gradlew" ]]; then
        build_command=("$PROJECT_DIR/gradlew" assembleDebug)
    elif command -v gradle >/dev/null 2>&1; then
        build_command=(gradle assembleDebug)
    else
        die "configuration enregistrée, mais Gradle est introuvable"
    fi
    printf '\nCompilation de contrôle…\n'
    (cd -- "$PROJECT_DIR" && "${build_command[@]}")
fi

cat <<'EOF'

Configuration locale terminée.
Dans Play Console, vérifiez encore que :
  1. le package fr.audeladedonnees.examen.civique et le SHA-1 debug sont liés au jeu ;
  2. les comptes Gmail de test sont autorisés ;
  3. « Saved games » est activé ;
  4. les 2 classements et les 12 succès sont publiés.
EOF
