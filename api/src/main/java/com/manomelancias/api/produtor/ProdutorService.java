package com.manomelancias.api.produtor;

import com.manomelancias.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutorService {

    private final ProdutorRepository produtorRepository;

    @Transactional
    public Produtor buscarOuCriar(String nome, String cidade) {
        return produtorRepository.findByNomeIgnoreCaseAndCidadeIgnoreCase(nome, cidade)
                .orElseGet(() -> produtorRepository.save(
                        Produtor.builder().nome(nome).cidade(cidade).ativo(true).build()));
    }

    public List<Produtor> listarAtivos() {
        return produtorRepository.findByAtivoTrue();
    }

    public List<Produtor> buscar(String nome, String cidade) {
        return produtorRepository.buscar(nome, cidade);
    }

    public Produtor buscarPorId(UUID id) {
        return produtorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produtor não encontrado: " + id));
    }

    @Transactional
    public Produtor criar(Produtor produtor) {
        produtor.setAtivo(true);
        return produtorRepository.save(produtor);
    }

    @Transactional
    public Produtor atualizar(UUID id, Produtor dados) {
        Produtor existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setCidade(dados.getCidade());
        existente.setTelefone(dados.getTelefone());
        return produtorRepository.save(existente);
    }

    @Transactional
    public void inativar(UUID id) {
        Produtor produtor = buscarPorId(id);
        produtor.setAtivo(false);
        produtorRepository.save(produtor);
    }
}
