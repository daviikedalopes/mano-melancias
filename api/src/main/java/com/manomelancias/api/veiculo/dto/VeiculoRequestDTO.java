package com.manomelancias.api.veiculo.dto;

import com.manomelancias.api.veiculo.Veiculo;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class VeiculoRequestDTO {

    @NotBlank(message = "placa é obrigatória")
    private String placa;

    @NotBlank(message = "cidade é obrigatória")
    private String cidade;

    private UUID motoristaId;

    public Veiculo toEntity() {
        return Veiculo.builder()
                .placa(placa)
                .cidade(cidade)
                .build();
    }
}
