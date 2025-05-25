package com.stu.socialnetworkapi.dto.response;

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
public class UserCommonInformationResponse {
    UUID id;
    String username;
    String givenName;
    String familyName;
    String profilePictureUrl;
    boolean isFriend;
    boolean isOnline;
    ZonedDateTime lastOnline;
    int mutualFriendsCount;
}
