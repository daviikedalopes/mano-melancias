(function () {
  const currencyFmt = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
  const decimalFmt = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const decimal4Fmt = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 4 });
  const intFmt = new Intl.NumberFormat('pt-BR');
  const dateTimeFmt = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });

  function toNumber(value) {
    if (value === null || value === undefined || value === '') return null;
    const n = typeof value === 'number' ? value : Number(String(value).replace(',', '.'));
    return Number.isFinite(n) ? n : null;
  }

  function currency(value) {
    const n = toNumber(value);
    return n === null ? '—' : currencyFmt.format(n);
  }

  function weight(value, withSuffix) {
    const n = toNumber(value);
    if (n === null) return '—';
    return decimalFmt.format(n) + (withSuffix === false ? '' : ' kg');
  }

  function pricePerKg(value) {
    const n = toNumber(value);
    return n === null ? '—' : decimal4Fmt.format(n) + '/kg';
  }

  function integer(value) {
    const n = toNumber(value);
    return n === null ? '—' : intFmt.format(n);
  }

  function isoDateToDisplay(iso) {
    if (!iso) return '—';
    const [y, m, d] = iso.split('-');
    if (!y || !m || !d) return iso;
    return `${d}/${m}/${y}`;
  }

  function dateTime(iso) {
    if (!iso) return '—';
    return dateTimeFmt.format(new Date(iso));
  }

  function todayIso() {
    const d = new Date();
    const local = new Date(d.getTime() - d.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 10);
  }

  function firstDayOfMonthIso() {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`;
  }

  function maskCpf(value) {
    const digits = (value || '').replace(/\D/g, '').slice(0, 11);
    if (digits.length > 9) return digits.replace(/(\d{3})(\d{3})(\d{3})(\d{1,2})/, '$1.$2.$3-$4');
    if (digits.length > 6) return digits.replace(/(\d{3})(\d{3})(\d{1,3})/, '$1.$2.$3');
    if (digits.length > 3) return digits.replace(/(\d{3})(\d{1,3})/, '$1.$2');
    return digits;
  }

  function isValidCpf(value) {
    const digits = (value || '').replace(/\D/g, '');
    if (digits.length !== 11 || /^(\d)\1{10}$/.test(digits)) return false;
    const nums = digits.split('').map(Number);
    const calcDigit = (qty, startWeight) => {
      let sum = 0;
      for (let i = 0; i < qty; i++) sum += nums[i] * (startWeight - i);
      const rest = sum % 11;
      return rest < 2 ? 0 : 11 - rest;
    };
    return nums[9] === calcDigit(9, 10) && nums[10] === calcDigit(10, 11);
  }

  function maskPlaca(value) {
    return (value || '').toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 7);
  }

  function maskPhone(value) {
    const digits = (value || '').replace(/\D/g, '').slice(0, 11);
    if (digits.length > 10) return digits.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    if (digits.length > 5) return digits.replace(/(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
    if (digits.length > 2) return digits.replace(/(\d{2})(\d{0,5})/, '($1) $2');
    return digits;
  }

  function statusLabel(status) {
    return { PENDENTE: 'Pendente', PAGO_PARCIAL: 'Pago parcial', PAGO: 'Pago' }[status] || status;
  }

  function papelLabel(papel) {
    return papel === 'ADMIN' ? 'Administrador' : 'Operador';
  }

  function escapeHtml(str) {
    return String(str == null ? '' : str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }
  window.escapeHtml = escapeHtml;

  window.Fmt = {
    currency,
    weight,
    pricePerKg,
    integer,
    isoDateToDisplay,
    dateTime,
    todayIso,
    firstDayOfMonthIso,
    maskCpf,
    isValidCpf,
    maskPlaca,
    maskPhone,
    toNumber,
    statusLabel,
    papelLabel,
  };
})();
