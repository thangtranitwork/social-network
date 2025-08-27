package com.stu.socialnetworkapi.validation.validator;

import com.stu.socialnetworkapi.validation.annotation.ImagesAndVideosOnly;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ImagesAndVideosOnlyValidator implements ConstraintValidator<ImagesAndVideosOnly, List<MultipartFile>> {


    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if (files == null || files.isEmpty()) return true;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String mimeType = file.getContentType();
            if (mimeType == null || (!mimeType.startsWith("image/") && !mimeType.startsWith("video/"))) {
                return false;
            }
        }

        return true;
    }
}
