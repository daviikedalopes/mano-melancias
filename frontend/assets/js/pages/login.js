(function () {
  if (window.Auth.isAuthenticated()) {
    location.href = 'index.html';
    return;
  }

  const form = document.getElementById('login-form');
  const errorBox = document.getElementById('login-error');
  const submitBtn = document.getElementById('login-submit');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorBox.hidden = true;
    submitBtn.disabled = true;
    submitBtn.textContent = 'Entrando...';

    const email = document.getElementById('email').value.trim();
    const senha = document.getElementById('senha').value;

    try {
      const res = await window.Api.post('/auth/login', { email, senha });
      window.Auth.login(res.token);
      location.href = 'index.html';
    } catch (err) {
      errorBox.textContent = err.message || 'Não foi possível entrar. Tente novamente.';
      errorBox.hidden = false;
      submitBtn.disabled = false;
      submitBtn.textContent = 'Entrar';
    }
  });
})();
