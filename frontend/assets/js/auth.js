(function () {
  const TOKEN_KEY = 'mm_token';

  function decodeJwtPayload(token) {
    try {
      const payload = token.split('.')[1];
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const json = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
          .join('')
      );
      return JSON.parse(json);
    } catch (e) {
      return null;
    }
  }

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function getClaims() {
    const token = getToken();
    return token ? decodeJwtPayload(token) : null;
  }

  function isAuthenticated() {
    const claims = getClaims();
    if (!claims) return false;
    if (claims.exp && Date.now() >= claims.exp * 1000) return false;
    return true;
  }

  function getPapel() {
    const claims = getClaims();
    return claims ? claims.papel : null;
  }

  function getEmail() {
    const claims = getClaims();
    return claims ? claims.email : null;
  }

  function isAdmin() {
    return getPapel() === 'ADMIN';
  }

  function login(token) {
    localStorage.setItem(TOKEN_KEY, token);
  }

  function logout(opts) {
    localStorage.removeItem(TOKEN_KEY);
    if (!(opts && opts.silent)) {
      location.href = 'login.html';
    }
  }

  function requireAuth() {
    if (!isAuthenticated()) {
      logout({ silent: true });
      location.href = 'login.html';
      return false;
    }
    return true;
  }

  function requireAdmin() {
    if (!requireAuth()) return false;
    if (!isAdmin()) {
      location.href = 'index.html';
      return false;
    }
    return true;
  }

  window.Auth = {
    getToken,
    getClaims,
    isAuthenticated,
    getPapel,
    getEmail,
    isAdmin,
    login,
    logout,
    requireAuth,
    requireAdmin,
  };
})();
