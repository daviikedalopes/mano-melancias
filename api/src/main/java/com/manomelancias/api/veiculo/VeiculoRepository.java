package com.manomelancias.api.veiculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {

    Optional<Veiculo> findByPlacaIgnoreCase(String placa);

    List<Veiculo> findByAtivoTrue();

    /**
     * incluirInativos = false (padrão): só veículos ativos.
     * incluirInativos = true: ativos e inativos juntos (ordenados com ativos primeiro).
     */
    @Query("""
        SELECT v FROM Veiculo v
        LEFT JOIN FETCH v.motorista
        WHERE (:incluirInativos = true OR v.ativo = true)
          AND (:placa IS NULL OR LOWER(v.placa) LIKE LOWER(CONCAT('%', CAST(:placa AS string), '%')))
          AND (:motoristaId IS NULL OR v.motorista.id = :motoristaId)
        ORDER BY v.ativo DESC, v.placa ASC
        """)
    List<Veiculo> buscar(
            @Param("placa") String placa,
            @Param("motoristaId") UUID motoristaId,
            @Param("incluirInativos") boolean incluirInativos);

    @Query("""
        SELECT v FROM Veiculo v
        LEFT JOIN FETCH v.motorista
        WHERE v.id = :id
        """)
    Optional<Veiculo> buscarPorIdComMotorista(@Param("id") UUID id);
}