(function () {
  const state = { editingId: null };
  const els = {};
  const qs = (id) => document.getElementById(id);

  function bindEls() {
    els.filterForm = qs('filter-form');
    els.tbody = qs('motoristas-body');
    els.empty = qs('motoristas-empty');
    els.modalOverlay = qs('modal-overlay');
    els.modalTitle = qs('modal-title');
    els.form = qs('entity-form');
    els.submitBtn = qs('form-submit');
  }

  async function loadList() {
    const params = { nome: qs('f-nome').value.trim() };
    const list = await window.Api.get('/motoristas' + window.Api.buildQuery(params));
    renderList(list);
  }

  function renderList(list) {
    if (list.length === 0) {
      els.tbody.innerHTML = '';
      els.empty.hidden = false;
      return;
    }
    els.empty.hidden = true;
    els.tbody.innerHTML = list
      .map(
        (m) => `
      <tr>
        <td><span class="table__primary">${window.escapeHtml(m.nome)}</span></td>
        <td class="num">${window.Fmt.maskCpf(m.cpf)}</td>
        <td>${m.telefone ? window.escapeHtml(m.telefone) : '<span class="text-faint">—</span>'}</td>
        <td class="table__actions">
          <button class="btn btn-ghost btn-sm" type="button" data-edit="${m.id}">Editar</button>
          <button class="btn btn-danger btn-sm" type="button" data-inativar="${m.id}">Inativar</button>
        </td>
      </tr>`
      )
      .join('');

    els.tbody.querySelectorAll('[data-edit]').forEach((btn) => {
      btn.addEventListener('click', () => openEdit(list.find((m) => m.id === btn.dataset.edit)));
    });
    els.tbody.querySelectorAll('[data-inativar]').forEach((btn) => {
      btn.addEventListener('click', () => inativar(btn.dataset.inativar));
    });
  }

  async function inativar(id) {
    if (!confirm('Inativar este motorista? Ele deixará de aparecer nas buscas e no lançamento de vendas.')) return;
    try {
      await window.Api.del('/motoristas/' + id);
      window.Toast.success('Motorista inativado.');
      loadList();
    } catch (err) {
      window.Toast.error(err.message || 'Não foi possível inativar o motorista.');
    }
  }

  function openNew() {
    state.editingId = null;
    els.modalTitle.textContent = 'Novo motorista';
    els.form.reset();
    clearErrors();
    openModal();
  }

  function openEdit(m) {
    if (!m) return;
    state.editingId = m.id;
    els.modalTitle.textContent = 'Editar motorista';
    qs('m-nome').value = m.nome;
    qs('m-cpf').value = window.Fmt.maskCpf(m.cpf);
    qs('m-telefone').value = m.telefone || '';
    clearErrors();
    openModal();
  }

  function openModal() {
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
    els.form.querySelectorAll('.input').forEach((el) => el.removeAttribute('aria-invalid'));
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

    const cpf = qs('m-cpf').value.trim();
    if (!window.Fmt.isValidCpf(cpf)) {
      showFieldErrors({ cpf: 'CPF inválido — confira os números digitados.' });
      qs('m-cpf').focus();
      return;
    }

    const payload = {
      nome: qs('m-nome').value.trim(),
      cpf,
      telefone: qs('m-telefone').value.trim() || null,
    };
    els.submitBtn.disabled = true;
    els.submitBtn.textContent = 'Salvando...';
    try {
      if (state.editingId) {
        await window.Api.put('/motoristas/' + state.editingId, payload);
        window.Toast.success('Motorista atualizado.');
      } else {
        await window.Api.post('/motoristas', payload);
        window.Toast.success('Motorista cadastrado.');
      }
      closeModal();
      loadList();
    } catch (err) {
      if (err.erros) showFieldErrors(err.erros);
      else window.Toast.error(err.message || 'Não foi possível salvar o motorista.');
    } finally {
      els.submitBtn.disabled = false;
      els.submitBtn.textContent = 'Salvar';
    }
  }

  function init() {
    bindEls();
    qs('btn-new').addEventListener('click', openNew);
    const emptyNewBtn = qs('empty-new');
    if (emptyNewBtn) emptyNewBtn.addEventListener('click', openNew);
    document.querySelectorAll('[data-modal-close]').forEach((el) => el.addEventListener('click', closeModal));
    els.modalOverlay.addEventListener('click', (e) => {
      if (e.target === els.modalOverlay) closeModal();
    });
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && !els.modalOverlay.hidden) closeModal();
    });
    els.form.addEventListener('submit', onSubmit);
    els.filterForm.addEventListener('submit', (e) => {
      e.preventDefault();
      loadList().catch((err) => window.Toast.error(err.message || 'Erro ao buscar motoristas.'));
    });
    qs('m-cpf').addEventListener('input', (e) => {
      e.target.value = window.Fmt.maskCpf(e.target.value);
    });
    qs('m-telefone').addEventListener('input', (e) => {
      e.target.value = window.Fmt.maskPhone(e.target.value);
    });

    loadList().catch((err) => window.Toast.error(err.message || 'Erro ao carregar motoristas.'));
  }

  if (window.Shell.boot()) init();
})();
