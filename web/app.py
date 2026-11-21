"""Parcours citoyen: a companion Flask site for the Android app."""
import json
import random
import re
import unicodedata
from pathlib import Path

from flask import Flask, abort, jsonify, render_template, request
try:
    from .subjects import HISTORY_EVENTS, PLACES
except ImportError:
    from subjects import HISTORY_EVENTS, PLACES

BASE = Path(__file__).resolve().parent
app = Flask(__name__)


def read(name):
    return json.loads((BASE / "data" / name).read_text())


def key(text):
    plain = unicodedata.normalize("NFKD", text).casefold()
    return re.sub(r"[^a-z0-9]", "", plain)


THEME_CONTEXT = {
    "Principes et valeurs de la République": "Révisez les symboles, la liberté de conscience, la laïcité et l’égalité devant la loi. Cherchez comment ce principe s’applique aux personnes et aux services publics.",
    "Système institutionnel et politique": "Identifiez l’institution concernée et sa compétence. Le Parlement vote les lois ; le gouvernement conduit la politique nationale ; les collectivités gèrent les affaires locales.",
    "Droits et devoirs": "Repérez le droit en jeu, ses limites prévues par la loi et les droits des autres personnes. Une situation concrète aide à distinguer une liberté d’une obligation.",
    "Histoire géographie et culture": "Situez ce fait dans la chronologie ou sur la carte, puis reliez-le à son rôle dans la France d’aujourd’hui.",
    "Vivre dans la société française": "Cherchez le service compétent et vérifiez les conditions de la démarche selon la situation personnelle sur les sites officiels.",
}


PRACTICE = read("practice.json")
ADDITIONAL_PRACTICE = read("additional_practice.json")
CENTER_DIRECTORY = read("centers.json")
PARLIAMENT = read("parliament.json")
REGIONS = read("regions.json")
CENTERS = CENTER_DIRECTORY["centers"]
BY_PROMPT = {key(q["prompt"]): q for q in PRACTICE}
TRACKS = {
    "csp": {"title": "Carte de séjour pluriannuelle", "short": "Séjour pluriannuel", "data": read("csp_official.json")},
    "resident": {"title": "Carte de résident", "short": "Carte de résident", "data": read("resident_official.json")},
    "naturalisation": {"title": "Naturalisation", "short": "Naturalisation", "data": read("naturalisation_official.json")},
}
for track in TRACKS.values():
    track["quiz"] = [dict(BY_PROMPT[key(q["prompt"])], theme=q["theme"])
                     for q in track["data"]["questions"] if key(q["prompt"]) in BY_PROMPT]
for question in ADDITIONAL_PRACTICE:
    TRACKS[question["mode"]]["quiz"].append(question)

# Only reviewed choices are served in quizzes. The full answer bank remains
# available for catalogue explanations, where no distractors are displayed.
REVIEWED_RESIDENT_CHOICES = read("reviewed_resident_choices.json")
REVIEWED_RESIDENT_EXPLANATIONS = read("reviewed_resident_explanations.json")
for slug in ("resident", "naturalisation"):
    track = TRACKS[slug]
    track["catalogue_answers"] = track["quiz"]
    base = [q for q in track["quiz"] if "category" in q]
    original = [q for q in track["quiz"] if q.get("original")]
    if slug == "resident":
        by_prompt = {q["prompt"]: q for q in track["quiz"]}
        reviewed = []
        for prompt, wrong in REVIEWED_RESIDENT_CHOICES.items():
            question = dict(by_prompt[prompt])
            question["answers"] = [question["answers"][0], *wrong]
            question["detail"] = REVIEWED_RESIDENT_EXPLANATIONS[prompt]
            reviewed.append(question)
        track["quiz"] = base + original + reviewed
    else:
        track["quiz"] = base + original

POLITICS_POOL = [q for q in TRACKS["csp"]["quiz"] if q["theme"] == "Système institutionnel et politique"]
HISTORY_POOL = [q for q in TRACKS["csp"]["quiz"] if q["theme"] == "Histoire géographie et culture"][:17]
GEOGRAPHY_POOL = [q for q in PRACTICE if q["category"] == "Régions & paysages"]

LESSONS = [
    {"title": "La vie à la mairie", "level": "A2", "text": "Samira vient d'emménager à Lyon. Elle se rend à la mairie pour inscrire sa fille à l'école. L'agent lui demande un justificatif de domicile et le livret de famille. Samira prépare les documents, puis reçoit une confirmation par courriel.",
     "questions": [{"prompt": "Pourquoi Samira va-t-elle à la mairie ?", "answers": ["Pour inscrire sa fille à l'école", "Pour demander un passeport", "Pour prendre le train"], "correct": 0}, {"prompt": "Quels documents prépare-t-elle ?", "answers": ["Un justificatif de domicile et le livret de famille", "Un billet de train", "Une ordonnance"], "correct": 0}]},
    {"title": "Un rendez-vous médical", "level": "B1", "text": "Thomas a une douleur persistante au genou. Il appelle son médecin traitant et obtient un rendez-vous jeudi matin. Après la consultation, le médecin lui remet une ordonnance et lui conseille de se reposer. Thomas présente sa carte Vitale pour faciliter le remboursement de ses soins.",
     "questions": [{"prompt": "Quand Thomas consulte-t-il le médecin ?", "answers": ["Jeudi matin", "Lundi soir", "Samedi midi"], "correct": 0}, {"prompt": "À quoi sert sa carte Vitale ?", "answers": ["À faciliter le remboursement des soins", "À payer son loyer", "À voter"], "correct": 0}]},
    {"title": "Une association de quartier", "level": "B1", "text": "Chaque samedi, une association organise une collecte de livres dans le quartier. Les bénévoles trient les ouvrages et les distribuent à des familles. Les habitants peuvent participer librement. Cette initiative permet de partager la culture et de rencontrer ses voisins.",
     "questions": [{"prompt": "Que font les bénévoles ?", "answers": ["Ils trient et distribuent des livres", "Ils vendent des voitures", "Ils organisent une élection"], "correct": 0}, {"prompt": "La participation est-elle obligatoire ?", "answers": ["Non, elle est libre", "Oui, pour tous", "Seulement pour les élus"], "correct": 0}]},
]


