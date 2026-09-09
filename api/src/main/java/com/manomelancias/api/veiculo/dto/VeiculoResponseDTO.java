package com.manomelancias.api.veiculo.dto;

import com.manomelancias.api.veiculo.Veiculo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class VeiculoResponseDTO {

    private UUID id;
    private String placa;
    private String cidade;
    private UUID motoristaId;
    private String motoristaNome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VeiculoResponseDTO from(Veiculo veiculo) {
        return new VeiculoResponseDTO(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getCidade(),
                veiculo.getMotorista() != null ? veiculo.getMotorista().getId() : null,
                veiculo.getMotorista() != null ? veiculo.getMotorista().getNome() : null,
                veiculo.getCreatedAt(),
                veiculo.getUpdatedAt());
    }
}
