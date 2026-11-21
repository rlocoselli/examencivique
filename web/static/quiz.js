const quiz = document.getElementById('quiz');
const answers = document.getElementById('answers');
const feedback = document.getElementById('feedback');
const next = document.getElementById('next');
let index = 0, correct = 0, selected = false, seconds = minutes * 60;
const responses = [];
const shuffled = deck.map(q => ({...q, choices: q.answers.map((answer, i) => ({answer, good: i === 0})).sort(() => Math.random() - .5)}));
function render() {
  if (index >= shuffled.length || seconds <= 0) {
    clearInterval(timer);
    quiz.innerHTML = `<div class="result"><span class="result-icon">✦</span><p class="eyebrow dark">VOTRE BILAN</p><h2>${correct} / ${shuffled.length}</h2><p>${exam ? (correct >= 32 ? 'Objectif de connaissances atteint.' : 'Poursuivez vos révisions.') : 'Chaque question vous aide à progresser.'} Les corrections sont pédagogiques et les résultats ne constituent pas une attestation officielle.</p><button class="button primary" onclick="location.reload()">Recommencer →</button></div>`;
    const review = document.createElement('div'); review.className = 'quiz-review';
    const heading = document.createElement('h3'); heading.textContent = 'Revoir les réponses'; review.append(heading);
    shuffled.forEach((question, position) => {
      const item = document.createElement('details');
      const summary = document.createElement('summary');
      summary.textContent = `${position + 1}. ${question.prompt} — ${responses[position]?.good ? '✓' : 'À revoir'}`;
      const given = document.createElement('p'); given.textContent = `Votre réponse : ${responses[position]?.answer || 'Aucune réponse'}`;
      const expected = document.createElement('p'); expected.className = 'review-answer'; expected.textContent = `Réponse attendue : ${question.answers[0]}`;
      const explanation = document.createElement('p'); explanation.textContent = question.detail;
      item.append(summary, given, expected, explanation); review.append(item);
    });
    quiz.querySelector('.result').append(review);
    return;
  }
  const q = shuffled[index]; selected = false;
  document.getElementById('step').textContent = `QUESTION ${index + 1} / ${shuffled.length}`;
  document.getElementById('score').textContent = `${correct} bonne${correct > 1 ? 's' : ''} réponse${correct > 1 ? 's' : ''}`;
  document.getElementById('progress').style.width = `${index / shuffled.length * 100}%`;
  document.getElementById('theme').textContent = q.theme || q.category;
  document.getElementById('prompt').textContent = q.prompt;
  document.getElementById('provenance').textContent = q.original
    ? 'Question originale · Choix et correction rédigés pour cet entraînement.'
    : 'Intitulé de la liste publiée · Choix et correction rédigés pour cet entraînement.';
  answers.replaceChildren(); feedback.replaceChildren(); next.hidden = true;
  q.choices.forEach((choice, i) => {
    const button = document.createElement('button'); button.type = 'button'; button.className = 'answer';
    const letter = document.createElement('b'); letter.textContent = String.fromCharCode(65 + i);
    const label = document.createElement('span'); label.textContent = choice.answer;
    button.append(letter, label);
    button.addEventListener('click', () => {
      if (selected) return; selected = true;
      if (choice.good) correct++;
      responses[index] = {answer: choice.answer, good: choice.good};
      [...answers.children].forEach((child, j) => {child.disabled = true; if (q.choices[j].good) child.classList.add('correct'); else if (j === i) child.classList.add('incorrect');});
      const heading = document.createElement('strong'); heading.textContent = choice.good ? 'Bonne réponse !' : 'À retenir';
      const detail = document.createElement('p'); detail.textContent = q.detail;
      feedback.append(heading, detail); next.hidden = false;
      next.textContent = index === shuffled.length - 1 ? 'Voir mon bilan →' : 'Question suivante →';
    });
    answers.append(button);
  });
}
next.addEventListener('click', () => { index++; render(); });
const timer = setInterval(() => { seconds--; document.getElementById('clock').textContent = `${String(Math.floor(seconds / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`; if (seconds <= 0) render(); }, 1000);
render();
