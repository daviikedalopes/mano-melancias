package com.manomelancias.api.venda.dto;

import com.manomelancias.api.venda.StatusPagamento;
import com.manomelancias.api.venda.TipoFrete;
import com.manomelancias.api.venda.Venda;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class VendaResponseDTO {

    private UUID id;
    private Integer numero;
    private LocalDate dataVenda;

    private UUID clienteId;
    private String clienteNome;
    private String clienteMunicipio;
    private String clienteEstado;

    private UUID produtorId;
    private String produtorNome;
    private String produtorCidade;

    private UUID motoristaId;
    private String motoristaNome;
    private String motoristaCpf;
    private String motoristaTelefone;

    private UUID veiculoId;
    private String veiculoPlaca;
    private String veiculoCidade;

    private BigDecimal pesoBruto;
    private BigDecimal descTara;
    private BigDecimal descPalha;
    private BigDecimal pesoLiquido;
    private Integer totalFrutas;
    private BigDecimal mediaPeso;
    private BigDecimal precoKg;
    private BigDecimal valorMercadoria;

    private TipoFrete tipoFrete;
    private BigDecimal precoFreteKg;
    private BigDecimal valorFrete;
    private BigDecimal restantePagar;

    private LocalDate vencimento;
    private String nf;
    private StatusPagamento statusPagamento;
    private String observacoes;

    private String criadoPor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VendaResponseDTO from(Venda v) {
        return new VendaResponseDTO(
                v.getId(),
                v.getNumero(),
                v.getDataVenda(),
                v.getCliente().getId(),
                v.getCliente().getNome(),
                v.getCliente().getMunicipio(),
                v.getCliente().getEstado(),
                v.getProdutor().getId(),
                v.getProdutor().getNome(),
                v.getProdutor().getCidade(),
                v.getMotorista().getId(),
                v.getMotorista().getNome(),
                v.getMotorista().getCpf(),
                v.getMotorista().getTelefone(),
                v.getVeiculo().getId(),
                v.getVeiculo().getPlaca(),
                v.getVeiculo().getCidade(),
                v.getPesoBruto(),
                v.getDescTara(),
                v.getDescPalha(),
                v.getPesoLiquido(),
                v.getTotalFrutas(),
                v.getMediaPeso(),
                v.getPrecoKg(),
                v.getValorMercadoria(),
                v.getTipoFrete(),
                v.getPrecoFreteKg(),
                v.getValorFrete(),
                v.getRestantePagar(),
                v.getVencimento(),
                v.getNf(),
                v.getStatusPagamento(),
                v.getObservacoes(),
                v.getCreatedBy() != null ? v.getCreatedBy().getNome() : null,
                v.getCreatedAt(),
                v.getUpdatedAt());
    }
}
