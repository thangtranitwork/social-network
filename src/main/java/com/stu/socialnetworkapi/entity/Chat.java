package com.stu.socialnetworkapi.entity;

import com.stu.socialnetworkapi.enums.ChatType;
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
public class Chat {
    @Id
    @GeneratedValue(GeneratedValue.UUIDGenerator.class)
    UUID id;
    String name;
    ChatType type;
    int memberCount;
    @Builder.Default
    ZonedDateTime createdAt = ZonedDateTime.now();
    boolean canMemberChangeChatInfo;
    boolean canMemberAddNewMember;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "IS_MEMBER_OF", direction = Relationship.Direction.INCOMING)
    List<User> members;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "IS_LEADER_OF", direction = Relationship.Direction.INCOMING)
    User leader;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "HAS_CHAT_IMAGE", direction = Relationship.Direction.OUTGOING)
    File image;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "HAS_MESSAGE", direction = Relationship.Direction.OUTGOING)
    List<Message> messages;

    public static final int MAX_MEMBER_COUNT = 100;

    public boolean isGroupChat() {
        return ChatType.GROUP.equals(type);
    }
}
