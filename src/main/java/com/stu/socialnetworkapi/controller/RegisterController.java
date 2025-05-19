package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.RegisterRequest;
import com.stu.socialnetworkapi.dto.request.VerifyRequest;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.service.itf.RegisterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/register")
public class RegisterController {
    private final RegisterService registerService;

    @PostMapping
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        registerService.register(request);
        return ApiResponse.success();
    }

    @PostMapping("/resend-email")
    public ApiResponse<Void> resend(
            @RequestParam
            @NotBlank(message = "EMAIL_REQUIRED")
            @Email(message = "INVALID_EMAIL")
            String email) {
        registerService.resend(email);
        return ApiResponse.success();
    }

    @PatchMapping("/verify")
    public ApiResponse<Void> verify(@Valid @RequestBody VerifyRequest request) {
        registerService.verify(request);
        return ApiResponse.success();
    }
}
