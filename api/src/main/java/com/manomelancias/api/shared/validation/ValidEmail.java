package com.manomelancias.api.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida não só o formato do e-mail (exige domínio com TLD, ex: rejeita
 * "admin@manomelancias") como também que o domínio realmente existe e
 * está apto a receber mensagens (consulta DNS por registro MX ou A/AAAA).
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailDomainValidator.class)
public @interface ValidEmail {
    String message() default "e-mail inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
