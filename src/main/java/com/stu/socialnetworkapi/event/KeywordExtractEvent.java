package com.stu.socialnetworkapi.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordExtractEvent {
    private UUID postId;
    private String content;
    private boolean isUpdate;
}
