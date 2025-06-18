package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.service.itf.FriendService;
import com.stu.socialnetworkapi.validation.annotation.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/friends")
public class FriendController {
    private final FriendService friendService;

    @GetMapping("/{username}")
    public ApiResponse<List<UserCommonInformationResponse>> getFriends(@PathVariable @Username String username, Neo4jPageable pageable) {
        return ApiResponse.success(friendService.getFriends(username, pageable));
    }

    @GetMapping("/suggested")
    public ApiResponse<List<UserCommonInformationResponse>> getSuggestedFriends(Neo4jPageable pageable) {
        return ApiResponse.success(friendService.getSuggestedFriends(pageable));
    }

    @GetMapping("/mutual-friends/{username}")
    public ApiResponse<List<UserCommonInformationResponse>> getMutualFriends(@PathVariable @Username String username, Neo4jPageable pageable) {
        return ApiResponse.success(friendService.getMutualFriends(username, pageable));
    }

    @DeleteMapping("/{username}")
    public ApiResponse<Void> deleteFriend(@PathVariable @Username String username) {
        friendService.unfriend(username);
        return ApiResponse.success();
    }
}
