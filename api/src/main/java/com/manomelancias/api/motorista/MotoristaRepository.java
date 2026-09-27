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

    /**
     * incluirInativos = false (padrão): só motoristas ativos.
     * incluirInativos = true: ativos e inativos juntos (ordenados com ativos primeiro).
     */
    @Query("""
        SELECT m FROM Motorista m
        WHERE (:incluirInativos = true OR m.ativo = true)
          AND (:nome IS NULL OR LOWER(m.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS string), '%')))
        ORDER BY m.ativo DESC, m.nome ASC
        """)
    List<Motorista> buscar(@Param("nome") String nome, @Param("incluirInativos") boolean incluirInativos);
}