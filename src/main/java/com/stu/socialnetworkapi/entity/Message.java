package com.stu.socialnetworkapi.entity;

import com.stu.socialnetworkapi.enums.ChatAction;
import com.stu.socialnetworkapi.enums.MessageType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Node
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Message {
    @Id
    @GeneratedValue(GeneratedValue.UUIDGenerator.class)
    UUID id;
    String content;
    @Builder.Default
    ZonedDateTime sentAt = ZonedDateTime.now();
    MessageType type;
    ChatAction action;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "SENT", direction = Relationship.Direction.INCOMING)
    User sender;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "AFFECTED_TO", direction = Relationship.Direction.OUTGOING)
    User target;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "HAS_MESSAGE", direction = Relationship.Direction.INCOMING)
    Chat chat;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "READ", direction = Relationship.Direction.INCOMING)
    List<User> reader;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "DELETED", direction = Relationship.Direction.INCOMING)
    List<User> deleted;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "ATTACH_FILE", direction = Relationship.Direction.OUTGOING)
    File attachedFile;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "HAS_HISTORY", direction = Relationship.Direction.OUTGOING)
    List<History> history;

    public static final int MAX_CONTENT_LENGTH = 10000;
    public static final int MINUTES_TO_DELETE_MESSAGE = 15;
    public static final int MINUTES_TO_EDIT_MESSAGE = 15;
}
