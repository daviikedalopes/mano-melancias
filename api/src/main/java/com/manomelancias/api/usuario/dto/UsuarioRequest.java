package com.manomelancias.api.usuario.dto;

import com.manomelancias.api.shared.validation.SenhaForte;
import com.manomelancias.api.shared.validation.ValidEmail;
import com.manomelancias.api.usuario.Papel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequest {

    @NotBlank(message = "nome é obrigatório")
    @Size(max = 255, message = "nome deve ter no máximo 255 caracteres")
    private String nome;

    @NotBlank(message = "e-mail é obrigatório")
    @Size(max = 255, message = "e-mail deve ter no máximo 255 caracteres")
    @ValidEmail
    private String email;

    @NotBlank(message = "senha é obrigatória")
    @Size(max = 72, message = "senha deve ter no máximo 72 caracteres")
    @SenhaForte
    private String senha;

    @NotNull(message = "papel é obrigatório")
    private Papel papel;
}
