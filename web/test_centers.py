import unittest

from app import app, search_centers


class CenterSearchTests(unittest.TestCase):
    def test_city_and_postal_code_find_same_center(self):
        by_city = search_centers("Grenoble")
        by_postal = search_centers("38000")
        self.assertEqual(len(by_city), 1)
        self.assertEqual(by_city[0]["name"], by_postal[0]["name"])

    def test_accents_and_network_filter(self):
        self.assertEqual(search_centers("Saint Etienne"), search_centers("Saint-Étienne"))
        self.assertTrue(all("France Éducation International" in center["network"]
                            for center in search_centers("Paris", "fei")))

    def test_api_and_page_show_local_results(self):
        client = app.test_client()
        response = client.get("/api/centres?q=Grenoble")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json["total"], 1)
        page = client.get("/centres?ville=Grenoble")
        self.assertEqual(page.status_code, 200)
        self.assertIn(b"THE PLACE DEBY", page.data)


if __name__ == "__main__":
    unittest.main()
