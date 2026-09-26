#!/usr/bin/env python3
"""Upload listing assets and an optional AAB through Android Publisher API."""

from __future__ import annotations

import argparse
import json
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError
from googleapiclient.http import MediaFileUpload


SCOPE = "https://www.googleapis.com/auth/androidpublisher"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--credentials", type=Path, required=True)
    parser.add_argument("--package-name", required=True)
    parser.add_argument("--metadata", type=Path, required=True)
    parser.add_argument("--track", default="internal")
    parser.add_argument("--release-status", default="draft")
    parser.add_argument("--aab", type=Path)
    parser.add_argument("--validate-only", action="store_true")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    locale = "fr-FR"
    locale_dir = args.metadata / locale
    image_dir = locale_dir / "images"
    listing = {
        "title": (locale_dir / "title.txt").read_text(encoding="utf-8").strip(),
        "shortDescription": (locale_dir / "short_description.txt").read_text(encoding="utf-8").strip(),
        "fullDescription": (locale_dir / "full_description.txt").read_text(encoding="utf-8").strip(),
    }
    credentials = service_account.Credentials.from_service_account_file(
        str(args.credentials), scopes=[SCOPE]
    )
    service = build("androidpublisher", "v3", credentials=credentials, cache_discovery=False)
    edit_id = None
    try:
        edit_id = service.edits().insert(packageName=args.package_name, body={}).execute()["id"]
        service.edits().listings().update(
            packageName=args.package_name, editId=edit_id, language=locale, body=listing
        ).execute()

        for image_type, path in (
            ("icon", image_dir / "icon.png"),
            ("featureGraphic", image_dir / "featureGraphic.png"),
        ):
            service.edits().images().upload(
                packageName=args.package_name, editId=edit_id, language=locale,
                imageType=image_type,
                media_body=MediaFileUpload(str(path), mimetype="image/png"),
            ).execute()

        screenshots = sorted((image_dir / "phoneScreenshots").glob("*.png"))
        service.edits().images().deleteall(
            packageName=args.package_name, editId=edit_id, language=locale,
            imageType="phoneScreenshots",
        ).execute()
        for screenshot in screenshots:
            service.edits().images().upload(
                packageName=args.package_name, editId=edit_id, language=locale,
                imageType="phoneScreenshots",
                media_body=MediaFileUpload(str(screenshot), mimetype="image/png"),
            ).execute()

        if args.aab:
            bundle = service.edits().bundles().upload(
                packageName=args.package_name, editId=edit_id,
                media_body=MediaFileUpload(
                    str(args.aab), mimetype="application/octet-stream", resumable=True
                ),
            ).execute()
            service.edits().tracks().update(
                packageName=args.package_name, editId=edit_id, track=args.track,
                body={"releases": [{
                    "name": f"Parcours citoyen {bundle['versionCode']}",
                    "versionCodes": [str(bundle["versionCode"])],
                    "status": args.release_status,
                }]},
            ).execute()

        if args.validate_only:
            service.edits().validate(packageName=args.package_name, editId=edit_id).execute()
            service.edits().delete(packageName=args.package_name, editId=edit_id).execute()
            edit_id = None
            print("Édition Google Play validée puis abandonnée (--validate-only).")
        else:
            service.edits().commit(packageName=args.package_name, editId=edit_id).execute()
            edit_id = None
            print("Fiche, images et version Google Play validées.")
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
                service.edits().delete(packageName=args.package_name, editId=edit_id).execute()
            except Exception:
                pass
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
