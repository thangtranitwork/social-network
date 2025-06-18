package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.StringeeCallEvent;
import com.stu.socialnetworkapi.dto.response.StringeeResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StringeeService {
    List<StringeeResponse> handleAnswer(
            String appToPhone,
            int timeout,
            int maxConnectTime,
            boolean peerToPeerCall,
            boolean isRecord,
            String recordFormat,
            boolean fromInternal,
            UUID fromid,
            UUID toid,
            String projectId,
            String callId,
            boolean videocall);

    Map<String, String> handleEvent(StringeeCallEvent event);
}
