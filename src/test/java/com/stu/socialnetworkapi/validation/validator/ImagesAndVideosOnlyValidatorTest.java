package com.stu.socialnetworkapi.validation.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImagesAndVideosOnlyValidatorTest {
    private ImagesAndVideosOnlyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ImagesAndVideosOnlyValidator();
    }

    @Test
    void shouldReturnTrue_WhenAllFileIsValid() {
        MultipartFile file1 = new MockMultipartFile("file-1", "test-1.png", "image/png", "dummy".getBytes());
        MultipartFile file2 = new MockMultipartFile("file-2", "test-2.mp4", "video/mp4", "dummy".getBytes());
        List<MultipartFile> files = List.of(file1, file2);
        assertTrue(validator.isValid(files, null));
    }

    @Test
    void shouldReturnTrue_WhenAtLeastOneFileIsNotValid() {
        MultipartFile file1 = new MockMultipartFile("file-1", "test-1.png", "image/png", "dummy".getBytes());
        MultipartFile file2 = new MockMultipartFile("file-2", "test-2.txt", "text/plain", "dummy".getBytes());
        List<MultipartFile> files = List.of(file1, file2);
        assertFalse(validator.isValid(files, null));
    }

    @Test
    void shouldReturnTrue_WhenIsNull() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void shouldReturnTrue_WhenIsEmpty() {
        assertTrue(validator.isValid(List.of(), null));
    }
}