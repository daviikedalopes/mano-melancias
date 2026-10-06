// Endereço da API Spring Boot. Em localhost usa o backend local; fora dele, o do Render.
const LOCAL_HOSTS = ['localhost', '127.0.0.1', '[::1]'];

window.APP_CONFIG = {
  API_BASE_URL: LOCAL_HOSTS.includes(location.hostname)
    ? 'http://localhost:8080'
    : 'https://mano-melancias-backend.onrender.com',
};
