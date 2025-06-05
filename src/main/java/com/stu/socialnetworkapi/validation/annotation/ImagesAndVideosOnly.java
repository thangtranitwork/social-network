package com.stu.socialnetworkapi.validation.annotation;

import com.stu.socialnetworkapi.validation.validator.ImagesAndVideosOnlyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ImagesAndVideosOnlyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImagesAndVideosOnly {
    String message() default "REQUIRED_IMAGE_OR_VIDEO_FILE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
