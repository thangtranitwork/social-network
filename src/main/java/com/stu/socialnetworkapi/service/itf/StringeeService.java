package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.StringeeCallEvent;
import com.stu.socialnetworkapi.dto.response.AuthenticationResponse;
import com.stu.socialnetworkapi.dto.response.StringeeResponse;

import java.util.List;
import java.util.Map;

public interface StringeeService {
    List<StringeeResponse> handleAnswer(
            String appToPhone,
            int timeout,
            int maxConnectTime,
            boolean peerToPeerCall,
            boolean isRecord,
            String recordFormat,
            boolean fromInternal,
            String fromid,
            String toid,
            String projectId,
            String callId,
            boolean videocall);

    Map<String, String> handleEvent(StringeeCallEvent event);

    AuthenticationResponse createToken();
}
