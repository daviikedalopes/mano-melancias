package com.manomelancias.api.usuario.dto;

import com.manomelancias.api.usuario.Papel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequest {

    @NotBlank(message = "nome é obrigatório")
    private String nome;

    @NotBlank(message = "e-mail é obrigatório")
    @Email(message = "e-mail inválido")
    private String email;

    @NotBlank(message = "senha é obrigatória")
    @Size(min = 6, message = "senha deve ter no mínimo 6 caracteres")
    private String senha;

    @NotNull(message = "papel é obrigatório")
    private Papel papel;
}
