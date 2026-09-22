(function () {
  const qs = (id) => document.getElementById(id);
  const state = {
    vendaId: new URLSearchParams(location.search).get('id'),
    pickers: {},
    draftInterval: null,
    pristineSnapshot: null,
  };

  function round2(n) {
    return Math.round((n + Number.EPSILON) * 100) / 100;
  }

  function num(value) {
    const n = Number(value);
    return Number.isFinite(n) ? n : 0;
  }

  // ---- Cálculo ao vivo (espelha VendaService no backend) ----
  function recalc() {
    const pesoBruto = num(qs('f-peso-bruto').value);
    const descTara = num(qs('f-desc-tara').value);
    const descPalha = num(qs('f-desc-palha').value);
    const totalFrutas = parseInt(qs('f-total-frutas').value, 10);
    const precoKg = num(qs('f-preco-kg').value);

    const pesoLiquido = round2(pesoBruto - descTara - descPalha);
    qs('calc-peso-liquido').textContent = window.Fmt.weight(pesoLiquido);

    if (totalFrutas > 0) {
      qs('calc-media-peso').textContent = window.Fmt.weight(round2(pesoLiquido / totalFrutas));
    } else {
      qs('calc-media-peso').textContent = '— kg';
    }

    const valorMercadoria = round2(pesoLiquido * precoKg);
    qs('calc-valor-mercadoria').textContent = window.Fmt.currency(valorMercadoria);

    const tipoFrete = document.querySelector('input[name="tipoFrete"]:checked').value;
    let valorFrete;
    if (tipoFrete === 'POR_KG') {
      const precoFreteKg = num(qs('f-preco-frete-kg').value);
      valorFrete = round2(pesoLiquido * precoFreteKg);
    } else {
      valorFrete = num(qs('f-valor-frete').value);
    }

    const restante = round2(valorMercadoria - valorFrete);
    qs('calc-restante').textContent = window.Fmt.currency(restante);
  }

  function onTipoFreteChange() {
    const tipoFrete = document.querySelector('input[name="tipoFrete"]:checked').value;
    qs('field-valor-frete').hidden = tipoFrete !== 'NEGOCIADO';
    qs('field-preco-frete-kg').hidden = tipoFrete !== 'POR_KG';
    recalc();
  }

  function wireRecalc() {
    [
      'f-peso-bruto',
      'f-desc-tara',
      'f-desc-palha',
      'f-total-frutas',
      'f-preco-kg',
      'f-valor-frete',
      'f-preco-frete-kg',
    ].forEach((id) => qs(id).addEventListener('input', () => { recalc(); saveDraft(); }));

    ['f-data', 'f-vencimento', 'f-nf', 'f-status', 'f-obs'].forEach((id) => {
      qs(id).addEventListener('input', saveDraft);
      qs(id).addEventListener('change', saveDraft);
    });

    document.querySelectorAll('input[name="tipoFrete"]').forEach((el) =>
      el.addEventListener('change', () => { onTipoFreteChange(); saveDraft(); })
    );
  }

  // ---- Rascunho automático (localStorage) ----
  function draftKey() {
    return 'mm_venda_draft:' + (state.vendaId || 'novo');
  }

  function collectDraft() {
    const tipoFreteEl = document.querySelector('input[name="tipoFrete"]:checked');
    return {
      savedAt: Date.now(),
      fields: {
        data: qs('f-data').value,
        pesoBruto: qs('f-peso-bruto').value,
        descTara: qs('f-desc-tara').value,
        descPalha: qs('f-desc-palha').value,
        totalFrutas: qs('f-total-frutas').value,
        precoKg: qs('f-preco-kg').value,
        tipoFrete: tipoFreteEl ? tipoFreteEl.value : null,
        valorFrete: qs('f-valor-frete').value,
        precoFreteKg: qs('f-preco-frete-kg').value,
        vencimento: qs('f-vencimento').value,
        nf: qs('f-nf').value,
        status: qs('f-status').value,
        obs: qs('f-obs').value,
      },
      pickers: {
        cliente: pickerSnapshot(state.pickers.cliente),
        produtor: pickerSnapshot(state.pickers.produtor),
        motorista: pickerSnapshot(state.pickers.motorista),
        veiculo: pickerSnapshot(state.pickers.veiculo),
      },
    };
  }

  // Guarda o estado completo do EntityPicker: um registro já selecionado
  // (com título/subtítulo, não só o id) OU um cadastro novo em andamento
  // (formulário aberto + valores já digitados), para restaurar de verdade.
  function pickerSnapshot(picker) {
    if (!picker) return null;
    return {
      selected: picker.selected || null,
      novoOpen: !!picker.novoOpen,
      novoValues: picker.novoValues || {},
    };
  }

  function restorePicker(picker, snapshot) {
    if (!picker || !snapshot) return;
    if (snapshot.selected) {
      picker.setSelected(snapshot.selected, { silent: true });
      return;
    }
    if (snapshot.novoOpen) {
      picker.openNewForm();
      Object.entries(snapshot.novoValues || {}).forEach(([nome, valor]) => {
        const input = picker.newFormEl.querySelector(`[data-field="${nome}"]`);
        if (input) input.value = valor;
      });
      picker.syncNovoValues();
    }
  }

  function saveDraft() {
    try {
      localStorage.setItem(draftKey(), JSON.stringify(collectDraft()));
    } catch (err) {
      // localStorage indisponível (modo privado, etc.) — rascunho simplesmente não é salvo
    }
  }

  function loadDraft() {
    try {
      const raw = localStorage.getItem(draftKey());
      return raw ? JSON.parse(raw) : null;
    } catch (err) {
      return null;
    }
  }

  function clearDraft() {
    try {
      localStorage.removeItem(draftKey());
    } catch (err) {
      // ignora
    }
  }

  function applyDraft(draft) {
    const f = draft.fields || {};
    qs('f-data').value = f.data || qs('f-data').value;
    qs('f-peso-bruto').value = f.pesoBruto || '';
    qs('f-desc-tara').value = f.descTara || '';
    qs('f-desc-palha').value = f.descPalha || '';
    qs('f-total-frutas').value = f.totalFrutas || '';
    qs('f-preco-kg').value = f.precoKg || '';
    qs('f-valor-frete').value = f.valorFrete || '';
    qs('f-preco-frete-kg').value = f.precoFreteKg || '';
    qs('f-vencimento').value = f.vencimento || '';
    qs('f-nf').value = f.nf || '';
    if (f.status) qs('f-status').value = f.status;
    qs('f-obs').value = f.obs || '';

    if (f.tipoFrete === 'POR_KG') {
      qs('f-tipo-porkg').checked = true;
    } else if (f.tipoFrete === 'NEGOCIADO') {
      qs('f-tipo-negociado').checked = true;
    }
    onTipoFreteChange();

    ['cliente', 'produtor', 'motorista', 'veiculo'].forEach((nome) => {
      restorePicker(state.pickers[nome], draft.pickers && draft.pickers[nome]);
    });

    recalc();
  }

  // Compara o rascunho salvo com o estado "limpo" do formulário (capturado
  // antes de qualquer edição do usuário). Se forem iguais, o rascunho não
  // representa nenhuma alteração real e não há motivo para perguntar nada.
  function draftTemAlteracoes(draft) {
    if (!state.pristineSnapshot) return true;
    return (
      JSON.stringify(draft.fields) !== JSON.stringify(state.pristineSnapshot.fields) ||
      JSON.stringify(draft.pickers) !== JSON.stringify(state.pristineSnapshot.pickers)
    );
  }

  async function ofertarRestaurarRascunho() {
    const draft = loadDraft();
    if (!draft) return;

    if (!draftTemAlteracoes(draft)) {
      clearDraft();
      return;
    }

    const restaurar = await window.ConfirmModal.show({
      title: 'Rascunho encontrado',
      message: 'Encontramos um rascunho não salvo desta venda. Deseja continuar de onde parou?',
      confirmText: 'Continuar de onde parei',
      cancelText: 'Começar do zero',
    });
    if (restaurar) {
      applyDraft(draft);
    } else {
      clearDraft();
    }
  }

  // ---- Pickers de cliente / produtor / motorista / veículo ----
  function initPickers() {
    state.pickers.cliente = new window.EntityPicker(qs('picker-cliente'), {
      onChange: saveDraft,
      placeholder: 'Buscar cliente por nome...',
      newLabel: 'Cadastrar novo cliente',
      searchFn: async (q) => {
        const list = await window.Api.get('/clientes' + window.Api.buildQuery({ nome: q }));
        return list.map((c) => ({ id: c.id, title: c.nome, subtitle: `${c.municipio}/${c.estado}` }));
      },
      newFields: [
        { name: 'nome', label: 'Nome', required: true },
        { name: 'municipio', label: 'Município', required: true },
        { name: 'estado', label: 'Estado (UF)', required: true, maxlength: 2, mask: (v) => v.toUpperCase().slice(0, 2) },
        { name: 'telefone', label: 'Telefone', required: false, mask: window.Fmt.maskPhone },
      ],
    });

    state.pickers.produtor = new window.EntityPicker(qs('picker-produtor'), {
      onChange: saveDraft,
      placeholder: 'Buscar produtor por nome...',
      newLabel: 'Cadastrar novo produtor',
      searchFn: async (q) => {
        const list = await window.Api.get('/produtores' + window.Api.buildQuery({ nome: q }));
        return list.map((p) => ({ id: p.id, title: p.nome, subtitle: p.cidade }));
      },
      newFields: [
        { name: 'nome', label: 'Nome', required: true },
        { name: 'cidade', label: 'Cidade', required: true },
        { name: 'telefone', label: 'Telefone', required: false, mask: window.Fmt.maskPhone },
      ],
    });

    state.pickers.motorista = new window.EntityPicker(qs('picker-motorista'), {
      onChange: saveDraft,
      placeholder: 'Buscar motorista por nome...',
      newLabel: 'Cadastrar novo motorista',
      searchFn: async (q) => {
        const list = await window.Api.get('/motoristas' + window.Api.buildQuery({ nome: q }));
        return list.map((m) => ({ id: m.id, title: m.nome, subtitle: window.Fmt.maskCpf(m.cpf) }));
      },
      newFields: [
        { name: 'nome', label: 'Nome', required: true },
        { name: 'cpf', label: 'CPF', required: true, maxlength: 14, mask: window.Fmt.maskCpf },
        { name: 'telefone', label: 'Telefone', required: false, mask: window.Fmt.maskPhone },
      ],
    });

    state.pickers.veiculo = new window.EntityPicker(qs('picker-veiculo'), {
      onChange: saveDraft,
      placeholder: 'Buscar veículo por placa...',
      newLabel: 'Cadastrar novo veículo',
      searchFn: async (q) => {
        const list = await window.Api.get('/veiculos' + window.Api.buildQuery({ placa: q }));
        return list.map((v) => ({ id: v.id, title: v.placa, subtitle: v.cidade }));
      },
      newFields: [
        { name: 'placa', label: 'Placa', required: true, maxlength: 7, mask: window.Fmt.maskPlaca },
        { name: 'cidade', label: 'Cidade', required: true },
      ],
    });
  }

  function clearErrors() {
    document.querySelectorAll('[data-field-error-for]').forEach((el) => {
      el.hidden = true;
      el.textContent = '';
    });
  }

  function showFieldErrors(erros) {
    let matchedAny = false;
    Object.entries(erros || {}).forEach(([field, msg]) => {
      const errEl = document.querySelector(`[data-field-error-for="${field}"]`);
      if (errEl) {
        errEl.hidden = false;
        errEl.textContent = msg;
        matchedAny = true;
      }
    });
    return matchedAny;
  }

  function buildPayload() {
    const tipoFrete = document.querySelector('input[name="tipoFrete"]:checked').value;

    const payload = {
      dataVenda: qs('f-data').value,
      pesoBruto: num(qs('f-peso-bruto').value),
      descTara: num(qs('f-desc-tara').value),
      descPalha: num(qs('f-desc-palha').value) || 0,
      totalFrutas: parseInt(qs('f-total-frutas').value, 10),
      precoKg: num(qs('f-preco-kg').value),
      tipoFrete,
      vencimento: qs('f-vencimento').value || null,
      nf: qs('f-nf').value.trim() || null,
      statusPagamento: qs('f-status').value,
      observacoes: qs('f-obs').value.trim() || null,
    };

    if (tipoFrete === 'POR_KG') {
      payload.precoFreteKg = num(qs('f-preco-frete-kg').value);
    } else {
      payload.valorFrete = num(qs('f-valor-frete').value);
    }

    const clienteVal = state.pickers.cliente.getValue();
    if (clienteVal && clienteVal.id) payload.clienteId = clienteVal.id;
    else if (clienteVal && clienteVal.novo) payload.clienteNovo = clienteVal.novo;

    const produtorVal = state.pickers.produtor.getValue();
    if (produtorVal && produtorVal.id) payload.produtorId = produtorVal.id;
    else if (produtorVal && produtorVal.novo) payload.produtorNovo = produtorVal.novo;

    const motoristaVal = state.pickers.motorista.getValue();
    if (motoristaVal && motoristaVal.id) payload.motoristaId = motoristaVal.id;
    else if (motoristaVal && motoristaVal.novo) payload.motoristaNovo = motoristaVal.novo;

    const veiculoVal = state.pickers.veiculo.getValue();
    if (veiculoVal && veiculoVal.id) {
      payload.veiculoId = veiculoVal.id;
    } else if (veiculoVal && veiculoVal.novo) {
      payload.veiculoNovo = Object.assign({}, veiculoVal.novo);
      if (payload.motoristaId) payload.veiculoNovo.motoristaId = payload.motoristaId;
    }

    return { payload, clienteVal, produtorVal, motoristaVal, veiculoVal };
  }

  function validatePickers(clienteVal, produtorVal, motoristaVal, veiculoVal) {
    if (!clienteVal) return 'Selecione um cliente existente ou preencha o cadastro novo.';
    if (!produtorVal) return 'Selecione um produtor existente ou preencha o cadastro novo.';
    if (!motoristaVal) return 'Selecione um motorista existente ou preencha o cadastro novo.';
    if (!veiculoVal) return 'Selecione um veículo existente ou preencha o cadastro novo.';
    return null;
  }

  async function onSubmit(e) {
    e.preventDefault();
    clearErrors();

    const { payload, clienteVal, produtorVal, motoristaVal, veiculoVal } = buildPayload();
    const pickerError = validatePickers(clienteVal, produtorVal, motoristaVal, veiculoVal);
    if (pickerError) {
      window.Toast.error(pickerError);
      return;
    }

    const submitBtn = qs('btn-submit');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Salvando...';

    try {
      let venda;
      if (state.vendaId) {
        venda = await window.Api.put('/vendas/' + state.vendaId, payload);
        window.Toast.success('Venda atualizada.');
      } else {
        venda = await window.Api.post('/vendas', payload);
      }
      clearDraft();
      if (state.draftInterval) clearInterval(state.draftInterval);
      location.href = 'venda-detalhe.html?id=' + venda.id + (state.vendaId ? '' : '&created=1');
    } catch (err) {
      if (err.erros) {
        const matchedAny = showFieldErrors(err.erros);
        if (matchedAny) {
          window.Toast.error('Confira os campos destacados.');
        } else {
          window.Toast.error(Object.values(err.erros)[0] || 'Não foi possível salvar a venda.');
        }
      } else {
        window.Toast.error(err.message || 'Não foi possível salvar a venda.');
      }
      submitBtn.disabled = false;
      submitBtn.textContent = 'Salvar venda';
    }
  }

  async function loadForEdit() {
    const v = await window.Api.get('/vendas/' + state.vendaId);

    qs('page-title').textContent = 'Editar venda Nº ' + v.numero;
    qs('page-subtitle').textContent = 'Lançada em ' + window.Fmt.isoDateToDisplay(v.dataVenda);
    document.title = 'Editar venda · Mano Melancias';

    qs('f-data').value = v.dataVenda;
    qs('f-peso-bruto').value = v.pesoBruto;
    qs('f-desc-tara').value = v.descTara;
    qs('f-desc-palha').value = v.descPalha;
    qs('f-total-frutas').value = v.totalFrutas;
    qs('f-preco-kg').value = v.precoKg;
    qs('f-vencimento').value = v.vencimento || '';
    qs('f-nf').value = v.nf || '';
    qs('f-status').value = v.statusPagamento;
    qs('f-obs').value = v.observacoes || '';

    if (v.tipoFrete === 'POR_KG') {
      qs('f-tipo-porkg').checked = true;
      qs('f-preco-frete-kg').value = v.precoFreteKg;
    } else {
      qs('f-tipo-negociado').checked = true;
      qs('f-valor-frete').value = v.valorFrete;
    }
    onTipoFreteChange();

    state.pickers.cliente.setSelected(
      { id: v.clienteId, title: v.clienteNome, subtitle: `${v.clienteMunicipio}/${v.clienteEstado}` },
      { silent: true }
    );
    state.pickers.produtor.setSelected({ id: v.produtorId, title: v.produtorNome, subtitle: v.produtorCidade }, { silent: true });
    state.pickers.motorista.setSelected(
      { id: v.motoristaId, title: v.motoristaNome, subtitle: window.Fmt.maskCpf(v.motoristaCpf) },
      { silent: true }
    );
    state.pickers.veiculo.setSelected({ id: v.veiculoId, title: v.veiculoPlaca, subtitle: v.veiculoCidade }, { silent: true });

    recalc();
  }

  async function init() {
    initPickers();
    wireRecalc();
    qs('venda-form').addEventListener('submit', onSubmit);

    window.addEventListener('beforeunload', saveDraft);
    window.addEventListener('pagehide', saveDraft);
    state.draftInterval = setInterval(saveDraft, 3000);

    if (state.vendaId) {
      try {
        await loadForEdit();
      } catch (err) {
        window.Toast.error(err.message || 'Não foi possível carregar esta venda.');
        return;
      }
    } else {
      qs('f-data').value = window.Fmt.todayIso();
      recalc();
    }

    state.pristineSnapshot = collectDraft();
    await ofertarRestaurarRascunho();
  }

  if (window.Shell.boot()) init();
})();
