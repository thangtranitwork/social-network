package com.stu.socialnetworkapi.entity;

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
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Report {
    @Id
    @GeneratedValue(GeneratedValue.UUIDGenerator.class)
    UUID id;
    String content;
    ObjectType type;
    UUID objectId;
    boolean processed;
    @Builder.Default
    ZonedDateTime createdAt = ZonedDateTime.now();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "REPORTED", direction = Relationship.Direction.INCOMING)
    User reporter;

    public static final int MAX_CONTENT_LENGTH = 10000;
}
