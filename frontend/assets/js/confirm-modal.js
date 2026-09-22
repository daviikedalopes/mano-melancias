(function () {
  let activeResolve = null;
  let overlay = null;

  function close(result) {
    if (!overlay) return;
    overlay.hidden = true;
    const resolve = activeResolve;
    activeResolve = null;
    if (resolve) resolve(result);
  }

  function ensureOverlay() {
    if (overlay) return overlay;

    overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.id = 'confirm-modal-overlay';
    overlay.hidden = true;
    overlay.innerHTML =
      '<div class="modal modal--confirm" role="alertdialog" aria-modal="true" aria-labelledby="confirm-modal-title" aria-describedby="confirm-modal-message">' +
      '  <div class="modal__header">' +
      '    <h2 id="confirm-modal-title"></h2>' +
      '    <button type="button" class="modal__close" data-confirm-close aria-label="Fechar">&times;</button>' +
      '  </div>' +
      '  <div class="modal__body">' +
      '    <p id="confirm-modal-message"></p>' +
      '  </div>' +
      '  <div class="modal__footer">' +
      '    <button type="button" class="btn btn-secondary" data-confirm-cancel></button>' +
      '    <button type="button" class="btn btn-primary" data-confirm-ok></button>' +
      '  </div>' +
      '</div>';
    document.body.appendChild(overlay);

    overlay.querySelector('[data-confirm-ok]').addEventListener('click', () => close(true));
    overlay.querySelector('[data-confirm-cancel]').addEventListener('click', () => close(false));
    overlay.querySelector('[data-confirm-close]').addEventListener('click', () => close(false));
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) close(false);
    });
    document.addEventListener('keydown', (e) => {
      if (overlay && !overlay.hidden && e.key === 'Escape') close(false);
    });

    return overlay;
  }

  function show(opts) {
    const options = opts || {};
    const el = ensureOverlay();

    el.querySelector('#confirm-modal-title').textContent = options.title || 'Confirmar';
    el.querySelector('#confirm-modal-message').textContent = options.message || '';

    const okBtn = el.querySelector('[data-confirm-ok]');
    const cancelBtn = el.querySelector('[data-confirm-cancel]');
    okBtn.textContent = options.confirmText || 'Confirmar';
    cancelBtn.textContent = options.cancelText || 'Cancelar';
    okBtn.className = 'btn ' + (options.confirmVariant === 'danger' ? 'btn-danger' : 'btn-primary');

    el.hidden = false;

    return new Promise((resolve) => {
      activeResolve = resolve;
      okBtn.focus();
    });
  }

  window.ConfirmModal = { show };
})();
