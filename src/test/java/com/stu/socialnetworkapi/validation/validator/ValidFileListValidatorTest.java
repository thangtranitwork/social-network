package com.stu.socialnetworkapi.validation.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidFileListValidatorTest {
    private ValidFileListValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ValidFileListValidator();
    }

    @Test
    void shouldReturnTrue_WhenFileListIsNull() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void shouldReturnTrue_WhenFileListIsEmpty() {
        assertTrue(validator.isValid(List.of(), null));
    }

    @Test
    void shouldReturnFalse_WhenAreValidFile() {
        MultipartFile imageFile = new MockMultipartFile("file", "file.png", "image/png", "1".getBytes());
        MultipartFile videoFile = new MockMultipartFile("file", "file.mp4", "video/mp4", "1".getBytes());
        MultipartFile emptyFile = new MockMultipartFile("file", "file.mp4", "video/mp4", new byte[0]);

        assertFalse(validator.isValid(List.of(imageFile, videoFile, emptyFile), null));
    }

    @Test
    void shouldReturnFalse_WhenHaveAInvalidFile() {
        MultipartFile imageFile = new MockMultipartFile("file", "file.png", "image/png", "1".getBytes());
        MultipartFile videoFile = new MockMultipartFile("file", "file.mp4", "video/mp4", "1".getBytes());
        MultipartFile emptyFile = new MockMultipartFile("file", "file.mp4", "video/mp4", new byte[0]);
        MultipartFile invalidFile = new MockMultipartFile("file", "test.bat", null, "dummy".getBytes());

        assertFalse(validator.isValid(List.of(imageFile, videoFile, emptyFile, invalidFile), null));
    }

}

