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
    VERIFICATION_CODE_REQUIRED(1015, "Verification code is required", HttpStatus.BAD_REQUEST),
    ACCOUNT_VERIFIED(1016, "Account verified", HttpStatus.CONFLICT),

    GIVEN_NAME_REQUIRED(2000, "Given name is required", HttpStatus.BAD_REQUEST),
    FAMILY_NAME_REQUIRED(2001, "Family name is required", HttpStatus.BAD_REQUEST),
    BIRTHDATE_REQUIRED(2002, "Birth date is required", HttpStatus.BAD_REQUEST),
    AGE_MUST_BE_AT_LEAST_16(2003, "This user is under age", HttpStatus.BAD_REQUEST),
    INVALID_GIVEN_NAME_LENGTH(2004, "Given name is too long", HttpStatus.BAD_REQUEST),
    INVALID_FAMILY_NAME_LENGTH(2005, "Family name is too long", HttpStatus.BAD_REQUEST),
    LESS_THAN_30_DAYS_SINCE_LAST_BIRTHDATE_CHANGE(2006, "Less than 30 days since last date of birth change", HttpStatus.BAD_REQUEST),
    LESS_THAN_30_DAYS_SINCE_LAST_NAME_CHANGE(2007, "Less than 30 days since last name change", HttpStatus.BAD_REQUEST),
    LESS_THAN_30_DAYS_SINCE_LAST_USERNAME_CHANGE(2008, "Less than 30 days since last username change", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED(2009, "Email is not verified", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(2010, "User not found", HttpStatus.NOT_FOUND),
    INVALID_USERNAME(2010, "Invalid username", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(2011, "Username is required", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS(2012, "Username already exists", HttpStatus.BAD_REQUEST),
    PROFILE_PICTURE_REQUIRED(2013, "Profile picture is required", HttpStatus.BAD_REQUEST),
    NOTHING_CHANGED(2014, "Nothing changed", HttpStatus.BAD_REQUEST),

    STORAGE_INITIALIZATION_ERROR(3000, "Storage initialization error", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_REQUIRED(3001, "File is required", HttpStatus.BAD_REQUEST),
    INVALID_FILE(3002, "Invalid file", HttpStatus.BAD_REQUEST),
    UPLOAD_FILE_FAILED(3003, "Upload file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND(3004, "File not found", HttpStatus.NOT_FOUND),
    DELETE_FILE_FAILED(3005, "Delete file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    LOAD_FILE_FAILED(3006, "Load file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    REQUIRED_IMAGE_FILE(3007, "Required image file", HttpStatus.BAD_REQUEST),
    INVALID_FILE_SIZE(3008, "Invalid file size", HttpStatus.BAD_REQUEST),
    LIST_CONTAINS_INVALID_FILE(3009, "List contains invalid file", HttpStatus.BAD_REQUEST),
    REQUIRED_IMAGE_OR_VIDEO_FILE(3010, "Required image or video file", HttpStatus.BAD_REQUEST),

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

    POST_CONTENT_AND_ATTACH_FILES_BOTH_EMPTY(5000, "Post content and attach files cannot be empty", HttpStatus.BAD_REQUEST),
    INVALID_POST_CONTENT_LENGTH(5001, "Invalid post content length", HttpStatus.BAD_REQUEST),
    INVALID_NUMBER_OF_POST_ATTACHMENTS(5002, "Invalid number of post attachments", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND(5003, "Post not found", HttpStatus.BAD_REQUEST),
    ONLY_PUBLIC_POST_CAN_BE_SHARED(5005, "Only public post can be shared", HttpStatus.BAD_REQUEST),
    PRIVACY_UNCHANGED(5006, "Privacy unchanged", HttpStatus.BAD_REQUEST),
    INVALID_DELETE_ATTACHMENT(5007, "Invalid delete attachment", HttpStatus.BAD_REQUEST),
    POST_CONTENT_UNCHANGED(5008, "Post content unchanged", HttpStatus.BAD_REQUEST),
    LIKED_POST(5009, "Liked post", HttpStatus.BAD_REQUEST),
    NOT_LIKED_POST(5010, "Not liked post", HttpStatus.BAD_REQUEST),
    DELETED_POST(5011, "Deleted post", HttpStatus.BAD_REQUEST),

    COMMENT_NOT_FOUND(6000, "Comment not found", HttpStatus.NOT_FOUND),
    COMMENT_CONTENT_AND_ATTACH_FILE_BOTH_EMPTY(6001, "Comment content and attach file cannot be empty", HttpStatus.BAD_REQUEST),
    INVALID_COMMENT_CONTENT_LENGTH(6002, "Invalid comment content length", HttpStatus.BAD_REQUEST),
    POST_ID_REQUIRED(6003, "Post id is required", HttpStatus.BAD_REQUEST),
    ORIGINAL_COMMENT_ID_REQUIRED(6004, "Original comment id is required", HttpStatus.BAD_REQUEST),
    LIKED_COMMENT(6005, "Liked comment", HttpStatus.BAD_REQUEST),
    NOT_LIKED_COMMENT(6006, "Not liked comment", HttpStatus.BAD_REQUEST),
    CAN_NOT_REPLY_REPLIED_COMMENT(6007, "Can't reply replied comment", HttpStatus.BAD_REQUEST),
    COMMENT_CONTENT_UNCHANGED(6008, "Comment content unchanged", HttpStatus.BAD_REQUEST),

    CHAT_NOT_FOUND(7000, "Chat not found", HttpStatus.BAD_REQUEST),
    MESSAGE_USERNAME_REQUIRED(7001, "Username is required", HttpStatus.BAD_REQUEST),
    INVALID_MESSAGE_CONTENT_LENGTH(7002, "Invalid message content length", HttpStatus.BAD_REQUEST),
    CHAT_ID_AND_USER_ID_BOTH_EMPTY(7003, "Chat id and user id cannot be empty", HttpStatus.BAD_REQUEST),
    MESSAGE_NOT_FOUND(7004, "Message not found", HttpStatus.BAD_REQUEST),
    CAN_NOT_DELETE_MESSAGE(7005, "Can't delete message", HttpStatus.BAD_REQUEST),
    TEXT_MESSAGE_CONTENT_REQUIRED(7006, "Text message content is required", HttpStatus.BAD_REQUEST),
    TEXT_MESSAGE_CONTENT_UNCHANGED(7007, "Text message content unchanged", HttpStatus.BAD_REQUEST),
    CAN_NOT_EDIT_FILE_MESSAGE(7008, "Can't edit file message", HttpStatus.BAD_REQUEST),
    CAN_NOT_EDIT_MESSAGE(7009, "Can't edit message", HttpStatus.BAD_REQUEST),
    FILE_MESSAGE_REQUIRED(7010, "File message is required", HttpStatus.BAD_REQUEST),
    ALREADY_IN_CALL(7011, "You are already in call", HttpStatus.BAD_REQUEST),
    TARGET_ALREADY_IN_IN_CALL(7012, "Target is already in call", HttpStatus.BAD_REQUEST),
    CALL_NOT_FOUND(7013, "Call not found", HttpStatus.NOT_FOUND),
    NOT_READY_FOR_CALL(7014, "Not ready for call", HttpStatus.BAD_REQUEST),

    SEARCH_QUERY_REQUIRED(9000, "Search query is required", HttpStatus.BAD_REQUEST),
    INVALID_WEBSOCKET_CHANNEL(9992, "Invalid websocket channel", HttpStatus.BAD_REQUEST),
    ONLY_LETTER_ACCEPTED(9993, "Only letter accepted", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(9994, "Unauthorized", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(9995, "Invalid input", HttpStatus.BAD_REQUEST),
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
