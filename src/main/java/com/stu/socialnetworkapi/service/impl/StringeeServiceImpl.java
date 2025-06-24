package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.StringeeCallEvent;
import com.stu.socialnetworkapi.dto.response.AuthenticationResponse;
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
import com.stu.socialnetworkapi.service.itf.UserService;
import com.stu.socialnetworkapi.util.JwtUtil;
import com.stu.socialnetworkapi.util.StringeeTokenUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class StringeeServiceImpl implements StringeeService {
    private final JwtUtil jwtUtil;
    private final ChatService chatService;
    private final UserService userService;
    private final BlockService blockService;
    private final CallRepository callRepository;
    private final StringeeTokenUtil stringeeTokenUtil;
    private final InCallRedisRepository inCallRedisRepository;

    @Override
    public AuthenticationResponse createToken() {
        return new AuthenticationResponse(stringeeTokenUtil.createAccessToken(jwtUtil.getUsernameRequiredAuthentication()));
    }

    @Override
    public List<StringeeResponse> handleAnswer(String appToPhone, int timeout, int maxConnectTime, boolean peerToPeerCall, boolean isRecord, String recordFormat, boolean fromInternal, String fromid, String toid, String projectId, String callId, boolean videocall) {
        List<StringeeResponse> sccoList = new ArrayList<>();
        User caller = userService.getUser(fromid);
        User callee = userService.getUser(toid);
        if (inCallRedisRepository.isInCall(toid) || !BlockStatus.NORMAL.equals(blockService.getBlockStatus(caller.getId(), callee.getId()))) {
            return sccoList;
        }

        if (isRecord) {
            StringeeResponse recordAction = new StringeeResponse();
            recordAction.setAction("record");
            recordAction.setEventUrl("");
            recordAction.setFormat(recordFormat);
            sccoList.add(recordAction);
        }

        boolean isAppToPhone = "true".equalsIgnoreCase(appToPhone);

        // Create connect action
        StringeeResponse connectAction = new StringeeResponse();
        connectAction.setAction("connect");

        StringeeUser from = new StringeeUser();
        from.setType(fromInternal ? "internal" : "external");
        from.setNumber(caller.getUsername());
        from.setAlias(caller.getFullName());
        connectAction.setFrom(from);

        StringeeUser to = new StringeeUser();
        to.setType(isAppToPhone ? "external" : "internal");
        to.setNumber(callee.getUsername());
        to.setAlias(callee.getFullName());
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
        String callerUsername = event.from().getNumber();
        String calleeUsername = event.to().getNumber();
        User caller = userService.getUser(callerUsername);
        User callee = userService.getUser(calleeUsername);

        Chat chat = chatService.getOrCreateDirectChat(caller, callee);

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
                    inCallRedisRepository.call(callerUsername, calleeUsername);
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
                    inCallRedisRepository.endCall(callerUsername, calleeUsername);
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
