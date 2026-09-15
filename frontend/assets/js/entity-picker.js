(function () {
  function debounce(fn, wait) {
    let timer;
    return (...args) => {
      clearTimeout(timer);
      timer = setTimeout(() => fn(...args), wait);
    };
  }

  const escapeHtml = window.escapeHtml || ((str) => String(str == null ? '' : str));

  /**
   * Componente de busca com cadastro "on the fly": digita, escolhe um
   * registro existente OU preenche um cadastro novo — sem travar o
   * lançamento da venda por falta de um cadastro prévio.
   */
  class EntityPicker {
    constructor(root, options) {
      this.root = root;
      this.opts = Object.assign({ minChars: 1 }, options);
      this.selected = null;
      this.novoOpen = false;
      this.novoValues = {};
      this.results = [];
      this.render();
      if (this.opts.initial) this.setSelected(this.opts.initial, { silent: true });
    }

    render() {
      this.root.innerHTML = `
        <div class="picker">
          <div data-search-wrap>
            <input type="text" class="input" placeholder="${escapeHtml(this.opts.placeholder || 'Buscar...')}" data-search-input autocomplete="off" />
            <div class="picker__menu" data-menu hidden></div>
          </div>
          <div data-selected-wrap hidden>
            <div class="picker__selected">
              <div>
                <div class="picker__selected-text" data-selected-title></div>
                <div class="picker__selected-sub" data-selected-sub></div>
              </div>
              <button type="button" class="picker__clear" data-clear>Trocar</button>
            </div>
          </div>
          <div class="picker__new-form" data-new-form hidden></div>
        </div>
      `;
      this.searchInput = this.root.querySelector('[data-search-input]');
      this.searchWrap = this.root.querySelector('[data-search-wrap]');
      this.menu = this.root.querySelector('[data-menu]');
      this.selectedWrap = this.root.querySelector('[data-selected-wrap]');
      this.selectedTitle = this.root.querySelector('[data-selected-title]');
      this.selectedSub = this.root.querySelector('[data-selected-sub]');
      this.newFormEl = this.root.querySelector('[data-new-form]');

      this.searchInput.addEventListener('input', debounce(() => this.onSearch(), 280));
      this.searchInput.addEventListener('focus', () => {
        if (this.results.length) this.openMenu();
      });
      document.addEventListener('click', (e) => {
        if (!this.root.contains(e.target)) this.closeMenu();
      });
      this.root.querySelector('[data-clear]').addEventListener('click', () => this.clearSelected());
    }

    async onSearch() {
      const q = this.searchInput.value.trim();
      if (this.novoOpen) this.closeNewForm();
      if (q.length < this.opts.minChars) {
        this.results = [];
        this.closeMenu();
        return;
      }
      try {
        this.results = await this.opts.searchFn(q);
      } catch (e) {
        this.results = [];
      }
      this.renderMenu(q);
    }

    renderMenu(query) {
      const items = this.results
        .map(
          (r, idx) => `
        <div class="picker__option" data-idx="${idx}">
          <div class="picker__option-title">${escapeHtml(r.title)}</div>
          ${r.subtitle ? `<div class="picker__option-sub">${escapeHtml(r.subtitle)}</div>` : ''}
        </div>
      `
        )
        .join('');
      const emptyMsg =
        this.results.length === 0
          ? `<div class="picker__empty">Nenhum resultado para "${escapeHtml(query)}"</div>`
          : '';
      this.menu.innerHTML = `
        ${items}
        ${emptyMsg}
        <div class="picker__option picker__option--new" data-new-option>+ ${escapeHtml(this.opts.newLabel)}</div>
      `;
      this.menu.querySelectorAll('[data-idx]').forEach((el) => {
        el.addEventListener('click', () => {
          this.setSelected(this.results[Number(el.dataset.idx)]);
          this.closeMenu();
        });
      });
      this.menu.querySelector('[data-new-option]').addEventListener('click', () => {
        this.openNewForm(query);
        this.closeMenu();
      });
      this.openMenu();
    }

    openMenu() {
      this.menu.hidden = false;
    }
    closeMenu() {
      this.menu.hidden = true;
    }

    setSelected(r, opts) {
      this.selected = r;
      this.searchWrap.hidden = true;
      this.selectedWrap.hidden = false;
      this.selectedTitle.textContent = r.title;
      this.selectedSub.textContent = r.subtitle || '';
      this.closeNewForm({ silent: true });
      if (!(opts && opts.silent)) this.emitChange();
    }

    clearSelected() {
      this.selected = null;
      this.searchWrap.hidden = false;
      this.selectedWrap.hidden = true;
      this.searchInput.value = '';
      this.results = [];
      this.searchInput.focus();
      this.emitChange();
    }

    openNewForm(query) {
      this.novoOpen = true;
      this.searchWrap.hidden = true;
      const fieldsHtml = this.opts.newFields
        .map(
          (f) => `
        <div class="field">
          <label>${escapeHtml(f.label)}${f.required ? '' : ' <span class="text-faint">(opcional)</span>'}</label>
          <input type="text" class="input" data-field="${f.name}" placeholder="${escapeHtml(f.placeholder || '')}" ${
            f.maxlength ? `maxlength="${f.maxlength}"` : ''
          } />
        </div>
      `
        )
        .join('');
      this.newFormEl.innerHTML = `
        <div class="picker__new-form-title">${escapeHtml(this.opts.newLabel)}</div>
        ${fieldsHtml}
        <button type="button" class="btn btn-ghost btn-sm" data-cancel-new>Cancelar e buscar de novo</button>
      `;
      this.newFormEl.hidden = false;

      const firstInput = this.newFormEl.querySelector('[data-field]');
      if (firstInput) {
        firstInput.value = query || '';
        firstInput.focus();
      }

      this.opts.newFields.forEach((f) => {
        const input = this.newFormEl.querySelector(`[data-field="${f.name}"]`);
        input.addEventListener('input', () => {
          if (f.mask) input.value = f.mask(input.value);
          this.syncNovoValues();
        });
      });
      this.newFormEl.querySelector('[data-cancel-new]').addEventListener('click', () => {
        this.closeNewForm();
        this.searchWrap.hidden = false;
        this.searchInput.focus();
      });

      this.syncNovoValues();
    }

    closeNewForm(opts) {
      this.novoOpen = false;
      this.newFormEl.hidden = true;
      this.newFormEl.innerHTML = '';
      this.novoValues = {};
      if (!(opts && opts.silent)) this.emitChange();
    }

    syncNovoValues() {
      const values = {};
      this.opts.newFields.forEach((f) => {
        const input = this.newFormEl.querySelector(`[data-field="${f.name}"]`);
        values[f.name] = input ? input.value.trim() : '';
      });
      this.novoValues = values;
      this.emitChange();
    }

    emitChange() {
      if (this.opts.onChange) this.opts.onChange(this.getValue());
    }

    getValue() {
      if (this.selected) return { id: this.selected.id };
      if (this.novoOpen) {
        const hasRequired = this.opts.newFields.filter((f) => f.required).every((f) => this.novoValues[f.name]);
        if (!hasRequired) return null;
        return { novo: Object.assign({}, this.novoValues) };
      }
      return null;
    }

    reset() {
      this.clearSelected();
      this.closeNewForm({ silent: true });
    }
  }

  window.EntityPicker = EntityPicker;
})();
