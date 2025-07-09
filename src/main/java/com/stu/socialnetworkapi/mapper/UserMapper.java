package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.projection.*;
import com.stu.socialnetworkapi.dto.response.AdminUserViewResponse;
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
                .friendCount(projection.friendCount())
                .birthdate(projection.birthdate())
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnline())
                .isFriend(projection.isFriend())
                .mutualFriendsCount(projection.mutualFriendsCount())
                .request(projection.request())
                .blockStatus(projection.blockStatus())
                .postCount(projection.postCount())
                .build();
    }

    public UserCommonInformationResponse toUserCommonInformationResponse(final User user) {
        if (user == null) {
            return null;
        }
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(user.getId());
        String profilePictureUrl = user.getProfilePicture() != null
                ? File.getPath(user.getProfilePicture().getId())
                : null;
        return UserCommonInformationResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .givenName(user.getGivenName())
                .familyName(user.getFamilyName())
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnline())
                .profilePictureUrl(profilePictureUrl)
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
                .lastOnline(online.getLastOnline())
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
                .lastOnline(online.getLastOnline())
                .build();
    }

    public UserCommonInformationResponse toSenderUserCommonInformationResponse(final ChatProjection projection) {
        if (projection == null || projection.latestMessageSenderId() == null) {
            return null;
        }
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.latestMessageSenderId());
        return UserCommonInformationResponse.builder()
                .id(projection.latestMessageSenderId())
                .givenName(projection.latestMessageSenderGivenName())
                .familyName(projection.latestMessageSenderFamilyName())
                .username(projection.latestMessageSenderUsername())
                .profilePictureUrl(File.getPath(projection.latestMessageSenderProfilePictureId()))
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnline())
                .build();
    }

    public UserCommonInformationResponse toSenderUserCommonInformationResponse(final MessageProjection projection) {
        if (projection == null || projection.senderId() == null) {
            return null;
        }
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.senderId());
        return UserCommonInformationResponse.builder()
                .id(projection.senderId())
                .givenName(projection.senderGivenName())
                .familyName(projection.senderFamilyName())
                .username(projection.senderUsername())
                .profilePictureUrl(File.getPath(projection.senderProfilePictureId()))
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnline())
                .build();
    }

    public UserCommonInformationResponse toTargetUserCommonInformationResponse(final ChatProjection projection) {
        if (projection == null || projection.targetId() == null) {
            return null;
        }
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.targetId());
        return UserCommonInformationResponse.builder()
                .id(projection.targetId())
                .givenName(projection.targetGivenName())
                .familyName(projection.targetFamilyName())
                .username(projection.targetUsername())
                .profilePictureUrl(File.getPath(projection.targetProfilePictureId()))
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnline())
                .isFriend(projection.isFriend())
                .build();
    }

    public UserCommonInformationResponse toPostAuthorCommonInformationResponse(final PostProjection projection) {
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.authorId());
        return UserCommonInformationResponse.builder()
                .id(projection.authorId())
                .givenName(projection.authorGivenName())
                .familyName(projection.authorFamilyName())
                .username(projection.authorUsername())
                .profilePictureUrl(File.getPath(projection.authorProfilePictureId()))
                .isFriend(projection.isFriend())
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnline())
                .build();
    }

    public UserCommonInformationResponse toCommentAuthorCommonInformationResponse(final CommentProjection projection) {
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.authorId());
        return UserCommonInformationResponse.builder()
                .id(projection.authorId())
                .givenName(projection.authorGivenName())
                .familyName(projection.authorFamilyName())
                .username(projection.authorUsername())
                .profilePictureUrl(File.getPath(projection.authorProfilePictureId()))
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnline())
                .build();
    }

    public UserCommonInformationResponse toOriginalPostAuthorCommonInformationResponse(final PostProjection projection) {
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.originalPostId());
        return UserCommonInformationResponse.builder()
                .id(projection.originalPostAuthorId())
                .givenName(projection.originalPostAuthorGivenName())
                .familyName(projection.originalPostAuthorFamilyName())
                .username(projection.originalPostAuthorUsername())
                .profilePictureUrl(File.getPath(projection.originalPostAuthorProfilePictureId()))
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnline())
                .build();
    }

    public AdminUserViewResponse toAdminUserViewResponse(final AdminUserViewProjection projection) {
        OnlineResponse online = isOnlineRedisRepository.getLastSeen(projection.userId());
        return AdminUserViewResponse.builder()
                .id(projection.userId())
                .givenName(projection.givenName())
                .familyName(projection.familyName())
                .username(projection.username())
                .profilePictureUrl(File.getPath(projection.profilePictureId()))
                .bio(projection.bio())
                .birthdate(projection.birthdate())
                .friendCount(projection.friendCount())
                .blockCount(projection.blockCount())
                .requestSentCount(projection.requestSentCount())
                .requestReceivedCount(projection.requestReceivedCount())
                .postCount(projection.postCount())
                .commentCount(projection.commentCount())
                .uploadedFileCount(projection.uploadedFileCount())
                .messageCount(projection.messageCount() - projection.callCount() )
                .callCount(projection.callCount())
                .registrationDate(projection.registrationDate())
                .email(projection.email())
                .isVerified(projection.isVerified())
                .isOnline(online.isOnline())
                .lastOnline(online.getLastOnline())
                .build();
    }
}
