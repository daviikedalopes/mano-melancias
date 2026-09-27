package com.manomelancias.api.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.regex.Pattern;

public class EmailDomainValidator implements ConstraintValidator<ValidEmail, String> {

    private static final Logger log = LoggerFactory.getLogger(EmailDomainValidator.class);

    // Exige um domínio com TLD (pelo menos um ponto, terminando em 2+ letras) —
    // rejeita hosts sem TLD como "manomelancias".
    private static final Pattern FORMATO = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return true; // @NotBlank cuida da ausência
        }

        String valor = email.trim();
        if (!FORMATO.matcher(valor).matches()) {
            return falhar(context, "Formato de e-mail inválido");
        }

        String dominio = valor.substring(valor.indexOf('@') + 1);
        if (!dominioExiste(dominio)) {
            return falhar(context, "O domínio do e-mail não existe ou não recebe mensagens");
        }

        return true;
    }

    /**
     * Um domínio é considerado apto a receber e-mail se tiver registro MX,
     * ou, na ausência dele, um registro A/AAAA (fallback implícito do MX
     * por RFC 5321).
     */
    private boolean dominioExiste(String dominio) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("com.sun.jndi.dns.timeout.initial", "2000");
        env.put("com.sun.jndi.dns.timeout.retries", "1");
        try {
            DirContext ctx = new InitialDirContext(env);
            return temRegistro(ctx, dominio, "MX")
                    || temRegistro(ctx, dominio, "A")
                    || temRegistro(ctx, dominio, "AAAA");
        } catch (NamingException ex) {
            log.warn("Não foi possível verificar o domínio de e-mail '{}': {}", dominio, ex.getMessage());
            return false;
        }
    }

    private boolean temRegistro(DirContext ctx, String dominio, String tipo) {
        try {
            Attributes attrs = ctx.getAttributes(dominio, new String[]{tipo});
            Attribute attr = attrs.get(tipo);
            return attr != null && attr.size() > 0;
        } catch (NamingException ex) {
            return false;
        }
    }

    private boolean falhar(ConstraintValidatorContext context, String mensagem) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(mensagem).addConstraintViolation();
        return false;
    }
}
