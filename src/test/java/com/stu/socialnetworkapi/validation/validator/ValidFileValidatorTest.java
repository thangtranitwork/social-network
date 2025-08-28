package com.stu.socialnetworkapi.validation.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidFileValidatorTest {
    private ValidFileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ValidFileValidator();
    }

    @Test
    void shouldReturnTrue_WhenIsValidFile() {
        MultipartFile file = new MockMultipartFile("file", "file.png", "image/png", "test".getBytes());
        assertTrue(validator.isValid(file, null));
    }

    @Test
    void shouldReturnTrue_WhenIsNull() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void shouldReturnTrue_WhenIsEmpty() {
        MultipartFile file = new MockMultipartFile("file", "file.png", "image/png", new byte[0]);
        assertTrue(validator.isValid(file, null));
    }

    @Test
    void shouldReturnFalse_WhenIsInvalidFile() {
        MultipartFile file = new MockMultipartFile("file", "file.bat", "application/", "test".getBytes());
        assertFalse(validator.isValid(file, null));
    }
}