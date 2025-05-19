package com.stu.socialnetworkapi.validation.annotation;


import com.stu.socialnetworkapi.validation.validatior.ImageFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ImageFileValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ImageFile {
    String message() default "REQUIRED_IMAGE_FILE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

