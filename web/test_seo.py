import json
import unittest
from xml.etree import ElementTree

from bs4 import BeautifulSoup

from app import SITE_ORIGIN, app


class SeoTests(unittest.TestCase):
    def setUp(self):
        self.client = app.test_client()

    def test_indexable_pages_have_unique_metadata_and_canonicals(self):
        paths = ['/', '/guide', '/hymne', '/francais', '/centres',
                 '/parcours/csp', '/parcours/resident', '/parcours/naturalisation',
                 '/sujets/politique', '/sujets/geographie', '/sujets/histoire']
        descriptions = []
        for path in paths:
            response = self.client.get(path)
            self.assertEqual(response.status_code, 200)
            page = BeautifulSoup(response.data, 'html.parser')
            descriptions.append(page.find('meta', attrs={'name': 'description'})['content'])
            self.assertEqual(page.find('link', rel='canonical')['href'], SITE_ORIGIN + path)
            self.assertEqual(page.find('meta', attrs={'name': 'robots'})['content'], 'index,follow')
            self.assertTrue(page.title.text.strip())
        self.assertEqual(len(descriptions), len(set(descriptions)))

    def test_filters_canonicalize_and_random_quizzes_are_not_indexed(self):
        filtered = BeautifulSoup(self.client.get('/parcours/resident?theme=Droits+et+devoirs').data, 'html.parser')
        self.assertEqual(filtered.find('link', rel='canonical')['href'], SITE_ORIGIN + '/parcours/resident')
        quiz = BeautifulSoup(self.client.get('/quiz/resident?mode=exam').data, 'html.parser')
        self.assertEqual(quiz.find('meta', attrs={'name': 'robots'})['content'], 'noindex,follow')
        self.assertIsNone(quiz.find('link', rel='canonical'))

    def test_sitemap_and_robots_reference_only_public_pages(self):
        response = self.client.get('/sitemap.xml')
        self.assertEqual(response.mimetype, 'application/xml')
        root = ElementTree.fromstring(response.data)
        urls = {element.text for element in root.iter() if element.tag.endswith('loc')}
        self.assertEqual(len(urls), 11)
        self.assertIn(SITE_ORIGIN + '/', urls)
        self.assertFalse(any('/quiz/' in url or '?' in url for url in urls))
        self.assertIn(f'Sitemap: {SITE_ORIGIN}/sitemap.xml', self.client.get('/robots.txt').text)
        home = BeautifulSoup(self.client.get('/').data, 'html.parser')
        structured = json.loads(home.find('script', type='application/ld+json').string)
        self.assertEqual(structured['@type'], 'WebSite')
        self.assertEqual(structured['url'], SITE_ORIGIN + '/')


if __name__ == '__main__':
    unittest.main()
