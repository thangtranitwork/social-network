package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.AdminUserViewResponse;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.UserProfileResponse;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.service.itf.UserService;
import com.stu.socialnetworkapi.validation.annotation.Age;
import com.stu.socialnetworkapi.validation.annotation.ImageFile;
import com.stu.socialnetworkapi.validation.annotation.OnlyLetter;
import com.stu.socialnetworkapi.validation.annotation.Username;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{username}")
    public ApiResponse<UserProfileResponse> getUser(@PathVariable String username) {
        return ApiResponse.success(userService.getUserProfile(username));
    }

    @GetMapping
    public ApiResponse<List<AdminUserViewResponse>> getAll(Neo4jPageable pageable) {
        return ApiResponse.success(userService.getUsers(pageable));
    }

    @PatchMapping("/update-bio")
    public ApiResponse<Void> updateBio(@RequestParam(required = false) String bio) {
        userService.updateBio(bio);
        return ApiResponse.success();
    }

    @PatchMapping("/update-birthdate")
    public ApiResponse<LocalDate> updateBirthdate(
            @Valid
            @RequestParam(required = false)
            @Age
            @NotNull(message = "BIRTHDATE_REQUIRED")
            LocalDate birthdate) {
        return ApiResponse.success(userService.updateBirthdate(birthdate));
    }

    @PatchMapping("/update-name")
    public ApiResponse<LocalDate> updateName(
            @RequestParam(required = false)
            @NotBlank(message = "GIVEN_NAME_REQUIRED")
            @OnlyLetter
            @Length(max = User.MAX_GIVEN_NAME_LENGTH, message = "INVALID_GIVEN_NAME_LENGTH")
            String givenName,
            @RequestParam(required = false)
            @NotBlank(message = "FAMILY_NAME_REQUIRED")
            @OnlyLetter
            @Length(max = User.MAX_FAMILY_NAME_LENGTH, message = "INVALID_FAMILY_NAME_LENGTH")
            String familyName) {
        return ApiResponse.success(userService.updateName(familyName, givenName));
    }

    @PatchMapping("/update-username")
    public ApiResponse<LocalDate> updateUsername(
            @RequestParam(required = false)
            @NotBlank(message = "USERNAME_REQUIRED")
            @Username
            String username
    ) {
        return ApiResponse.success(userService.updateUsername(username));
    }

    @PatchMapping("/update-profile-picture")
    public ApiResponse<String> updateProfilePicture(
            @ImageFile
            MultipartFile file
    ) {
        return ApiResponse.success(userService.updateProfilePicture(file));
    }
}