// ── Ocean View Resort · Shared App Utilities ──
// Include this script in every page AFTER theme.js

(function () {

  // ─── CONFIG ───────────────────────────────────────────
  const API = '';
  const token = () => localStorage.getItem('token');
  const getRole = () => localStorage.getItem('role') || 'STAFF';
  const getUsername = () => localStorage.getItem('username') || 'User';

  // ─── INIT ON DOM READY ────────────────────────────────
  document.addEventListener('DOMContentLoaded', async () => {
    if (!token()) { window.location.href = '/login.html'; return; }

    // Set user info in sidebar
    const uName = document.getElementById('userName');
    const uAvatar = document.getElementById('userAvatar');
    if (uName) uName.textContent = getUsername();
    if (uAvatar) uAvatar.textContent = getUsername().charAt(0).toUpperCase();

    // Load role from server and cache it
    await syncRole();

    // Show role badge in sidebar footer if present
    renderRoleBadge();

    // Load reservation count badge for nav
    loadNavBadge();

    // Inject admin permission modal into page
    injectPermissionModal();

    // Inject profile link into sidebar footer
    injectProfileLink();
  });

  // ─── SYNC ROLE FROM SERVER ────────────────────────────
  async function syncRole() {
    try {
      const res = await fetch('/api/profile', {
        headers: { 'Authorization': 'Bearer ' + token() }
      });
      if (res.status === 401) { localStorage.clear(); window.location.href = '/login.html'; return; }
      const data = await res.json();
      if (data.success) {
        localStorage.setItem('role', data.data.role);
        localStorage.setItem('username', data.data.username);
        // Update sidebar display
        const uName = document.getElementById('userName');
        if (uName) uName.textContent = data.data.username;
      }
    } catch (e) { console.warn('Could not sync role:', e); }
  }

  // ─── ROLE BADGE ───────────────────────────────────────
  function renderRoleBadge() {
    const footer = document.querySelector('.sidebar-footer .user-info');
    if (!footer) return;
    const existing = document.getElementById('roleBadge');
    if (existing) existing.remove();
    const role = getRole();
    const badge = document.createElement('div');
    badge.id = 'roleBadge';
    badge.style.cssText = `
      display:inline-flex;align-items:center;padding:0.2rem 0.6rem;
      border-radius:100px;font-size:0.65rem;font-weight:700;letter-spacing:0.06em;
      margin-top:0.25rem;
      background:${role === 'ADMIN' ? 'rgba(255,56,92,0.15)' : 'rgba(59,130,246,0.15)'};
      color:${role === 'ADMIN' ? '#FF385C' : '#3B82F6'};
    `;
    badge.textContent = role;
    footer.querySelector('.user-name')?.parentElement?.appendChild(badge);
  }

  // ─── NAV BADGE ────────────────────────────────────────
  async function loadNavBadge() {
    try {
      const res = await fetch('/api/reservations', {
        headers: { 'Authorization': 'Bearer ' + token() }
      });
      const data = await res.json();
      if (!data.success) return;
      const confirmed = data.data.filter(r => r.status === 'CONFIRMED').length;
      if (confirmed === 0) return;

      // Find all nav items and add badge to Reservations link
      document.querySelectorAll('.nav-item').forEach(item => {
        if (item.textContent.includes('Reservations') && !item.querySelector('.nav-badge')) {
          const badge = document.createElement('span');
          badge.className = 'nav-badge';
          badge.textContent = confirmed;
          badge.style.cssText = `
            margin-left:auto;background:#FF385C;color:white;
            font-size:0.65rem;font-weight:700;padding:0.15rem 0.45rem;
            border-radius:100px;min-width:18px;text-align:center;
          `;
          item.appendChild(badge);
        }
      });
    } catch (e) { console.warn('Nav badge error:', e); }
  }

  // ─── PROFILE LINK IN SIDEBAR ─────────────────────────
  function injectProfileLink() {
    const footer = document.querySelector('.sidebar-footer');
    if (!footer || document.getElementById('profileNavBtn')) return;
    const btn = document.createElement('a');
    btn.id = 'profileNavBtn';
    btn.href = '/profile.html';
    btn.className = 'nav-item';
    btn.innerHTML = '<span class="nav-icon">👤</span> My Profile';
    footer.insertBefore(btn, footer.querySelector('#logoutBtn'));
  }

  // ─── ADMIN PERMISSION MODAL ───────────────────────────
  function injectPermissionModal() {
    if (document.getElementById('adminPermModal')) return;

    const modal = document.createElement('div');
    modal.id = 'adminPermModal';
    modal.style.cssText = `
      display:none;position:fixed;inset:0;background:rgba(0,0,0,0.7);
      z-index:9999;align-items:center;justify-content:center;backdrop-filter:blur(6px);
    `;
    modal.innerHTML = `
      <div style="
        background:#1a1a2e;border:1px solid rgba(255,255,255,0.1);
        border-radius:24px;padding:2rem;max-width:400px;width:90%;
        animation:modalIn 0.3s ease;font-family:'DM Sans',sans-serif;
      ">
        <div style="text-align:center;margin-bottom:1.5rem;">
          <div style="font-size:2.5rem;margin-bottom:0.75rem;">🔐</div>
          <div style="font-family:'Cormorant Garamond',serif;font-size:1.4rem;color:white;margin-bottom:0.4rem;">
            Admin Permission Required
          </div>
          <div style="font-size:0.825rem;color:rgba(255,255,255,0.5);line-height:1.5;">
            This action requires admin credentials.<br>Access is granted for one action only.
          </div>
        </div>
        <div id="adminPermAlert" style="
          display:none;background:rgba(239,68,68,0.12);border:1px solid rgba(239,68,68,0.3);
          color:#EF4444;padding:0.6rem 0.9rem;border-radius:10px;font-size:0.8rem;margin-bottom:1rem;
        "></div>
        <div style="margin-bottom:0.9rem;">
          <label style="font-size:0.72rem;color:rgba(255,255,255,0.5);text-transform:uppercase;letter-spacing:0.06em;display:block;margin-bottom:0.35rem;">
            Admin Username
          </label>
          <input id="adminPermUser" type="text" placeholder="admin" style="
            width:100%;padding:0.75rem 1rem;background:rgba(255,255,255,0.07);
            border:1px solid rgba(255,255,255,0.15);border-radius:12px;
            color:white;font-family:'DM Sans',sans-serif;font-size:0.9rem;outline:none;box-sizing:border-box;
          "/>
        </div>
        <div style="margin-bottom:1.5rem;">
          <label style="font-size:0.72rem;color:rgba(255,255,255,0.5);text-transform:uppercase;letter-spacing:0.06em;display:block;margin-bottom:0.35rem;">
            Admin Password
          </label>
          <input id="adminPermPass" type="password" placeholder="••••••••" style="
            width:100%;padding:0.75rem 1rem;background:rgba(255,255,255,0.07);
            border:1px solid rgba(255,255,255,0.15);border-radius:12px;
            color:white;font-family:'DM Sans',sans-serif;font-size:0.9rem;outline:none;box-sizing:border-box;
          "/>
        </div>
        <div style="display:flex;gap:0.75rem;">
          <button onclick="OVR.cancelPermission()" style="
            flex:1;padding:0.7rem;border-radius:12px;background:rgba(255,255,255,0.07);
            border:1px solid rgba(255,255,255,0.1);color:rgba(255,255,255,0.6);
            font-family:'DM Sans',sans-serif;font-size:0.875rem;cursor:pointer;
          ">Cancel</button>
          <button onclick="OVR.submitPermission()" id="adminPermSubmit" style="
            flex:1;padding:0.7rem;border-radius:12px;background:#FF385C;
            border:none;color:white;font-family:'DM Sans',sans-serif;
            font-size:0.875rem;cursor:pointer;font-weight:500;
          ">Verify & Proceed</button>
        </div>
      </div>
    `;
    document.body.appendChild(modal);

    // Allow Enter key to submit
    modal.addEventListener('keydown', e => {
      if (e.key === 'Enter') OVR.submitPermission();
    });
  }

  // ─── PUBLIC API ───────────────────────────────────────
  let _permissionCallback = null;
  let _permissionReject = null;

  window.OVR = {

    // Check if current user is admin
    isAdmin: () => getRole() === 'ADMIN',

    // Request admin permission — returns Promise
    // Usage: await OVR.requireAdmin('reason text')
    requireAdmin: function (reason) {
      if (getRole() === 'ADMIN') return Promise.resolve(true);

      return new Promise((resolve, reject) => {
        _permissionCallback = resolve;
        _permissionReject = reject;

        const modal = document.getElementById('adminPermModal');
        const alert = document.getElementById('adminPermAlert');
        const userInput = document.getElementById('adminPermUser');
        const passInput = document.getElementById('adminPermPass');

        alert.style.display = 'none';
        userInput.value = '';
        passInput.value = '';
        modal.style.display = 'flex';

        // Update description if reason provided
        if (reason) {
          const desc = modal.querySelector('div[style*="one action only"]');
          if (desc) desc.innerHTML = `${reason}<br><span style="color:rgba(255,255,255,0.4);font-size:0.78rem;">Access is granted for one action only.</span>`;
        }

        setTimeout(() => userInput.focus(), 100);
      });
    },

    submitPermission: async function () {
      const username = document.getElementById('adminPermUser').value.trim();
      const password = document.getElementById('adminPermPass').value;
      const alert = document.getElementById('adminPermAlert');
      const btn = document.getElementById('adminPermSubmit');

      if (!username || !password) {
        alert.textContent = 'Please enter both username and password.';
        alert.style.display = 'block';
        return;
      }

      btn.textContent = 'Verifying...'; btn.disabled = true;

      try {
        const res = await fetch('/api/auth/verify-admin', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token() },
          body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (data.success) {
          document.getElementById('adminPermModal').style.display = 'none';
          if (_permissionCallback) { _permissionCallback(true); _permissionCallback = null; }
        } else {
          alert.textContent = 'Invalid admin credentials. Please try again.';
          alert.style.display = 'block';
          document.getElementById('adminPermPass').value = '';
        }
      } catch (e) {
        alert.textContent = 'Server error. Please try again.';
        alert.style.display = 'block';
      }

      btn.textContent = 'Verify & Proceed'; btn.disabled = false;
    },

    cancelPermission: function () {
      document.getElementById('adminPermModal').style.display = 'none';
      if (_permissionReject) { _permissionReject(false); _permissionReject = null; }
    },

    // Wrap any action that requires admin
    // Usage: OVR.adminAction('Delete this record?', async () => { ... your code ... })
    adminAction: async function (reason, action) {
      try {
        await OVR.requireAdmin(reason);
        await action();
      } catch (e) {
        // Permission denied or cancelled — do nothing
      }
    }
  };

})();