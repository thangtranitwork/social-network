package com.stu.socialnetworkapi.validation.validator;

import com.stu.socialnetworkapi.validation.annotation.ValidVoice;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class ValidVoiceValidator implements ConstraintValidator<ValidVoice, MultipartFile> {

    private final String[] allowedTypes = new String[]
            {
                    "audio/mpeg", // mp3
                    "audio/wav",
                    "audio/ogg",
                    "audio/webm",
                    "audio/x-wav"
            };

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Kiểm tra định dạng
        String contentType = file.getContentType();
        return Arrays.stream(allowedTypes).anyMatch(t -> t.equalsIgnoreCase(contentType));
    }
}
