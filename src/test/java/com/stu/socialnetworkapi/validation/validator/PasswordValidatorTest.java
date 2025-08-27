package com.stu.socialnetworkapi.validation.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTest {

    private PasswordValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
    }

    @ParameterizedTest
    @CsvSource({
            "ChauLen3@@@, true",
            "123, false",
            "CHAULEN3@@@, false",
            "chaulen3@@@, false",
            "Chaulen3333, false"
    })
    void shouldValidatePasswordCorrectly(String password, boolean expected) {
        boolean actual = validator.isValid(password, null);
        if (expected) {
            assertTrue(actual);
        } else {
            assertFalse(actual);
        }
    }
}
