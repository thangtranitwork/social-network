package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.projection.UserProfileProjection;
import com.stu.socialnetworkapi.entity.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends Neo4jRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    @Query("MATCH (u:User {username: $username}) RETURN u.id")
    Optional<UUID> getUserIdByUsername(String username);

    @Query("""
            MATCH (user:User {username: $username})
            OPTIONAL MATCH (user)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
            OPTIONAL MATCH (user)-[:HAS_COVER_PICTURE]->(coverPic:File)
            
            OPTIONAL MATCH (currentUser:User {id: $currentUserId})
            
            // Check friendship
            OPTIONAL MATCH (currentUser)-[friendship:FRIEND]->(user)
            
            // Check friend request (outgoing or incoming)
            OPTIONAL MATCH (currentUser)-[request:REQUEST]-(user)
            
            // Check block
            OPTIONAL MATCH (currentUser)-[block:BLOCKED]->(user)
            
            WITH user, profilePic, coverPic, currentUser, friendship, request, block,
            
                 CASE
                     WHEN currentUser IS NOT NULL THEN
                         size([(currentUser)-[:FRIEND]->(mutual:User)<-[:FRIEND]-(user) | mutual])
                     ELSE 0
                 END AS mutualFriendsCount
            
            RETURN
                user.id AS userId,
                user.username AS username,
                user.givenName AS givenName,
                user.familyName AS familyName,
                user.bio AS bio,
                user.birthdate AS birthdate,
                CASE WHEN profilePic IS NOT NULL THEN profilePic.id ELSE NULL END AS profilePictureId,
                CASE WHEN coverPic IS NOT NULL THEN coverPic.id ELSE NULL END AS coverPictureId,
                COALESCE(user.friendCount, 0) AS friendCount,
                mutualFriendsCount AS mutualFriendsCount,
                user.lastSeen AS lastSeen,
                CASE WHEN friendship IS NOT NULL THEN true ELSE false END AS isFriend,
                friendship.uuid AS friendId,
                request.uuid AS requestId,
                block.uuid AS blockId,
                COALESCE(user.showFriends, true) AS showFriends,
                COALESCE(user.allowFriendRequest, true) AS allowFriendRequest
            LIMIT 1
            """)
    Optional<UserProfileProjection> findProfileByUsername(String username, UUID currentUserId);


    boolean existsByUsername(String username);
}
