package com.stu.socialnetworkapi.validation.annotation;

import com.stu.socialnetworkapi.validation.validator.UsernameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UsernameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Username {
    String message() default "INVALID_USERNAME";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}