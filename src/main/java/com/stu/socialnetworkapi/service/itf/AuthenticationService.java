package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.LoginRequest;
import com.stu.socialnetworkapi.dto.response.AuthenticationResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    AuthenticationResponse authenticate(LoginRequest request, HttpServletResponse response);

    AuthenticationResponse refresh(String token);

    void logout(String token, HttpServletResponse response);
}
