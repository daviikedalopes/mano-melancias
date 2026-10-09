(async function () {
  const okBox = document.getElementById('confirm-ok');
  const errorBox = document.getElementById('confirm-error');
  const loading = document.getElementById('confirm-loading');
  const loginLink = document.getElementById('confirm-login');

  // O token vem no link do e-mail (?token=...). A confirmação é feita por POST
  // (e não por GET) para que leitores de e-mail que "pré-abrem" links não confirmem sozinhos.
  const token = new URLSearchParams(location.search).get('token');

  try {
    if (!token) throw new Error('Link inválido. Use o link que enviamos por e-mail.');
    await window.Api.post('/auth/confirmar-email', { token });
    okBox.hidden = false;
  } catch (err) {
    errorBox.textContent = err.message || 'Não foi possível confirmar o e-mail.';
    errorBox.hidden = false;
  } finally {
    loading.hidden = true;
    loginLink.hidden = false;
  }
})();
