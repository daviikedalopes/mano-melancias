package com.manomelancias.api.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exige senha com pelo menos 8 caracteres, 1 letra maiúscula, 1 número e
 * 1 caractere especial.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordStrengthValidator.class)
public @interface SenhaForte {
    String message() default "senha não atende aos requisitos mínimos de segurança";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
