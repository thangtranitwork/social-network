package com.stu.socialnetworkapi.validation.annotation;

import com.stu.socialnetworkapi.validation.validator.ImageAndVideoOnlyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ImageAndVideoOnlyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImageAndVideoOnly {
    String message() default "REQUIRED_IMAGE_FILE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
