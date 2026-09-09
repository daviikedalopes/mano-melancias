package com.manomelancias.api.motorista.dto;

import com.manomelancias.api.motorista.Motorista;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MotoristaRequestDTO {

    @NotBlank(message = "nome é obrigatório")
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    private String telefone;

    public Motorista toEntity() {
        return Motorista.builder()
                .nome(nome)
                .cpf(cpf)
                .telefone(telefone)
                .build();
    }
}
