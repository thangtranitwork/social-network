package com.stu.socialnetworkapi.util;

public class TruncateText {

    private static final int LIMIT = 50;
    private TruncateText() {}
    public static String truncateByWord(String content) {
        if (content == null || content.length() <= LIMIT) {
            return content;
        }

        String truncated = content.substring(0, LIMIT);

        int lastSpaceIndex = truncated.lastIndexOf(' ');

        if (lastSpaceIndex != -1) {
            truncated = truncated.substring(0, lastSpaceIndex);
        }

        return truncated + "...";
    }
}
