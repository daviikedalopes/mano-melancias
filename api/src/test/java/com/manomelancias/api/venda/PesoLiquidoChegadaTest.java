package com.manomelancias.api.venda;

import com.manomelancias.api.cliente.Cliente;
import com.manomelancias.api.produtor.Produtor;
import com.manomelancias.api.venda.dto.VendaRequestDTO;
import com.manomelancias.api.venda.dto.VendaResponseDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O peso líquido na chegada é um campo livre: aparece na resposta como foi
 * informado, é opcional e não interfere nos cálculos da venda.
 */
class PesoLiquidoChegadaTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Venda venda(BigDecimal chegada) {
        return Venda.builder()
                .cliente(Cliente.builder().nome("Cliente").municipio("Curitiba").estado("PR").build())
                .produtor(Produtor.builder().nome("Produtor").cidade("Petrolina").build())
                .pesoLiquido(new BigDecimal("19480.00"))
                .pesoLiquidoChegada(chegada)
                .build();
    }

    @Test
    void resposta_devolveOPesoDeChegadaInformado() {
        VendaResponseDTO dto = VendaResponseDTO.from(venda(new BigDecimal("18900.50")));

        assertEquals(new BigDecimal("18900.50"), dto.getPesoLiquidoChegada());
        assertEquals(new BigDecimal("19480.00"), dto.getPesoLiquido()); // o de saída não muda
    }

    @Test
    void resposta_semPesoDeChegada_devolveNulo() {
        assertNull(VendaResponseDTO.from(venda(null)).getPesoLiquidoChegada());
    }

    @Test
    void pesoDeChegada_podeSerMaiorOuMenorQueODeSaida_semRegraDeNegocio() {
        // sem comparação com o peso de saída: qualquer valor não negativo é aceito
        for (String valor : new String[]{"0", "100", "19480", "25000.75"}) {
            VendaRequestDTO req = new VendaRequestDTO();
            req.setPesoLiquidoChegada(new BigDecimal(valor));
            assertTrue(validator.validateProperty(req, "pesoLiquidoChegada").isEmpty(), valor);
        }
    }

    @Test
    void pesoDeChegada_ausenteOuNegativo() {
        VendaRequestDTO req = new VendaRequestDTO();

        assertTrue(validator.validateProperty(req, "pesoLiquidoChegada").isEmpty()); // opcional

        req.setPesoLiquidoChegada(new BigDecimal("-1"));
        assertEquals(1, validator.validateProperty(req, "pesoLiquidoChegada").size());
    }

    @Test
    void pesoDeChegada_acimaDoLimiteDoBanco_eRecusadoEmVezDeEstourarNoInsert() {
        VendaRequestDTO req = new VendaRequestDTO();
        req.setPesoLiquidoChegada(new BigDecimal("123456789.00")); // NUMERIC(10,2) comporta 8 inteiros

        assertEquals(1, validator.validateProperty(req, "pesoLiquidoChegada").size());
    }
}
