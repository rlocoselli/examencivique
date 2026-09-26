#!/usr/bin/env bash
set -Eeuo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"
readonly PACKAGE_NAME="fr.audeladedonnees.examen.civique"
readonly DEFAULT_KEY="$PROJECT_DIR/.secrets/google-play-service-account.json"
readonly DEFAULT_METADATA="$PROJECT_DIR/fastlane/metadata/android"

key_file="$DEFAULT_KEY"
aab_file=""
track="internal"
release_status="draft"
metadata_only=false
validate_only=false
contact_email=""
contact_website=""
data_safety_csv=""
play_games_app_id=""
verify_all_countries=false

usage() {
    cat <<'EOF'
Publie Parcours citoyen et ses visuels dans une application Google Play existante.

Usage :
  tools/publish_google_play.sh --aab CHEMIN [options]
  tools/publish_google_play.sh --metadata-only [options]

Options :
  --credentials CHEMIN  Clé JSON du compte de service
                        (défaut : .secrets/google-play-service-account.json)
  --aab CHEMIN          Android App Bundle signé à envoyer
  --metadata-only       Envoie uniquement la fiche et les images
  --track NOM           Piste Google Play (défaut : internal)
  --release-status ÉTAT draft, completed, halted ou inProgress (défaut : draft)
  --validate-only       Demande à Google de valider sans appliquer les changements
  --contact-email EMAIL Adresse publique obligatoire de support
  --contact-website URL Site public de support (facultatif)
  --data-safety-csv CSV Déclaration Data safety exportée depuis Play Console
  --verify-all-countries Vérifie que « reste du monde » est activé
  --play-games-app-id ID Crée ensuite classements/succès et configure le backend
  -h, --help            Affiche cette aide

Prérequis Google : l'application fr.audeladedonnees.examen.civique doit avoir été créée dans
Play Console et un premier binaire doit avoir été envoyé manuellement. Le compte
de service doit ensuite être invité avec les droits adaptés à cette application.
EOF
}

die() {
    printf 'Erreur : %s\n' "$*" >&2
    exit 1
}

