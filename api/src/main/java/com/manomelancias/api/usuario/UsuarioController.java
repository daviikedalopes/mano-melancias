package com.manomelancias.api.usuario;

import com.manomelancias.api.usuario.dto.LoginRequest;
import com.manomelancias.api.usuario.dto.LoginResponse;
import com.manomelancias.api.usuario.dto.UsuarioRequest;
import com.manomelancias.api.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String token = usuarioService.autenticar(request.getEmail(), request.getSenha());
        return new LoginResponse(token);
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.criarUsuario(
                request.getNome(), request.getEmail(), request.getSenha(), request.getPapel());
        return UsuarioResponse.from(usuario);
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar().stream()
                .map(UsuarioResponse::from)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/usuarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        usuarioService.excluirUsuario(id);
    }

    @PostMapping("/usuarios/{id}/inativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable UUID id) {
        usuarioService.inativar(id);
    }

    @PostMapping("/usuarios/{id}/reativar")
    public UsuarioResponse reativar(@PathVariable UUID id) {
        return UsuarioResponse.from(usuarioService.reativar(id));
    }
}
