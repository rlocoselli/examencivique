const timelineButtons = [...document.querySelectorAll('.timeline-filters button')];
const timelineEvents = [...document.querySelectorAll('.timeline li')];
timelineButtons.forEach(button => {
  button.addEventListener('click', () => {
    const group = button.dataset.group;
    timelineButtons.forEach(item => item.setAttribute('aria-pressed', String(item === button)));
    timelineEvents.forEach(event => { event.hidden = group !== 'all' && event.dataset.group !== group; });
  });
});
