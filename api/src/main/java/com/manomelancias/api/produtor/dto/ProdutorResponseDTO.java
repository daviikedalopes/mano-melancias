package com.manomelancias.api.produtor.dto;

import com.manomelancias.api.produtor.Produtor;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ProdutorResponseDTO {

    private UUID id;
    private String nome;
    private String cidade;
    private String telefone;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProdutorResponseDTO from(Produtor produtor) {
        return new ProdutorResponseDTO(
                produtor.getId(),
                produtor.getNome(),
                produtor.getCidade(),
                produtor.getTelefone(),
                produtor.getAtivo(),
                produtor.getCreatedAt(),
                produtor.getUpdatedAt());
    }
}
