package com.manomelancias.api.motorista;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfValidatorTest {

    @Test
    void cpfValido_semFormatacao_ehAceito() {
        assertTrue(CpfValidator.isValid("11144477735"));
        assertTrue(CpfValidator.isValid("52998224725"));
    }

    @Test
    void cpfValido_comFormatacao_ehAceito() {
        assertTrue(CpfValidator.isValid("111.444.777-35"));
    }

    @Test
    void cpfComDigitoVerificadorErrado_ehRejeitado() {
        assertFalse(CpfValidator.isValid("12345678900"));
    }

    @Test
    void cpfComTodosDigitosIguais_ehRejeitado() {
        assertFalse(CpfValidator.isValid("11111111111"));
    }

    @Test
    void cpfComTamanhoInvalido_ehRejeitado() {
        assertFalse(CpfValidator.isValid("123456789"));
    }

    @Test
    void cpfNulo_ehRejeitado() {
        assertFalse(CpfValidator.isValid(null));
    }
}
