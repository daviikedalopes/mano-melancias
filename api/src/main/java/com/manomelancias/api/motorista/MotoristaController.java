package com.manomelancias.api.motorista;

import com.manomelancias.api.motorista.dto.MotoristaRequestDTO;
import com.manomelancias.api.motorista.dto.MotoristaResponseDTO;
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
@RequestMapping("/motoristas")
@RequiredArgsConstructor
public class MotoristaController {

    private final MotoristaService motoristaService;

    @GetMapping
    public List<MotoristaResponseDTO> listar(@RequestParam(required = false) String nome) {
        return motoristaService.buscar(nome).stream()
                .map(MotoristaResponseDTO::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MotoristaResponseDTO buscarPorId(@PathVariable UUID id) {
        return MotoristaResponseDTO.from(motoristaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MotoristaResponseDTO criar(@Valid @RequestBody MotoristaRequestDTO dto) {
        return MotoristaResponseDTO.from(motoristaService.criar(dto.toEntity()));
    }

    @PutMapping("/{id}")
    public MotoristaResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody MotoristaRequestDTO dto) {
        return MotoristaResponseDTO.from(motoristaService.atualizar(id, dto.toEntity()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable UUID id) {
        motoristaService.inativar(id);
    }
}
