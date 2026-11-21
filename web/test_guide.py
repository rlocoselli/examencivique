import unittest

from app import ADDITIONAL_PRACTICE, TRACKS, app


class StudyGuideTests(unittest.TestCase):
    def setUp(self):
        self.client = app.test_client()

    def test_exam_banks_have_four_choices_and_an_explanation(self):
        self.assertGreaterEqual(len(TRACKS['resident']['quiz']), 170)
        self.assertGreaterEqual(len(TRACKS['naturalisation']['quiz']), 225)
        for row in ADDITIONAL_PRACTICE:
            self.assertEqual(len(row['answers']), 4)
            self.assertEqual(len(set(row['answers'])), 4)
            self.assertTrue(row['detail'].strip())
            self.assertIn(row['mode'], ('resident', 'naturalisation'))

    def test_guide_hymn_and_exam_pages(self):
        for path in ('/guide', '/hymne', '/quiz/resident?mode=exam',
                     '/quiz/naturalisation?mode=exam', '/static/audio/marseillaise_piano.ogg'):
            response = self.client.get(path)
            self.assertEqual(response.status_code, 200)
            response.close()
        self.assertIn(b'40 questions', self.client.get('/guide').data)
        self.assertIn(b'marseillaise_piano.ogg', self.client.get('/hymne').data)


if __name__ == '__main__':
    unittest.main()
