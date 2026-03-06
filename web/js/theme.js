// ── Ocean View Resort · Shared Theme Manager ──
// Include this script in every HTML page

(function () {
  // Apply saved theme immediately on load (before render)
  const saved = localStorage.getItem('ovr-theme') || 'dark';
  document.documentElement.setAttribute('data-theme', saved);

  // Once DOM is ready, update toggle button icon
  document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('themeBtn');
    if (btn) {
      btn.textContent = saved === 'dark' ? '🌙' : '☀️';
      btn.addEventListener('click', () => {
        const current = document.documentElement.getAttribute('data-theme');
        const next = current === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', next);
        localStorage.setItem('ovr-theme', next);
        btn.textContent = next === 'dark' ? '🌙' : '☀️';
      });
    }
  });
})();