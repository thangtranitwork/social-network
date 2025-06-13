package com.stu.socialnetworkapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stu.socialnetworkapi.enums.BlockStatus;
import com.stu.socialnetworkapi.enums.RequestDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserCommonInformationResponse {
    UUID id;
    String username;
    String givenName;
    String familyName;
    String profilePictureUrl;
    Boolean isFriend;
    Boolean isOnline;
    ZonedDateTime lastOnline;
    Integer mutualFriendsCount;
    RequestDirection request;
    BlockStatus blockStatus;
}
