package com.stu.socialnetworkapi.validation.validator;

import com.stu.socialnetworkapi.validation.annotation.ImageFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.Set;

public class ImageFileValidator implements ConstraintValidator<ImageFile, MultipartFile> {

    private static final Set<String> IMAGE_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp", "image/svg+xml"
    );

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg"
    );

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) return true;

        String mimeType = file.getContentType();
        if (mimeType != null && IMAGE_MIME_TYPES.contains(mimeType.toLowerCase())) {
            return true;
        }

        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf('.') + 1).toLowerCase())
                .orElse("");

        return IMAGE_EXTENSIONS.contains(ext);
    }
}

