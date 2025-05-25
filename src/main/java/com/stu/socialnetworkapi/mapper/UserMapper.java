package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.projection.*;
import com.stu.socialnetworkapi.dto.response.OnlineResponse;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.dto.response.UserProfileResponse;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.repository.IsOnlineRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final IsOnlineRedisRepository isOnlineRedisRepository;

    public UserProfileResponse toUserProfileResponse(final UserProfileProjection projection) {
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.userId());
        return UserProfileResponse.builder()
                .id(projection.userId())
                .givenName(projection.givenName())
                .familyName(projection.familyName())
                .bio(projection.bio())
                .username(projection.username())
                .profilePictureUrl(File.getPath(projection.profilePictureId()))
                .coverPictureUrl(File.getPath(projection.coverPictureId()))
                .friendCount(projection.friendCount())
                .showFriends(projection.showFriends())
                .allowFriendRequest(projection.allowFriendRequest())
                .birthdate(projection.birthdate())
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnlineAt())
                .isFriend(projection.isFriend())
                .mutualFriendsCount(projection.mutualFriendsCount())
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final User user) {
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(user.getId());
        return UserCommonInformationResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .givenName(user.getGivenName())
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnlineAt())
                .profilePictureUrl(File.getPath(user.getProfilePicture().getId()))
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final RequestProjection projection) {
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.userId());
        return UserCommonInformationResponse.builder()
                .id(projection.userId())
                .givenName(projection.givenName())
                .familyName(projection.familyName())
                .username(projection.username())
                .profilePictureUrl(File.getPath(projection.profilePictureId()))
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnlineAt())
                .isFriend(false)
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final BlockProjection projection) {
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.userId());
        return UserCommonInformationResponse.builder()
                .id(projection.userId())
                .givenName(projection.givenName())
                .familyName(projection.familyName())
                .username(projection.username())
                .profilePictureUrl(File.getPath(projection.profilePictureId()))
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnlineAt())
                .isFriend(false)
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final FriendProjection projection) {
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.userId());
        return UserCommonInformationResponse.builder()
                .id(projection.userId())
                .givenName(projection.givenName())
                .familyName(projection.familyName())
                .username(projection.username())
                .profilePictureUrl(File.getPath(projection.profilePictureId()))
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnlineAt())
                .isFriend(false)
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final UserProjection projection) {
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.userId());
        return UserCommonInformationResponse.builder()
                .id(projection.userId())
                .givenName(projection.givenName())
                .familyName(projection.familyName())
                .username(projection.username())
                .profilePictureUrl(File.getPath(projection.profilePictureId()))
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnlineAt())
                .isFriend(projection.isFriend())
                .mutualFriendsCount(projection.mutualFriendsCount())
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final NotificationProjection projection) {
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.userId());
        return UserCommonInformationResponse.builder()
                .id(projection.userId())
                .givenName(projection.givenName())
                .familyName(projection.familyName())
                .username(projection.username())
                .profilePictureUrl(File.getPath(projection.profilePictureId()))
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnlineAt())
                .build();
    }
}
