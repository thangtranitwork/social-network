package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.service.itf.RequestService;
import com.stu.socialnetworkapi.validation.annotation.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/friend-request")
public class RequestController {
    private final RequestService requestService;

    @GetMapping("/sent-requests")
    public ApiResponse<List<UserCommonInformationResponse>> getSentRequest(Neo4jPageable pageable) {
        return ApiResponse.success(requestService.getSentRequests(pageable));
    }

    @GetMapping("/received-requests")
    public ApiResponse<List<UserCommonInformationResponse>> getReceivedRequest(Neo4jPageable pageable) {
        return ApiResponse.success(requestService.getReceivedRequests(pageable));
    }

    @PostMapping("/send/{username}")
    public ApiResponse<Void> sentAddFriendRequest(@PathVariable @Username String username) {
        requestService.sendAddFriendRequest(username);
        return ApiResponse.success();
    }

    @PostMapping("/accept/{username}")
    public ApiResponse<Void> acceptRequest(@PathVariable @Username String username) {
        requestService.acceptRequest(username);
        return ApiResponse.success();
    }

    @DeleteMapping("/delete/{username}")
    public ApiResponse<Void> deleteRequest(@PathVariable @Username String username) {
        requestService.deleteRequest(username);
        return ApiResponse.success();
    }
}
