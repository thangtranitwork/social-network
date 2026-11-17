package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.StringeeCallEvent;
import com.stu.socialnetworkapi.dto.response.AuthenticationResponse;
import com.stu.socialnetworkapi.dto.response.StringeeResponse;
import com.stu.socialnetworkapi.dto.response.StringeeUser;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.BlockStatus;
import com.stu.socialnetworkapi.repository.redis.InCallRepository;
import com.stu.socialnetworkapi.service.itf.BlockService;
import com.stu.socialnetworkapi.service.itf.CallService;
import com.stu.socialnetworkapi.service.itf.StringeeService;
import com.stu.socialnetworkapi.service.itf.UserService;
import com.stu.socialnetworkapi.util.JwtUtil;
import com.stu.socialnetworkapi.util.StringeeTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StringeeServiceImpl implements StringeeService {
    private final JwtUtil jwtUtil;
    private final CallService callService;
    private final UserService userService;
    private final BlockService blockService;
    private final StringeeTokenUtil stringeeTokenUtil;
    private final InCallRepository inCallRepository;

    private static final Map<String, String> success = Map.of("status", "success");

    @Override
    public AuthenticationResponse createToken() {
        return new AuthenticationResponse(stringeeTokenUtil.createAccessToken(jwtUtil.getUsernameRequiredAuthentication()));
    }

    @Override
    public List<StringeeResponse> handleAnswer(String appToPhone, int timeout, int maxConnectTime, boolean peerToPeerCall, boolean isRecord, String recordFormat, boolean fromInternal, String fromid, String toid, String projectId, String callId, boolean videocall) {
        List<StringeeResponse> sccoList = new ArrayList<>();
        User caller = userService.getUser(fromid);
        User callee = userService.getUser(toid);
        if (inCallRepository.isInCall(toid) || !BlockStatus.NORMAL.equals(blockService.getBlockStatus(caller.getUsername(), callee.getUsername()))) {
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

        connectAction.setCustomData(File.getPath(caller.getProfilePicture()));

        sccoList.add(connectAction);
        return sccoList;
    }


    @Override
    public Map<String, String> handleEvent(StringeeCallEvent event) {
        String eventType = event.type();
        String callId = event.call_id();
        if (eventType == null) {
            return success;
        }
        String callerUsername = event.from() != null ? event.from().getNumber() : null;
        String calleeUsername = event.to() != null ? event.to().getNumber() : null;
        boolean isVideoCall = event.isVideoCall();

        if (eventType.equals("stringee_call")) {
            String callStatus = event.call_status();
            if (callStatus == null) {
                return success;
            }
            switch (callStatus) {
                case "started":
                    callService.start(callId, callerUsername, calleeUsername, isVideoCall);
                    break;
                case "ringing":
                    log.debug("Call ringing - Call ID: {}", callId);
                    break;
                case "answered":
                    callService.answer(callId);
                    log.debug("Call answered - Call ID: {}", callId);
                    break;
                case "ended":
                    log.debug("Call ended - Call ID: {}", callId);
                    callService.end(callId);
                    break;
                case "failed":
                case "busy":
                case "timeout":
                    log.debug("Call {} - Call ID: {}", callStatus, callId);
                    break;
                default:
                    log.warn("Unknown call status: {} - Call ID: {}", callStatus, callId);
            }
        } else {
            log.warn("Unknown event type: {} - Call ID: {}", eventType, callId);
        }
        return success;
    }
}
