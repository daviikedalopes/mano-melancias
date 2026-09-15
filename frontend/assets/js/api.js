(function () {
  const BASE_URL = window.APP_CONFIG.API_BASE_URL;

  class ApiError extends Error {
    constructor(status, message, erros) {
      super(message);
      this.status = status;
      this.erros = erros || null;
    }
  }

  function buildQuery(params) {
    const usp = new URLSearchParams();
    Object.entries(params || {}).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        usp.append(key, value);
      }
    });
    const qs = usp.toString();
    return qs ? '?' + qs : '';
  }

  async function request(method, path, body) {
    const headers = { Accept: 'application/json' };
    const token = window.Auth ? window.Auth.getToken() : null;
    if (token) headers['Authorization'] = 'Bearer ' + token;

    let payload;
    if (body !== undefined) {
      headers['Content-Type'] = 'application/json';
      payload = JSON.stringify(body);
    }

    let response;
    try {
      response = await fetch(BASE_URL + path, { method, headers, body: payload });
    } catch (networkError) {
      throw new ApiError(0, 'Não foi possível conectar à API. Verifique se o backend está rodando.');
    }

    if (response.status === 204) return null;

    const contentType = response.headers.get('content-type') || '';
    const isJson = contentType.includes('application/json');
    const data = isJson ? await response.json().catch(() => null) : null;

    if (!response.ok) {
      if (response.status === 401) {
        window.Auth.logout({ silent: true });
        if (!location.pathname.endsWith('login.html')) {
          location.href = 'login.html';
        }
      }
      const message = (data && data.message) || 'Ocorreu um erro inesperado.';
      throw new ApiError(response.status, message, data && data.erros);
    }

    return data;
  }

  async function requestBlob(path) {
    const headers = {};
    const token = window.Auth ? window.Auth.getToken() : null;
    if (token) headers['Authorization'] = 'Bearer ' + token;

    const response = await fetch(BASE_URL + path, { headers });
    if (!response.ok) {
      throw new ApiError(response.status, 'Não foi possível gerar o arquivo.');
    }
    return response.blob();
  }

  window.Api = {
    get: (path) => request('GET', path),
    post: (path, body) => request('POST', path, body),
    put: (path, body) => request('PUT', path, body),
    del: (path) => request('DELETE', path),
    getBlob: (path) => requestBlob(path),
    buildQuery,
    ApiError,
  };
})();
