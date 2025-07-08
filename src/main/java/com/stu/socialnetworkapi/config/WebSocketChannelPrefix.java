package com.stu.socialnetworkapi.config;

public final class WebSocketChannelPrefix {

    private WebSocketChannelPrefix() {
        // prevent instantiation
    }

    public static final String NOTIFICATION_CHANNEL_PREFIX = "/notifications"; // /notifications/{userid}
    public static final String CHAT_CHANNEL_PREFIX = "/chat";                   // /chat/{chatid}
    public static final String MESSAGE_CHANNEL_PREFIX = "/message";            // /message/{userid}
    public static final String USER_WEBSOCKET_ERROR_CHANNEL_PREFIX = "/errors";
    public static final String ONLINE_CHANNEL_PREFIX = "/online";
    public static final String TYPING_CHANNEL_PREFIX = "/typing"; // /typing/{chatid}
}
