import json
import unittest
from pathlib import Path
from app import app

BASE = Path(__file__).resolve().parent

class OpenDataTests(unittest.TestCase):
    def test_parliament_totals_and_pages(self):
        data = json.loads((BASE / 'data/parliament.json').read_text())
        for chamber in ('assembly', 'senate'):
            item = data[chamber]
            self.assertEqual(sum(g['seats'] for g in item['groups']), item['filled'])
            self.assertEqual(item['filled'] + item['vacant'], item['seats'])
        page = app.test_client().get('/sujets/politique').data.decode()
        self.assertIn('parliament-chart', page)
        self.assertIn(data['updated'], page)

    def test_region_department_coverage_and_map(self):
        data = json.loads((BASE / 'data/regions.json').read_text())
        self.assertEqual(len(data['regions']), 18)
        self.assertEqual(sum(len(r['departments']) for r in data['regions']), 101)
        self.assertEqual(sum(not r['overseas'] for r in data['regions']), 13)
        page = app.test_client().get('/sujets/geographie').data.decode()
        self.assertEqual(page.count('class="region-shape"'), 13)
        self.assertIn('Guadeloupe', page)

if __name__ == '__main__':
    unittest.main()
