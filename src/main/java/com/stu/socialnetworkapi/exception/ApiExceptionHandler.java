package com.stu.socialnetworkapi.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.WebSocketExceptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {
    private final SimpMessagingTemplate messagingTemplate;

    @MessageExceptionHandler(WebSocketException.class)
    public void handleWebSocketException(WebSocketException exception, Principal principal) {

        ErrorCode errorCode = exception.getErrorCode();
        WebSocketExceptionResponse response = WebSocketExceptionResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        String userId = principal.getName();
        if (userId != null && !userId.isBlank() && !userId.equals("anonymousUser"))
            messagingTemplate.convertAndSend(WebSocketChannelPrefix.USER_WEBSOCKET_ERROR_CHANNEL_PREFIX + "/" + userId, response);
        else log.error("WebSocket error: {}", response);
    }

    @ExceptionHandler(ApiException.class)
    private ResponseEntity<ApiResponse<Map<String, Object>>> handlerAppException(ApiException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        Map<String, Object> attributes = exception.getAttributes();
        return ApiResponse.error(errorCode, attributes)
                .toResponseEntity(errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    private ResponseEntity<ApiResponse<String>> handlerAppException(MethodArgumentTypeMismatchException exception) {
        String param = exception.getName();
        String value = exception.getValue() != null ? exception.getValue().toString() : "";
        String message = String.format("Invalid value '%s' for parameter '%s'.", value, param);
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;
        return ApiResponse.error(errorCode, message)
                .toResponseEntity(errorCode.getHttpStatus());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    private ResponseEntity<ApiResponse<Void>> handlerMaxUploadSizeExceededException() {
        ErrorCode errorCode = ErrorCode.INVALID_FILE_SIZE;
        return ApiResponse.error(errorCode)
                .toResponseEntity(errorCode.getHttpStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    private ResponseEntity<ApiResponse<Void>> handlerHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        if (exception.getCause() instanceof InvalidFormatException formatEx) {
            Class<?> targetType = formatEx.getTargetType();

            // Xử lý lỗi khi deserialize UUID chẳng hạn
            if (targetType == UUID.class) {
                ErrorCode invalidKey = ErrorCode.INVALID_UUID;
                return ApiResponse.error(invalidKey)
                        .toResponseEntity(invalidKey.getHttpStatus());
            }
        }
        ErrorCode invalidInput = ErrorCode.INVALID_INPUT;
        return ApiResponse.error(invalidInput)
                .toResponseEntity(invalidInput.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<ApiResponse<Void>> handlerMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String key = Objects.requireNonNull(exception.getFieldError())
                .getDefaultMessage();
        ErrorCode errorCode = ErrorCode.valueOf(key);
        return ApiResponse.error(errorCode)
                .toResponseEntity(errorCode.getHttpStatus());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    private ResponseEntity<ApiResponse<Void>> handlerHandlerMethodValidationException(HandlerMethodValidationException exception) {
        String key = Objects.requireNonNull(exception.getAllErrors().get(0).getDefaultMessage());
        ErrorCode errorCode = ErrorCode.valueOf(key);
        return ApiResponse.error(errorCode)
                .toResponseEntity(errorCode.getHttpStatus());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    private ResponseEntity<ApiResponse<String>> handlerNoResourceFoundException(NoResourceFoundException exception) {
        ErrorCode errorCode = ErrorCode.NO_RESOURCE_FOUND;
        return ApiResponse.error(ErrorCode.NO_RESOURCE_FOUND, exception.getResourcePath())
                .toResponseEntity(errorCode.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<ApiResponse<Void>> handlerException(Exception exception) {
        log.error(exception.getMessage(), exception);
        ErrorCode errorCode = ErrorCode.UNKNOWN_ERROR;
        return ApiResponse.error(errorCode)
                .toResponseEntity(errorCode.getHttpStatus());
    }
}
