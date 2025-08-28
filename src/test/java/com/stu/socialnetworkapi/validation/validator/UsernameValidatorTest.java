package com.stu.socialnetworkapi.validation.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsernameValidatorTest {
    private UsernameValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UsernameValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test",
            "test-1",
            "test.2-"
    })
    void shouldReturnTrue_WhenIsValidUsername(String username) {
        assertTrue(validator.isValid(username, null));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            " ",
            "!!!",
            "@",
            ""
    })
    void shouldReturnFalse_WhenIsInvalidUsername(String username) {
        assertFalse(validator.isValid(username, null));
    }
}