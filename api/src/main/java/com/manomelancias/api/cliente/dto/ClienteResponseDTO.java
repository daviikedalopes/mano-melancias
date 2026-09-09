package com.manomelancias.api.cliente.dto;

import com.manomelancias.api.cliente.Cliente;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ClienteResponseDTO {

    private UUID id;
    private String nome;
    private String municipio;
    private String estado;
    private String telefone;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ClienteResponseDTO from(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getMunicipio(),
                cliente.getEstado(),
                cliente.getTelefone(),
                cliente.getAtivo(),
                cliente.getCreatedAt(),
                cliente.getUpdatedAt());
    }
}
