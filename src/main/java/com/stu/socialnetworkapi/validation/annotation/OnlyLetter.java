package com.stu.socialnetworkapi.validation.annotation;

import com.stu.socialnetworkapi.validation.validator.OnlyLetterValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OnlyLetterValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnlyLetter {
    String message() default "ONLY_LETTER_ACCEPTED"; // Thông báo mặc định

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
