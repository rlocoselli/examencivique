import unittest
from app import app, GEOGRAPHY_POOL, HISTORY_POOL, POLITICS_POOL
from subjects import HISTORY_EVENTS, PLACES


class SubjectPagesTests(unittest.TestCase):
    def setUp(self):
        self.client = app.test_client()

    def test_subject_pages_and_quizzes(self):
        for slug in ("politique", "geographie", "histoire"):
            self.assertEqual(self.client.get(f"/sujets/{slug}").status_code, 200)
            self.assertEqual(self.client.get(f"/quiz/{slug}").status_code, 200)
        self.assertGreaterEqual(len(POLITICS_POOL), 20)
        self.assertGreaterEqual(len(HISTORY_POOL), 15)
        self.assertGreaterEqual(len(GEOGRAPHY_POOL), 20)

    def test_map_points_and_timeline_have_matching_content(self):
        map_page = self.client.get("/sujets/geographie").data.decode()
        self.assertEqual(map_page.count('class="geo-point"'), len(PLACES))
        self.assertEqual(map_page.count('data-place="'), len(PLACES) * 2)
        self.assertEqual(map_page.count('role="button" tabindex="0"'), len(PLACES))
        history_page = self.client.get("/sujets/histoire").data.decode()
        self.assertEqual(history_page.count('class="timeline-content"'), len(HISTORY_EVENTS))


if __name__ == "__main__":
    unittest.main()
