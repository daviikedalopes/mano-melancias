package com.manomelancias.api.produtor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutorRepository extends JpaRepository<Produtor, UUID> {

    Optional<Produtor> findByNomeIgnoreCaseAndCidadeIgnoreCase(String nome, String cidade);

    List<Produtor> findByAtivoTrue();

    /**
     * incluirInativos = false (padrão): só produtores ativos.
     * incluirInativos = true: ativos e inativos juntos (ordenados com ativos primeiro).
     */
    @Query("""
        SELECT p FROM Produtor p
        WHERE (:incluirInativos = true OR p.ativo = true)
          AND (:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS string), '%')))
          AND (:cidade IS NULL OR LOWER(p.cidade) LIKE LOWER(CONCAT('%', CAST(:cidade AS string), '%')))
        ORDER BY p.ativo DESC, p.nome ASC
        """)
    List<Produtor> buscar(
            @Param("nome") String nome,
            @Param("cidade") String cidade,
            @Param("incluirInativos") boolean incluirInativos);
}