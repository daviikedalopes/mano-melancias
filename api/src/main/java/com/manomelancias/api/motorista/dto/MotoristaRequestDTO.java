package com.manomelancias.api.motorista.dto;

import com.manomelancias.api.motorista.Motorista;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MotoristaRequestDTO {

    @NotBlank(message = "nome é obrigatório")
    @Size(max = 255, message = "nome deve ter no máximo 255 caracteres")
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Size(max = 20, message = "CPF inválido")
    private String cpf;

    @Size(max = 20, message = "telefone deve ter no máximo 20 caracteres")
    private String telefone;

    public Motorista toEntity() {
        return Motorista.builder()
                .nome(nome)
                .cpf(cpf)
                .telefone(telefone)
                .build();
    }
}
