package com.stu.socialnetworkapi.validation.annotation;

import com.stu.socialnetworkapi.validation.validator.AgeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AgeValidator.class) // Liên kết với validator logic
@Target({ ElementType.FIELD, ElementType.PARAMETER }) // Áp dụng cho trường và tham số
@Retention(RetentionPolicy.RUNTIME) // Lưu annotation ở thời gian runtime
public @interface Age {

    String message() default "AGE_MUST_BE_AT_LEAST_16"; // Thông báo mặc định

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

