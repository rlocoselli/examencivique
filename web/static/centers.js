const centerForm = document.getElementById('center-search');
const centerInput = document.getElementById('ville');
const resultsTitle = document.getElementById('results-title');
const resultsCount = document.getElementById('results-count');
const resultsList = document.getElementById('results-list');
const resultsLimit = document.getElementById('results-limit');
let pendingSearch;
let activeRequest;

function createLink(label, href, className = '') {
  const link = document.createElement('a');
  link.textContent = label;
  link.href = href;
  if (className) link.className = className;
  if (!href.startsWith('tel:')) {
    link.target = '_blank';
    link.rel = 'noopener noreferrer';
  }
  return link;
}

function centerCard(center) {
  const article = document.createElement('article');
  article.className = 'center-result';
  const main = document.createElement('div');
  main.className = 'center-result-main';
  const top = document.createElement('div');
  top.className = 'center-result-top';
  const network = document.createElement('span');
  network.className = 'center-network';
  network.textContent = center.network;
  top.append(network);
  if (center.confidence !== 'vérifié') {
    const status = document.createElement('span');
    status.className = 'center-unconfirmed';
    status.textContent = 'Coordonnées à vérifier';
    top.append(status);
  }
  const name = document.createElement('h4');
  name.textContent = center.name;
  const location = document.createElement('p');
  location.className = 'center-location';
  location.textContent = `⌖ ${center.address ? center.address + ', ' : ''}${center.postal_code} ${center.city}`;
  main.append(top, name, location);
  if (center.mentions) {
    const mentions = document.createElement('p');
    mentions.className = 'center-mentions';
    mentions.textContent = center.mentions;
    main.append(mentions);
  }
  const actions = document.createElement('div');
  actions.className = 'center-result-actions';
  if (center.phone) actions.append(createLink(center.phone, `tel:${center.phone}`));
  if (center.website) actions.append(createLink('Site du centre ↗', center.website));
  if (center.sources.length) actions.append(createLink('Vérifier dans l’annuaire officiel ↗', center.sources[0], 'source-link'));
  article.append(main, actions);
  return article;
}

function showMessage(title, detail) {
  const box = document.createElement('div');
  box.className = 'results-empty';
  const strong = document.createElement('strong');
  strong.textContent = title;
  const paragraph = document.createElement('p');
  paragraph.textContent = detail;
  box.append(strong, paragraph);
  resultsList.replaceChildren(box);
}

async function searchCenters() {
  if (activeRequest) activeRequest.abort();
  const query = centerInput.value.trim();
  const network = centerForm.querySelector('input[name="reseau"]:checked').value;
  const pageUrl = new URL(window.location.href);
  if (query) pageUrl.searchParams.set('ville', query);
  else pageUrl.searchParams.delete('ville');
  if (network !== 'all') pageUrl.searchParams.set('reseau', network);
  else pageUrl.searchParams.delete('reseau');
  window.history.replaceState(null, '', pageUrl);
  resultsLimit.hidden = true;
  if (!query) {
    resultsTitle.textContent = 'Recherchez un centre près de chez vous';
    resultsCount.hidden = true;
    showMessage('Commencez par saisir votre ville.', 'La recherche accepte aussi un code postal, un département ou le nom d’un organisme.');
    return;
  }
  activeRequest = new AbortController();
  try {
    const apiUrl = new URL('/api/centres', window.location.origin);
    apiUrl.searchParams.set('q', query);
    apiUrl.searchParams.set('reseau', network);
    const response = await fetch(apiUrl, {signal: activeRequest.signal});
    if (!response.ok) throw new Error('Search failed');
    const data = await response.json();
    resultsTitle.textContent = `${data.total} centre${data.total === 1 ? '' : 's'} trouvé${data.total === 1 ? '' : 's'} pour « ${query} »`;
    resultsCount.textContent = `${data.total} résultat${data.total === 1 ? '' : 's'}`;
    resultsCount.hidden = false;
    if (data.total === 0) {
      showMessage(`Aucun centre trouvé pour « ${query} ».`, 'Essayez le code postal, le département ou une ville voisine. Les annuaires officiels peuvent contenir des ajouts récents.');
    } else {
      resultsList.replaceChildren(...data.results.map(centerCard));
    }
    if (data.total > data.results.length) {
      resultsLimit.textContent = `Les ${data.results.length} premiers centres sont affichés. Précisez votre recherche pour voir les autres.`;
      resultsLimit.hidden = false;
    }
  } catch (error) {
    if (error.name !== 'AbortError') showMessage('Recherche momentanément indisponible.', 'Réessayez ou consultez les annuaires officiels ci-dessous.');
  }
}

centerForm.addEventListener('submit', event => {
  event.preventDefault();
  clearTimeout(pendingSearch);
  searchCenters();
});
centerInput.addEventListener('input', () => {
  clearTimeout(pendingSearch);
  pendingSearch = setTimeout(searchCenters, 250);
});
centerForm.querySelectorAll('input[name="reseau"]').forEach(radio => {
  radio.addEventListener('change', () => {
    clearTimeout(pendingSearch);
    searchCenters();
  });
});
document.querySelectorAll('[data-query]').forEach(example => {
  example.addEventListener('click', event => {
    event.preventDefault();
    centerInput.value = example.dataset.query;
    searchCenters();
    centerInput.focus();
  });
});
