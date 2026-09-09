package com.manomelancias.api.motorista.dto;

import com.manomelancias.api.motorista.Motorista;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MotoristaResponseDTO {

    private UUID id;
    private String nome;
    private String cpf;
    private String telefone;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MotoristaResponseDTO from(Motorista motorista) {
        return new MotoristaResponseDTO(
                motorista.getId(),
                motorista.getNome(),
                motorista.getCpf(),
                motorista.getTelefone(),
                motorista.getAtivo(),
                motorista.getCreatedAt(),
                motorista.getUpdatedAt());
    }
}
