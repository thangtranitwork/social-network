package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.StringeeResponse;
import com.stu.socialnetworkapi.dto.response.StringeeUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/stringee")
@RequiredArgsConstructor
public class StringeeController {

    @GetMapping("/answer")
    public ResponseEntity<List<StringeeResponse>> handleAnswerUrl(
            @RequestParam(defaultValue = "false") String appToPhone,
            @RequestParam(defaultValue = "60") int timeout,
            @RequestParam(defaultValue = "-1") int maxConnectTime,
            @RequestParam(defaultValue = "true") boolean peerToPeerCall,
            @RequestParam(defaultValue = "false") boolean record,
            @RequestParam(defaultValue = "mp3") String recordFormat,
            @RequestParam boolean fromInternal,
            @RequestParam(name = "from") UUID fromid,
            @RequestParam(name = "to") UUID toid,
            @RequestParam String projectId,
            @RequestParam String callId,
            @RequestParam(defaultValue = "false") boolean videocall,
            HttpServletRequest request) {
        List<StringeeResponse> sccoList = new ArrayList<>();

        // Add record action if recording is enabled
        if (record) {
            StringeeResponse recordAction = new StringeeResponse();
            recordAction.setAction("record");
            recordAction.setEventUrl("");
            recordAction.setFormat(recordFormat);
            sccoList.add(recordAction);
        }

        // Determine call type based on appToPhone parameter
        boolean isAppToPhone = false;
        if ("true".equalsIgnoreCase(appToPhone)) {
            isAppToPhone = true;
        } else if ("auto".equalsIgnoreCase(appToPhone)) {
            isAppToPhone = false;
        }

        // Create connect action
        StringeeResponse connectAction = new StringeeResponse();
        connectAction.setAction("connect");

        StringeeUser from = new StringeeUser();
        from.setType(fromInternal ? "internal" : "external");
        from.setNumber(fromid.toString());
        from.setAlias(fromid.toString());
        connectAction.setFrom(from);

        StringeeUser to = new StringeeUser();
        to.setType(isAppToPhone ? "external" : "internal");
        to.setNumber(toid.toString());
        to.setAlias(toid.toString());
        connectAction.setTo(to);

        connectAction.setTimeout(timeout);
        connectAction.setMaxConnectTime(maxConnectTime);
        connectAction.setPeerToPeerCall(peerToPeerCall);

        sccoList.add(connectAction);
        return ResponseEntity.ok(sccoList);
    }

    @PostMapping("/event")
    public ResponseEntity<Map<String, String>> handleCallEvent(@RequestBody Map<String, Object> payload) {
        System.out.println("=== Stringee Event Called ===");
        System.out.println("Event payload: " + payload);

        // Use "type" instead of "event" and "call_id" instead of "callId"
        String eventType = (String) payload.get("type");
        String callId = (String) payload.get("call_id");

        System.out.println("Event Type: " + eventType);
        System.out.println("Call ID: " + callId);

        if (eventType == null) {
            return ResponseEntity.ok(Map.of("status", "success"));
        }

        switch (eventType) {
            case "stringee_call":
                // Handle specific call statuses based on call_status
                String callStatus = (String) payload.get("call_status");
                switch (callStatus) {
                    case "ringing":
                        System.out.println("📞 Cuộc gọi đang đổ chuông - Call ID: " + callId);
                        break;
                    case "answered":
                        System.out.println("✅ Cuộc gọi được trả lời - Call ID: " + callId);
                        break;
                    case "ended":
                        System.out.println("🔚 Cuộc gọi kết thúc - Call ID: " + callId);
                        // Có thể lưu thống kê cuộc gọi vào database ở đây
                        break;
                    case "failed":
                        System.out.println("❌ Cuộc gọi thất bại - Call ID: " + callId);
                        break;
                    case "busy":
                        System.out.println("📵 Máy bận - Call ID: " + callId);
                        break;
                    case "timeout":
                        System.out.println("⏰ Hết thời gian chờ - Call ID: " + callId);
                        break;
                    default:
                        System.out.println("❓ Call status không xác định: " + callStatus + " - Call ID: " + callId);
                }
                break;
            default:
                System.out.println("❓ Event không xác định: " + eventType + " - Call ID: " + callId);
        }

        // Trả về response success
        return ResponseEntity.ok(Map.of("status", "success"));
    }
}