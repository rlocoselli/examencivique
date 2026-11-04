const regionsByCode = Object.fromEntries(regionsData.regions.map(region => [region.code, region]));
const regionShapes = [...document.querySelectorAll('.region-shape')];
const regionButtons = [...document.querySelectorAll('.overseas-pickers [data-region]')];
const regionSelect = document.getElementById('region-select');
function selectRegion(code) {
  const region = regionsByCode[code];
  if (!region) return;
  regionSelect.value = code;
  document.getElementById('region-name').textContent = region.name;
  document.getElementById('region-landscape').textContent = region.landscape;
  document.getElementById('region-culture').textContent = region.culture;
  document.getElementById('department-count').textContent = region.departments.length;
  const list = document.getElementById('department-list');
  list.replaceChildren();
  for (const department of region.departments) {
    const chip = document.createElement('span');
    chip.textContent = `${department.code} · ${department.name}`;
    list.appendChild(chip);
  }
  regionShapes.forEach(shape => shape.classList.toggle('is-active', shape.dataset.region === code));
  regionButtons.forEach(button => button.classList.toggle('is-active', button.dataset.region === code));
}
regionShapes.forEach(shape => {
  shape.addEventListener('click', () => selectRegion(shape.dataset.region));
  shape.addEventListener('keydown', event => {
    if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); selectRegion(shape.dataset.region); }
  });
});
regionButtons.forEach(button => button.addEventListener('click', () => selectRegion(button.dataset.region)));
regionSelect.addEventListener('change', () => selectRegion(regionSelect.value));
selectRegion('11');
