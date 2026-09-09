package com.manomelancias.api.veiculo;

import com.manomelancias.api.veiculo.dto.VeiculoRequestDTO;
import com.manomelancias.api.veiculo.dto.VeiculoResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService veiculoService;

    @GetMapping
    public List<VeiculoResponseDTO> listar(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) UUID motoristaId) {
        return veiculoService.buscar(placa, motoristaId).stream()
                .map(VeiculoResponseDTO::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public VeiculoResponseDTO buscarPorId(@PathVariable UUID id) {
        return VeiculoResponseDTO.from(veiculoService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeiculoResponseDTO criar(@Valid @RequestBody VeiculoRequestDTO dto) {
        return VeiculoResponseDTO.from(veiculoService.criar(dto.toEntity(), dto.getMotoristaId()));
    }

    @PutMapping("/{id}")
    public VeiculoResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody VeiculoRequestDTO dto) {
        return VeiculoResponseDTO.from(veiculoService.atualizar(id, dto.toEntity(), dto.getMotoristaId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        veiculoService.excluir(id);
    }
}
