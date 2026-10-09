package com.manomelancias.api.shared.mail;

import com.manomelancias.api.shared.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Envia e-mails de texto simples pelo servidor SMTP configurado (MAIL_HOST etc.).
 *
 * Sem SMTP configurado (ex.: desenvolvimento local), o e-mail NÃO é enviado: o
 * conteúdo, com o link, vai para o log da aplicação e o método devolve false.
 */
@Component
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String host;
    private final String remetente;

    public EmailSender(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${spring.mail.host:}") String host,
            @Value("${app.mail.from:}") String from,
            @Value("${spring.mail.username:}") String username) {
        this.mailSender = mailSender;
        this.host = host == null ? "" : host.trim();
        this.remetente = !from.isBlank() ? from.trim() : username.trim();
    }

    public boolean configurado() {
        return !host.isEmpty();
    }

    /** @return true se o e-mail foi entregue ao servidor SMTP; false se só foi escrito no log (SMTP não configurado). */
    public boolean enviar(String para, String assunto, String texto) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (!configurado() || sender == null) {
            log.warn("SMTP não configurado (MAIL_HOST vazio): e-mail NÃO enviado.\nPara: {}\nAssunto: {}\n{}",
                    para, assunto, texto);
            return false;
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();
        if (!remetente.isEmpty()) {
            mensagem.setFrom(remetente);
        }
        mensagem.setTo(para);
        mensagem.setSubject(assunto);
        mensagem.setText(texto);
        try {
            sender.send(mensagem);
            return true;
        } catch (MailException ex) {
            log.error("Falha ao enviar e-mail para {}: {}", para, ex.getMessage());
            throw new BusinessException(
                    "Não foi possível enviar o e-mail de confirmação. Confira o endereço e tente novamente.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
