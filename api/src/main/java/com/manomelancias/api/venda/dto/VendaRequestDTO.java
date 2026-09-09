package com.manomelancias.api.venda.dto;

import com.manomelancias.api.cliente.dto.ClienteRequestDTO;
import com.manomelancias.api.motorista.dto.MotoristaRequestDTO;
import com.manomelancias.api.produtor.dto.ProdutorRequestDTO;
import com.manomelancias.api.veiculo.dto.VeiculoRequestDTO;
import com.manomelancias.api.venda.StatusPagamento;
import com.manomelancias.api.venda.TipoFrete;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cada relação (cliente/produtor/motorista/veiculo) aceita OU um id já
 * cadastrado, OU os dados de um cadastro novo — permitindo criar "on the fly"
 * direto do formulário de venda, sem travar o fluxo de quem está lançando.
 */
@Getter
@Setter
public class VendaRequestDTO {

    @NotNull(message = "data da venda é obrigatória")
    private LocalDate dataVenda;

    private UUID clienteId;
    @Valid
    private ClienteRequestDTO clienteNovo;

    private UUID produtorId;
    @Valid
    private ProdutorRequestDTO produtorNovo;

    private UUID motoristaId;
    @Valid
    private MotoristaRequestDTO motoristaNovo;

    private UUID veiculoId;
    @Valid
    private VeiculoRequestDTO veiculoNovo;

    @NotNull(message = "peso bruto é obrigatório")
    @PositiveOrZero
    private BigDecimal pesoBruto;

    @NotNull(message = "desconto de tara é obrigatório")
    @PositiveOrZero
    private BigDecimal descTara;

    @PositiveOrZero
    private BigDecimal descPalha = BigDecimal.ZERO;

    @NotNull(message = "total de frutas é obrigatório")
    @Positive
    private Integer totalFrutas;

    @NotNull(message = "preço por kg é obrigatório")
    @Positive
    private BigDecimal precoKg;

    @NotNull(message = "tipo de frete é obrigatório")
    private TipoFrete tipoFrete;

    private BigDecimal precoFreteKg;

    private BigDecimal valorFrete;

    private LocalDate vencimento;

    private String nf;

    private StatusPagamento statusPagamento;

    private String observacoes;
}
