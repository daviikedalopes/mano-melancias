(function () {
  const state = { editingId: null, motoristas: [] };
  const els = {};
  const qs = (id) => document.getElementById(id);

  function bindEls() {
    els.filterForm = qs('filter-form');
    els.tbody = qs('veiculos-body');
    els.empty = qs('veiculos-empty');
    els.modalOverlay = qs('modal-overlay');
    els.modalTitle = qs('modal-title');
    els.form = qs('entity-form');
    els.submitBtn = qs('form-submit');
    els.filterMotorista = qs('f-motorista');
    els.formMotorista = qs('m-motorista');
  }

  async function loadMotoristas() {
    state.motoristas = await window.Api.get('/motoristas');
    const options = state.motoristas
      .map((m) => `<option value="${m.id}">${window.escapeHtml(m.nome)}</option>`)
      .join('');
    els.filterMotorista.innerHTML = '<option value="">Todos</option>' + options;
    els.formMotorista.innerHTML = '<option value="">Nenhum</option>' + options;
  }

  async function loadList() {
    const params = {
      placa: qs('f-placa').value.trim(),
      motoristaId: els.filterMotorista.value || '',
    };
    const list = await window.Api.get('/veiculos' + window.Api.buildQuery(params));
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
        (v) => `
      <tr>
        <td><span class="table__primary num">${window.escapeHtml(v.placa)}</span></td>
        <td>${window.escapeHtml(v.cidade)}</td>
        <td>${v.motoristaNome ? window.escapeHtml(v.motoristaNome) : '<span class="text-faint">—</span>'}</td>
        <td class="table__actions">
          <button class="btn btn-ghost btn-sm" type="button" data-edit="${v.id}">Editar</button>
          <button class="btn btn-danger btn-sm" type="button" data-excluir="${v.id}">Excluir</button>
        </td>
      </tr>`
      )
      .join('');

    els.tbody.querySelectorAll('[data-edit]').forEach((btn) => {
      btn.addEventListener('click', () => openEdit(list.find((v) => v.id === btn.dataset.edit)));
    });
    els.tbody.querySelectorAll('[data-excluir]').forEach((btn) => {
      btn.addEventListener('click', () => excluir(btn.dataset.excluir));
    });
  }

  async function excluir(id) {
    if (!confirm('Excluir este veículo definitivamente? Essa ação não pode ser desfeita.')) return;
    try {
      await window.Api.del('/veiculos/' + id);
      window.Toast.success('Veículo excluído.');
      loadList();
    } catch (err) {
      window.Toast.error(err.message || 'Não foi possível excluir o veículo.');
    }
  }

  function openNew() {
    state.editingId = null;
    els.modalTitle.textContent = 'Novo veículo';
    els.form.reset();
    clearErrors();
    openModal();
  }

  function openEdit(v) {
    if (!v) return;
    state.editingId = v.id;
    els.modalTitle.textContent = 'Editar veículo';
    qs('m-placa').value = v.placa;
    qs('m-cidade').value = v.cidade;
    els.formMotorista.value = v.motoristaId || '';
    clearErrors();
    openModal();
  }

  function openModal() {
    els.modalOverlay.hidden = false;
    qs('m-placa').focus();
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
      placa: window.Fmt.maskPlaca(qs('m-placa').value),
      cidade: qs('m-cidade').value.trim(),
      motoristaId: els.formMotorista.value || null,
    };
    els.submitBtn.disabled = true;
    els.submitBtn.textContent = 'Salvando...';
    try {
      if (state.editingId) {
        await window.Api.put('/veiculos/' + state.editingId, payload);
        window.Toast.success('Veículo atualizado.');
      } else {
        await window.Api.post('/veiculos', payload);
        window.Toast.success('Veículo cadastrado.');
      }
      closeModal();
      loadList();
    } catch (err) {
      if (err.erros) showFieldErrors(err.erros);
      else window.Toast.error(err.message || 'Não foi possível salvar o veículo.');
    } finally {
      els.submitBtn.disabled = false;
      els.submitBtn.textContent = 'Salvar';
    }
  }

  async function init() {
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
      loadList().catch((err) => window.Toast.error(err.message || 'Erro ao buscar veículos.'));
    });
    qs('m-placa').addEventListener('input', (e) => {
      e.target.value = window.Fmt.maskPlaca(e.target.value);
    });

    try {
      await loadMotoristas();
      await loadList();
    } catch (err) {
      window.Toast.error(err.message || 'Erro ao carregar veículos.');
    }
  }

  if (window.Shell.boot()) init();
})();
