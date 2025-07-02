package com.stu.socialnetworkapi.entity;

import com.stu.socialnetworkapi.enums.PostPrivacy;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Node
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Post {
    @Id
    @GeneratedValue(GeneratedValue.UUIDGenerator.class)
    UUID id;
    String content;
    int likeCount;
    int shareCount;
    int commentCount;
    @Builder.Default
    ZonedDateTime createdAt = ZonedDateTime.now();
    ZonedDateTime updatedAt;
    ZonedDateTime deletedAt;
    PostPrivacy privacy;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "POSTED", direction = Relationship.Direction.INCOMING)
    User author;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "LIKED", direction = Relationship.Direction.INCOMING)
    List<User> liker;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "ATTACH_FILES", direction = Relationship.Direction.OUTGOING)
    List<File> attachedFiles;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "SHARED", direction = Relationship.Direction.OUTGOING)
    Post originalPost;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "HAS_KEYWORDS", direction = Relationship.Direction.OUTGOING)
    Set<Keyword> keywords;

    public static final int MAX_CONTENT_LENGTH = 10000;
    public static final int MAX_ATTACH_FILES = 10;

    public boolean isSharedPost() {
        return originalPost != null;
    }
}
