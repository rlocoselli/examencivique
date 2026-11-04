"""Refresh the dated official parliamentary and administrative snapshots.

Run: python3 -m web.update_open_data
Requires beautifulsoup4 (web/requirements.txt).
"""
import json
import re
from datetime import datetime, timezone
from pathlib import Path
from urllib.request import Request, urlopen

from bs4 import BeautifulSoup

BASE = Path(__file__).resolve().parent
ASSEMBLY = 'https://www2.assemblee-nationale.fr/instances/liste/groupes_politiques/effectif'
SENATE = 'https://www.senat.fr/senateurs/grp.html'
REGIONS = 'https://geo.api.gouv.fr/regions'
DEPARTMENTS = 'https://geo.api.gouv.fr/departements'
MAP = 'https://raw.githubusercontent.com/gregoiredavid/france-geojson/master/regions-version-simplifiee.geojson'

# Illustrative cultural markers, not a claim that one practice defines a whole region.
CULTURE = {
 '11': ('La Seine, les musées et les grands monuments', 'Île-de-France : patrimoine urbain et cuisines venues du monde entier.'),
 '24': ('Châteaux de la Loire et jardins', 'Le Val de Loire est marqué par la Renaissance et la viticulture.'),
 '27': ('Vignobles et horlogerie', 'Bourgogne et Franche-Comté associent traditions viticoles, comté et savoir-faire horloger.'),
 '28': ('Falaises, bocage et ports', 'La Normandie est connue pour le cidre, le camembert et son héritage maritime.'),
 '32': ('Beffrois et patrimoine industriel', 'Les Hauts-de-France ont des traditions flamandes et picardes, dont les estaminets.'),
 '44': ('Cultures alsacienne, lorraine et champenoise', 'Le Grand Est réunit notamment la route des vins d’Alsace, la quiche lorraine et le champagne.'),
 '52': ('Loire et façade atlantique', 'Les Pays de la Loire mêlent patrimoine ligérien, marais salants et culture maritime.'),
 '53': ('Côtes et langues de Bretagne', 'La Bretagne valorise le breton et le gallo ; crêpes et fest-noz en sont des repères culturels.'),
 '75': ('Océan, vignobles et Pyrénées', 'La Nouvelle-Aquitaine inclut traditions basques, occitanes et charentaises.'),
 '76': ('Méditerranée, Pyrénées et langue occitane', 'L’Occitanie associe notamment cassoulet, culture catalane et héritage romain.'),
 '84': ('Alpes, volcans et vallées', 'L’Auvergne-Rhône-Alpes réunit traditions auvergnates, lyonnaises et savoyardes.'),
 '93': ('Méditerranée et Alpes du Sud', 'La Provence-Alpes-Côte d’Azur est marquée par la culture provençale et la cuisine méditerranéenne.'),
 '94': ('Montagnes et littoral insulaires', 'La Corse conserve une langue propre et des traditions de chants polyphoniques.'),
 '01': ('Archipel des Caraïbes', 'La Guadeloupe valorise le créole, le gwoka et une cuisine antillaise.'),
 '02': ('Île de la Caraïbe', 'La Martinique valorise le créole, le bèlè et les traditions antillaises.'),
 '03': ('Amazonie et côte atlantique', 'La Guyane rassemble de nombreuses langues et cultures, amérindiennes et créoles notamment.'),
 '04': ('Île de l’océan Indien', 'La Réunion est connue pour le maloya, le créole réunionnais et une cuisine métissée.'),
 '06': ('Archipel de l’océan Indien', 'Mayotte associe langues shimaoré et kibushi, lagon et traditions de l’océan Indien.'),
}

def fetch(url):
    with urlopen(Request(url, headers={'User-Agent': 'ParcoursCitoyen/1.0 (educational open-data updater)'}), timeout=30) as response:
        return response.read()

def cells(row):
    return [c.get_text(' ', strip=True) for c in row.find_all(['th', 'td'], recursive=False)]

