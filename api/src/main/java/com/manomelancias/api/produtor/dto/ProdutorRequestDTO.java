package com.manomelancias.api.produtor.dto;

import com.manomelancias.api.produtor.Produtor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProdutorRequestDTO {

    @NotBlank(message = "nome é obrigatório")
    @Size(max = 255, message = "nome deve ter no máximo 255 caracteres")
    private String nome;

    @NotBlank(message = "cidade é obrigatória")
    @Size(max = 255, message = "cidade deve ter no máximo 255 caracteres")
    private String cidade;

    @Size(max = 20, message = "telefone deve ter no máximo 20 caracteres")
    private String telefone;

    public Produtor toEntity() {
        return Produtor.builder()
                .nome(nome)
                .cidade(cidade)
                .telefone(telefone)
                .build();
    }
}
