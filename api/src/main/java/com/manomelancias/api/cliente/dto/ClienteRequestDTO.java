package com.manomelancias.api.cliente.dto;

import com.manomelancias.api.cliente.Cliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    private String nome;

    @NotBlank(message = "Município é obrigatório")
    @Size(max = 255, message = "Município deve ter no máximo 255 caracteres")
    private String municipio;

    @NotBlank(message = "Estado é obrigatório")
    @Size(min = 2, max = 2, message = "Estado deve ter 2 letras (UF)")
    private String estado;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    public Cliente toEntity() {
        return Cliente.builder()
                .nome(nome)
                .municipio(municipio)
                .estado(estado.toUpperCase())
                .telefone(telefone)
                .build();
    }
}
