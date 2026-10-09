package com.manomelancias.api.usuario;

/** Resultado do cadastro: o usuário e se o e-mail de confirmação saiu de fato (false = SMTP não configurado). */
public record UsuarioCriado(Usuario usuario, boolean emailEnviado) {
}
