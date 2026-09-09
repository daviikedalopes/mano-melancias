package com.manomelancias.api.venda;

import com.manomelancias.api.cliente.Cliente;
import com.manomelancias.api.motorista.Motorista;
import com.manomelancias.api.produtor.Produtor;
import com.manomelancias.api.usuario.Usuario;
import com.manomelancias.api.veiculo.Veiculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "venda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @Column(name = "data_venda", nullable = false)
    private LocalDate dataVenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produtor_id", nullable = false)
    private Produtor produtor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id", nullable = false)
    private Motorista motorista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @Column(name = "peso_bruto", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoBruto;

    @Column(name = "desc_tara", nullable = false, precision = 10, scale = 2)
    private BigDecimal descTara;

    @Column(name = "desc_palha", nullable = false, precision = 10, scale = 2)
    private BigDecimal descPalha;

    @Column(name = "peso_liquido", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoLiquido;

    @Column(name = "total_frutas", nullable = false)
    private Integer totalFrutas;

    @Column(name = "media_peso", nullable = false, precision = 10, scale = 2)
    private BigDecimal mediaPeso;

    @Column(name = "preco_kg", nullable = false, precision = 10, scale = 4)
    private BigDecimal precoKg;

    @Column(name = "valor_mercadoria", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorMercadoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_frete", nullable = false)
    private TipoFrete tipoFrete;

    @Column(name = "preco_frete_kg", precision = 10, scale = 4)
    private BigDecimal precoFreteKg;

    @Column(name = "valor_frete", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorFrete;

    @Column(name = "restante_pagar", nullable = false, precision = 12, scale = 2)
    private BigDecimal restantePagar;

    private LocalDate vencimento;

    private String nf;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento", nullable = false)
    @Builder.Default
    private StatusPagamento statusPagamento = StatusPagamento.PENDENTE;

    @Column(columnDefinition = "text")
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Usuario createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
