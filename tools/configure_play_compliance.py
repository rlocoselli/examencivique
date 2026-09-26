#!/usr/bin/env python3
"""Update automatable Play Console details and verify country availability."""

from __future__ import annotations

import argparse
import json
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError


PACKAGE_NAME = "fr.audeladedonnees.examen.civique"
SCOPE = "https://www.googleapis.com/auth/androidpublisher"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--credentials", type=Path, required=True)
    parser.add_argument("--contact-email", required=True)
    parser.add_argument("--contact-website", default="")
    parser.add_argument("--data-safety-csv", type=Path)
    parser.add_argument("--verify-all-countries", action="store_true")
    parser.add_argument("--track", default="production")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    credentials = service_account.Credentials.from_service_account_file(
        str(args.credentials), scopes=[SCOPE]
    )
    service = build("androidpublisher", "v3", credentials=credentials, cache_discovery=False)

    edit_id = None
    try:
        edit_id = service.edits().insert(packageName=PACKAGE_NAME, body={}).execute()["id"]
        details = {"defaultLanguage": "fr-FR", "contactEmail": args.contact_email}
        if args.contact_website:
            details["contactWebsite"] = args.contact_website
        service.edits().details().patch(
            packageName=PACKAGE_NAME, editId=edit_id, body=details
        ).execute()
        service.edits().commit(packageName=PACKAGE_NAME, editId=edit_id).execute()
        edit_id = None
        print("Coordonnées Play Store mises à jour.")

        if args.data_safety_csv:
            safety_labels = args.data_safety_csv.read_text(encoding="utf-8")
            if not safety_labels.strip():
                raise SystemExit("Erreur : le CSV Data safety est vide")
            service.applications().dataSafety(
                packageName=PACKAGE_NAME, body={"safetyLabels": safety_labels}
            ).execute()
            print("Déclaration Data safety envoyée.")

        if args.verify_all_countries:
            edit_id = service.edits().insert(packageName=PACKAGE_NAME, body={}).execute()["id"]
            availability = service.edits().countryavailability().get(
                packageName=PACKAGE_NAME, editId=edit_id, track=args.track
            ).execute()
            rest_of_world = bool(availability.get("restOfWorld"))
            countries = availability.get("countries", [])
            service.edits().delete(packageName=PACKAGE_NAME, editId=edit_id).execute()
            edit_id = None
            print(f"Pays explicitement disponibles : {len(countries)}")
            print(f"Reste du monde disponible : {str(rest_of_world).lower()}")
            if not rest_of_world:
                raise SystemExit(
                    "Action Play Console requise : la disponibilité mondiale ne peut "
                    "pas être activée par l’API Publishing."
                )
    except HttpError as exc:
        status = getattr(exc.resp, "status", "inconnu")
        try:
            message = json.loads(exc.content.decode("utf-8"))["error"]["message"]
        except Exception:
            message = str(exc)
        raise SystemExit(f"Erreur API Google Play ({status}) : {message}") from exc
    finally:
        if edit_id is not None:
            try:
                service.edits().delete(packageName=PACKAGE_NAME, editId=edit_id).execute()
            except Exception:
                pass
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
