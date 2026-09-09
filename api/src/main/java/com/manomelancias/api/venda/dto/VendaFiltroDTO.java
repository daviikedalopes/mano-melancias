package com.manomelancias.api.venda.dto;

import com.manomelancias.api.venda.StatusPagamento;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class VendaFiltroDTO {

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private UUID clienteId;
    private UUID produtorId;
    private UUID motoristaId;
    private StatusPagamento statusPagamento;
}
