package com.stu.socialnetworkapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stu.socialnetworkapi.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    int code;
    String message;
    String timestamp;
    T body;

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(T body) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("OK")
                .timestamp(ZonedDateTime.now().toString())
                .body(body)
                .build();
    }

    public static ApiResponse<Void> error(ErrorCode e) {
        return ApiResponse.<Void>builder()
                .code(e.getCode())
                .message(e.getMessage())
                .timestamp(ZonedDateTime.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode e, T body) {
        return ApiResponse.<T>builder()
                .code(e.getCode())
                .message(e.getMessage())
                .timestamp(ZonedDateTime.now().toString())
                .body(body)
                .build();
    }

    public ResponseEntity<ApiResponse<T>> toResponseEntity(HttpStatus status) {
        return ResponseEntity.status(status)
                .body(this);
    }

}
