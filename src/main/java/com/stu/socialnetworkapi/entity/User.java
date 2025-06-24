package com.stu.socialnetworkapi.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Node
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    // Properties section
    @Id
    UUID id;
    String givenName;
    String familyName;
    String username;
    LocalDate birthdate;
    String bio;

    int friendCount;
    int blockCount;
    int requestSentCount;
    int requestReceivedCount;

    @Builder.Default
    ZonedDateTime createdAt = ZonedDateTime.now();
    @Builder.Default
    LocalDate nextChangeNameDate = LocalDate.now();
    @Builder.Default
    LocalDate nextChangeBirthdateDate = LocalDate.now();
    @Builder.Default
    LocalDate nextChangeUsernameDate = LocalDate.now();

    // Relationship section
    @Relationship(type = "HAS_PROFILE_PICTURE", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    File profilePicture;

    @Relationship(type = "FRIEND", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<User> friends;

    // Constant section
    public static final int MAX_FRIEND_COUNT = 100;
    public static final int MAX_BLOCK_COUNT = 100;
    public static final int MAX_SENT_REQUEST_COUNT = 100;
    public static final int MAX_RECEIVED_REQUEST_COUNT = 100;
    public static final int CHANGE_NAME_COOLDOWN_DAY = 30;
    public static final int CHANGE_USERNAME_COOLDOWN_DAY = 30;
    public static final int CHANGE_BIRTHDATE_COOLDOWN_DAY = 30;
    public static final int MAX_GIVEN_NAME_LENGTH = 64;
    public static final int MAX_FAMILY_NAME_LENGTH = 64;
    public static final int MAX_USERNAME_LENGTH = 32;

    //Method
    public String getFullName() {
        return givenName + " " + familyName;
    }
}
