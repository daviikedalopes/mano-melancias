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

    /**
     * incluirInativos = false (padrão): só clientes ativos.
     * incluirInativos = true: ativos e inativos juntos (ordenados com ativos primeiro).
     */
    @Query("""
        SELECT c FROM Cliente c
        WHERE (:incluirInativos = true OR c.ativo = true)
          AND (:nome IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS string), '%')))
          AND (:municipio IS NULL OR LOWER(c.municipio) LIKE LOWER(CONCAT('%', CAST(:municipio AS string), '%')))
          AND (:estado IS NULL OR LOWER(c.estado) = LOWER(CAST(:estado AS string)))
        ORDER BY c.ativo DESC, c.nome ASC
        """)
    List<Cliente> buscar(
            @Param("nome") String nome,
            @Param("municipio") String municipio,
            @Param("estado") String estado,
            @Param("incluirInativos") boolean incluirInativos);
}