package com.stu.socialnetworkapi.validation.annotation;

import com.stu.socialnetworkapi.validation.validatior.ValidFileListValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidFileListValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFileList {
    String message() default "INVALID_FILE_TYPE";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
