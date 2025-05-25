package com.stu.socialnetworkapi.validation.validator;

import com.stu.socialnetworkapi.validation.annotation.OnlyLetter;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class OnlyLetterValidator implements ConstraintValidator<OnlyLetter, String> {
    private static final Pattern LETTER_SPACE_PATTERN = Pattern.compile("^[\\p{L}\\s]+$");

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return LETTER_SPACE_PATTERN.matcher(s.trim()).matches();
    }
}
