package com.manomelancias.api.motorista;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MotoristaRepository extends JpaRepository<Motorista, UUID> {

    Optional<Motorista> findByCpf(String cpf);

    List<Motorista> findByAtivoTrue();

    @Query("""
        SELECT m FROM Motorista m
        WHERE m.ativo = true
          AND (:nome IS NULL OR LOWER(m.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
        """)
    List<Motorista> buscar(@Param("nome") String nome);
}
