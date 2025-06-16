package com.stu.socialnetworkapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StringeeResponse {

    // Action: connect, reject, play, etc.
    private String action;

    // From user ID
    private UUID from;

    // To user ID
    private UUID to;

    // Type: internal, external, pstn
    private String type;

    // Additional fields for other actions
    private String url;      // For play action
    private Integer loop;    // For play action
    private Integer maxTime; // For connect action (in seconds)
    private String record;   // For record action

    // Constructor for simple connect action
    public static StringeeResponse connect(UUID from, UUID to) {
        return StringeeResponse.builder()
                .action("connect")
                .from(from)
                .to(to)
                .type("internal")
                .build();
    }

    // Constructor for reject action
    public static StringeeResponse reject() {
        return StringeeResponse.builder()
                .action("reject")
                .build();
    }

    // Constructor for play action
    public static StringeeResponse play(String audioUrl, Integer loop) {
        return StringeeResponse.builder()
                .action("play")
                .url(audioUrl)
                .loop(loop)
                .build();
    }
}