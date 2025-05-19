package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.UserProfileResponse;
import com.stu.socialnetworkapi.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

public interface UserService {
    User getCurrentUserRequiredAuthentication();
    User getUser(String username);
    User getUser(UUID id);
    UUID getCurrentUserId();
    UUID getCurrentUserIdRequiredAuthentication();
    UserProfileResponse getUserProfile(String username);
    LocalDate updateUsername(String username);
    LocalDate updateName(String familyName, String givenName);
    LocalDate updateBirthdate(LocalDate birthdate);
    void updateBio(String bio);
    String updateProfilePicture(MultipartFile file);
    String updateCoverPicture(MultipartFile file);
}
