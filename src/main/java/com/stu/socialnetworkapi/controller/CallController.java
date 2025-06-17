package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.AuthenticationResponse;
import com.stu.socialnetworkapi.service.itf.UserService;
import com.stu.socialnetworkapi.util.StringeeTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/call")
public class CallController {
    private final UserService userService;
    private final StringeeTokenUtil tokenUtil;

    @PostMapping("/create-token")
    public ResponseEntity<AuthenticationResponse> getToken() {
        UUID userId = userService.getCurrentUserIdRequiredAuthentication();
        String token = tokenUtil.createAccessToken(userId);
        return ResponseEntity.ok(new AuthenticationResponse(token));
    }

}
