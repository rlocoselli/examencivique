document.querySelectorAll('.reading-card').forEach(card => {
  card.querySelector('.check-reading').addEventListener('click', () => {
    const groups = [...card.querySelectorAll('fieldset')];
    const chosen = groups.map(group => group.querySelector('input:checked'));
    const output = card.querySelector('.reading-feedback');
    if (chosen.some(value => !value)) {output.textContent = 'Répondez à chaque question avant de vérifier.'; return;}
    const score = chosen.filter(value => value.dataset.correct === 'true').length;
    output.textContent = `${score} bonne${score > 1 ? 's' : ''} réponse${score > 1 ? 's' : ''} sur ${groups.length}.`;
    groups.forEach(group => group.querySelectorAll('label').forEach(label => {
      const input = label.querySelector('input'); label.classList.toggle('right', input.dataset.correct === 'true');
      label.classList.toggle('wrong', input.checked && input.dataset.correct !== 'true');
    }));
  });
});
