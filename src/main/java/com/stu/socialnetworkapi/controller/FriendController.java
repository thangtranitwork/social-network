package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.FriendResponse;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.service.itf.FriendService;
import com.stu.socialnetworkapi.validation.annotation.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/friends")
public class FriendController {
    private final FriendService friendService;

    @GetMapping("/{username}")
    public ApiResponse<List<FriendResponse>> getFriends(@PathVariable @Username String username ,Pageable pageable) {
        return ApiResponse.success(friendService.getFriends(username, pageable));
    }

    @GetMapping("/suggested")
    public ApiResponse<List<UserCommonInformationResponse>> getSuggestedFriends(Pageable pageable) {
        return ApiResponse.success(friendService.getSuggestedFriends(pageable));
    }

    @GetMapping("/mutual-friends/{username}")
    public ApiResponse<List<UserCommonInformationResponse>> getMutualFriends(@PathVariable @Username String username, Pageable pageable) {
        return ApiResponse.success(friendService.getMutualFriends(username, pageable));
    }

    @DeleteMapping("/{uuid}")
    public ApiResponse<Void> deleteFriend(@PathVariable UUID uuid) {
        friendService.unfriend(uuid);
        return ApiResponse.success();
    }
}
