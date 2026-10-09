package com.manomelancias.api.usuario.confirmacao;

import com.manomelancias.api.shared.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfirmacaoEmailTokenServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-trinta-e-dois-caracteres-abcdef";

    private final ConfirmacaoEmailTokenService service = new ConfirmacaoEmailTokenService(SEGREDO);

    @Test
    void tokenGerado_deveSerValidadoComIdEEmail() {
        UUID id = UUID.randomUUID();

        Claims claims = service.validar(service.gerar(id, "Ana@Exemplo.com.br"));

        assertEquals(id.toString(), claims.getSubject());
        assertEquals("ana@exemplo.com.br", claims.get("email", String.class));
    }

    @Test
    void tokenAdulterado_deveSerRecusado() {
        String token = service.gerar(UUID.randomUUID(), "ana@exemplo.com.br");
        String adulterado = token.substring(0, token.length() - 3) + "abc";

        assertThrows(JwtException.class, () -> service.validar(adulterado));
    }

    @Test
    void tokenExpirado_deveSerRecusado() {
        ConfirmacaoEmailTokenService expirado = new ConfirmacaoEmailTokenService(SEGREDO, Duration.ofSeconds(-5));

        String token = expirado.gerar(UUID.randomUUID(), "ana@exemplo.com.br");

        assertThrows(JwtException.class, () -> service.validar(token));
    }

    @Test
    void tokenAssinadoComOutroSegredo_deveSerRecusado() {
        ConfirmacaoEmailTokenService outro = new ConfirmacaoEmailTokenService("outro-segredo-com-mais-de-trinta-e-dois-caracteres-xyz");

        String token = outro.gerar(UUID.randomUUID(), "ana@exemplo.com.br");

        assertThrows(JwtException.class, () -> service.validar(token));
    }

    @Test
    void tokenDeLogin_naoPodeServirComoConfirmacao_eViceVersa() {
        JwtService jwtService = new JwtService(SEGREDO, 480);
        UUID id = UUID.randomUUID();

        String tokenDeLogin = jwtService.gerarToken(id, "ana@exemplo.com.br", "ADMIN");
        String tokenDeConfirmacao = service.gerar(id, "ana@exemplo.com.br");

        assertThrows(JwtException.class, () -> service.validar(tokenDeLogin));
        assertThrows(JwtException.class, () -> jwtService.validarEExtrairClaims(tokenDeConfirmacao));
    }
}
