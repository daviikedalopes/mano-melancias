(function () {
  const qs = (id) => document.getElementById(id);
  // Filtros do último "Gerar" bem-sucedido: o PDF sai exatamente com o que está nos cartões,
  // mesmo que o usuário já tenha mexido nos campos sem clicar em Gerar.
  const state = { filtroAplicado: null };

  async function loadFiltros() {
    // incluirInativos: vendas antigas podem envolver produtores/clientes já inativados.
    const [produtores, clientes] = await Promise.all([
      window.Api.get('/produtores?incluirInativos=true'),
      window.Api.get('/clientes?incluirInativos=true'),
    ]);
    const options = (list) =>
      list.map((x) => `<option value="${x.id}">${window.escapeHtml(x.nome)}</option>`).join('');
    qs('f-produtor').innerHTML = '<option value="">Todos</option>' + options(produtores);
    qs('f-cliente').innerHTML = '<option value="">Todos</option>' + options(clientes);
  }

  function filtroAtual() {
    return {
      inicio: qs('f-inicio').value,
      fim: qs('f-fim').value,
      produtorId: qs('f-produtor').value,
      clienteId: qs('f-cliente').value,
    };
  }

  async function loadPeriodo() {
    const filtro = filtroAtual();
    const data = await window.Api.get('/relatorios/vendas-periodo' + window.Api.buildQuery(filtro));
    state.filtroAplicado = filtro;
    qs('p-qtd').textContent = window.Fmt.integer(data.quantidadeVendas || 0);
    qs('p-mercadoria').textContent = window.Fmt.currency(data.totalValorMercadoria || 0);
    qs('p-frete').textContent = window.Fmt.currency(data.totalValorFrete || 0);
    qs('p-restante').textContent = window.Fmt.currency(data.totalRestantePagar || 0);
  }

  async function baixarPdfPeriodo() {
    if (!state.filtroAplicado) return;
    const btn = qs('btn-periodo-pdf');
    const originalText = btn.textContent;
    btn.disabled = true;
    btn.textContent = 'Gerando...';
    try {
      const blob = await window.Api.getBlob(
        '/relatorios/vendas-periodo/pdf' + window.Api.buildQuery(state.filtroAplicado)
      );
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.target = '_blank';
      a.rel = 'noopener';
      document.body.appendChild(a);
      a.click();
      a.remove();
      setTimeout(() => URL.revokeObjectURL(url), 30000);
    } catch (err) {
      window.Toast.error(err.message || 'Não foi possível gerar o PDF.');
    } finally {
      btn.disabled = false;
      btn.textContent = originalText;
    }
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
    qs('btn-periodo-pdf').addEventListener('click', baixarPdfPeriodo);
    loadFiltros().catch((err) => window.Toast.error(err.message || 'Erro ao carregar filtros.'));
    loadAll();
  }

  if (window.Shell.boot()) init();
})();
