(function () {
  const state = { editingId: null };
  const els = {};
  const qs = (id) => document.getElementById(id);

  function bindEls() {
    els.filterForm = qs('filter-form');
    els.tbody = qs('produtores-body');
    els.empty = qs('produtores-empty');
    els.modalOverlay = qs('modal-overlay');
    els.modalTitle = qs('modal-title');
    els.form = qs('entity-form');
    els.submitBtn = qs('form-submit');
  }

  async function loadList() {
    const params = {
      nome: qs('f-nome').value.trim(),
      cidade: qs('f-cidade').value.trim(),
    };
    const list = await window.Api.get('/produtores' + window.Api.buildQuery(params));
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
        (p) => `
      <tr>
        <td><span class="table__primary">${window.escapeHtml(p.nome)}</span></td>
        <td>${window.escapeHtml(p.cidade)}</td>
        <td>${p.telefone ? window.escapeHtml(p.telefone) : '<span class="text-faint">—</span>'}</td>
        <td class="table__actions">
          <button class="btn btn-ghost btn-sm" type="button" data-edit="${p.id}">Editar</button>
          <button class="btn btn-danger btn-sm" type="button" data-inativar="${p.id}">Inativar</button>
        </td>
      </tr>`
      )
      .join('');

    els.tbody.querySelectorAll('[data-edit]').forEach((btn) => {
      btn.addEventListener('click', () => openEdit(list.find((p) => p.id === btn.dataset.edit)));
    });
    els.tbody.querySelectorAll('[data-inativar]').forEach((btn) => {
      btn.addEventListener('click', () => inativar(btn.dataset.inativar));
    });
  }

  async function inativar(id) {
    if (!confirm('Inativar este produtor? Ele deixará de aparecer nas buscas e no lançamento de vendas.')) return;
    try {
      await window.Api.del('/produtores/' + id);
      window.Toast.success('Produtor inativado.');
      loadList();
    } catch (err) {
      window.Toast.error(err.message || 'Não foi possível inativar o produtor.');
    }
  }

  function openNew() {
    state.editingId = null;
    els.modalTitle.textContent = 'Novo produtor';
    els.form.reset();
    clearErrors();
    openModal();
  }

  function openEdit(p) {
    if (!p) return;
    state.editingId = p.id;
    els.modalTitle.textContent = 'Editar produtor';
    qs('m-nome').value = p.nome;
    qs('m-cidade').value = p.cidade;
    qs('m-telefone').value = p.telefone || '';
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
    const payload = {
      nome: qs('m-nome').value.trim(),
      cidade: qs('m-cidade').value.trim(),
      telefone: qs('m-telefone').value.trim() || null,
    };
    els.submitBtn.disabled = true;
    els.submitBtn.textContent = 'Salvando...';
    try {
      if (state.editingId) {
        await window.Api.put('/produtores/' + state.editingId, payload);
        window.Toast.success('Produtor atualizado.');
      } else {
        await window.Api.post('/produtores', payload);
        window.Toast.success('Produtor cadastrado.');
      }
      closeModal();
      loadList();
    } catch (err) {
      if (err.erros) showFieldErrors(err.erros);
      else window.Toast.error(err.message || 'Não foi possível salvar o produtor.');
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
      loadList().catch((err) => window.Toast.error(err.message || 'Erro ao buscar produtores.'));
    });
    qs('m-telefone').addEventListener('input', (e) => {
      e.target.value = window.Fmt.maskPhone(e.target.value);
    });

    loadList().catch((err) => window.Toast.error(err.message || 'Erro ao carregar produtores.'));
  }

  if (window.Shell.boot()) init();
})();
