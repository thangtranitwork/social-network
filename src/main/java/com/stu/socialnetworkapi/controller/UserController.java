package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.UpdateInfoRequest;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.UserProfileResponse;
import com.stu.socialnetworkapi.service.itf.UserService;
import com.stu.socialnetworkapi.validation.annotation.ImageFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{username}")
    public ApiResponse<UserProfileResponse> getUser(@PathVariable String username) {
        return ApiResponse.success(userService.getUserProfile(username));
    }

    @PatchMapping("/update-profile-picture")
    public ApiResponse<String> updateProfilePicture(
            @ImageFile
            MultipartFile file
    ) {
        return ApiResponse.success(userService.updateProfilePicture(file));
    }

    @PutMapping("/update-info")
    public ApiResponse<Void> updateInfo(@Valid @RequestBody UpdateInfoRequest request) {
        userService.updateInfo(request);
        return ApiResponse.success();
    }
}
