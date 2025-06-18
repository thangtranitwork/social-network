package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.dto.response.StringeeUser;

public record StringeeCallEvent(
        String actorType,
        boolean original,
        boolean isVideoCall,
        boolean peerToPeer,
        long timestamp_ms,
        String type,
        String call_id,
        String actor,
        String callCreatedReason,
        String call_status,
        String event_id,
        String project_id,
        int serial,
        String request_from_user_id,
        String account_sid,
        StringeeUser from,
        StringeeUser to,
        String clientCustomData
) {
}
