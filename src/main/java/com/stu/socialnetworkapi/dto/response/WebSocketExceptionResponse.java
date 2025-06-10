package com.stu.socialnetworkapi.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketExceptionResponse {
    int code;
    String message;
}