@app.route("/")
def home():
    return render_template("home.html", tracks=TRACKS)


@app.route("/parcours/<slug>")
def track(slug):
    item = TRACKS.get(slug)
    if item is None:
        abort(404)
    theme = request.args.get("theme", "")
    search = request.args.get("q", "").strip()
    questions = [q for q in item["data"]["questions"] if (not theme or q["theme"] == theme)
                 and (not search or search.casefold() in q["prompt"].casefold())]
    explanations = {key(q["prompt"]): q for q in item.get("catalogue_answers", item["quiz"])}
    explanations.update({key(q["prompt"]): q for q in item["quiz"]})
    questions = [dict(q, practice=explanations.get(key(q["prompt"])),
                      context=THEME_CONTEXT.get(q["theme"], "Consultez la fiche officielle de ce thème."))
                 for q in questions]
    themes = list(dict.fromkeys(q["theme"] for q in item["data"]["questions"]))
    return render_template("track.html", slug=slug, item=item, questions=questions,
                           themes=themes, theme=theme, search=search)


@app.route("/quiz/<slug>")
def quiz(slug):
    if slug in TRACKS:
        pool = TRACKS[slug]["quiz"]
        title = TRACKS[slug]["title"]
    elif slug in ("grammaire", "litterature", "regions", "gastronomie"):
        labels = {"grammaire": "Grammaire", "litterature": "Littérature", "regions": "Régions & paysages", "gastronomie": "Gastronomie"}
        title = labels[slug]
        pool = [q for q in PRACTICE if q["category"] == title]
    elif slug in ("politique", "histoire", "geographie"):
        title = {"politique": "Institutions et politique", "histoire": "Histoire de France", "geographie": "Géographie de la France"}[slug]
        pool = {"politique": POLITICS_POOL, "histoire": HISTORY_POOL, "geographie": GEOGRAPHY_POOL}[slug]
    else:
        abort(404)
    exam = slug in TRACKS and request.args.get("mode") == "exam"
    deck = random.sample(pool, min(40 if exam else 20, len(pool)))
    return render_template("quiz.html", slug=slug, title=title, deck=deck, exam=exam,
                           minutes=45 if exam else 20)


@app.route("/francais")
def french():
    return render_template("french.html", lessons=LESSONS)


@app.route("/guide")
def guide():
    return render_template("guide.html")


@app.route("/hymne")
def hymn():
    return render_template("hymn.html")


@app.route("/sujets/<slug>")
def subject(slug):
    if slug == "politique":
        return render_template("politics.html", question_count=len(POLITICS_POOL), parliament=PARLIAMENT)
    if slug == "geographie":
        return render_template("geography.html", places=PLACES, question_count=len(GEOGRAPHY_POOL), regions=REGIONS)
    if slug == "histoire":
        return render_template("history.html", events=HISTORY_EVENTS, question_count=len(HISTORY_POOL))
    abort(404)


@app.route("/centres")
def centers():
    query = request.args.get("ville", "").strip()[:80]
    network = request.args.get("reseau", "all")
    if network not in {"all", "fei", "cci"}:
        network = "all"
    matches = search_centers(query, network)
    return render_template("centers.html", query=query, network=network,
                           matches=matches[:60], total=len(matches),
                           directory=CENTER_DIRECTORY)


def search_normalize(value):
    value = unicodedata.normalize("NFKD", value).casefold()
    value = "".join(char for char in value if not unicodedata.combining(char))
    return " ".join(re.findall(r"[a-z0-9]+", value))


def search_centers(query, network="all"):
    query = search_normalize(query)
    if not query:
        return []
    tokens = query.split()
    matches = []
    for center in CENTERS:
        net = search_normalize(center["network"])
        if network == "fei" and "france education international" not in net:
            continue
        if network == "cci" and "cci paris" not in net:
            continue
        city = search_normalize(center["city"])
        postal = search_normalize(center["postal_code"])
        name = search_normalize(center["name"])
        department = search_normalize(center["department"])
        haystack = " ".join((city, postal, name, department, search_normalize(center["address"])))
        if not all(token in haystack for token in tokens):
            continue
        score = 0 if query in {city, postal} else 1 if city.startswith(query) else 2 if query in city else 3 if postal.startswith(query) else 4 if query in name else 5
        matches.append((score, city, name, center))
    return [item[3] for item in sorted(matches, key=lambda item: item[:3])]


@app.route("/api/centres")
def centers_api():
    query = request.args.get("q", "").strip()[:80]
    network = request.args.get("reseau", "all")
    if network not in {"all", "fei", "cci"}:
        network = "all"
    matches = search_centers(query, network)
    return jsonify({"total": len(matches), "results": matches[:60],
                    "surveyed": "2026-10-10"})


if __name__ == "__main__":
    app.run(debug=True)
