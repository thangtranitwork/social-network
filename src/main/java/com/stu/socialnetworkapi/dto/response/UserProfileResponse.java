package com.stu.socialnetworkapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse extends UserCommonInformationResponse {
    String bio;
    LocalDate birthdate;
    int friendCount;
    int postCount;
}
