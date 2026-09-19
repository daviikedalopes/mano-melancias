package com.manomelancias.api.veiculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {

    Optional<Veiculo> findByPlacaIgnoreCase(String placa);

    @Query("""
        SELECT v FROM Veiculo v
        LEFT JOIN FETCH v.motorista
        WHERE (:placa IS NULL OR LOWER(v.placa) LIKE LOWER(CONCAT('%', CAST(:placa AS string), '%')))
          AND (:motoristaId IS NULL OR v.motorista.id = :motoristaId)
        """)
    List<Veiculo> buscar(@Param("placa") String placa, @Param("motoristaId") UUID motoristaId);

    @Query("""
        SELECT v FROM Veiculo v
        LEFT JOIN FETCH v.motorista
        WHERE v.id = :id
        """)
    Optional<Veiculo> buscarPorIdComMotorista(@Param("id") UUID id);
}