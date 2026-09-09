package com.manomelancias.api.relatorio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ProdutorRelatorioDTO {

    private UUID produtorId;
    private String produtorNome;
    private Long quantidadeCargas;
    private BigDecimal totalPesoLiquido;
    private BigDecimal totalComprado;
}
