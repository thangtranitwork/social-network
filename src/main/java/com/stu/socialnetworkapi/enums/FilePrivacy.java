package com.stu.socialnetworkapi.enums;

public enum FilePrivacy {
    PUBLIC,
    FRIEND,
    PRIVATE,
    IN_POST,
    IN_CHAT;

    public static FilePrivacy toFilePrivacy(PostPrivacy postPrivacy) {
        return FilePrivacy.valueOf(postPrivacy.name());
    }
}
