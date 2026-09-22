(function () {
  function currentPage() {
    return location.pathname.split('/').pop() || 'index.html';
  }

  function initSidebarState() {
    const page = currentPage();
    document.querySelectorAll('.sidebar__link').forEach((link) => {
      link.classList.toggle('is-active', link.getAttribute('href') === page);
    });
  }

  function initUserInfo() {
    const emailEl = document.querySelector('[data-user-email]');
    const papelEl = document.querySelector('[data-user-papel]');
    if (emailEl) emailEl.textContent = window.Auth.getEmail() || '';
    if (papelEl) papelEl.textContent = window.Fmt.papelLabel(window.Auth.getPapel());
    document.querySelectorAll('[data-requires-admin]').forEach((el) => {
      el.hidden = !window.Auth.isAdmin();
    });
  }

  function initLogout() {
    document.querySelectorAll('[data-logout]').forEach((btn) => {
      btn.addEventListener('click', async () => {
        const confirmado = await window.ConfirmModal.show({
          title: 'Sair do sistema',
          message: 'Tem certeza de que deseja sair do sistema?',
          confirmText: 'Sair',
          cancelText: 'Cancelar',
          confirmVariant: 'danger',
        });
        if (confirmado) window.Auth.logout();
      });
    });
  }

  function initMobileMenu() {
    const toggle = document.querySelector('[data-menu-toggle]');
    const sidebar = document.querySelector('.sidebar');
    const scrim = document.querySelector('[data-sidebar-scrim]');
    if (!toggle || !sidebar) return;

    const close = () => {
      sidebar.classList.remove('is-open');
      if (scrim) scrim.classList.remove('is-visible');
    };
    toggle.addEventListener('click', () => {
      sidebar.classList.toggle('is-open');
      if (scrim) scrim.classList.toggle('is-visible');
    });
    if (scrim) scrim.addEventListener('click', close);
    sidebar.querySelectorAll('a').forEach((a) => a.addEventListener('click', close));
  }

  function reveal() {
    document.documentElement.setAttribute('data-auth-ready', 'true');
    const veil = document.getElementById('auth-veil');
    if (veil) veil.remove();
  }

  function boot(options) {
    const opts = options || {};
    const ok = opts.admin ? window.Auth.requireAdmin() : window.Auth.requireAuth();
    if (!ok) return false;
    reveal();
    initSidebarState();
    initUserInfo();
    initLogout();
    initMobileMenu();
    return true;
  }

  window.Shell = { boot };
})();
