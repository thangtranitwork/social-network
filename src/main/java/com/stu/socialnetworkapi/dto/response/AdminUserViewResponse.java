package com.stu.socialnetworkapi.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminUserViewResponse extends UserCommonInformationResponse {
    String bio;
    LocalDate birthdate;
    int friendCount;
    int blockCount;
    int requestSentCount;
    int requestReceivedCount;
    int postCount;
    int commentCount;
    int uploadedFileCount;
    int messageCount;
    int callCount;
    String email;
    ZonedDateTime registrationDate;
    boolean isVerified;
}
