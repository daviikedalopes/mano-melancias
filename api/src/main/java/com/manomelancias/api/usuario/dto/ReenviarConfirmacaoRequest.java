package com.manomelancias.api.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReenviarConfirmacaoRequest {

    @NotBlank(message = "e-mail é obrigatório")
    @Email(message = "e-mail inválido")
    @Size(max = 255, message = "e-mail deve ter no máximo 255 caracteres")
    private String email;
}
