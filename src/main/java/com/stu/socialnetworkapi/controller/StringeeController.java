package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.StringeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/stringee")
@RequiredArgsConstructor
public class StringeeController {

    @GetMapping("/answer")
    public ResponseEntity<List<StringeeResponse>> handleAnswerUrl(Map<String, Object> payload) {
        System.out.println("=== Stringee Answer URL Called ===");
        System.out.println("Payload: " + payload);

        try {
            // Lấy thông tin từ payload
            String fromStr = payload.get("from").toString();
            String toStr = payload.get("to").toString();

            System.out.println("From: " + fromStr);
            System.out.println("To: " + toStr);

            // Tạo response với action "connect" để kết nối cuộc gọi
            StringeeResponse response = StringeeResponse.builder()
                    .action("connect")
                    .from(UUID.fromString(fromStr))
                    .to(UUID.fromString(toStr))
                    .type("internal")
                    .build();

            List<StringeeResponse> responseList = List.of(response);
            System.out.println("Response: " + responseList);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(responseList);

        } catch (Exception e) {
            System.err.println("Error processing answer URL: " + e.getMessage());
            e.printStackTrace();

            // Trả về response reject nếu có lỗi
            StringeeResponse errorResponse = StringeeResponse.builder()
                    .action("reject")
                    .build();

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(List.of(errorResponse));
        }
    }

    @PostMapping("/event")
    public ResponseEntity<Map<String, String>> handleCallEvent(@RequestBody Map<String, Object> payload) {
        System.out.println("=== Stringee Event Called ===");
        System.out.println("Event payload: " + payload);

        String eventType = (String) payload.get("event");
        String callId = (String) payload.get("callId");

        System.out.println("Event Type: " + eventType);
        System.out.println("Call ID: " + callId);

        switch (eventType) {
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
                System.out.println("❓ Event không xác định: " + eventType + " - Call ID: " + callId);
        }

        // Trả về response success
        return ResponseEntity.ok(Map.of("status", "success"));
    }
}