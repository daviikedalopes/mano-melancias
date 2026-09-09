package com.manomelancias.api.relatorio.dto;

import com.manomelancias.api.venda.StatusPagamento;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ContaReceberDTO {

    private UUID vendaId;
    private Integer numero;
    private String clienteNome;
    private BigDecimal valorRestante;
    private LocalDate vencimento;
    private StatusPagamento statusPagamento;
}
