package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.UpdatePasswordRequest;
import com.stu.socialnetworkapi.dto.request.VerifyRequest;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.service.itf.UpdatePasswordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/v1/update-password", "/v1/forgot-password"})
public class UpdatePasswordController {
    private final UpdatePasswordService updatePasswordService;

    @PostMapping({"", "/resend-email"})
    public ApiResponse<Void> sendEmail(
            @RequestParam
            @NotBlank(message = "EMAIL_REQUIRED")
            @Email(message = "INVALID_EMAIL")
            String email,
            @RequestHeader(name = "X-Continue-Page") String continueUrl
    ) {
        updatePasswordService.send(email, continueUrl);
        return ApiResponse.success();
    }

    @PatchMapping("/verify")
    public ApiResponse<Void> verify(@Valid @RequestBody VerifyRequest request) {
        updatePasswordService.verify(request);
        return ApiResponse.success();
    }

    @PatchMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody UpdatePasswordRequest request) {
        updatePasswordService.updatePassword(request);
        return ApiResponse.success();
    }

}
