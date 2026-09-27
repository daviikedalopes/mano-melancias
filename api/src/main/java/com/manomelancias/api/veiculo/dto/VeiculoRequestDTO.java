package com.manomelancias.api.veiculo.dto;

import com.manomelancias.api.veiculo.Veiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class VeiculoRequestDTO {

    @NotBlank(message = "placa é obrigatória")
    @Size(max = 8, message = "placa deve ter no máximo 8 caracteres")
    private String placa;

    @NotBlank(message = "cidade é obrigatória")
    @Size(max = 255, message = "cidade deve ter no máximo 255 caracteres")
    private String cidade;

    private UUID motoristaId;

    public Veiculo toEntity() {
        return Veiculo.builder()
                .placa(placa)
                .cidade(cidade)
                .build();
    }
}
