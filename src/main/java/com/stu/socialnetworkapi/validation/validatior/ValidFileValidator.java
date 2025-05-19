package com.stu.socialnetworkapi.validation.validatior;

import com.stu.socialnetworkapi.validation.annotation.ValidFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public class ValidFileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    public static final Set<String> INVALID_FILE_EXTENSIONS = Set.of(
            ".php", ".js", ".exe", ".sh", ".bat", ".dll", ".com", ".msi"
    );

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return false;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex < 0) {
            return false;
        }

        String extension = originalFilename.substring(lastDotIndex).toLowerCase();

        return !INVALID_FILE_EXTENSIONS.contains(extension);
    }

}
