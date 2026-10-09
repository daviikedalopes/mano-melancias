package com.manomelancias.api.usuario.confirmacao;

import com.manomelancias.api.shared.exception.BusinessException;
import com.manomelancias.api.shared.exception.ResourceNotFoundException;
import com.manomelancias.api.shared.mail.EmailSender;
import com.manomelancias.api.usuario.Usuario;
import com.manomelancias.api.usuario.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Confirmação de e-mail: só quem consegue abrir a caixa de entrada do endereço
 * cadastrado recebe o link e ativa a conta — a única prova real de que o
 * e-mail existe e pertence à pessoa.
 */
@Service
public class ConfirmacaoEmailService {

    static final Duration INTERVALO_REENVIO = Duration.ofMinutes(1);

    private final UsuarioRepository usuarioRepository;
    private final ConfirmacaoEmailTokenService tokenService;
    private final EmailSender emailSender;
    private final String urlPublica;

    // Evita que o reenvio vire canal de spam. Em memória (reseta ao reiniciar), como o LoginRateLimiter.
    private final Map<UUID, Instant> ultimoEnvio = new ConcurrentHashMap<>();

    public ConfirmacaoEmailService(
            UsuarioRepository usuarioRepository,
            ConfirmacaoEmailTokenService tokenService,
            EmailSender emailSender,
            @Value("${app.public-url}") String urlPublica) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.emailSender = emailSender;
        this.urlPublica = urlPublica.endsWith("/") ? urlPublica.substring(0, urlPublica.length() - 1) : urlPublica;
    }

    /**
     * Envia o link de confirmação. Falha de envio lança exceção (quem chama decide o rollback).
     * @return false se o SMTP não está configurado e o link só foi escrito no log
     */
    public boolean enviarConfirmacao(Usuario usuario) {
        String token = tokenService.gerar(usuario.getId(), usuario.getEmail());
        String link = urlPublica + "/confirmar-email.html?token=" + token;
        String texto = "Olá, " + usuario.getNome() + "!\n\n"
                + "Uma conta foi criada para você no sistema Mano Melancias.\n"
                + "Para ativá-la, confirme este e-mail pelo link abaixo (válido por 48 horas):\n\n"
                + link + "\n\n"
                + "Se você não esperava este e-mail, pode ignorá-lo.\n";
        boolean enviado = emailSender.enviar(usuario.getEmail(), "Confirme seu e-mail - Mano Melancias", texto);
        ultimoEnvio.put(usuario.getId(), Instant.now());
        return enviado;
    }

    @Transactional
    public void confirmar(String token) {
        Claims claims;
        try {
            claims = tokenService.validar(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw linkInvalido();
        }

        Usuario usuario = usuarioRepository.findById(UUID.fromString(claims.getSubject())).orElseThrow(this::linkInvalido);
        String emailDoToken = claims.get("email", String.class);
        if (emailDoToken == null || !emailDoToken.equalsIgnoreCase(usuario.getEmail())) {
            throw linkInvalido();
        }

        if (!Boolean.TRUE.equals(usuario.getEmailConfirmado())) {
            usuario.setEmailConfirmado(true);
            usuarioRepository.save(usuario);
        }
    }

    /**
     * Reenvio pedido por quem não recebeu o e-mail (sem login). Nunca revela se o
     * e-mail existe: o resultado é sempre o mesmo para quem chama.
     */
    public void reenviarPorEmail(String email) {
        usuarioRepository.findByEmailIgnoreCase(email.trim())
                .filter(u -> !Boolean.TRUE.equals(u.getEmailConfirmado()))
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .filter(this::podeReenviar)
                .ifPresent(usuario -> {
                    try {
                        enviarConfirmacao(usuario);
                    } catch (BusinessException ex) {
                        // Já registrado no log; engolido para não revelar que o e-mail existe.
                    }
                });
    }

    /** Reenvio pelo administrador, na tela de usuários. */
    public boolean reenviarPorId(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        if (Boolean.TRUE.equals(usuario.getEmailConfirmado())) {
            throw new BusinessException("Este e-mail já foi confirmado.", HttpStatus.CONFLICT);
        }
        if (!podeReenviar(usuario)) {
            throw new BusinessException(
                    "Um e-mail de confirmação acabou de ser enviado. Aguarde um minuto para reenviar.",
                    HttpStatus.TOO_MANY_REQUESTS);
        }
        return enviarConfirmacao(usuario);
    }

    private boolean podeReenviar(Usuario usuario) {
        Instant ultimo = ultimoEnvio.get(usuario.getId());
        return ultimo == null || Instant.now().isAfter(ultimo.plus(INTERVALO_REENVIO));
    }

    private BusinessException linkInvalido() {
        return new BusinessException("Link inválido ou expirado. Peça um novo e-mail de confirmação.");
    }
}
