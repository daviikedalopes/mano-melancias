(function () {
  const els = {};
  const qs = (id) => document.getElementById(id);

  function bindEls() {
    els.tbody = qs('usuarios-body');
    els.empty = qs('usuarios-empty');
    els.modalOverlay = qs('modal-overlay');
    els.form = qs('entity-form');
    els.submitBtn = qs('form-submit');
  }

  async function loadList() {
    const list = await window.Api.get('/usuarios');
    if (list.length === 0) {
      els.tbody.innerHTML = '';
      els.empty.hidden = false;
      return;
    }
    els.empty.hidden = true;
    els.tbody.innerHTML = list
      .map(
        (u) => `
      <tr>
        <td class="table__primary">${window.escapeHtml(u.nome)}</td>
        <td>${window.escapeHtml(u.email)}</td>
        <td><span class="badge badge--${u.papel.toLowerCase()}">${window.Fmt.papelLabel(u.papel)}</span></td>
      </tr>`
      )
      .join('');
  }

  function openModal() {
    els.form.reset();
    clearErrors();
    els.modalOverlay.hidden = false;
    qs('m-nome').focus();
  }
  function closeModal() {
    els.modalOverlay.hidden = true;
  }

  function clearErrors() {
    els.form.querySelectorAll('[data-field-error-for]').forEach((el) => {
      el.hidden = true;
      el.textContent = '';
    });
    els.form.querySelectorAll('.input, .select').forEach((el) => el.removeAttribute('aria-invalid'));
  }

  function showFieldErrors(erros) {
    Object.entries(erros || {}).forEach(([field, msg]) => {
      const input = qs('m-' + field);
      const errEl = document.querySelector(`[data-field-error-for="${field}"]`);
      if (input) input.setAttribute('aria-invalid', 'true');
      if (errEl) {
        errEl.hidden = false;
        errEl.textContent = msg;
      }
    });
  }

  async function onSubmit(e) {
    e.preventDefault();
    clearErrors();
    const payload = {
      nome: qs('m-nome').value.trim(),
      email: qs('m-email').value.trim(),
      senha: qs('m-senha').value,
      papel: qs('m-papel').value,
    };
    els.submitBtn.disabled = true;
    els.submitBtn.textContent = 'Criando...';
    try {
      await window.Api.post('/usuarios', payload);
      window.Toast.success('Usuário criado.');
      closeModal();
      loadList();
    } catch (err) {
      if (err.erros) showFieldErrors(err.erros);
      else window.Toast.error(err.message || 'Não foi possível criar o usuário.');
    } finally {
      els.submitBtn.disabled = false;
      els.submitBtn.textContent = 'Criar usuário';
    }
  }

  function init() {
    bindEls();
    qs('btn-new').addEventListener('click', openModal);
    const emptyNewBtn = qs('empty-new');
    if (emptyNewBtn) emptyNewBtn.addEventListener('click', openModal);
    document.querySelectorAll('[data-modal-close]').forEach((el) => el.addEventListener('click', closeModal));
    els.modalOverlay.addEventListener('click', (e) => {
      if (e.target === els.modalOverlay) closeModal();
    });
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && !els.modalOverlay.hidden) closeModal();
    });
    els.form.addEventListener('submit', onSubmit);

    loadList().catch((err) => window.Toast.error(err.message || 'Erro ao carregar usuários.'));
  }

  if (window.Shell.boot({ admin: true })) init();
})();
