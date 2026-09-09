package com.manomelancias.api.motorista;

import com.manomelancias.api.shared.exception.BusinessException;
import com.manomelancias.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MotoristaService {

    private final MotoristaRepository motoristaRepository;

    public void validarCpf(String cpf) {
        if (!CpfValidator.isValid(cpf)) {
            throw new BusinessException("CPF inválido: " + cpf);
        }
    }

    @Transactional
    public Motorista buscarOuCriar(String nome, String cpf, String telefone) {
        String cpfNormalizado = CpfValidator.normalizar(cpf);
        return motoristaRepository.findByCpf(cpfNormalizado)
                .orElseGet(() -> {
                    validarCpf(cpfNormalizado);
                    return motoristaRepository.save(
                            Motorista.builder()
                                    .nome(nome)
                                    .cpf(cpfNormalizado)
                                    .telefone(telefone)
                                    .ativo(true)
                                    .build());
                });
    }

    public List<Motorista> listarAtivos() {
        return motoristaRepository.findByAtivoTrue();
    }

    public List<Motorista> buscar(String nome) {
        return motoristaRepository.buscar(nome);
    }

    public Motorista buscarPorId(UUID id) {
        return motoristaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado: " + id));
    }

    @Transactional
    public Motorista criar(Motorista motorista) {
        String cpfNormalizado = CpfValidator.normalizar(motorista.getCpf());
        validarCpf(cpfNormalizado);
        motorista.setCpf(cpfNormalizado);
        motorista.setAtivo(true);
        return motoristaRepository.save(motorista);
    }

    @Transactional
    public Motorista atualizar(UUID id, Motorista dados) {
        Motorista existente = buscarPorId(id);
        String cpfNormalizado = CpfValidator.normalizar(dados.getCpf());
        validarCpf(cpfNormalizado);

        existente.setNome(dados.getNome());
        existente.setCpf(cpfNormalizado);
        existente.setTelefone(dados.getTelefone());
        return motoristaRepository.save(existente);
    }

    @Transactional
    public void inativar(UUID id) {
        Motorista motorista = buscarPorId(id);
        motorista.setAtivo(false);
        motoristaRepository.save(motorista);
    }
}
