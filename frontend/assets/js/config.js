// Endereço da API Spring Boot, escolhido pelo host em que a página está aberta:
//  - localhost: backend local;
//  - qualquer outro domínio (servidor): mesma origem, em /api (o Caddy repassa ao backend).
window.APP_CONFIG = {
  API_BASE_URL: ['localhost', '127.0.0.1', '[::1]'].includes(location.hostname)
    ? 'http://localhost:8080'
    : `${location.origin}/api`,
};
