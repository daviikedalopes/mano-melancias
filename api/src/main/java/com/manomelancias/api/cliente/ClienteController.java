package com.manomelancias.api.cliente;

import com.manomelancias.api.cliente.dto.ClienteRequestDTO;
import com.manomelancias.api.cliente.dto.ClienteResponseDTO;
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
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public List<ClienteResponseDTO> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false) String estado) {
        return clienteService.buscar(nome, municipio, estado).stream()
                .map(ClienteResponseDTO::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ClienteResponseDTO buscarPorId(@PathVariable UUID id) {
        return ClienteResponseDTO.from(clienteService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponseDTO criar(@Valid @RequestBody ClienteRequestDTO dto) {
        return ClienteResponseDTO.from(clienteService.criar(dto.toEntity()));
    }

    @PutMapping("/{id}")
    public ClienteResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody ClienteRequestDTO dto) {
        return ClienteResponseDTO.from(clienteService.atualizar(id, dto.toEntity()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable UUID id) {
        clienteService.inativar(id);
    }
}
