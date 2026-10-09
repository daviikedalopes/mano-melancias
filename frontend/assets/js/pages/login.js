(function () {
  if (window.Auth.isAuthenticated()) {
    location.href = 'index.html';
    return;
  }

  const form = document.getElementById('login-form');
  const errorBox = document.getElementById('login-error');
  const submitBtn = document.getElementById('login-submit');
  const okBox = document.getElementById('login-ok');
  const resendBtn = document.getElementById('login-resend');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorBox.hidden = true;
    okBox.hidden = true;
    resendBtn.hidden = true;
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
      // 403 por e-mail ainda não confirmado: oferece reenviar o link
      resendBtn.hidden = !(err.status === 403 && /confirme seu e-mail/i.test(err.message || ''));
      submitBtn.disabled = false;
      submitBtn.textContent = 'Entrar';
    }
  });

  resendBtn.addEventListener('click', async () => {
    const email = document.getElementById('email').value.trim();
    resendBtn.disabled = true;
    try {
      await window.Api.post('/auth/reenviar-confirmacao', { email });
      errorBox.hidden = true;
      okBox.textContent = 'Se houver uma conta aguardando confirmação para este e-mail, enviamos um novo link.';
      okBox.hidden = false;
    } catch (err) {
      errorBox.textContent = err.message || 'Não foi possível reenviar agora. Tente novamente.';
      errorBox.hidden = false;
    } finally {
      resendBtn.disabled = false;
    }
  });
})();
