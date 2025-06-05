package com.stu.socialnetworkapi.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyCode {
    @Id
    UUID code;
    boolean verified;
    LocalDateTime expiryTime;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "HAS_VERIFY_CODE", direction = Relationship.Direction.INCOMING)
    Account account;
}
