package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.service.itf.CallService;
import com.stu.socialnetworkapi.validation.annotation.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/call")
public class CallController {
    private final CallService callService;

    @GetMapping("/init/{callee}")
    public ApiResponse<Void> init(@PathVariable @Username String callee) {
        callService.init(callee);
        return ApiResponse.success();
    }

    @DeleteMapping("/reject/{callId}")
    public ApiResponse<Void> reject(@PathVariable String callId) {
        callService.reject(callId);
        return ApiResponse.success();
    }
}
