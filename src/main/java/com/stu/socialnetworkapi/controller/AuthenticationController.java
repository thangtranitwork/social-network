package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.LoginRequest;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.AuthenticationResponse;
import com.stu.socialnetworkapi.service.itf.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        return ApiResponse.success(authenticationService
                .authenticate(request, response));
    }

    @PostMapping("/login-admin")
    public ApiResponse<AuthenticationResponse> authenticateAdmin(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        return ApiResponse.success(authenticationService
                .authenticateAdmin(request, response));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@CookieValue(name = "token", required = false) String token) {
        return ApiResponse.success(authenticationService.refresh(token));
    }

    @DeleteMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = "token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authenticationService.logout(refreshToken, response);
        return ApiResponse.success();
    }


}
