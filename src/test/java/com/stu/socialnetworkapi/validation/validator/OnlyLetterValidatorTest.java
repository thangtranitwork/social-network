package com.stu.socialnetworkapi.validation.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlyLetterValidatorTest {
    private OnlyLetterValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OnlyLetterValidator();
    }

    @Test
    void ShouldReturnTrue_WhenIsValid() {
        String text = "Cháu lên ba";
        assertTrue(validator.isValid(text, null));
    }

    @Test
    void ShouldReturnTrue_WhenIsNull() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void ShouldReturnTrue_WhenIsEmpty() {
        assertTrue(validator.isValid("", null));
    }

    @Test
    void ShouldReturnFalse_WhenHaveAInvalidCharacter() {
        String text = "Cháu lên 3";
        assertFalse(validator.isValid(text, null));
    }
}