package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.StringeeCallEvent;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.AuthenticationResponse;
import com.stu.socialnetworkapi.dto.response.StringeeResponse;
import com.stu.socialnetworkapi.service.itf.StringeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/stringee")
@RequiredArgsConstructor
public class StringeeController {
    private final StringeeService stringeeService;

    @GetMapping("/answer")
    public ResponseEntity<List<StringeeResponse>> handleAnswerUrl(
            @RequestParam(defaultValue = "false") String appToPhone,
            @RequestParam(defaultValue = "60") int timeout,
            @RequestParam(defaultValue = "-1") int maxConnectTime,
            @RequestParam(defaultValue = "true") boolean peerToPeerCall,
            @RequestParam(defaultValue = "false", name = "record") boolean isRecord,
            @RequestParam(defaultValue = "mp3") String recordFormat,
            @RequestParam boolean fromInternal,
            @RequestParam(name = "from") String fromid,
            @RequestParam(name = "to") String toid,
            @RequestParam String projectId,
            @RequestParam String callId,
            @RequestParam(defaultValue = "false") boolean videocall) {

        return ResponseEntity.ok(stringeeService.handleAnswer(appToPhone, timeout, maxConnectTime, peerToPeerCall, isRecord, recordFormat, fromInternal, fromid, toid, projectId, callId, videocall));
    }

    @PostMapping("/event")
    public ResponseEntity<Map<String, String>> handleCallEvent(@RequestBody StringeeCallEvent event) {
        // Trả về response success
        return ResponseEntity.ok(stringeeService.handleEvent(event));
    }

    @PostMapping("/create-token")
    public ApiResponse<AuthenticationResponse> getToken() {

        return ApiResponse.success(stringeeService.createToken());
    }

}