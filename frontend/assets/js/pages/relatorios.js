(function () {
  const qs = (id) => document.getElementById(id);

  async function loadPeriodo() {
    const inicio = qs('f-inicio').value;
    const fim = qs('f-fim').value;
    const data = await window.Api.get('/relatorios/vendas-periodo' + window.Api.buildQuery({ inicio, fim }));
    qs('p-qtd').textContent = window.Fmt.integer(data.quantidadeVendas || 0);
    qs('p-mercadoria').textContent = window.Fmt.currency(data.totalValorMercadoria || 0);
    qs('p-frete').textContent = window.Fmt.currency(data.totalValorFrete || 0);
    qs('p-restante').textContent = window.Fmt.currency(data.totalRestantePagar || 0);
  }

  async function loadPorProdutor() {
    const list = await window.Api.get('/relatorios/por-produtor');
    const tbody = qs('produtor-body');
    if (list.length === 0) {
      tbody.innerHTML = '';
      qs('produtor-empty').hidden = false;
      return;
    }
    qs('produtor-empty').hidden = true;
    const ordenado = [...list].sort((a, b) => window.Fmt.toNumber(b.totalComprado) - window.Fmt.toNumber(a.totalComprado));
    tbody.innerHTML = ordenado
      .map(
        (p) => `
      <tr>
        <td class="table__primary">${window.escapeHtml(p.produtorNome)}</td>
        <td class="num-col num">${window.Fmt.integer(p.quantidadeCargas)}</td>
        <td class="num-col num">${window.Fmt.weight(p.totalPesoLiquido)}</td>
        <td class="num-col num">${window.Fmt.currency(p.totalComprado)}</td>
      </tr>`
      )
      .join('');
  }

  async function loadPorCliente() {
    const list = await window.Api.get('/relatorios/por-cliente');
    const tbody = qs('cliente-body');
    if (list.length === 0) {
      tbody.innerHTML = '';
      qs('cliente-empty').hidden = false;
      return;
    }
    qs('cliente-empty').hidden = true;
    const ordenado = [...list].sort((a, b) => window.Fmt.toNumber(b.totalComprado) - window.Fmt.toNumber(a.totalComprado));
    tbody.innerHTML = ordenado
      .map(
        (c) => `
      <tr>
        <td class="table__primary">${window.escapeHtml(c.clienteNome)}</td>
        <td class="num-col num">${window.Fmt.integer(c.quantidadeVendas)}</td>
        <td class="num-col num">${window.Fmt.currency(c.totalComprado)}</td>
      </tr>`
      )
      .join('');
  }

  async function loadContas() {
    const list = await window.Api.get('/relatorios/contas-a-receber');
    const tbody = qs('contas-body');
    if (list.length === 0) {
      tbody.innerHTML = '';
      qs('contas-empty').hidden = false;
      return;
    }
    qs('contas-empty').hidden = true;
    const hoje = window.Fmt.todayIso();
    tbody.innerHTML = list
      .map((c) => {
        const vencida = c.vencimento && c.vencimento < hoje;
        return `
      <tr class="is-clickable" data-href="venda-detalhe.html?id=${c.vendaId}">
        <td><span class="table__primary">Nº ${c.numero}</span></td>
        <td>${window.escapeHtml(c.clienteNome)}</td>
        <td class="num-col num">${window.Fmt.currency(c.valorRestante)}</td>
        <td>${c.vencimento ? window.Fmt.isoDateToDisplay(c.vencimento) : 'Sem vencimento'}${vencida ? ' <span class="badge badge--inativo">vencida</span>' : ''}</td>
        <td><span class="badge badge--${c.statusPagamento.toLowerCase()}">${window.Fmt.statusLabel(c.statusPagamento)}</span></td>
      </tr>`;
      })
      .join('');
    tbody.querySelectorAll('tr[data-href]').forEach((tr) => {
      tr.addEventListener('click', () => (location.href = tr.dataset.href));
    });
  }

  async function loadAll() {
    try {
      await Promise.all([loadPeriodo(), loadPorProdutor(), loadPorCliente(), loadContas()]);
    } catch (err) {
      window.Toast.error(err.message || 'Erro ao carregar relatórios.');
    }
  }

  function init() {
    qs('f-inicio').value = window.Fmt.firstDayOfMonthIso();
    qs('f-fim').value = window.Fmt.todayIso();
    qs('periodo-form').addEventListener('submit', (e) => {
      e.preventDefault();
      loadPeriodo().catch((err) => window.Toast.error(err.message || 'Erro ao gerar relatório do período.'));
    });
    loadAll();
  }

  if (window.Shell.boot()) init();
})();
