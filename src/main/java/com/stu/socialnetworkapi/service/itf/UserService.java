package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.UpdateInfoRequest;
import com.stu.socialnetworkapi.dto.response.UserProfileResponse;
import com.stu.socialnetworkapi.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {
    User getCurrentUserRequiredAuthentication();

    User getUser(String username);

    User getUser(UUID id);

    UUID getUserId(String username);

    UUID getCurrentUserId();

    UUID getCurrentUserIdRequiredAuthentication();

    UserProfileResponse getUserProfile(String username);

    String updateProfilePicture(MultipartFile file);

    UserProfileResponse updateInfo(UpdateInfoRequest request);
}
