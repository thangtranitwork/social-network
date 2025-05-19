package com.stu.socialnetworkapi.entity;

import com.stu.socialnetworkapi.enums.AccountRole;
import lombok.*;
import lombok.experimental.FieldDefaults;
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
public class Account {
    @Id
    UUID id;
    String email;
    String password;
    @Builder.Default
    AccountRole role = AccountRole.USER;
    boolean verified;
    boolean isLocked;
    ZonedDateTime lockoutTime;
    int failedAttempts;

    @ToString.Exclude
    @Relationship(type = "HAS_VERIFY_CODE", direction = Relationship.Direction.OUTGOING)
    VerifyCode verifyCode;
    @ToString.Exclude
    @Relationship(type = "HAS_INFO", direction = Relationship.Direction.OUTGOING)
    User user;
}
