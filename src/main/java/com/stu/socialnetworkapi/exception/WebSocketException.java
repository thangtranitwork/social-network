package com.stu.socialnetworkapi.exception;

import lombok.Getter;

@Getter
public class WebSocketException extends ApiException {
    public WebSocketException(ErrorCode errorCode) {
        super(errorCode);
    }
}
