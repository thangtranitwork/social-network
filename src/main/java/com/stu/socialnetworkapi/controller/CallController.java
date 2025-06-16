package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.config.WebSocketConfig;
import com.stu.socialnetworkapi.dto.response.AuthenticationResponse;
import com.stu.socialnetworkapi.dto.response.CallResponse;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.CallAction;
import com.stu.socialnetworkapi.mapper.UserMapper;
import com.stu.socialnetworkapi.service.itf.UserService;
import com.stu.socialnetworkapi.util.StringeeTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/call")
public class CallController {
    private final UserMapper userMapper;
    private final UserService userService;
    private final StringeeTokenUtil tokenUtil;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public void call(UUID callee) {
        User caller = userService.getCurrentUserRequiredAuthentication();
        System.out.println("User " + caller.getUsername() + " calling " + callee);
        CallResponse response = CallResponse.builder()
                .action(CallAction.INCOMING_CALL)
                .caller(userMapper.toUserCommonInformationResponse(caller))
                .build();
        messagingTemplate.convertAndSend(WebSocketConfig.CALL_CHANNEL_PREFIX + "/" + callee, response);
    }

    @PostMapping("/create-token")
    public ResponseEntity<AuthenticationResponse> getToken() {
        UUID userId = userService.getCurrentUserIdRequiredAuthentication();
        String token = tokenUtil.createAccessToken(userId);
        System.out.println("User " + userId + " created token " + token);
        return ResponseEntity.ok(new AuthenticationResponse(token));
    }

}
