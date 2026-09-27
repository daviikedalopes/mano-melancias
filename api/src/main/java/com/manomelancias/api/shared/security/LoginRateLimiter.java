package com.manomelancias.api.shared.security;

import com.manomelancias.api.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloqueia tentativas de login após muitas falhas seguidas para o mesmo
 * e-mail, mitigando força bruta/credential stuffing em /auth/login.
 *
 * Limitações conhecidas (aceitáveis para o porte atual do projeto): o
 * contador é em memória (reseta se a aplicação reiniciar e não é
 * compartilhado entre múltiplas instâncias), e a chave é só o e-mail, não o
 * IP de origem.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_TENTATIVAS = 5;
    private static final Duration JANELA_BLOQUEIO = Duration.ofMinutes(15);

    private record Tentativas(int contagem, Instant bloqueadoAte) {}

    private final Map<String, Tentativas> tentativasPorEmail = new ConcurrentHashMap<>();

    public void verificarBloqueio(String email) {
        Tentativas atual = tentativasPorEmail.get(chave(email));
        if (atual != null && atual.bloqueadoAte() != null && Instant.now().isBefore(atual.bloqueadoAte())) {
            throw new BusinessException(
                    "Muitas tentativas de login. Tente novamente em alguns minutos.",
                    HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    public void registrarFalha(String email) {
        tentativasPorEmail.compute(chave(email), (k, atual) -> {
            int novaContagem = (atual == null ? 0 : atual.contagem()) + 1;
            Instant bloqueadoAte = novaContagem >= MAX_TENTATIVAS ? Instant.now().plus(JANELA_BLOQUEIO) : null;
            return new Tentativas(novaContagem, bloqueadoAte);
        });
    }

    public void registrarSucesso(String email) {
        tentativasPorEmail.remove(chave(email));
    }

    private String chave(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
