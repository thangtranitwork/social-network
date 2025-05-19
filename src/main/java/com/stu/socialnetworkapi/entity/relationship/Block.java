package com.stu.socialnetworkapi.entity.relationship;

import com.stu.socialnetworkapi.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.util.UUID;

@RelationshipProperties
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Block {
    @Id
    @GeneratedValue
    Long id;
    @Builder.Default
    UUID uuid = UUID.randomUUID();
    @TargetNode
    User target;
}
