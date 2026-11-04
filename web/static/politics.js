const chart = document.getElementById('parliament-chart');
const groupList = document.getElementById('parliament-groups');
const palette = ['#255c88','#e67764','#6a66a8','#c1934f','#397a75','#9b6b8b','#547ba7','#aa8557','#5792a6','#786c54','#b15e6c','#8a9baa'];
let chamber = 'assembly';
let selected = null;

function renderParliament() {
  const data = parliamentData[chamber];
  const groups = data.groups;
  document.getElementById('chamber-label').textContent = chamber === 'assembly' ? 'ASSEMBLÉE NATIONALE' : 'SÉNAT';
  document.getElementById('chamber-total').textContent = `${data.filled} sièges pourvus sur ${data.seats}${data.vacant ? ` · ${data.vacant} vacants` : ''}`;
  const cx = 270, cy = 254, outer = 220, inner = 78;
  const dots = [];
  const seats = groups.flatMap((group, index) => Array.from({length: group.seats}, () => index));
  if (data.vacant) seats.push(...Array(data.vacant).fill(-1));
  // Semi-circular rows, filled from left to right; this is a proportional diagram, not physical seating.
  const rows = 16;
  let positions = [];
  for (let row = 0; row < rows; row++) {
    const radius = inner + (outer - inner) * row / (rows - 1);
    const count = Math.round((Math.PI * radius) / 12);
    for (let i = 0; i < count; i++) {
      const angle = Math.PI - (i + .5) * Math.PI / count;
      positions.push({x: cx + radius * Math.cos(angle), y: cy - radius * Math.sin(angle)});
    }
  }
  positions = positions.slice(0, seats.length);
  const svg = document.createElementNS('http://www.w3.org/2000/svg','svg');
  svg.setAttribute('viewBox','0 0 540 275');
  svg.setAttribute('aria-hidden','true');
  seats.forEach((groupIndex, i) => {
    const point = positions[i];
    if (!point) return;
    const circle = document.createElementNS('http://www.w3.org/2000/svg','circle');
    circle.setAttribute('cx',point.x.toFixed(1)); circle.setAttribute('cy',point.y.toFixed(1)); circle.setAttribute('r','4.1');
    circle.setAttribute('fill',groupIndex < 0 ? '#ced8df' : palette[groupIndex % palette.length]);
    circle.setAttribute('class',`parliament-dot${selected !== null && groupIndex !== selected ? ' is-dim' : ''}`);
    if (groupIndex >= 0) { circle.setAttribute('tabindex','0'); circle.setAttribute('role','button'); circle.setAttribute('aria-label',`${groups[groupIndex].name} : ${groups[groupIndex].seats} sièges`); circle.addEventListener('click',() => selectGroup(groupIndex)); circle.addEventListener('keydown',e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); selectGroup(groupIndex); } }); }
    svg.appendChild(circle);
  });
  chart.replaceChildren(svg);
  groupList.replaceChildren();
  groups.forEach((group,index) => {
    const button = document.createElement('button'); button.type = 'button'; button.className = `group-row${selected === index ? ' is-active' : ''}`;
    button.setAttribute('aria-pressed',String(selected === index));
    const dot = document.createElement('i'); dot.style.background = palette[index % palette.length];
    const label = document.createElement('span'); label.textContent = group.name;
    const count = document.createElement('b'); count.textContent = group.seats;
    button.append(dot,label,count); button.addEventListener('click',() => selectGroup(index)); groupList.appendChild(button);
  });
  const group = selected === null ? null : groups[selected];
  document.getElementById('group-name').textContent = group ? group.name : 'Sélectionnez un groupe';
  document.getElementById('group-value').textContent = group ? `${group.seats} sièges · ${(100 * group.seats / data.seats).toFixed(1).replace('.', ',')} % des ${data.seats} sièges` : 'Les points représentent les sièges, répartis proportionnellement.';
}
function selectGroup(index) { selected = selected === index ? null : index; renderParliament(); }
document.querySelectorAll('[data-chamber]').forEach(tab => tab.addEventListener('click',() => {
  chamber = tab.dataset.chamber; selected = null;
  document.querySelectorAll('[data-chamber]').forEach(other => other.setAttribute('aria-selected', String(other === tab)));
  renderParliament();
}));
renderParliament();
