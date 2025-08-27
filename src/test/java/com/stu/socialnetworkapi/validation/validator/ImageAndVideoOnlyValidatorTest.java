package com.stu.socialnetworkapi.validation.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageAndVideoOnlyValidatorTest {
    private ImageAndVideoOnlyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ImageAndVideoOnlyValidator();
    }

    @Test
    void shouldReturnTrue_WhenIsImageFile() {
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "dummy".getBytes());
        assertTrue(validator.isValid(file, null));
    }

    @Test
    void shouldReturnTrue_WhenIsVideoFile() {
        MultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "dummy".getBytes());
        assertTrue(validator.isValid(file, null));
    }

    @Test
    void shouldReturnTrue_WhenIsNull() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void shouldReturnTrue_WhenIsEmpty() {
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[0]);
        assertTrue(validator.isValid(file, null));
    }

    @Test
    void shouldReturnFalse_WhenIsNotBothImageAndVideoFile() {
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "dummy".getBytes());
        assertFalse(validator.isValid(file, null));
    }

    @Test
    void shouldReturnFalse_WhenMimeTypeIsNull() {
        MultipartFile file = new MockMultipartFile("file", "test.txt", null, "dummy".getBytes());
        assertFalse(validator.isValid(file, null));
    }
}