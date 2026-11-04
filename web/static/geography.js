const placeById = Object.fromEntries(geographyPlaces.map(place => [place.id, place]));
const mapPoints = [...document.querySelectorAll('.geo-point')];
const placeButtons = [...document.querySelectorAll('.geo-picker [data-place]')];
const challengeTitle = document.getElementById('challenge-title');
const challengeFeedback = document.getElementById('challenge-feedback');
const challengeStart = document.getElementById('challenge-start');
let challenge = [];
let challengeIndex = 0;
let challengeScore = 0;

function selectPlace(id) {
  const place = placeById[id];
  if (!place) return;
  document.getElementById('geo-name').textContent = place.name;
  document.getElementById('geo-region').textContent = place.region;
  document.getElementById('geo-fact').textContent = place.fact;
  mapPoints.forEach(point => point.classList.toggle('is-active', point.dataset.place === id));
  placeButtons.forEach(button => button.classList.toggle('is-active', button.dataset.place === id));
  if (challenge.length && challengeIndex < challenge.length) {
    const good = id === challenge[challengeIndex].id;
    if (good) challengeScore++;
    challengeIndex++;
    if (challengeIndex === challenge.length) {
      challengeTitle.textContent = `Défi terminé : ${challengeScore} / ${challenge.length}`;
      challengeFeedback.textContent = 'Vous pouvez recommencer pour améliorer votre score.';
      challengeStart.hidden = false;
      challengeStart.textContent = 'Rejouer →';
      challenge = [];
    } else {
      challengeTitle.textContent = `Trouvez ${challenge[challengeIndex].name}`;
      challengeFeedback.textContent = `${good ? 'Bien joué !' : `C’était ${challenge[challengeIndex - 1].name}.`} Score : ${challengeScore} / ${challengeIndex}`;
    }
  }
}

mapPoints.forEach(point => {
  point.addEventListener('click', () => selectPlace(point.dataset.place));
  point.addEventListener('keydown', event => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      selectPlace(point.dataset.place);
    }
  });
});
placeButtons.forEach(button => button.addEventListener('click', () => selectPlace(button.dataset.place)));
challengeStart.addEventListener('click', () => {
  challenge = [...geographyPlaces].sort(() => Math.random() - .5).slice(0, 5);
  challengeIndex = 0;
  challengeScore = 0;
  challengeTitle.textContent = `Trouvez ${challenge[0].name}`;
  challengeFeedback.textContent = 'Cliquez sur le point correspondant, ou choisissez une ville dans la liste.';
  challengeStart.hidden = true;
});
selectPlace('paris');
