package com.manomelancias.api.venda;

import com.manomelancias.api.relatorio.dto.ClienteRelatorioDTO;
import com.manomelancias.api.relatorio.dto.ContaReceberDTO;
import com.manomelancias.api.relatorio.dto.ProdutorRelatorioDTO;
import com.manomelancias.api.relatorio.dto.VendasPeriodoResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VendaRepository extends JpaRepository<Venda, UUID> {

    @Query(value = "SELECT nextval('venda_numero_seq')", nativeQuery = true)
    Long proximoNumero();

    @Query("""
        SELECT v FROM Venda v
        WHERE (:dataInicio IS NULL OR v.dataVenda >= :dataInicio)
          AND (:dataFim IS NULL OR v.dataVenda <= :dataFim)
          AND (:clienteId IS NULL OR v.cliente.id = :clienteId)
          AND (:produtorId IS NULL OR v.produtor.id = :produtorId)
          AND (:motoristaId IS NULL OR v.motorista.id = :motoristaId)
          AND (:statusPagamento IS NULL OR v.statusPagamento = :statusPagamento)
        ORDER BY v.dataVenda DESC, v.numero DESC
        """)
    List<Venda> buscar(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("clienteId") UUID clienteId,
            @Param("produtorId") UUID produtorId,
            @Param("motoristaId") UUID motoristaId,
            @Param("statusPagamento") StatusPagamento statusPagamento);

    // --- Relatórios (usados por RelatorioService, que não tem repository próprio) ---

    @Query("""
        SELECT new com.manomelancias.api.relatorio.dto.VendasPeriodoResponseDTO(
            COUNT(v), COALESCE(SUM(v.valorMercadoria), 0), COALESCE(SUM(v.valorFrete), 0), COALESCE(SUM(v.restantePagar), 0))
        FROM Venda v
        WHERE v.dataVenda BETWEEN :inicio AND :fim
        """)
    VendasPeriodoResponseDTO totalVendidoPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
        SELECT new com.manomelancias.api.relatorio.dto.ProdutorRelatorioDTO(
            p.id, p.nome, COUNT(v), COALESCE(SUM(v.pesoLiquido), 0), COALESCE(SUM(v.valorMercadoria), 0))
        FROM Venda v JOIN v.produtor p
        WHERE (:produtorId IS NULL OR p.id = :produtorId)
        GROUP BY p.id, p.nome
        ORDER BY SUM(v.valorMercadoria) DESC
        """)
    List<ProdutorRelatorioDTO> totalPorProdutor(@Param("produtorId") UUID produtorId);

    @Query("""
        SELECT new com.manomelancias.api.relatorio.dto.ClienteRelatorioDTO(
            c.id, c.nome, COUNT(v), COALESCE(SUM(v.valorMercadoria), 0))
        FROM Venda v JOIN v.cliente c
        WHERE (:clienteId IS NULL OR c.id = :clienteId)
        GROUP BY c.id, c.nome
        ORDER BY SUM(v.valorMercadoria) DESC
        """)
    List<ClienteRelatorioDTO> totalPorCliente(@Param("clienteId") UUID clienteId);

    @Query("""
        SELECT new com.manomelancias.api.relatorio.dto.ContaReceberDTO(
            v.id, v.numero, c.nome, v.restantePagar, v.vencimento, v.statusPagamento)
        FROM Venda v JOIN v.cliente c
        WHERE v.restantePagar > 0 AND v.statusPagamento <> com.manomelancias.api.venda.StatusPagamento.PAGO
        ORDER BY CASE WHEN v.vencimento IS NULL THEN 1 ELSE 0 END, v.vencimento ASC
        """)
    List<ContaReceberDTO> contasAReceber();
}
