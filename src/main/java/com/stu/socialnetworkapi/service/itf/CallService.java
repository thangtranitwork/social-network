package com.stu.socialnetworkapi.service.itf;

import java.util.UUID;

public interface CallService {
    void init(String callee);

    void start(String callId, String callerUsername, String calleeUsername, boolean isVideoCall);

    void answer(String callId);

    void reject(String callId);

    void end(String callId);

    void end(UUID userId);
}
