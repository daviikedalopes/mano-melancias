package com.manomelancias.api.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    Optional<Cliente> findByNomeIgnoreCaseAndMunicipioIgnoreCaseAndEstadoIgnoreCase(
            String nome, String municipio, String estado);

    List<Cliente> findByAtivoTrue();

    @Query("""
        SELECT c FROM Cliente c
        WHERE c.ativo = true
          AND (:nome IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
          AND (:municipio IS NULL OR LOWER(c.municipio) LIKE LOWER(CONCAT('%', :municipio, '%')))
          AND (:estado IS NULL OR LOWER(c.estado) = LOWER(:estado))
        """)
    List<Cliente> buscar(@Param("nome") String nome, @Param("municipio") String municipio, @Param("estado") String estado);
}
