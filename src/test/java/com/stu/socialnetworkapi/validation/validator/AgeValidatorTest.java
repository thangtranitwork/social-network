package com.stu.socialnetworkapi.validation.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AgeValidatorTest {

    private AgeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AgeValidator();
    }

    @Test
    void shouldReturnFalse_WhenBirthdateIsNull() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    void shouldReturnFalse_WhenAgeLessThan16() {
        LocalDate under16 = LocalDate.now().minusYears(15);
        assertFalse(validator.isValid(under16, null));
    }

    @Test
    void shouldReturnTrue_WhenAgeExactly16() {
        LocalDate exactly16 = LocalDate.now().minusYears(16);
        assertTrue(validator.isValid(exactly16, null));
    }

    @Test
    void shouldReturnTrue_WhenAgeGreaterThan16() {
        LocalDate older = LocalDate.now().minusYears(20);
        assertTrue(validator.isValid(older, null));
    }
}
