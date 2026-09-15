(function () {
  const qs = (id) => document.getElementById(id);
  const params = new URLSearchParams(location.search);
  const vendaId = params.get('id');

  function setText(id, value) {
    qs(id).textContent = value === null || value === undefined || value === '' ? '—' : value;
  }

  function render(v) {
    document.title = `Venda Nº ${v.numero} · Mano Melancias`;
    qs('page-subtitle').textContent = `Nº ${v.numero} · lançada em ${window.Fmt.isoDateToDisplay(v.dataVenda)}`;
    qs('btn-edit').href = 'venda-form.html?id=' + v.id;

    qs('t-numero').textContent = String(v.numero).padStart(6, '0');
    qs('t-data').textContent = window.Fmt.isoDateToDisplay(v.dataVenda);

    const statusEl = qs('t-status');
    statusEl.className = 'badge badge--' + v.statusPagamento.toLowerCase();
    statusEl.textContent = window.Fmt.statusLabel(v.statusPagamento);
    setText('t-criado-por', v.criadoPor ? `Lançada por ${v.criadoPor}` : '');

    setText('t-cliente-nome', v.clienteNome);
    setText('t-cliente-municipio', v.clienteMunicipio);
    setText('t-cliente-estado', v.clienteEstado);

    setText('t-peso-bruto', window.Fmt.weight(v.pesoBruto));
    setText('t-desc-tara', window.Fmt.weight(v.descTara));
    setText('t-desc-palha', window.Fmt.weight(v.descPalha));
    setText('t-peso-liquido', window.Fmt.weight(v.pesoLiquido));
    setText('t-total-frutas', window.Fmt.integer(v.totalFrutas));
    setText('t-media-peso', window.Fmt.weight(v.mediaPeso));

    setText('t-preco-kg', window.Fmt.pricePerKg(v.precoKg));
    setText('t-tipo-frete', v.tipoFrete === 'POR_KG' ? `Por kg (${window.Fmt.pricePerKg(v.precoFreteKg)})` : 'Negociado');
    setText('t-vencimento', v.vencimento ? window.Fmt.isoDateToDisplay(v.vencimento) : 'Sem vencimento');
    setText('t-nf', v.nf);

    setText('t-valor-mercadoria', window.Fmt.currency(v.valorMercadoria));
    setText('t-valor-frete', window.Fmt.currency(v.valorFrete));
    setText('t-restante', window.Fmt.currency(v.restantePagar));

    setText('t-motorista-nome', v.motoristaNome);
    setText('t-motorista-cpf', window.Fmt.maskCpf(v.motoristaCpf));
    setText('t-motorista-telefone', v.motoristaTelefone);
    setText('t-veiculo-placa', v.veiculoPlaca);
    setText('t-veiculo-cidade', v.veiculoCidade);
    setText('t-produtor-nome', v.produtorNome);

    if (v.observacoes) {
      qs('section-obs').hidden = false;
      qs('t-observacoes').textContent = v.observacoes;
    }

    qs('ticket').hidden = false;
  }

  async function baixarPdf() {
    const btn = qs('btn-pdf');
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.textContent = 'Gerando...';
    try {
      const blob = await window.Api.getBlob('/vendas/' + vendaId + '/pdf');
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
      btn.innerHTML = originalText;
    }
  }

  async function excluir(numero) {
    if (!confirm(`Excluir a venda Nº ${numero} definitivamente? Essa ação não pode ser desfeita.`)) return;
    try {
      await window.Api.del('/vendas/' + vendaId);
      window.Toast.success('Venda excluída.');
      location.href = 'vendas.html';
    } catch (err) {
      window.Toast.error(err.message || 'Não foi possível excluir a venda.');
    }
  }

  async function init() {
    if (!vendaId) {
      window.Toast.error('Nenhuma venda informada.');
      location.href = 'vendas.html';
      return;
    }

    qs('btn-pdf').addEventListener('click', baixarPdf);

    try {
      const v = await window.Api.get('/vendas/' + vendaId);
      render(v);
      qs('btn-excluir').addEventListener('click', () => excluir(v.numero));
      if (params.get('created') === '1') {
        window.Toast.success(`Venda Nº ${v.numero} registrada com sucesso.`);
      }
    } catch (err) {
      window.Toast.error(err.message || 'Não foi possível carregar esta venda.');
    }
  }

  if (window.Shell.boot()) init();
})();
