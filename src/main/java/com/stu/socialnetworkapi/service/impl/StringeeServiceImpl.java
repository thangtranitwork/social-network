package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.StringeeCallEvent;
import com.stu.socialnetworkapi.dto.response.StringeeResponse;
import com.stu.socialnetworkapi.dto.response.StringeeUser;
import com.stu.socialnetworkapi.entity.Call;
import com.stu.socialnetworkapi.entity.Chat;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.BlockStatus;
import com.stu.socialnetworkapi.enums.MessageType;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.CallRepository;
import com.stu.socialnetworkapi.repository.InCallRedisRepository;
import com.stu.socialnetworkapi.service.itf.BlockService;
import com.stu.socialnetworkapi.service.itf.ChatService;
import com.stu.socialnetworkapi.service.itf.StringeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class StringeeServiceImpl implements StringeeService {
    private final ChatService chatService;
    private final BlockService blockService;
    private final CallRepository callRepository;
    private final InCallRedisRepository inCallRedisRepository;

    @Override
    public List<StringeeResponse> handleAnswer(String appToPhone, int timeout, int maxConnectTime, boolean peerToPeerCall, boolean isRecord, String recordFormat, boolean fromInternal, UUID fromid, UUID toid, String projectId, String callId, boolean videocall) {
        List<StringeeResponse> sccoList = new ArrayList<>();

        // Cần websocket để thông báo
        if (inCallRedisRepository.isInCall(toid) || !BlockStatus.NORMAL.equals(blockService.getBlockStatus(fromid, toid))) {
            return sccoList;
        }

        if (isRecord) {
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
        return sccoList;
    }


    @Override
    public Map<String, String> handleEvent(StringeeCallEvent event) {
        String eventType = event.type();
        String callId = event.call_id();
        UUID callerId = UUID.fromString(event.from().getNumber());
        UUID calleeId = UUID.fromString(event.to().getNumber());
        Chat chat = chatService.getOrCreateDirectChat(callerId, calleeId);
        User caller = chat.getMembers().stream()
                .filter(member -> member.getId().equals(callerId))
                .findFirst()
                .orElse(null);

        if (eventType == null) {
            return Map.of("status", "success");
        }

        if (eventType.equals("stringee_call")) {
            String callStatus = event.call_status();
            switch (callStatus) {
                case "started":
                    Call call = Call.builder()
                            .chat(chat)
                            .sender(caller)
                            .callId(callId)
                            .type(MessageType.CALL)
                            .isVideoCall(true)
                            .callAt(ZonedDateTime.now())
                            .build();
                    callRepository.save(call);
                    inCallRedisRepository.call(callerId, calleeId);
                    break;
                case "ringing":
                    System.out.println("📞 Cuộc gọi đang đổ chuông - Call ID: " + callId);
                    break;
                case "answered":
                    Call answeredCall = callRepository.findByCallId(callId)
                            .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND));
                    answeredCall.setAnswered(true);
                    answeredCall.setAnswerAt(ZonedDateTime.now());
                    callRepository.save(answeredCall);
                    System.out.println("✅ Cuộc gọi được trả lời - Call ID: " + callId);
                    break;
                case "ended":
                    // reject sẽ vào kết thúc luôn, hủy cũng z
                    System.out.println("🔚 Cuộc gọi kết thúc - Call ID: " + callId);
                    Call endCall = callRepository.findByCallId(callId)
                            .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND));
                    endCall.setEndAt(ZonedDateTime.now());
                    callRepository.save(endCall);
                    inCallRedisRepository.endCall(callerId, calleeId);
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
        } else {
            System.out.println("❓ Event không xác định: " + eventType + " - Call ID: " + callId);
        }
        return Map.of("status", "success");
    }
}
