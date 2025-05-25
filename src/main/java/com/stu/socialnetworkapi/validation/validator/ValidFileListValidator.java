package com.stu.socialnetworkapi.validation.validator;

import com.stu.socialnetworkapi.validation.annotation.ValidFileList;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Set;

public class ValidFileListValidator implements ConstraintValidator<ValidFileList, Collection<MultipartFile>> {

    public static final Set<String> INVALID_FILE_EXTENSIONS = Set.of(
            ".php", ".js", ".exe", ".sh", ".bat", ".dll", ".com", ".msi"
    );

    @Override
    public boolean isValid(Collection<MultipartFile> files, ConstraintValidatorContext context) {
        if (files == null || files.isEmpty()) {
            return true;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                return false;
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return false;
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (INVALID_FILE_EXTENSIONS.contains(extension)) {
                return false;
            }
        }

        return true;
    }
}
