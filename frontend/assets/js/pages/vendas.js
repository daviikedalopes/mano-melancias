(function () {
  const els = {};
  const qs = (id) => document.getElementById(id);

  function bindEls() {
    els.filterForm = qs('filter-form');
    els.tbody = qs('vendas-body');
    els.empty = qs('vendas-empty');
    els.cliente = qs('f-cliente');
    els.produtor = qs('f-produtor');
    els.motorista = qs('f-motorista');
  }

  async function loadFiltroOptions() {
    const [clientes, produtores, motoristas] = await Promise.all([
      window.Api.get('/clientes'),
      window.Api.get('/produtores'),
      window.Api.get('/motoristas'),
    ]);
    els.cliente.innerHTML =
      '<option value="">Todos</option>' +
      clientes.map((c) => `<option value="${c.id}">${window.escapeHtml(c.nome)}</option>`).join('');
    els.produtor.innerHTML =
      '<option value="">Todos</option>' +
      produtores.map((p) => `<option value="${p.id}">${window.escapeHtml(p.nome)}</option>`).join('');
    els.motorista.innerHTML =
      '<option value="">Todos</option>' +
      motoristas.map((m) => `<option value="${m.id}">${window.escapeHtml(m.nome)}</option>`).join('');
  }

  async function loadList() {
    const params = {
      dataInicio: qs('f-inicio').value,
      dataFim: qs('f-fim').value,
      clienteId: els.cliente.value,
      produtorId: els.produtor.value,
      motoristaId: els.motorista.value,
      statusPagamento: qs('f-status').value,
    };
    const list = await window.Api.get('/vendas' + window.Api.buildQuery(params));
    renderList(list);
  }

  function renderList(list) {
    if (list.length === 0) {
      els.tbody.innerHTML = '';
      els.empty.hidden = false;
      return;
    }
    els.empty.hidden = true;
    const ordenadas = [...list].sort((a, b) => (b.numero || 0) - (a.numero || 0));
    els.tbody.innerHTML = ordenadas
      .map(
        (v) => `
      <tr class="is-clickable" data-href="venda-detalhe.html?id=${v.id}">
        <td><span class="table__primary">Nº ${v.numero}</span><div class="table__secondary">${window.Fmt.isoDateToDisplay(v.dataVenda)}</div></td>
        <td>${window.escapeHtml(v.clienteNome)}<div class="table__secondary">${window.escapeHtml(v.clienteMunicipio)}/${window.escapeHtml(v.clienteEstado)}</div></td>
        <td>${window.escapeHtml(v.produtorNome)}</td>
        <td class="num-col num">${window.Fmt.weight(v.pesoLiquido)}</td>
        <td class="num-col num">${window.Fmt.currency(v.valorMercadoria)}</td>
        <td class="num-col num">${window.Fmt.currency(v.restantePagar)}</td>
        <td><span class="badge badge--${v.statusPagamento.toLowerCase()}">${window.Fmt.statusLabel(v.statusPagamento)}</span></td>
      </tr>`
      )
      .join('');

    els.tbody.querySelectorAll('tr[data-href]').forEach((tr) => {
      tr.addEventListener('click', () => (location.href = tr.dataset.href));
    });
  }

  function clearFilters() {
    els.filterForm.reset();
    loadList().catch((err) => window.Toast.error(err.message || 'Erro ao buscar vendas.'));
  }

  async function init() {
    bindEls();
    els.filterForm.addEventListener('submit', (e) => {
      e.preventDefault();
      loadList().catch((err) => window.Toast.error(err.message || 'Erro ao buscar vendas.'));
    });
    qs('btn-clear-filters').addEventListener('click', clearFilters);

    try {
      await loadFiltroOptions();
      await loadList();
    } catch (err) {
      window.Toast.error(err.message || 'Erro ao carregar vendas.');
    }
  }

  if (window.Shell.boot()) init();
})();
