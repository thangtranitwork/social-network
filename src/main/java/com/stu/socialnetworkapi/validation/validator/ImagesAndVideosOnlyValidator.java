package com.stu.socialnetworkapi.validation.validator;

import com.stu.socialnetworkapi.validation.annotation.ImagesAndVideosOnly;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ImagesAndVideosOnlyValidator implements ConstraintValidator<ImagesAndVideosOnly, List<MultipartFile>> {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            // Images
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp", "image/svg+xml",
            // Videos
            "video/mp4", "video/quicktime", "video/x-msvideo", "video/x-ms-wmv", "video/webm", "video/ogg", "video/mpeg"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            // Image extensions
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg",
            // Video extensions
            "mp4", "mov", "avi", "wmv", "webm", "ogv", "mpeg"
    );

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if (files == null || files.isEmpty()) return true;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String mimeType = file.getContentType();
            if (mimeType != null && ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
                continue;
            }

            String ext = Optional.ofNullable(file.getOriginalFilename())
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(f.lastIndexOf('.') + 1).toLowerCase())
                    .orElse("");

            if (!ALLOWED_EXTENSIONS.contains(ext)) return false;
        }

        return true;
    }
}
