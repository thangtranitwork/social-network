package com.stu.socialnetworkapi.entity;

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
public class Comment {
    @Id
    @GeneratedValue(GeneratedValue.UUIDGenerator.class)
    UUID id;
    String content;
    int likeCount;
    int replyCount;
    @Builder.Default
    ZonedDateTime createdAt = ZonedDateTime.now();
    @Builder.Default
    ZonedDateTime updatedAt = ZonedDateTime.now();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "HAS_COMMENT", direction = Relationship.Direction.INCOMING)
    Post post;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "COMMENTED", direction = Relationship.Direction.INCOMING)
    User author;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "LIKED", direction = Relationship.Direction.INCOMING)
    List<User> liker;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "REPLIED", direction = Relationship.Direction.OUTGOING)
    Comment originalComment;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "REPLIED", direction = Relationship.Direction.INCOMING)
    List<Comment> repliedComments;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "ATTACH_FILE", direction = Relationship.Direction.OUTGOING)
    File attachedFile;

    public static final int MAX_CONTENT_LENGTH = 10000;

    public boolean isRepliedComment() {
        return originalComment != null;
    }
}
