package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import com.stu.socialnetworkapi.dto.response.MessageResponse;
import com.stu.socialnetworkapi.entity.Call;
import com.stu.socialnetworkapi.entity.Chat;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.MessageType;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.exception.WebSocketException;
import com.stu.socialnetworkapi.mapper.CallMapper;
import com.stu.socialnetworkapi.repository.CallRepository;
import com.stu.socialnetworkapi.repository.InCallRedisRepository;
import com.stu.socialnetworkapi.service.itf.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CallServiceImpl implements CallService {
    private final CallMapper callMapper;
    private final UserServiceImpl userService;
    private final ChatServiceImpl chatService;
    private final CallRepository callRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final InCallRedisRepository inCallRedisRepository;

    @Override
    public void init(String callee) {
        String caller = userService.getCurrentUsernameRequiredAuthentication();
        if (inCallRedisRepository.isInCall(caller)) {
            throw new ApiException(ErrorCode.ALREADY_IN_CALL);
        }
        if (inCallRedisRepository.isInCall(callee)) {
            throw new ApiException(ErrorCode.TARGET_ALREADY_IN_IN_CALL);
        }
        inCallRedisRepository.prepare(caller, callee);
    }

    @Override
    public void start(String callId, String callerUsername, String calleeUsername, boolean isVideoCall) {
        try {
            boolean anyInCall = inCallRedisRepository.isInCall(callerUsername) || inCallRedisRepository.isInCall(calleeUsername);
            boolean preparedForCall = inCallRedisRepository.isPreparedForCall(callerUsername, calleeUsername);
            if (anyInCall || !preparedForCall) {
                throw new WebSocketException(ErrorCode.NOT_READY_FOR_CALL);
            }

            User caller = userService.getUser(callerUsername);
            User callee = userService.getUser(calleeUsername);

            Chat chat = chatService.getOrCreateDirectChat(caller, callee);
            Call call = Call.builder()
                    .chat(chat)
                    .sender(caller)
                    .callId(callId)
                    .type(MessageType.CALL)
                    .isVideoCall(isVideoCall)
                    .callAt(ZonedDateTime.now())
                    .build();
            callRepository.save(call);
            MessageResponse response = callMapper.toMessageResponse(call);
            messagingTemplate.convertAndSend(WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX + chat.getId(), response);
            messagingTemplate.convertAndSend(WebSocketChannelPrefix.MESSAGE_CHANNEL_PREFIX + callee.getId(), response);
            inCallRedisRepository.call(callerUsername, calleeUsername, callId, caller.getId(), callee.getId());
        } catch (ApiException e) {
            throw new WebSocketException(e.getErrorCode());
        }
    }

    @Override
    public void answer(String callId) {
        Call call = callRepository.findByCallId(callId)
                .orElseThrow(() -> new ApiException(ErrorCode.CALL_NOT_FOUND));
        call.setAnswered(true);
        callRepository.save(call);
    }

    @Override
    public void reject(String callId) {
        Call call = callRepository.findByCallId(callId)
                .orElseThrow(() -> new ApiException(ErrorCode.CALL_NOT_FOUND));
        validateIsReceiver(call);
        call.setRejected(true);
        call.setEndAt(ZonedDateTime.now());
        callRepository.save(call);
    }

    @Override
    public void end(String callId) {
        Call endCall = callRepository.findByCallId(callId)
                .orElseThrow(() -> new WebSocketException(ErrorCode.CALL_NOT_FOUND));
        endCall.setEndAt(ZonedDateTime.now());
        callRepository.save(endCall);
        inCallRedisRepository.endCall(callId);
    }

    @Override
    public void end(UUID userId) {
        User user = userService.getUser(userId);
        inCallRedisRepository.endCallByMemberUsername(user.getUsername());
    }

    private void validateIsReceiver(Call call) {
        UUID userId = userService.getCurrentUserIdRequiredAuthentication();
        boolean inChat = call.getChat().getMembers().stream()
                .anyMatch(member -> member.getId().equals(userId));
        boolean isReceiver = !call.getSender().getId().equals(userId);
        if (!isReceiver || !inChat) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }
}
