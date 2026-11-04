"""Generate Android catalogues from the same official lists used by Flask."""
import json
import re
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = Path(__file__).resolve().parent / "data"
TARGET = ROOT / "app/src/main/java/fr/examen/civique/OfficialBanks.kt"


def norm(value):
    return re.sub(r"[^a-z0-9]", "", unicodedata.normalize("NFKD", value).casefold())


def quote(value):
    return json.dumps(value, ensure_ascii=False).replace("$", "\\$")


def main():
    practice = json.loads((DATA / "practice.json").read_text())
    lookup = {norm(row["prompt"]): row for row in practice}
    lines = ["package fr.audeladedonnees.examen.civique", "", "// Generated from the official question lists; answers are independently authored teaching material."]
    for name in ("resident", "naturalisation"):
        data = json.loads((DATA / f"{name}_official.json").read_text())
        lines.extend([f"const val {name.upper()}_SOURCE = {quote(data['source'])}", f"val {name}Prompts = listOf("])
        lines.extend(f"    {quote(q['prompt'])}," for q in data["questions"])
        lines.append(")")
        lines.append(f"val {name}Bank = listOf(")
        for q in data["questions"]:
            row = lookup.get(norm(q["prompt"]))
            if row is None:
                continue
            options = ", ".join(quote(a) for a in row["answers"])
            lines.append(f"    CultureQuestion({quote(q['theme'])}, {quote(q['prompt'])}, listOf({options}), {quote(row['detail'])}),")
        lines.extend([")", ""])
    TARGET.write_text("\n".join(lines) + "\n")
    print(f"Generated {TARGET}")


if __name__ == "__main__":
    main()
