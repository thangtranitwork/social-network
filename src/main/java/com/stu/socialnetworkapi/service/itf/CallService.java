package com.stu.socialnetworkapi.service.itf;

public interface CallService {
    void init(String callee);

    void start(String callId, String callerUsername, String calleeUsername, boolean isVideoCall);

    void answer(String callId);

    void reject(String callId);

    void end(String callId, String callerUsername, String calleeUsername);
}
