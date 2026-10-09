package com.manomelancias.api.usuario;

import com.manomelancias.api.shared.exception.BusinessException;
import com.manomelancias.api.shared.exception.ResourceNotFoundException;
import com.manomelancias.api.shared.security.JwtService;
import com.manomelancias.api.shared.security.LoginRateLimiter;
import com.manomelancias.api.usuario.confirmacao.ConfirmacaoEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;
    private final ConfirmacaoEmailService confirmacaoEmailService;

    public String autenticar(String email, String senha) {
        loginRateLimiter.verificarBloqueio(email);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (usuario == null || !passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            loginRateLimiter.registrarFalha(email);
            throw new BusinessException("E-mail ou senha inválidos", HttpStatus.UNAUTHORIZED);
        }

        if (!usuario.getAtivo()) {
            throw new BusinessException("Usuário inativo. Fale com um administrador.", HttpStatus.FORBIDDEN);
        }

        if (!Boolean.TRUE.equals(usuario.getEmailConfirmado())) {
            throw new BusinessException(
                    "Confirme seu e-mail antes de entrar: enviamos um link para a sua caixa de entrada (veja também o spam).",
                    HttpStatus.FORBIDDEN);
        }

        loginRateLimiter.registrarSucesso(email);
        return jwtService.gerarToken(usuario.getId(), usuario.getEmail(), usuario.getPapel().name());
    }

    @Transactional
    public UsuarioCriado criarUsuario(String nome, String email, String senha, Papel papel) {
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Já existe um usuário com este e-mail", HttpStatus.CONFLICT);
        }

        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senhaHash(passwordEncoder.encode(senha))
                .papel(papel)
                .ativo(true)
                .emailConfirmado(false)
                .build();

        usuario = usuarioRepository.save(usuario);
        // Se o envio falhar, a exceção desfaz a criação: não fica conta com e-mail que não recebe nada.
        boolean emailEnviado = confirmacaoEmailService.enviarConfirmacao(usuario);
        return new UsuarioCriado(usuario, emailEnviado);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    @Transactional
    public void inativar(UUID id) {
        Usuario usuario = buscarPorId(id);
        validarOperador(usuario);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario reativar(UUID id) {
        Usuario usuario = buscarPorId(id);
        validarOperador(usuario);
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void excluirUsuario(UUID id) {
        Usuario usuario = buscarPorId(id);
        validarOperador(usuario);
        usuarioRepository.delete(usuario);
    }

    /**
     * Contas ADMIN nunca podem ser excluídas/inativadas por este fluxo —
     * só usuários com papel OPERADOR podem ser alvo dessas ações.
     */
    private void validarOperador(Usuario usuario) {
        if (usuario.getPapel() != Papel.OPERADOR) {
            throw new BusinessException(
                    "Somente usuários com papel de operador podem ser excluídos ou inativados.",
                    HttpStatus.FORBIDDEN);
        }
    }
}
