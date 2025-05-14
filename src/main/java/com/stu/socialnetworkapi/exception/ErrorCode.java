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

    STORAGE_INITIALIZATION_ERROR(3000, "Storage initialization error", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_REQUIRED(3001, "File is required", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(3002, "Invalid file type", HttpStatus.BAD_REQUEST),
    UPLOAD_FILE_FAILED(3003, "Upload file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND(3004, "File not found", HttpStatus.NOT_FOUND),

    INVALID_INPUT(3005, "Invalid input", HttpStatus.BAD_REQUEST),
    INVALID_UUID(9996, "Invalid uuid", HttpStatus.BAD_REQUEST),
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
