(function () {
  async function load() {
    const inicio = window.Fmt.firstDayOfMonthIso();
    const fim = window.Fmt.todayIso();

    const [periodo, contas, vendas] = await Promise.all([
      window.Api.get('/relatorios/vendas-periodo' + window.Api.buildQuery({ inicio, fim })),
      window.Api.get('/relatorios/contas-a-receber'),
      window.Api.get('/vendas'),
    ]);

    renderStats(periodo, contas);
    renderUltimasVendas(vendas);
    renderContasAVencer(contas);
  }

  function renderStats(periodo, contas) {
    document.getElementById('stat-qtd-vendas').textContent = window.Fmt.integer(periodo.quantidadeVendas || 0);
    document.getElementById('stat-total-mercadoria').textContent = window.Fmt.currency(periodo.totalValorMercadoria || 0);
    document.getElementById('stat-total-frete').textContent = window.Fmt.currency(periodo.totalValorFrete || 0);

    const totalReceber = contas.reduce((sum, c) => sum + (window.Fmt.toNumber(c.valorRestante) || 0), 0);
    const hoje = window.Fmt.todayIso();
    const vencidas = contas.filter((c) => c.vencimento && c.vencimento < hoje).length;
    document.getElementById('stat-a-receber').textContent = window.Fmt.currency(totalReceber);
    document.getElementById('stat-a-receber-meta').textContent =
      vencidas > 0 ? `${vencidas} conta(s) vencida(s)` : 'Nenhuma conta vencida';
  }

  function renderUltimasVendas(vendas) {
    const tbody = document.getElementById('ultimas-vendas-body');
    const ordenadas = [...vendas].sort((a, b) => (b.numero || 0) - (a.numero || 0)).slice(0, 8);
    if (ordenadas.length === 0) {
      document.getElementById('ultimas-vendas-empty').hidden = false;
      return;
    }
    tbody.innerHTML = ordenadas
      .map(
        (v) => `
      <tr class="is-clickable" data-href="venda-detalhe.html?id=${v.id}">
        <td><span class="table__primary">Nº ${v.numero}</span><div class="table__secondary">${window.Fmt.isoDateToDisplay(v.dataVenda)}</div></td>
        <td>${window.escapeHtml(v.clienteNome)}</td>
        <td class="num-col num">${window.Fmt.weight(v.pesoLiquido)}</td>
        <td class="num-col num">${window.Fmt.currency(v.valorMercadoria)}</td>
        <td><span class="badge badge--${v.statusPagamento.toLowerCase()}">${window.Fmt.statusLabel(v.statusPagamento)}</span></td>
      </tr>`
      )
      .join('');
    tbody.querySelectorAll('tr[data-href]').forEach((tr) => {
      tr.addEventListener('click', () => (location.href = tr.dataset.href));
    });
  }

  function renderContasAVencer(contas) {
    const tbody = document.getElementById('contas-vencer-body');
    const top = contas.slice(0, 6);
    if (top.length === 0) {
      document.getElementById('contas-vencer-empty').hidden = false;
      return;
    }
    const hoje = window.Fmt.todayIso();
    tbody.innerHTML = top
      .map((c) => {
        const vencida = c.vencimento && c.vencimento < hoje;
        return `
      <tr class="is-clickable" data-href="venda-detalhe.html?id=${c.vendaId}">
        <td><span class="table__primary">Nº ${c.numero}</span></td>
        <td>${window.escapeHtml(c.clienteNome)}</td>
        <td class="num-col num">${window.Fmt.currency(c.valorRestante)}</td>
        <td>${c.vencimento ? window.Fmt.isoDateToDisplay(c.vencimento) : '—'}${vencida ? ' <span class="badge badge--inativo">vencida</span>' : ''}</td>
      </tr>`;
      })
      .join('');
    tbody.querySelectorAll('tr[data-href]').forEach((tr) => {
      tr.addEventListener('click', () => (location.href = tr.dataset.href));
    });
  }

  if (window.Shell.boot()) {
    load().catch((err) => window.Toast.error(err.message || 'Erro ao carregar o painel.'));
  }
})();
