package com.stu.socialnetworkapi.validation.validatior;

import com.stu.socialnetworkapi.validation.annotation.Age;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<Age, LocalDate> {

    private static final int MIN_AGE = 16;

    @Override
    public boolean isValid(LocalDate birthdate, ConstraintValidatorContext context) {
        LocalDate today = LocalDate.now();
        return birthdate != null && Period.between(birthdate, today).getYears() >= MIN_AGE;
    }
}
