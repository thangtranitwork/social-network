package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.BlockResponse;
import com.stu.socialnetworkapi.service.itf.BlockService;
import com.stu.socialnetworkapi.validation.annotation.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/blocks")
public class BlockController {
    private final BlockService blockService;

    @GetMapping
    public ApiResponse<List<BlockResponse>> getBlockedUsers(Pageable pageable) {
        return ApiResponse.success(blockService.getBlockedUsers(pageable));
    }

    @PostMapping("/{username}")
    public ApiResponse<Void> block(@PathVariable @Username String username) {
        blockService.block(username);
        return ApiResponse.success();
    }

    @DeleteMapping("/{username}")
    public ApiResponse<Void> unblock(@PathVariable @Username String username) {
        blockService.unblock(username);
        return ApiResponse.success();
    }
}
