package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.validation.annotation.Age;
import com.stu.socialnetworkapi.validation.annotation.OnlyLetter;
import com.stu.socialnetworkapi.validation.annotation.Username;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public record UpdateInfoRequest(
        @OnlyLetter
        @Length(max = User.MAX_FAMILY_NAME_LENGTH, message = "INVALID_FAMILY_NAME_LENGTH")
        String familyName,
        @OnlyLetter
        @Length(max = User.MAX_GIVEN_NAME_LENGTH, message = "INVALID_GIVEN_NAME_LENGTH")
        String givenName,
        @Username
        String username,
        @Age
        LocalDate birthdate,
        String bio
        ) {

}
