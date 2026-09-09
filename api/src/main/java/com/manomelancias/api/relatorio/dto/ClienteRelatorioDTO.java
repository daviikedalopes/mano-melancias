package com.manomelancias.api.relatorio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ClienteRelatorioDTO {

    private UUID clienteId;
    private String clienteNome;
    private Long quantidadeVendas;
    private BigDecimal totalComprado;
}
