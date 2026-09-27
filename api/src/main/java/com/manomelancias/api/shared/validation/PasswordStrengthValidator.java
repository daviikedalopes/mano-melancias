package com.manomelancias.api.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordStrengthValidator implements ConstraintValidator<SenhaForte, String> {

    private static final int TAMANHO_MINIMO = 8;
    private static final Pattern MAIUSCULA = Pattern.compile("[A-Z]");
    private static final Pattern NUMERO = Pattern.compile("[0-9]");
    private static final Pattern CARACTERE_ESPECIAL = Pattern.compile("[^A-Za-z0-9]");

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context) {
        if (senha == null || senha.isBlank()) {
            return true; // @NotBlank cuida da ausência
        }

        List<String> faltando = new ArrayList<>();
        if (senha.length() < TAMANHO_MINIMO) faltando.add("mínimo de " + TAMANHO_MINIMO + " caracteres");
        if (!MAIUSCULA.matcher(senha).find()) faltando.add("1 letra maiúscula");
        if (!NUMERO.matcher(senha).find()) faltando.add("1 número");
        if (!CARACTERE_ESPECIAL.matcher(senha).find()) faltando.add("1 caractere especial");

        if (faltando.isEmpty()) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Senha fraca: falta " + String.join(", ", faltando) + ".")
                .addConstraintViolation();
        return false;
    }
}
