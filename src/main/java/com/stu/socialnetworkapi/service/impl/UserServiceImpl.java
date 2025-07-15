package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.projection.UserProfileProjection;
import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.AdminUserViewResponse;
import com.stu.socialnetworkapi.dto.response.UserProfileResponse;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.entity.sqlite.OnlineUserLog;
import com.stu.socialnetworkapi.enums.BlockStatus;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.UserMapper;
import com.stu.socialnetworkapi.repository.neo4j.BlockRepository;
import com.stu.socialnetworkapi.repository.neo4j.UserRepository;
import com.stu.socialnetworkapi.repository.sqlite.OnlineUserLogRepository;
import com.stu.socialnetworkapi.service.itf.FileService;
import com.stu.socialnetworkapi.service.itf.UserService;
import com.stu.socialnetworkapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final BlockRepository blockRepository;
    private final OnlineUserLogRepository onlineUserLogRepository;

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
    public String getCurrentUsernameRequiredAuthentication() {
        return jwtUtil.getUsernameRequiredAuthentication();
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
    public LocalDate updateUsername(String username) {
        if (userRepository.existsByUsername(username))
            throw new ApiException(ErrorCode.USERNAME_ALREADY_EXISTS);
        User user = getCurrentUserRequiredAuthentication();
        if (user.getNextChangeUsernameDate().isAfter(LocalDate.now()))
            throw new ApiException(ErrorCode.LESS_THAN_30_DAYS_SINCE_LAST_NAME_CHANGE);
        user.setUsername(username);
        LocalDate nextChangeUsernameDate = LocalDate.now().plusDays(User.CHANGE_USERNAME_COOLDOWN_DAY);
        user.setNextChangeUsernameDate(nextChangeUsernameDate);
        userRepository.save(user);
        return nextChangeUsernameDate;
    }

    @Override
    public LocalDate updateName(String familyName, String givenName) {
        User user = getCurrentUserRequiredAuthentication();
        if (user.getNextChangeNameDate().isAfter(LocalDate.now()))
            throw new ApiException(ErrorCode.LESS_THAN_30_DAYS_SINCE_LAST_NAME_CHANGE);
        user.setGivenName(givenName);
        user.setFamilyName(familyName);
        LocalDate nextChangeNameDate = LocalDate.now().plusDays(User.CHANGE_NAME_COOLDOWN_DAY);
        user.setNextChangeNameDate(nextChangeNameDate);
        userRepository.save(user);
        return nextChangeNameDate;
    }

    @Override
    public LocalDate updateBirthdate(LocalDate birthdate) {
        User user = getCurrentUserRequiredAuthentication();
        if (user.getNextChangeBirthdateDate().isAfter(LocalDate.now()))
            throw new ApiException(ErrorCode.LESS_THAN_30_DAYS_SINCE_LAST_BIRTHDATE_CHANGE);
        user.setBirthdate(birthdate);
        LocalDate nextChangeBirthdateDate = LocalDate.now().plusDays(User.CHANGE_BIRTHDATE_COOLDOWN_DAY);
        user.setNextChangeBirthdateDate(nextChangeBirthdateDate);
        userRepository.save(user);
        return nextChangeBirthdateDate;
    }

    @Override
    public void updateBio(String bio) {
        User user = userRepository.findById(jwtUtil.getUserIdRequiredAuthentication())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        user.setBio(bio);
        userRepository.save(user);
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
                ? fileService.upload(file)
                : null;

        if (currentPicture != null) {
            fileService.deleteFile(currentPicture);
        }
        user.setProfilePicture(newPicture);
        userRepository.save(user);
        return File.getPath(newPicture);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public List<AdminUserViewResponse> getUsers(Neo4jPageable pageable) {

        return userRepository.getAllUsers(pageable.getSkip(), pageable.getLimit())
                .stream()
                .map(userMapper::toAdminUserViewResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public List<OnlineUserLog> getOnlineUserLogs(LocalDateTime from, LocalDateTime to) {
        return onlineUserLogRepository.findByTimestampBetween(from, to);
    }

    private void validateGetUserProfile(UUID currentUserId, UUID targetUserId) {
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            if (jwtUtil.isAdmin()) return; // Admin can view everyone
            BlockStatus blockStatus = blockRepository.getBlockStatus(currentUserId, targetUserId);
            switch (blockStatus) {
                case BLOCKED:
                    throw new ApiException(ErrorCode.HAS_BLOCKED);
                case HAS_BEEN_BLOCKED:
                    throw new ApiException(ErrorCode.HAS_BEEN_BLOCKED);
                case NORMAL:
                    userRepository.increaseViewProfile(currentUserId, targetUserId);
            }
        }
    }
}