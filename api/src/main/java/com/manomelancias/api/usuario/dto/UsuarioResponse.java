package com.manomelancias.api.usuario.dto;

import com.manomelancias.api.usuario.Papel;
import com.manomelancias.api.usuario.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UsuarioResponse {

    private UUID id;
    private String nome;
    private String email;
    private Papel papel;
    private Boolean ativo;
    private Boolean emailConfirmado;
    // Só preenchido na criação: false = o SMTP não está configurado e o e-mail NÃO saiu (null nas listagens)
    private Boolean emailEnviado;

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel(), usuario.getAtivo(),
                usuario.getEmailConfirmado(), null);
    }

    public static UsuarioResponse from(Usuario usuario, boolean emailEnviado) {
        return new UsuarioResponse(
                usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel(), usuario.getAtivo(),
                usuario.getEmailConfirmado(), emailEnviado);
    }
}
