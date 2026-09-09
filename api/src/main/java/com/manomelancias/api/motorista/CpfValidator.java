package com.manomelancias.api.motorista;

/**
 * Validação de CPF pelo algoritmo padrão de dígitos verificadores (módulo 11).
 */
public final class CpfValidator {

    private CpfValidator() {
    }

    public static boolean isValid(String cpf) {
        if (cpf == null) {
            return false;
        }

        String digits = normalizar(cpf);
        if (digits.length() != 11 || digits.chars().distinct().count() == 1) {
            return false;
        }

        int[] numeros = digits.chars().map(c -> c - '0').toArray();

        int dv1 = calcularDigito(numeros, 9, 10);
        int dv2 = calcularDigito(numeros, 10, 11);

        return numeros[9] == dv1 && numeros[10] == dv2;
    }

    private static int calcularDigito(int[] numeros, int quantidade, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += numeros[i] * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    public static String normalizar(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }
}
