package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.AdminUserViewResponse;
import com.stu.socialnetworkapi.dto.response.UserProfileResponse;
import com.stu.socialnetworkapi.entity.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface UserService {
    User getCurrentUserRequiredAuthentication();

    User getUser(String username);

    User getUser(UUID id);

    UUID getUserId(String username);

    UUID getCurrentUserId();

    UUID getCurrentUserIdRequiredAuthentication();

    String getCurrentUsernameRequiredAuthentication();

    UserProfileResponse getUserProfile(String username);

    LocalDate updateUsername(String username);

    LocalDate updateName(String familyName, String givenName);

    LocalDate updateBirthdate(LocalDate birthdate);

    void updateBio(String bio);

    String updateProfilePicture(MultipartFile file);

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    List<AdminUserViewResponse> getUsers(Neo4jPageable pageable);
}