def update_parliament(date):
    assembly = BeautifulSoup(fetch(ASSEMBLY), 'html.parser').find('table')
    if assembly is None:
        raise ValueError('Assembly table missing')
    agroups = []
    filled = vacant = seats = None
    for row in assembly.find_all('tr'):
        c = cells(row)
        if len(c) >= 5 and c[1] and c[1] != 'Groupe' and not c[1].startswith('Total groupe'):
            agroups.append({'name': c[1], 'seats': int(c[4])})
        elif c and c[0].startswith('Total des sièges pourvus'):
            filled = int(c[1])
        elif c and c[0].startswith('Sièges vacants'):
            vacant = int(c[1])
        elif c and c[0] == 'Total des sièges':
            seats = int(c[1])
    senate_html = fetch(SENATE).decode('utf-8')
    senate = BeautifulSoup(senate_html, 'html.parser').find('table')
    if senate is None:
        raise ValueError('Senate table missing')
    sgroups = []
    for row in senate.find_all('tr')[1:]:
        c = cells(row)
        if len(c) < 2:
            continue
        total = re.search(r'Total\s*:\s*(\d+)', c[1])
        members = re.search(r'Membres\s*:\s*(\d+)', c[1])
        if total or members:
            name = c[0].removeprefix('Groupe ').replace('Réunion administrative des Sénateurs ne figurant sur la liste d\'aucun groupe', 'Non inscrits')
            sgroups.append({'name': name, 'seats': int((total or members).group(1))})
    if sum(g['seats'] for g in agroups) != filled or filled + vacant != seats or sum(g['seats'] for g in sgroups) != 348:
        raise ValueError('Parliament totals do not reconcile; refusing to replace snapshot')
    obj = {'updated': date, 'assembly': {'seats': seats, 'filled': filled, 'vacant': vacant, 'groups': agroups, 'source': ASSEMBLY}, 'senate': {'seats': 348, 'filled': 348, 'vacant': 0, 'groups': sgroups, 'source': SENATE}}
    (BASE / 'data/parliament.json').write_text(json.dumps(obj, ensure_ascii=False, indent=2) + '\n')

def update_regions(date):
    regions = json.loads(fetch(REGIONS))
    departments = json.loads(fetch(DEPARTMENTS))
    if len(regions) != 18 or len(departments) != 101:
        raise ValueError('Unexpected administrative totals; refusing to replace snapshot')
    items = []
    for region in regions:
        ds = sorted(({'code': d['code'], 'name': d['nom']} for d in departments if d['codeRegion'] == region['code']), key=lambda d: d['code'])
        items.append({'code': region['code'], 'name': region['nom'], 'overseas': region['code'] in {'01','02','03','04','06'}, 'departments': ds, 'landscape': CULTURE[region['code']][0], 'culture': CULTURE[region['code']][1]})
    if sum(len(item['departments']) for item in items) != 101:
        raise ValueError('Department assignment incomplete')
    obj = {'updated': date, 'regions': sorted(items, key=lambda r: (r['overseas'], r['name'])), 'region_count': 18, 'department_count': 101, 'source_regions': REGIONS, 'source_departments': DEPARTMENTS, 'map_source': MAP}
    (BASE / 'data/regions.json').write_text(json.dumps(obj, ensure_ascii=False, indent=2) + '\n')
    geo = json.loads(fetch(MAP))
    paths = []
    def point(pair):
        lon, lat = pair
        return f'{(lon + 5.35) * 39 + 24:.1f},{(51.25 - lat) * 43 + 19:.1f}'
    for feature in geo['features']:
        code = feature['properties']['code']
        coordinates = feature['geometry']['coordinates']
        polygons = [coordinates] if feature['geometry']['type'] == 'Polygon' else coordinates
        parts = []
        for polygon in polygons:
            for ring in polygon:
                parts.append('M' + ' L'.join(point(pair) for pair in ring) + ' Z')
        paths.append(f'<path class="region-shape" data-region="{code}" tabindex="0" role="button" aria-label="{feature["properties"]["nom"]}" d="{" ".join(parts)}"/>')
    if len(paths) != 13:
        raise ValueError('Unexpected metropolitan map regions')
    svg = '<svg class="regions-map" viewBox="0 0 640 485" xmlns="http://www.w3.org/2000/svg" role="group" aria-label="Carte interactive des 13 régions de France métropolitaine et de Corse"><defs><filter id="region-shadow"><feDropShadow dx="0" dy="8" stdDeviation="8" flood-color="#081a30" flood-opacity=".3"/></filter></defs><g filter="url(#region-shadow)">' + ''.join(paths) + '</g></svg>\n'
    (BASE / 'templates/_regions_map.svg').write_text(svg)

if __name__ == '__main__':
    date = datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M UTC')
    update_parliament(date)
    update_regions(date)
    print(f'Official parliamentary and administrative snapshots updated: {date}')
