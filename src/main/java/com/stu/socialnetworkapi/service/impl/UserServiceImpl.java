package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.projection.UserProfileProjection;
import com.stu.socialnetworkapi.dto.request.UpdateInfoRequest;
import com.stu.socialnetworkapi.dto.response.UserProfileResponse;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.BlockStatus;
import com.stu.socialnetworkapi.enums.FilePrivacy;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.UserMapper;
import com.stu.socialnetworkapi.repository.BlockRepository;
import com.stu.socialnetworkapi.repository.UserRepository;
import com.stu.socialnetworkapi.service.itf.FileService;
import com.stu.socialnetworkapi.service.itf.UserService;
import com.stu.socialnetworkapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final BlockRepository blockRepository;

    @Override
    public User getCurrentUserRequiredAuthentication() {
        return userRepository.findById(jwtUtil.getUserIdRequiredAuthentication())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public UUID getUserId(String username) {
        return userRepository.getUserIdByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public UUID getCurrentUserId() {
        return jwtUtil.getUserId();
    }

    @Override
    public UUID getCurrentUserIdRequiredAuthentication() {
        return jwtUtil.getUserIdRequiredAuthentication();
    }

    @Override
    public UserProfileResponse getUserProfile(String username) {
        UUID currentUserId = jwtUtil.getUserId();
        UserProfileProjection targetUser = userRepository.findProfileByUsername(username, currentUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        validateGetUserProfile(currentUserId, targetUser.userId());
        return userMapper.toUserProfileResponse(targetUser);
    }

    @Override
    public String updateProfilePicture(MultipartFile file) {
        User user = getCurrentUserRequiredAuthentication();
        File currentPicture = user.getProfilePicture();
        if (currentPicture == null && file == null) {
            throw new ApiException(ErrorCode.PROFILE_PICTURE_REQUIRED);
        }
        // Then at least one of them is null
        File newPicture = file != null
                ? fileService.upload(file, FilePrivacy.PUBLIC)
                : null;

        if (currentPicture != null) {
            fileService.deleteFile(currentPicture);
        }
        user.setProfilePicture(newPicture);
        userRepository.save(user);
        return File.getPath(newPicture);
    }

    @Override
    public UserProfileResponse updateInfo(UpdateInfoRequest request) {
        User user = getCurrentUserRequiredAuthentication();
        boolean hasAnyChange = processChangeName(request, user)
                || processChangeBio(request, user)
                || processChangeUsername(request, user)
                || processChangeBirthdate(request, user);
        if (!hasAnyChange) throw new ApiException(ErrorCode.PROFILE_PICTURE_REQUIRED);
        userRepository.save(user);
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .givenName(user.getGivenName())
                .familyName(user.getFamilyName())
                .birthdate(user.getBirthdate())
                .build();

    }

    private boolean processChangeBirthdate(UpdateInfoRequest request, User user) {
        boolean hasBirthdateChange = user.getBirthdate().equals(request.birthdate());
        if (hasBirthdateChange) {
            if (user.getNextChangeBirthdateDate().isAfter(LocalDate.now()))
                throw new ApiException(ErrorCode.LESS_THAN_30_DAYS_SINCE_LAST_BIRTHDATE_CHANGE);
            user.setBirthdate(request.birthdate());
            LocalDate nextChangeBirthdateDate = LocalDate.now().plusDays(User.CHANGE_BIRTHDATE_COOLDOWN_DAY);
            user.setNextChangeBirthdateDate(nextChangeBirthdateDate);
        }
        return hasBirthdateChange;
    }

    private boolean processChangeUsername(UpdateInfoRequest request, User user) {
        boolean hasUsernameChange = user.getUsername().equals(request.username());
        if (hasUsernameChange) {
            if (userRepository.existsByUsername(request.username()))
                throw new ApiException(ErrorCode.USERNAME_ALREADY_EXISTS);
            if (user.getNextChangeUsernameDate().isAfter(LocalDate.now()))
                throw new ApiException(ErrorCode.LESS_THAN_30_DAYS_SINCE_LAST_NAME_CHANGE);
            user.setUsername(request.username());
            LocalDate nextChangeUsernameDate = LocalDate.now().plusDays(User.CHANGE_USERNAME_COOLDOWN_DAY);
            user.setNextChangeUsernameDate(nextChangeUsernameDate);
        }
        return hasUsernameChange;
    }

    private boolean processChangeBio(UpdateInfoRequest request, User user) {
        boolean hasBioChange = user.getBio().equals(request.bio());
        user.setBio(request.bio());
        return hasBioChange;
    }

    private boolean processChangeName(UpdateInfoRequest request, User user) {
        String familyNameAfterTrim = request.familyName() != null
                ? request.familyName().trim()
                : "";
        String givenNameAfterTrim = request.givenName() != null
                ? request.givenName().trim()
                : "";
        if (familyNameAfterTrim.isEmpty()) {
            throw new ApiException(ErrorCode.FAMILY_NAME_REQUIRED);
        }
        if (givenNameAfterTrim.isEmpty()) {
            throw new ApiException(ErrorCode.GIVEN_NAME_REQUIRED);
        }
        boolean hasNameChange = user.getFamilyName().equals(familyNameAfterTrim) || user.getGivenName().equals(givenNameAfterTrim);
        if (hasNameChange) {
            if (user.getNextChangeNameDate().isAfter(LocalDate.now()))
                throw new ApiException(ErrorCode.LESS_THAN_30_DAYS_SINCE_LAST_NAME_CHANGE);
            user.setFamilyName(familyNameAfterTrim);
            user.setGivenName(givenNameAfterTrim);
            LocalDate nextChangeNameDate = LocalDate.now().plusDays(User.CHANGE_NAME_COOLDOWN_DAY);
            user.setNextChangeNameDate(nextChangeNameDate);
        }
        return hasNameChange;
    }

    private void validateGetUserProfile(UUID currentUserId, UUID targetUserId) {
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            BlockStatus blockStatus = blockRepository.getBlockStatus(currentUserId, targetUserId);
            if (Objects.requireNonNull(blockStatus) == BlockStatus.BLOCKED) {
                throw new ApiException(ErrorCode.HAS_BLOCKED);
            } else if (blockStatus == BlockStatus.HAS_BEEN_BLOCKED) {
                throw new ApiException(ErrorCode.HAS_BEEN_BLOCKED);
            }
        }
    }
}
