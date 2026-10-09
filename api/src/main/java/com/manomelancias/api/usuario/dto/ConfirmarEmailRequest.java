package com.manomelancias.api.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmarEmailRequest {

    @NotBlank(message = "token é obrigatório")
    @Size(max = 2000, message = "token inválido")
    private String token;
}
