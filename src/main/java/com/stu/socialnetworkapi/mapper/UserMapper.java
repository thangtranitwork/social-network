package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.projection.*;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.dto.response.UserProfileResponse;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.repository.LastSeenRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final LastSeenRedisRepository lastSeenRedisRepository;

    public UserProfileResponse toUserProfileResponse(final UserProfileProjection projection) {
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
                .lastSeen(lastSeenRedisRepository.getLastSeen(projection.userId()))
                .isFriend(projection.isFriend())
                .mutualFriendsCount(projection.mutualFriendsCount())
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final User user) {
        return UserCommonInformationResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .givenName(user.getGivenName())
                .familyName(user.getFamilyName())
                .lastSeen(lastSeenRedisRepository.getLastSeen(user.getId()))
                .profilePictureUrl(File.getPath(user.getProfilePicture().getId()))
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final RequestProjection projection) {
        return UserCommonInformationResponse.builder()
                .id(projection.userId())
                .givenName(projection.givenName())
                .familyName(projection.familyName())
                .username(projection.username())
                .profilePictureUrl(File.getPath(projection.profilePictureId()))
                .lastSeen(lastSeenRedisRepository.getLastSeen(projection.userId()))
                .isFriend(false)
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final BlockProjection projection) {
        return UserCommonInformationResponse.builder()
                .id(projection.userId())
                .givenName(projection.givenName())
                .familyName(projection.familyName())
                .username(projection.username())
                .profilePictureUrl(File.getPath(projection.profilePictureId()))
                .lastSeen(lastSeenRedisRepository.getLastSeen(projection.userId()))
                .isFriend(false)
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final FriendProjection projection) {
        return UserCommonInformationResponse.builder()
                .id(projection.userId())
                .givenName(projection.givenName())
                .familyName(projection.familyName())
                .username(projection.username())
                .profilePictureUrl(File.getPath(projection.profilePictureId()))
                .lastSeen(lastSeenRedisRepository.getLastSeen(projection.userId()))
                .isFriend(false)
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final UserProjection projection) {
        return UserCommonInformationResponse.builder()
                .id(projection.userId())
                .givenName(projection.givenName())
                .familyName(projection.familyName())
                .username(projection.username())
                .profilePictureUrl(File.getPath(projection.profilePictureId()))
                .lastSeen(lastSeenRedisRepository.getLastSeen(projection.userId()))
                .isFriend(projection.isFriend())
                .mutualFriendsCount(projection.mutualFriendsCount())
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final NotificationProjection projection) {
        return UserCommonInformationResponse.builder()
                .id(projection.userId())
                .givenName(projection.givenName())
                .familyName(projection.familyName())
                .username(projection.username())
                .profilePictureUrl(File.getPath(projection.profilePictureId()))
                .lastSeen(lastSeenRedisRepository.getLastSeen(projection.userId()))
                .build();
    }
}
