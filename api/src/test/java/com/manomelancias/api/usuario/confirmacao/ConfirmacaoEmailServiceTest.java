package com.manomelancias.api.usuario.confirmacao;

import com.manomelancias.api.shared.exception.BusinessException;
import com.manomelancias.api.shared.mail.EmailSender;
import com.manomelancias.api.usuario.Papel;
import com.manomelancias.api.usuario.Usuario;
import com.manomelancias.api.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfirmacaoEmailServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-trinta-e-dois-caracteres-abcdef";

    private UsuarioRepository repository;
    private EmailSender emailSender;
    private ConfirmacaoEmailTokenService tokenService;
    private ConfirmacaoEmailService service;
    private Usuario pendente;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        emailSender = mock(EmailSender.class);
        tokenService = new ConfirmacaoEmailTokenService(SEGREDO);
        // URL pública com barra no final, para provar que não sai "//" no link
        service = new ConfirmacaoEmailService(repository, tokenService, emailSender, "https://sistema.exemplo.com.br/");

        pendente = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Ana")
                .email("ana@exemplo.com.br")
                .senhaHash("hash")
                .papel(Papel.OPERADOR)
                .ativo(true)
                .emailConfirmado(false)
                .build();
        when(repository.findById(pendente.getId())).thenReturn(Optional.of(pendente));
        when(repository.findByEmailIgnoreCase("ana@exemplo.com.br")).thenReturn(Optional.of(pendente));
    }

    @Test
    void enviarConfirmacao_deveMandarLinkComTokenParaOEmailDoUsuario() {
        service.enviarConfirmacao(pendente);

        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        verify(emailSender).enviar(org.mockito.ArgumentMatchers.eq("ana@exemplo.com.br"), anyString(), texto.capture());
        assertTrue(texto.getValue().contains("https://sistema.exemplo.com.br/confirmar-email.html?token="));
        assertFalse(texto.getValue().contains("br//confirmar"));
    }

    @Test
    void enviarConfirmacao_devolveSeOEmailSaiuDeFato() {
        when(emailSender.enviar(anyString(), anyString(), anyString())).thenReturn(true, false);

        assertTrue(service.enviarConfirmacao(pendente));   // SMTP configurado
        assertFalse(service.enviarConfirmacao(pendente));  // SMTP ausente: só foi para o log
    }

    @Test
    void reenviarPorId_devolveSeOEmailSaiuDeFato() {
        when(emailSender.enviar(anyString(), anyString(), anyString())).thenReturn(false);

        assertFalse(service.reenviarPorId(pendente.getId()));
    }

    @Test
    void confirmar_comTokenValido_marcaEmailComoConfirmado() {
        String token = tokenService.gerar(pendente.getId(), pendente.getEmail());

        service.confirmar(token);

        assertTrue(pendente.getEmailConfirmado());
        verify(repository).save(pendente);
    }

    @Test
    void confirmar_duasVezes_naoDaErro() {
        String token = tokenService.gerar(pendente.getId(), pendente.getEmail());

        service.confirmar(token);
        service.confirmar(token);

        assertTrue(pendente.getEmailConfirmado());
        verify(repository, times(1)).save(pendente);
    }

    @Test
    void confirmar_comTokenInvalido_deveRecusar() {
        assertThrows(BusinessException.class, () -> service.confirmar("lixo"));
        assertFalse(pendente.getEmailConfirmado());
    }

    @Test
    void confirmar_comTokenDeOutroEmail_deveRecusar() {
        String token = tokenService.gerar(pendente.getId(), "outra@exemplo.com.br");

        assertThrows(BusinessException.class, () -> service.confirmar(token));
        assertFalse(pendente.getEmailConfirmado());
    }

    @Test
    void reenviarPorEmail_emailInexistente_naoEnviaNemRevelaNada() {
        when(repository.findByEmailIgnoreCase("ninguem@exemplo.com.br")).thenReturn(Optional.empty());

        service.reenviarPorEmail("ninguem@exemplo.com.br");

        verify(emailSender, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void reenviarPorEmail_jaConfirmado_naoEnvia() {
        pendente.setEmailConfirmado(true);

        service.reenviarPorEmail("ana@exemplo.com.br");

        verify(emailSender, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void reenviarPorEmail_pendente_enviaUmaVezEBloqueiaRepeticaoImediata() {
        service.reenviarPorEmail("ana@exemplo.com.br");
        service.reenviarPorEmail("ana@exemplo.com.br");

        verify(emailSender, times(1)).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void reenviarPorEmail_falhaNoEnvio_naoVazaExcecao() {
        doThrow(new BusinessException("smtp fora", HttpStatus.SERVICE_UNAVAILABLE))
                .when(emailSender).enviar(anyString(), anyString(), anyString());

        service.reenviarPorEmail("ana@exemplo.com.br"); // não deve lançar
    }

    @Test
    void reenviarPorId_jaConfirmado_retornaConflito() {
        pendente.setEmailConfirmado(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.reenviarPorId(pendente.getId()));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void reenviarPorId_logoAposUmEnvio_retornaTooManyRequests() {
        service.reenviarPorId(pendente.getId());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.reenviarPorId(pendente.getId()));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatus());
    }
}
