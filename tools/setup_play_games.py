#!/usr/bin/env python3
"""Create the Play Games leaderboards/achievements and configure the Android app."""

from __future__ import annotations

import argparse
import json
import os
import sys
import tempfile
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError


ROOT = Path(__file__).resolve().parent.parent
DEFAULT_CREDENTIALS = ROOT / ".secrets" / "google-play-service-account.json"
CONFIG_FILE = ROOT / "google-play.properties"
SCOPE = "https://www.googleapis.com/auth/androidpublisher"

ACHIEVEMENTS = [
    ("first", "Premier pas", "Répondre à sa première question"),
    ("correct10", "Dix bonnes réponses", "Donner 10 bonnes réponses au total"),
    ("correct50", "Cinquante justes", "Donner 50 bonnes réponses au total"),
    ("correct250", "Deux cent cinquante justes", "Donner 250 bonnes réponses au total"),
    ("xp150", "Cent cinquante XP", "Cumuler 150 XP"),
    ("level5", "Citoyen engagé", "Atteindre le niveau 5"),
    ("level10", "Citoyen expert", "Atteindre le niveau 10"),
    ("streak3", "Trois jours d’affilée", "Jouer 3 jours de suite"),
    ("streak7", "Une semaine entière", "Jouer 7 jours de suite"),
    ("perfect", "Sans faute", "Terminer une partie à 100 %"),
    ("runs10", "Dix parcours", "Terminer 10 parties"),
    ("crown3", "Première couronne d’or", "Obtenir 3 couronnes dans un mode"),
]

LEADERBOARDS = [
    ("xp", "XP total", "2147483647"),
    ("streak", "Meilleure série", "100000"),
]


def localized(value: str) -> dict[str, object]:
    return {"translations": [{"locale": "fr-FR", "value": value}]}


def draft_name(item: dict[str, object]) -> str:
    detail = item.get("draft") or item.get("published") or {}
    if not isinstance(detail, dict):
        return ""
    name = detail.get("name", {})
    if not isinstance(name, dict):
        return ""
    translations = name.get("translations", [])
    if not isinstance(translations, list):
        return ""
    for translation in translations:
        if isinstance(translation, dict) and translation.get("locale") == "fr-FR":
            return str(translation.get("value", ""))
    return ""


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Crée les ressources Play Games de Parcours citoyen."
    )
    parser.add_argument("--app-id", required=True, help="ID numérique Play Games")
    parser.add_argument("--credentials", type=Path, default=DEFAULT_CREDENTIALS)
    parser.add_argument(
        "--dry-run", action="store_true", help="Liste les créations sans appeler l’API"
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    if not args.app_id.isdigit() or len(args.app_id) < 12:
        raise SystemExit("Erreur : --app-id doit contenir au moins 12 chiffres")
    if not args.credentials.is_file():
        raise SystemExit(f"Erreur : clé JSON introuvable : {args.credentials}")

    if args.dry_run:
        print("Classements :", ", ".join(name for _, name, _ in LEADERBOARDS))
        print("Succès :", ", ".join(title for _, title, _ in ACHIEVEMENTS))
        return 0

    credentials = service_account.Credentials.from_service_account_file(
        str(args.credentials), scopes=[SCOPE]
    )
    service = build(
        "gamesConfiguration", "v1configuration", credentials=credentials,
        cache_discovery=False,
    )

    try:
        existing_boards = service.leaderboardConfigurations().list(
            applicationId=args.app_id
        ).execute().get("items", [])
        existing_achievements = service.achievementConfigurations().list(
            applicationId=args.app_id
        ).execute().get("items", [])

        boards_by_name = {draft_name(item): item for item in existing_boards}
        achievements_by_name = {
            draft_name(item): item for item in existing_achievements
        }

        board_ids: dict[str, str] = {}
        for key, title, score_max in LEADERBOARDS:
            item = boards_by_name.get(title)
            if item is None:
                item = service.leaderboardConfigurations().insert(
                    applicationId=args.app_id,
                    body={
                        "scoreOrder": "LARGER_IS_BETTER",
                        "scoreMin": "0",
                        "scoreMax": score_max,
                        "draft": {
                            "name": localized(title),
                            "scoreFormat": {
                                "numberFormatType": "NUMERIC",
                                "numDecimalPlaces": 0,
                            },
                        },
                    },
                ).execute()
                print(f"Classement créé : {title}")
            else:
                print(f"Classement existant : {title}")
            board_ids[key] = str(item["id"])

        achievement_ids: list[str] = []
        for local_id, title, description in ACHIEVEMENTS:
            item = achievements_by_name.get(title)
            if item is None:
                item = service.achievementConfigurations().insert(
                    applicationId=args.app_id,
                    body={
                        "achievementType": "STANDARD",
                        "initialState": "REVEALED",
                        "draft": {
                            "name": localized(title),
                            "description": localized(description),
                            "pointValue": 5,
                        },
                    },
                ).execute()
                print(f"Succès créé : {title} ({local_id})")
            else:
                print(f"Succès existant : {title} ({local_id})")
            achievement_ids.append(str(item["id"]))

    except HttpError as exc:
        status = getattr(exc.resp, "status", "inconnu")
        try:
            message = json.loads(exc.content.decode("utf-8"))["error"]["message"]
        except Exception:
            message = str(exc)
        raise SystemExit(f"Erreur API Play Games ({status}) : {message}") from exc

    content = "\n".join([
        "# Généré par tools/setup_play_games.py — ne pas versionner.",
        f"PLAY_GAMES_APP_ID={args.app_id}",
        f"LEADERBOARD_XP={board_ids['xp']}",
        f"LEADERBOARD_STREAK={board_ids['streak']}",
        f"ACHIEVEMENTS_CSV={','.join(achievement_ids)}",
        "",
    ])
    CONFIG_FILE.parent.mkdir(parents=True, exist_ok=True)
    descriptor, temporary_name = tempfile.mkstemp(
        prefix=".google-play.properties.", dir=CONFIG_FILE.parent, text=True
    )
    try:
        with os.fdopen(descriptor, "w", encoding="utf-8") as handle:
            handle.write(content)
        os.chmod(temporary_name, 0o600)
        os.replace(temporary_name, CONFIG_FILE)
    finally:
        if os.path.exists(temporary_name):
            os.unlink(temporary_name)

    print(f"Configuration Android écrite : {CONFIG_FILE}")
    print("Publiez maintenant les changements Play Games dans Play Console.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
