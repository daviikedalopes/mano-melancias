package com.manomelancias.api.veiculo;

import com.manomelancias.api.motorista.Motorista;
import com.manomelancias.api.motorista.MotoristaRepository;
import com.manomelancias.api.shared.exception.BusinessException;
import com.manomelancias.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final MotoristaRepository motoristaRepository;

    public Veiculo buscarPorPlaca(String placa) {
        return veiculoRepository.findByPlacaIgnoreCase(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado para a placa: " + placa));
    }

    @Transactional
    public Veiculo buscarOuCriar(String placa, String cidade, UUID motoristaId) {
        return veiculoRepository.findByPlacaIgnoreCase(placa)
                .orElseGet(() -> veiculoRepository.save(
                        Veiculo.builder()
                                .placa(placa.toUpperCase())
                                .cidade(cidade)
                                .motorista(resolverMotorista(motoristaId))
                                .build()));
    }

    public List<Veiculo> buscar(String placa, UUID motoristaId) {
        return veiculoRepository.buscar(placa, motoristaId);
    }

    public Veiculo buscarPorId(UUID id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado: " + id));
    }

    @Transactional
    public Veiculo criar(Veiculo veiculo, UUID motoristaId) {
        veiculo.setPlaca(veiculo.getPlaca().toUpperCase());
        veiculo.setMotorista(resolverMotorista(motoristaId));
        return veiculoRepository.save(veiculo);
    }

    @Transactional
    public Veiculo atualizar(UUID id, Veiculo dados, UUID motoristaId) {
        Veiculo existente = buscarPorId(id);
        existente.setPlaca(dados.getPlaca().toUpperCase());
        existente.setCidade(dados.getCidade());
        existente.setMotorista(resolverMotorista(motoristaId));
        return veiculoRepository.save(existente);
    }

    @Transactional
    public void excluir(UUID id) {
        if (!veiculoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Veículo não encontrado: " + id);
        }
        veiculoRepository.deleteById(id);
    }

    private Motorista resolverMotorista(UUID motoristaId) {
        if (motoristaId == null) {
            return null;
        }
        return motoristaRepository.findById(motoristaId)
                .orElseThrow(() -> new BusinessException("Motorista não encontrado: " + motoristaId, HttpStatus.BAD_REQUEST));
    }
}
