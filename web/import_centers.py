"""Refresh the local centre directory from the attributed data.gouv.fr CSV.

Usage: python3 web/import_centers.py /path/to/centres.csv
The CSV is a dated third-party compilation of the two approved directories.
"""
import csv
import json
import sys
from pathlib import Path
from urllib.parse import urlparse

OUT = Path(__file__).resolve().parent / "data/centers.json"
DATASET = "https://www.data.gouv.fr/datasets/centres-dexamen-civique-agrees-en-france-recensement-au-10-octobre-2026"


def safe_url(value):
    value = value.strip()
    return value if urlparse(value).scheme in {"http", "https"} else ""


def main(source):
    centers = []
    with open(source, newline="", encoding="utf-8-sig") as stream:
        for row in csv.DictReader(stream):
            official_sources = [safe_url(item) for item in row["Source de la donnée"].split("|")]
            centers.append({
                "name": row["Nom du centre"].strip(),
                "network": row["Organisme agréeur"].strip(),
                "address": row["Adresse"].strip(),
                "postal_code": row["Code postal"].strip(),
                "city": row["Ville"].strip(),
                "department": row["Département"].strip(),
                "phone": row["Téléphone"].strip(),
                "website": safe_url(row["Site web"]),
                "mentions": row["Mentions acceptées"].strip(),
                "sources": [url for url in official_sources if url],
                "surveyed": row["Date de relevé"].strip(),
                "confidence": row["Niveau de confiance"].strip(),
            })
    OUT.write_text(json.dumps({"dataset": DATASET, "centers": centers}, ensure_ascii=False, indent=2) + "\n")
    print(f"Imported {len(centers)} centers into {OUT}")


if __name__ == "__main__":
    main(sys.argv[1])
