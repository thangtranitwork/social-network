package com.stu.socialnetworkapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        ApiResponse.error(errorCode))
        );
        response.getWriter().flush();
    }
}
