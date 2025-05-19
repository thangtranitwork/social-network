package com.stu.socialnetworkapi.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UserProfileResponse extends UserCommonInformationResponse {
    String bio;
    LocalDate birthdate;
    String coverPictureUrl;
    int friendCount;
    boolean showFriends;
    boolean allowFriendRequest;
}
