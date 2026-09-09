package com.manomelancias.api.venda;

import com.manomelancias.api.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Cobre os cálculos do VendaService com os números conferidos na ficha de
 * venda original em papel (ver seção 3 do documento de arquitetura):
 * peso líquido 19480, valor mercadoria 28.246,00, restante 19.896,00.
 */
class VendaServiceTest {

    private final VendaService vendaService = new VendaService(null, null, null, null, null, null);

    @Test
    void calcularPesoLiquido_deveBaterComExemploDaFicha() {
        BigDecimal resultado = vendaService.calcularPesoLiquido(
                new BigDecimal("31180"), new BigDecimal("11300"), new BigDecimal("400"));

        assertEquals(new BigDecimal("19480.00"), resultado);
    }

    @Test
    void calcularValorMercadoria_deveBaterComExemploDaFicha() {
        BigDecimal resultado = vendaService.calcularValorMercadoria(new BigDecimal("19480"), new BigDecimal("1.45"));

        assertEquals(new BigDecimal("28246.00"), resultado);
    }

    @Test
    void calcularRestante_deveBaterComExemploDaFicha() {
        BigDecimal resultado = vendaService.calcularRestante(new BigDecimal("28246.00"), new BigDecimal("8350.00"));

        assertEquals(new BigDecimal("19896.00"), resultado);
    }

    @Test
    void calcularMediaPeso_deveBaterComExemploDaFicha() {
        BigDecimal resultado = vendaService.calcularMediaPeso(new BigDecimal("19480"), 1280);

        assertEquals(new BigDecimal("15.22"), resultado);
    }

    @Test
    void calcularValorFrete_negociado_usaValorInformado() {
        BigDecimal resultado = vendaService.calcularValorFrete(
                TipoFrete.NEGOCIADO, new BigDecimal("19480"), null, new BigDecimal("8350.00"));

        assertEquals(new BigDecimal("8350.00"), resultado);
    }

    @Test
    void calcularValorFrete_porKg_calculaAPartirDoPesoLiquido() {
        BigDecimal resultado = vendaService.calcularValorFrete(
                TipoFrete.POR_KG, new BigDecimal("1000"), new BigDecimal("2.50"), null);

        assertEquals(new BigDecimal("2500.00"), resultado);
    }

    @Test
    void calcularValorFrete_negociado_semValor_lancaExcecao() {
        assertThrows(BusinessException.class, () ->
                vendaService.calcularValorFrete(TipoFrete.NEGOCIADO, new BigDecimal("1000"), null, null));
    }

    @Test
    void calcularValorFrete_porKg_semPreco_lancaExcecao() {
        assertThrows(BusinessException.class, () ->
                vendaService.calcularValorFrete(TipoFrete.POR_KG, new BigDecimal("1000"), null, null));
    }
}
