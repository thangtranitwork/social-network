package com.stu.socialnetworkapi.validation.validator;

import com.stu.socialnetworkapi.validation.annotation.ImageAndVideoOnly;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class ImageAndVideoOnlyValidator implements ConstraintValidator<ImageAndVideoOnly, MultipartFile> {
    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) return true;

        String mimeType = file.getContentType();
        return mimeType != null && (mimeType.startsWith("image/") || mimeType.startsWith("video/"));
    }
}
