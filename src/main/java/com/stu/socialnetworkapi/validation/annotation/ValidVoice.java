package com.stu.socialnetworkapi.validation.annotation;

import com.stu.socialnetworkapi.validation.validator.ValidVoiceValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidVoiceValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidVoice {
    String message() default "INVALID_VOICE"; // Thông báo mặc định

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
