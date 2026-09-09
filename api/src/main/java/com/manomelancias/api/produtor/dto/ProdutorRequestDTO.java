package com.manomelancias.api.produtor.dto;

import com.manomelancias.api.produtor.Produtor;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProdutorRequestDTO {

    @NotBlank(message = "nome é obrigatório")
    private String nome;

    @NotBlank(message = "cidade é obrigatória")
    private String cidade;

    private String telefone;

    public Produtor toEntity() {
        return Produtor.builder()
                .nome(nome)
                .cidade(cidade)
                .telefone(telefone)
                .build();
    }
}
