package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.RequestResponse;
import com.stu.socialnetworkapi.service.itf.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/friend-request")
public class RequestController {
    private final RequestService requestService;

    @GetMapping("/sent-requests")
    public ApiResponse<List<RequestResponse>> getSentRequest(Pageable pageable) {
        return ApiResponse.success(requestService.getSentRequests(pageable));
    }

    @GetMapping("/received-requests")
    public ApiResponse<List<RequestResponse>> getReceivedRequest(Pageable pageable) {
        return ApiResponse.success(requestService.getReceivedRequests(pageable));
    }

    @PostMapping("/{username}")
    public ApiResponse<Void> sentAddFriendRequest(@PathVariable String username) {
        requestService.sendAddFriendRequest(username);
        return ApiResponse.success();
    }

    @PostMapping("/accept/{uuid}")
    public ApiResponse<Void> acceptRequest(@PathVariable UUID uuid) {
        requestService.acceptRequest(uuid);
        return ApiResponse.success();
    }

    @DeleteMapping("/delete/{uuid}")
    public ApiResponse<Void> deleteRequest(@PathVariable UUID uuid) {
        requestService.deleteRequest(uuid);
        return ApiResponse.success();
    }
}
