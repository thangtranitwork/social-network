package com.stu.socialnetworkapi.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.ZonedDateTime;

@Node
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Call extends Message {
    String callId;
    ZonedDateTime callAt;
    ZonedDateTime endAt;
    boolean isAnswered;
    boolean isRejected;
    boolean isVideoCall;
}
