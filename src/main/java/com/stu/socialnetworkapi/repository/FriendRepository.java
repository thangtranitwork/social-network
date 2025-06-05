package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.projection.FriendProjection;
import com.stu.socialnetworkapi.dto.projection.UserProjection;
import com.stu.socialnetworkapi.entity.relationship.Friend;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FriendRepository extends Neo4jRepository<Friend, Long> {
    @Query("""
             MATCH (user:User {id: $userId})-[friend:FRIEND]->(target:User)
             OPTIONAL MATCH (target)-[:HAS_PROFILE_PICTURE]->(profile: File)
             RETURN friend.uuid AS friendId,
                    friend.createdAt AS createdAt,
                    target.id AS userId,
                    target.givenName AS givenName,
                    target.familyName AS familyName,
                    target.username AS username,
                    profile.id AS profilePictureId
            SKIP $skip LIMIT $limit
            """)
    List<FriendProjection> getFriends(UUID userId, Pageable pageable);

    @Query("""
            MATCH (user:User {id: $userId})-[friend:FRIEND {uuid: $friendId}]->()
            RETURN COUNT(friend) > 0
            """)
    boolean canUnfriend(UUID friendId, UUID userId);

    @Query("""
            MATCH (user:User)-[friend:FRIEND {uuid: $friendId}]->(target:User)
            OPTIONAL MATCH (target)-[reverseFriend:FRIEND]->(user)
            SET user.friendCount = CASE
                                      WHEN user.friendCount > 0 THEN user.friendCount - 1
                                      ELSE 0
                                    END,
                target.friendCount = CASE
                                      WHEN target.friendCount > 0 THEN target.friendCount - 1
                                      ELSE 0
                                    END
            DELETE friend, reverseFriend
            """)
    void unfriend(UUID friendId);

    @Query("""
            MATCH (user1:User {id: $userId1})-[friend:FRIEND]->(user2:User {id: $userId2})
            return COUNT(friend) > 0
            """)
    boolean isFriend(UUID userId1, UUID userId2);

    @Query("""
            // Match current user
            MATCH (currentUser:User {id: $userId})
            
            // Match friends of the current user
            MATCH (currentUser)-[:FRIEND]->(friend:User)
            
            // Match friends of friends who are not the current user
            MATCH (friend)-[:FRIEND]->(friendOfFriend:User)
            WHERE friendOfFriend.id <> $userId
            
            // Exclude users who are already friends with current user
            // or who have been blocked by current user or have blocked current user
            OPTIONAL MATCH (currentUser)-[r:FRIEND]->(friendOfFriend)
            OPTIONAL MATCH (currentUser)-[:BLOCK]->(friendOfFriend)
            OPTIONAL MATCH (friendOfFriend)-[:BLOCK]->(currentUser)
            OPTIONAL MATCH (friendOfFriend)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
            WITH currentUser, friend, friendOfFriend, profilePic
            WHERE r IS NULL
            AND NOT exists((currentUser)-[:BLOCK]->(friendOfFriend))
            AND NOT exists((friendOfFriend)-[:BLOCK]->(currentUser))
            
            // Group to count mutual friends and eliminate duplicates
            WITH friendOfFriend, profilePic, count(DISTINCT friend) AS mutualFriendsCount
            
            // Return results ordered by most mutual friends first
            RETURN
                friendOfFriend.id AS userId,
                friendOfFriend.username AS username,
                friendOfFriend.givenName AS givenName,
                friendOfFriend.familyName AS familyName,
                CASE WHEN profilePic IS NOT NULL
                    THEN profilePic.id
                    ELSE NULL END AS profilePictureId,
                false AS isFriend,
                mutualFriendsCount as mutualFriendsCount
            ORDER BY mutualFriendsCount DESC
            SKIP $skip LIMIT $limit
            """)
    List<UserProjection> getSuggestedFriends(UUID userId, Pageable pageable);

    @Query("""
        // Match both users
        MATCH (user1:User {id: $userId}), (user2:User {id: $targetId})
        
        // Find mutual friends (users who are friends with both user1 and user2)
        MATCH (user1)-[:FRIEND]->(mutualFriend:User)<-[:FRIEND]-(user2)
        
        // Get profile picture
        OPTIONAL MATCH (mutualFriend)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
        
        // Calculate mutual friends count between user1 and each mutual friend
        // (how many friends they have in common)
        WITH user1, mutualFriend, profilePic,
             size([(user1)-[:FRIEND]->(commonFriend:User)<-[:FRIEND]-(mutualFriend) | commonFriend]) AS mutualFriendsCount
        
        // Return mutual friends
        RETURN
            mutualFriend.id AS userId,
            mutualFriend.username AS username,
            mutualFriend.givenName AS givenName,
            mutualFriend.familyName AS familyName,
            CASE WHEN profilePic IS NOT NULL
                THEN profilePic.id
                ELSE NULL END AS profilePictureId,
            true AS isFriend,
            mutualFriendsCount AS mutualFriendsCount
        ORDER BY mutualFriendsCount DESC, mutualFriend.username
        SKIP $skip LIMIT $limit
        """)
    List<UserProjection> getMutualFriends(UUID userId, UUID targetId, Pageable pageable);
}
