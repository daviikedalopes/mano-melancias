package com.manomelancias.api.cliente;

import com.manomelancias.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    /**
     * Usado pelo cadastro de Venda para permitir criar o cliente "on the fly"
     * quando o operador digita um nome ainda não cadastrado.
     */
    @Transactional
    public Cliente buscarOuCriar(String nome, String municipio, String estado) {
        return clienteRepository
                .findByNomeIgnoreCaseAndMunicipioIgnoreCaseAndEstadoIgnoreCase(nome, municipio, estado)
                .orElseGet(() -> clienteRepository.save(
                        Cliente.builder()
                                .nome(nome)
                                .municipio(municipio)
                                .estado(estado.toUpperCase())
                                .ativo(true)
                                .build()));
    }

    public List<Cliente> listarAtivos() {
        return clienteRepository.findByAtivoTrue();
    }

    public List<Cliente> buscar(String nome, String municipio, String estado) {
        return clienteRepository.buscar(nome, municipio, estado);
    }

    public Cliente buscarPorId(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + id));
    }

    @Transactional
    public Cliente criar(Cliente cliente) {
        cliente.setAtivo(true);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizar(UUID id, Cliente dados) {
        Cliente existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setMunicipio(dados.getMunicipio());
        existente.setEstado(dados.getEstado());
        existente.setTelefone(dados.getTelefone());
        return clienteRepository.save(existente);
    }

    @Transactional
    public void inativar(UUID id) {
        Cliente cliente = buscarPorId(id);
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }
}
