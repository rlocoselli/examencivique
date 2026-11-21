import unittest

from app import ADDITIONAL_PRACTICE, TRACKS, app


class StudyGuideTests(unittest.TestCase):
    def setUp(self):
        self.client = app.test_client()

    def test_exam_banks_have_four_choices_and_an_explanation(self):
        self.assertEqual(len(TRACKS['resident']['quiz']), 40)
        self.assertGreaterEqual(len(TRACKS['naturalisation']['quiz']), 40)
        for mode in ('resident', 'naturalisation'):
            self.assertEqual(len({q['prompt'] for q in TRACKS[mode]['quiz']}), len(TRACKS[mode]['quiz']))
            for question in TRACKS[mode]['quiz']:
                self.assertEqual(len(question['answers']), 4)
                self.assertEqual(len(set(question['answers'])), 4)
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

    def test_catalogue_shows_corrections_and_marks_unanswered_questions(self):
        page = self.client.get('/parcours/resident').data.decode()
        self.assertIn('Voir la réponse et l’explication', page)
        self.assertIn('Réponse pédagogique :', page)
        self.assertIn('aucun corrigé fiable', page)
        self.assertIn('Où est le siège du Parlement européen ?', page)

    def test_resident_exam_uses_reviewed_choices(self):
        questions = {q['prompt']: q for q in TRACKS['resident']['quiz']}
        constitution = questions['Quel texte est le plus difficile à modifier ?']
        self.assertEqual(constitution['answers'][0], 'La Constitution')
        self.assertIn('Le Code civil', constitution['answers'])
        self.assertIn('article 89', constitution['detail'])
        self.assertTrue(all('ticket de caisse' not in answer.casefold()
                            for q in TRACKS['resident']['quiz'] for answer in q['answers']))


if __name__ == '__main__':
    unittest.main()
