package com.manomelancias.api.usuario.dto;

/** emailEnviado = false quando o SMTP não está configurado e o link só foi escrito no log da API. */
public record EnvioEmailResponse(boolean emailEnviado) {
}