while (($#)); do
    case "$1" in
        --credentials) [[ $# -ge 2 ]] || die "valeur absente après --credentials"; key_file=$2; shift 2 ;;
        --aab) [[ $# -ge 2 ]] || die "valeur absente après --aab"; aab_file=$2; shift 2 ;;
        --metadata-only) metadata_only=true; shift ;;
        --track) [[ $# -ge 2 ]] || die "valeur absente après --track"; track=$2; shift 2 ;;
        --release-status) [[ $# -ge 2 ]] || die "valeur absente après --release-status"; release_status=$2; shift 2 ;;
        --validate-only) validate_only=true; shift ;;
        --contact-email) [[ $# -ge 2 ]] || die "valeur absente après --contact-email"; contact_email=$2; shift 2 ;;
        --contact-website) [[ $# -ge 2 ]] || die "valeur absente après --contact-website"; contact_website=$2; shift 2 ;;
        --data-safety-csv) [[ $# -ge 2 ]] || die "valeur absente après --data-safety-csv"; data_safety_csv=$2; shift 2 ;;
        --verify-all-countries) verify_all_countries=true; shift ;;
        --play-games-app-id) [[ $# -ge 2 ]] || die "valeur absente après --play-games-app-id"; play_games_app_id=$2; shift 2 ;;
        -h|--help) usage; exit 0 ;;
        *) die "option inconnue : $1 (utilisez --help)" ;;
    esac
done

[[ -f $key_file ]] || die "clé JSON introuvable : $key_file"
[[ -r $key_file ]] || die "clé JSON illisible : $key_file"
[[ $contact_email =~ ^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$ ]] || \
    die "--contact-email est obligatoire et doit être une adresse valide"
[[ -z $data_safety_csv || -f $data_safety_csv ]] || die "CSV Data safety introuvable : $data_safety_csv"
[[ -z $play_games_app_id || $play_games_app_id =~ ^[0-9]{12,}$ ]] || die "ID Play Games invalide"
case "$release_status" in
    draft|completed|halted|inProgress) ;;
    *) die "statut de publication invalide : $release_status" ;;
esac
[[ $track =~ ^[A-Za-z0-9._-]+$ ]] || die "nom de piste invalide : $track"

if [[ $metadata_only == false ]]; then
    [[ -n $aab_file ]] || die "utilisez --aab CHEMIN ou --metadata-only"
    [[ -f $aab_file ]] || die "AAB introuvable : $aab_file"
    [[ $aab_file == *.aab ]] || die "le binaire doit avoir l'extension .aab"
elif [[ -n $aab_file ]]; then
    die "--aab et --metadata-only ne peuvent pas être utilisés ensemble"
fi

readonly IMAGE_DIR="$DEFAULT_METADATA/fr-FR/images"
[[ -f "$IMAGE_DIR/icon.png" ]] || die "image absente : $IMAGE_DIR/icon.png"
[[ -f "$IMAGE_DIR/featureGraphic.png" || -f "$IMAGE_DIR/featureGraphic.jpg" ]] || \
    die "bannière absente : $IMAGE_DIR/featureGraphic.png"
shopt -s nullglob
screenshots=("$IMAGE_DIR/phoneScreenshots/"*.png "$IMAGE_DIR/phoneScreenshots/"*.jpg "$IMAGE_DIR/phoneScreenshots/"*.jpeg)
shopt -u nullglob
((${#screenshots[@]} >= 2)) || die "ajoutez au moins deux captures dans $IMAGE_DIR/phoneScreenshots"

if command -v fastlane >/dev/null 2>&1; then
    supply_command=(fastlane supply)
elif command -v bundle >/dev/null 2>&1 && [[ -f "$PROJECT_DIR/Gemfile" ]]; then
    supply_command=(bundle exec fastlane supply)
else
    supply_command=()
fi

if [[ -x "$PROJECT_DIR/.venv-play-games/bin/python" ]]; then
    api_python="$PROJECT_DIR/.venv-play-games/bin/python"
elif python3 -c 'import google.oauth2.service_account, googleapiclient.discovery' >/dev/null 2>&1; then
    api_python=python3
else
    die "client Google absent : créez .venv-play-games selon BACKEND_GOOGLE.md"
fi

if [[ $validate_only == false ]]; then
    compliance_args=(
        --credentials "$key_file"
        --contact-email "$contact_email"
        --track "$track"
    )
    [[ -n $contact_website ]] && compliance_args+=(--contact-website "$contact_website")
    [[ -n $data_safety_csv ]] && compliance_args+=(--data-safety-csv "$data_safety_csv")
    [[ $verify_all_countries == true ]] && compliance_args+=(--verify-all-countries)
    "$api_python" "$PROJECT_DIR/tools/configure_play_compliance.py" "${compliance_args[@]}"
fi

supply_args=(
    --json_key "$key_file"
    --package_name "$PACKAGE_NAME"
    --metadata_path "$DEFAULT_METADATA"
    --track "$track"
    --release_status "$release_status"
)
if [[ $metadata_only == true ]]; then
    supply_args+=(--skip_upload_aab true --skip_upload_apk true)
else
    supply_args+=(--aab "$aab_file")
fi
if [[ $validate_only == true ]]; then
    supply_args+=(--validate_only true)
fi

printf 'Application : %s\nPiste : %s\nStatut : %s\n' "$PACKAGE_NAME" "$track" "$release_status"
[[ $validate_only == true ]] && printf 'Mode : validation uniquement\n'
if ((${#supply_command[@]})); then
    (cd -- "$PROJECT_DIR" && "${supply_command[@]}" "${supply_args[@]}")
else
    python_upload_args=(
        --credentials "$key_file"
        --package-name "$PACKAGE_NAME"
        --metadata "$DEFAULT_METADATA"
        --track "$track"
        --release-status "$release_status"
    )
    [[ $metadata_only == false ]] && python_upload_args+=(--aab "$aab_file")
    [[ $validate_only == true ]] && python_upload_args+=(--validate-only)
    "$api_python" "$PROJECT_DIR/tools/upload_play_store.py" "${python_upload_args[@]}"
fi

if [[ $validate_only == false && -n $play_games_app_id ]]; then
    "$api_python" "$PROJECT_DIR/tools/setup_play_games.py" \
        --credentials "$key_file" --app-id "$play_games_app_id"
fi

printf '\nEnvoi Google Play terminé. Vérifiez les changements dans Play Console.\n'
