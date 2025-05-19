package com.stu.socialnetworkapi.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    ACCOUNT_NOT_FOUND(1000, "Account not found", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_VERIFIED(1001, "Account not verified", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(1002, "Account locked", HttpStatus.LOCKED),
    AUTHENTICATION_FAILED(1003, "Authentication failed", HttpStatus.UNAUTHORIZED),
    INVALID_PASSWORD(1004, "Invalid password", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1005, "Invalid email", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1006, "Email is required", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1007, "Password is required", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_NOT_FOUND(1008, "Verification code not found", HttpStatus.NOT_FOUND),
    VERIFICATION_CODE_NOT_MATCHED_OR_EXPIRED(1009, "Verification code not matched or expired", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_REQUIRED(1010, "Refresh token is required", HttpStatus.BAD_REQUEST),
    INVALID_OR_EXPIRED_REFRESH_TOKEN(1011, "Invalid or expired refresh token", HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_EXISTS(1012, "Account already exists", HttpStatus.CONFLICT),
    INVALID_TOKEN(1013, "Invalid token", HttpStatus.BAD_REQUEST),
    EXPIRED_TOKEN(1014, "Expired token", HttpStatus.BAD_REQUEST),

    GIVEN_NAME_REQUIRED(2000, "Given name is required", HttpStatus.BAD_REQUEST),
    FAMILY_NAME_REQUIRED(2001, "Family name is required", HttpStatus.BAD_REQUEST),
    BIRTHDATE_REQUIRED(2002, "Birth date is required", HttpStatus.BAD_REQUEST),
    AGE_MUST_BE_AT_LEAST_16(2003, "This user is under age", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED(2004, "Email is not verified", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(2005, "User not found", HttpStatus.NOT_FOUND),
    LESS_THAN_30_DAYS_SINCE_LAST_BIRTHDATE_CHANGE(2006, "Less than 30 days since last date of birth change", HttpStatus.BAD_REQUEST),
    LESS_THAN_30_DAYS_SINCE_LAST_NAME_CHANGE(2007, "Less than 30 days since last name change", HttpStatus.BAD_REQUEST),
    LESS_THAN_30_DAYS_SINCE_LAST_USERNAME_CHANGE(2008, "Less than 30 days since last username change", HttpStatus.BAD_REQUEST),
    INVALID_GIVEN_NAME_LENGTH(2009, "Given name is too long", HttpStatus.BAD_REQUEST),
    INVALID_FAMILY_NAME_LENGTH(2010, "Family name is too long", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(2010, "Invalid username", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(2011, "Username is required", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS(2012, "Username already exists", HttpStatus.BAD_REQUEST),
    PROFILE_PICTURE_REQUIRED(2013, "Profile picture is required", HttpStatus.BAD_REQUEST),
    COVER_PICTURE_REQUIRED(2014, "Cover Picture is required", HttpStatus.BAD_REQUEST),

    STORAGE_INITIALIZATION_ERROR(3000, "Storage initialization error", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_REQUIRED(3001, "File is required", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(3002, "Invalid file type", HttpStatus.BAD_REQUEST),
    UPLOAD_FILE_FAILED(3003, "Upload file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND(3004, "File not found", HttpStatus.NOT_FOUND),
    DELETE_FILE_FAILED(3005, "Delete file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    LOAD_FILE_FAILED(3006, "Load file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    REQUIRED_IMAGE_FILE(3007, "Required image file", HttpStatus.BAD_REQUEST),
    INVALID_FILE_SIZE(3008, "Invalid file size", HttpStatus.BAD_REQUEST),

    CAN_NOT_MAKE_SELF_REQUEST(4000, "Can't make self request", HttpStatus.BAD_REQUEST),
    SENT_ADD_FRIEND_REQUEST_FAILED(4001, "Sent add friend request failed", HttpStatus.BAD_REQUEST),
    ADD_FRIEND_REQUEST_SENT_LIMIT_REACHED(4002, "Add friend request sent limit reached", HttpStatus.BAD_REQUEST),
    ADD_FRIEND_REQUEST_RECEIVED_LIMIT_REACHED(4003, "Add friend request received limit reached", HttpStatus.BAD_REQUEST),
    REQUEST_NOT_FOUND(4004, "Request not found", HttpStatus.NOT_FOUND),
    ACCEPT_REQUEST_FAILED(4005, "Accept request failed", HttpStatus.BAD_REQUEST),
    HAS_BLOCKED(4006, "Blocked", HttpStatus.BAD_REQUEST),
    HAS_BEEN_BLOCKED(4007, "Has been blocked", HttpStatus.BAD_REQUEST),
    NOT_BLOCK(4008, "Not blocked", HttpStatus.BAD_REQUEST),
    BLOCK_LIMIT_REACHED(4009, "Block limit reached", HttpStatus.BAD_REQUEST),
    CAN_NOT_BLOCK_YOURSELF(4010, "Can't block yourself", HttpStatus.BAD_REQUEST),
    BLOCK_NOT_FOUND(4011, "Block not found", HttpStatus.BAD_REQUEST),
    FRIEND_NOT_FOUND(4012, "Friend not found", HttpStatus.BAD_REQUEST),

    UNAUTHORIZED(9994, "Unauthorized", HttpStatus.UNAUTHORIZED),
    INVALID_INPUT(9995, "Invalid input", HttpStatus.BAD_REQUEST),
    INVALID_UUID(9996, "Invalid friendId", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(9997, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    NO_RESOURCE_FOUND(9998, "Resource not found", HttpStatus.NOT_FOUND),
    UNKNOWN_ERROR(9999, "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);

    int code;
    String message;
    HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
