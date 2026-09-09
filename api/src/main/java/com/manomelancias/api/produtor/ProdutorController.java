package com.manomelancias.api.produtor;

import com.manomelancias.api.produtor.dto.ProdutorRequestDTO;
import com.manomelancias.api.produtor.dto.ProdutorResponseDTO;
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
@RequestMapping("/produtores")
@RequiredArgsConstructor
public class ProdutorController {

    private final ProdutorService produtorService;

    @GetMapping
    public List<ProdutorResponseDTO> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cidade) {
        return produtorService.buscar(nome, cidade).stream()
                .map(ProdutorResponseDTO::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ProdutorResponseDTO buscarPorId(@PathVariable UUID id) {
        return ProdutorResponseDTO.from(produtorService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutorResponseDTO criar(@Valid @RequestBody ProdutorRequestDTO dto) {
        return ProdutorResponseDTO.from(produtorService.criar(dto.toEntity()));
    }

    @PutMapping("/{id}")
    public ProdutorResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody ProdutorRequestDTO dto) {
        return ProdutorResponseDTO.from(produtorService.atualizar(id, dto.toEntity()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable UUID id) {
        produtorService.inativar(id);
    }
}
