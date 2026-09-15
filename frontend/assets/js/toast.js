(function () {
  function ensureStack() {
    let stack = document.querySelector('.toast-stack');
    if (!stack) {
      stack = document.createElement('div');
      stack.className = 'toast-stack';
      document.body.appendChild(stack);
    }
    return stack;
  }

  function show(message, type) {
    const stack = ensureStack();
    const el = document.createElement('div');
    el.className = 'toast' + (type ? ' toast--' + type : '');
    el.textContent = message;
    el.setAttribute('role', 'status');
    stack.appendChild(el);
    setTimeout(() => {
      el.style.transition = 'opacity 200ms ease';
      el.style.opacity = '0';
      setTimeout(() => el.remove(), 220);
    }, 4200);
  }

  window.Toast = {
    success: (msg) => show(msg, 'success'),
    error: (msg) => show(msg, 'error'),
    info: (msg) => show(msg, null),
  };
})();
