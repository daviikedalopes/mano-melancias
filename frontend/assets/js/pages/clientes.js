(function () {
  const state = { editingId: null };
  const els = {};
  const qs = (id) => document.getElementById(id);

  function bindEls() {
    els.filterForm = qs('filter-form');
    els.tbody = qs('clientes-body');
    els.empty = qs('clientes-empty');
    els.modalOverlay = qs('modal-overlay');
    els.modalTitle = qs('modal-title');
    els.form = qs('entity-form');
    els.submitBtn = qs('form-submit');
  }

  async function loadList() {
    const params = {
      nome: qs('f-nome').value.trim(),
      municipio: qs('f-municipio').value.trim(),
      estado: qs('f-estado').value.trim(),
    };
    const list = await window.Api.get('/clientes' + window.Api.buildQuery(params));
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
        (c) => `
      <tr>
        <td><span class="table__primary">${window.escapeHtml(c.nome)}</span></td>
        <td>${window.escapeHtml(c.municipio)}/${window.escapeHtml(c.estado)}</td>
        <td>${c.telefone ? window.escapeHtml(c.telefone) : '<span class="text-faint">—</span>'}</td>
        <td class="table__actions">
          <button class="btn btn-ghost btn-sm" type="button" data-edit="${c.id}">Editar</button>
          <button class="btn btn-danger btn-sm" type="button" data-inativar="${c.id}">Inativar</button>
        </td>
      </tr>`
      )
      .join('');

    els.tbody.querySelectorAll('[data-edit]').forEach((btn) => {
      btn.addEventListener('click', () => openEdit(list.find((c) => c.id === btn.dataset.edit)));
    });
    els.tbody.querySelectorAll('[data-inativar]').forEach((btn) => {
      btn.addEventListener('click', () => inativar(btn.dataset.inativar));
    });
  }

  async function inativar(id) {
    if (!confirm('Inativar este cliente? Ele deixará de aparecer nas buscas e no lançamento de vendas.')) return;
    try {
      await window.Api.del('/clientes/' + id);
      window.Toast.success('Cliente inativado.');
      loadList();
    } catch (err) {
      window.Toast.error(err.message || 'Não foi possível inativar o cliente.');
    }
  }

  function openNew() {
    state.editingId = null;
    els.modalTitle.textContent = 'Novo cliente';
    els.form.reset();
    clearErrors();
    openModal();
  }

  function openEdit(c) {
    if (!c) return;
    state.editingId = c.id;
    els.modalTitle.textContent = 'Editar cliente';
    qs('m-nome').value = c.nome;
    qs('m-municipio').value = c.municipio;
    qs('m-estado').value = c.estado;
    qs('m-telefone').value = c.telefone || '';
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
      municipio: qs('m-municipio').value.trim(),
      estado: qs('m-estado').value.trim().toUpperCase(),
      telefone: qs('m-telefone').value.trim() || null,
    };
    els.submitBtn.disabled = true;
    els.submitBtn.textContent = 'Salvando...';
    try {
      if (state.editingId) {
        await window.Api.put('/clientes/' + state.editingId, payload);
        window.Toast.success('Cliente atualizado.');
      } else {
        await window.Api.post('/clientes', payload);
        window.Toast.success('Cliente cadastrado.');
      }
      closeModal();
      loadList();
    } catch (err) {
      if (err.erros) showFieldErrors(err.erros);
      else window.Toast.error(err.message || 'Não foi possível salvar o cliente.');
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
      loadList().catch((err) => window.Toast.error(err.message || 'Erro ao buscar clientes.'));
    });
    qs('m-estado').addEventListener('input', (e) => {
      e.target.value = e.target.value.toUpperCase().slice(0, 2);
    });
    qs('m-telefone').addEventListener('input', (e) => {
      e.target.value = window.Fmt.maskPhone(e.target.value);
    });

    loadList().catch((err) => window.Toast.error(err.message || 'Erro ao carregar clientes.'));
  }

  if (window.Shell.boot()) init();
})();
