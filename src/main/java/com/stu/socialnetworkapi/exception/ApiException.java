package com.stu.socialnetworkapi.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(force = true)
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Map<String, Object> attributes;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.attributes = new HashMap<>();
    }

    public ApiException(ErrorCode errorCode, Map<String, Object> attributes) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.attributes = attributes;
    }
}
