package com.manomelancias.api.relatorio.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class VendasPeriodoResponseDTO {

    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private Long quantidadeVendas;
    private BigDecimal totalValorMercadoria;
    private BigDecimal totalValorFrete;
    private BigDecimal totalRestantePagar;

    public VendasPeriodoResponseDTO() {
    }

    public VendasPeriodoResponseDTO(
            Long quantidadeVendas,
            BigDecimal totalValorMercadoria,
            BigDecimal totalValorFrete,
            BigDecimal totalRestantePagar) {
        this.quantidadeVendas = quantidadeVendas;
        this.totalValorMercadoria = totalValorMercadoria;
        this.totalValorFrete = totalValorFrete;
        this.totalRestantePagar = totalRestantePagar;
    }
}
