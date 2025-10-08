package com.stu.socialnetworkapi.entity;

import com.stu.socialnetworkapi.enums.NotificationAction;
import com.stu.socialnetworkapi.enums.ObjectType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.ZonedDateTime;
import java.util.UUID;

@Node
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    @GeneratedValue(GeneratedValue.UUIDGenerator.class)
    UUID id;
    NotificationAction action;
    boolean isRead;
    ObjectType targetType;
    UUID targetId;
    String shortenedContent;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "BY_USER", direction = Relationship.Direction.OUTGOING)
    User creator;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "HAS_NOTIFICATION", direction = Relationship.Direction.INCOMING)
    User receiver;
    @Builder.Default
    ZonedDateTime sentAt = ZonedDateTime.now();

    public Notification(Notification other) {
        this.id = other.id;
        this.action = other.action;
        this.targetType = other.targetType;
        this.targetId = other.targetId;
        this.creator = other.creator;
        this.receiver = other.receiver;
        this.shortenedContent = other.shortenedContent;
        this.sentAt = other.sentAt;
        this.isRead = other.isRead;
    }

    public static final int DAY_ALIVE = 30;
}
