package com.manomelancias.api.usuario;

import com.manomelancias.api.shared.exception.BusinessException;
import com.manomelancias.api.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new BusinessException("E-mail ou senha inválidos", HttpStatus.UNAUTHORIZED);
        }

        return jwtService.gerarToken(usuario.getId(), usuario.getEmail(), usuario.getPapel().name());
    }

    @Transactional
    public Usuario criarUsuario(String nome, String email, String senha, Papel papel) {
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Já existe um usuário com este e-mail", HttpStatus.CONFLICT);
        }

        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senhaHash(passwordEncoder.encode(senha))
                .papel(papel)
                .build();

        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }
}
