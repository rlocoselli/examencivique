"""Export the Android question banks for the Flask site."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "app/src/main/java/fr/examen/civique"
OUT = Path(__file__).resolve().parent / "data/practice.json"


def rows(block, category):
    questions = []
    for line in block.strip().splitlines():
        parts = line.strip().split("|")
        if len(parts) < 6:
            raise ValueError(f"Invalid {category} row: {line}")
        questions.append({"category": category, "prompt": parts[0],
                          "answers": parts[1:5], "detail": parts[5]})
    return questions


def main():
    civic = (SRC / "CivicQuestions.kt").read_text()
    culture = (SRC / "CultureQuestions.kt").read_text()
    data = rows(re.search(r'val civicBank = """(.*?)"""', civic, re.S).group(1), "Civique CSP")
    for category, block in re.findall(r'bank\("([^"]+)", """(.*?)"""\)', culture, re.S):
        data.extend(rows(block, category))
    OUT.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n")
    print(f"Exported {len(data)} practice questions")


if __name__ == "__main__":
    main()
