package com.stu.socialnetworkapi.entity;

import com.stu.socialnetworkapi.entity.relationship.Friend;
import com.stu.socialnetworkapi.entity.relationship.Request;
import com.stu.socialnetworkapi.enums.PostPrivacy;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
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
    int postStoredCount;

    @Builder.Default
    LocalDate nextChangeNameDate = LocalDate.now();
    @Builder.Default
    LocalDate nextChangeBirthdateDate = LocalDate.now();
    @Builder.Default
    LocalDate nextChangeUsernameDate = LocalDate.now();
    @Builder.Default
    boolean showFriends = true;
    @Builder.Default
    boolean allowFriendRequest = true;
    @Builder.Default
    PostPrivacy defaultPostPrivacy = PostPrivacy.PUBLIC;

    // Relationship section
    @Relationship(type = "HAS_PROFILE_PICTURE", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    File profilePicture;
    @Relationship(type = "HAS_COVER_PICTURE", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    File coverPicture;
    @Relationship(type = "REQUEST", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Request> sentRequests;
    @Relationship(type = "REQUEST", direction = Relationship.Direction.INCOMING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Request> receivedRequests;
    @Relationship(type = "FRIEND", direction = Relationship.Direction.INCOMING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Friend> friends;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "PINNED", direction = Relationship.Direction.OUTGOING)
    Post pinnedPost;
    @Relationship(type = "STORED", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Post> storedPosts;
    @Relationship(type = "IS_MEMBER_OF", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Chat> chats;

    // Constant section
    public static final int MAX_FRIEND_COUNT = 5000;
    public static final int MAX_BLOCK_COUNT = 5000;
    public static final int MAX_SENT_REQUEST_COUNT = 5000;
    public static final int MAX_RECEIVED_REQUEST_COUNT = 5000;
    public static final int MAX_STORED_POST_COUNT = 500;
    public static final int CHANGE_NAME_COOLDOWN_DAY = 30;
    public static final int CHANGE_USERNAME_COOLDOWN_DAY = 30;
    public static final int CHANGE_BIRTHDATE_COOLDOWN_DAY = 30;
    public static final int MAX_GIVEN_NAME_LENGTH = 64;
    public static final int MAX_FAMILY_NAME_LENGTH = 64;
    public static final int MAX_USERNAME_LENGTH = 32;

    //Method
}
