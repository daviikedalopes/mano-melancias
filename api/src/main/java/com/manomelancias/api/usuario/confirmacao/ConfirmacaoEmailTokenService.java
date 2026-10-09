package com.manomelancias.api.usuario.confirmacao;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Gera e valida o token do link de confirmação de e-mail.
 *
 * A chave é DERIVADA do segredo do JWT (e não o próprio segredo) de propósito:
 * assim um token de confirmação nunca é aceito como token de login, e um token
 * de login nunca é aceito como confirmação.
 */
@Service
public class ConfirmacaoEmailTokenService {

    private static final Duration VALIDADE_PADRAO = Duration.ofHours(48);

    private final SecretKey key;
    private final Duration validade;

    @Autowired
    public ConfirmacaoEmailTokenService(@Value("${app.jwt.secret}") String secret) {
        this(secret, VALIDADE_PADRAO);
    }

    ConfirmacaoEmailTokenService(String secret, Duration validade) {
        this.key = Keys.hmacShaKeyFor(derivar(secret));
        this.validade = validade;
    }

    public String gerar(UUID usuarioId, String email) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("email", email.toLowerCase())
                .claim("fin", "confirmacao-email")
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(validade)))
                .signWith(key)
                .compact();
    }

    /** Lança JwtException se o token for inválido, adulterado ou expirado. */
    public Claims validar(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static byte[] derivar(String secret) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest((secret + "|confirmacao-email").getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
