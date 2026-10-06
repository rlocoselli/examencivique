#!/usr/bin/env python3
"""Select the next Play version without publishing or committing an edit."""
from __future__ import annotations

import argparse
import os
from pathlib import Path

FIRST_VERSION_CODE = 1790408057
MAX_VERSION_CODE = 2100000000


def next_version(bundles: dict, apks: dict, tracks: dict) -> tuple[int, str]:
    codes = [int(item["versionCode"]) for group, key in
             ((bundles, "bundles"), (apks, "apks")) for item in group.get(key, [])]
    codes.extend(int(code) for track in tracks.get("tracks", [])
                 for release in track.get("releases", [])
                 for code in release.get("versionCodes", []))
    # Preserve the existing numbering lineage, including the first manual upload.
    code = max([FIRST_VERSION_CODE, *codes]) + 1
    if code > MAX_VERSION_CODE:
        raise ValueError("Google Play versionCode limit reached")
    return code, f"1.0.{code - FIRST_VERSION_CODE}"


def read_next_version(service, package_name: str) -> tuple[int, str]:
    edits = service.edits()
    edit_id = edits.insert(packageName=package_name, body={}).execute()["id"]
    args = {"packageName": package_name, "editId": edit_id}
    try:
        return next_version(edits.bundles().list(**args).execute(),
                            edits.apks().list(**args).execute(),
                            edits.tracks().list(**args).execute())
    finally:
        # Discard the temporary edit; the live store listing is never changed.
        edits.delete(**args).execute()


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--credentials", type=Path, required=True)
    parser.add_argument("--package-name", default="fr.audeladedonnees.examen.civique")
    args = parser.parse_args()
    from google.oauth2 import service_account
    from googleapiclient.discovery import build
    credentials = service_account.Credentials.from_service_account_file(
        str(args.credentials), scopes=["https://www.googleapis.com/auth/androidpublisher"])
    service = build("androidpublisher", "v3", credentials=credentials, cache_discovery=False)
    code, name = read_next_version(service, args.package_name)
    values = f"BUILD_NUMBER={code}\nVERSION_NAME={name}\n"
    if env_file := os.environ.get("GITHUB_ENV"):
        with Path(env_file).open("a", encoding="utf-8") as output:
            output.write(values)
    print(values, end="")


if __name__ == "__main__":
    main()
