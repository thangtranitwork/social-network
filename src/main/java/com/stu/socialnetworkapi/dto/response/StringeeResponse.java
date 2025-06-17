package com.stu.socialnetworkapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StringeeResponse {
    private String action;
    private String eventUrl;
    private String format;
    private StringeeUser from;
    private StringeeUser to;
    private String customData;
    private int timeout;
    private int maxConnectTime;
    private boolean peerToPeerCall;
}

