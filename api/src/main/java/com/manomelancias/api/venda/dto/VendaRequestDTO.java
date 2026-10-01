package com.manomelancias.api.venda.dto;

import com.manomelancias.api.cliente.dto.ClienteRequestDTO;
import com.manomelancias.api.produtor.dto.ProdutorRequestDTO;
import com.manomelancias.api.venda.StatusPagamento;
import com.manomelancias.api.venda.TipoFrete;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cliente e produtor aceitam OU um id já cadastrado, OU os dados de um
 * cadastro novo — permitindo criar "on the fly" direto do formulário de
 * venda, sem travar o fluxo de quem está lançando. Motorista e veículo não
 * são cadastros: são só campos da própria venda.
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

    @NotBlank(message = "nome do motorista é obrigatório")
    @Size(max = 255, message = "nome do motorista deve ter no máximo 255 caracteres")
    private String motoristaNome;

    @Size(max = 20, message = "CPF inválido")
    private String motoristaCpf;

    @NotBlank(message = "placa do veículo é obrigatória")
    @Size(max = 8, message = "placa deve ter no máximo 8 caracteres")
    private String veiculoPlaca;

    @NotBlank(message = "cidade do veículo é obrigatória")
    @Size(max = 255, message = "cidade do veículo deve ter no máximo 255 caracteres")
    private String veiculoCidade;

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

    @Size(max = 50, message = "NF deve ter no máximo 50 caracteres")
    private String nf;

    private StatusPagamento statusPagamento;

    @Size(max = 2000, message = "observações deve ter no máximo 2000 caracteres")
    private String observacoes;
}
