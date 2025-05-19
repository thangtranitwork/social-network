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

    @Query("""
            // Match the user by username
            MATCH (user:User {username: $username})
            
            // Optional match for profile picture
            OPTIONAL MATCH (user)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
            
            // Optional match for cover picture
            OPTIONAL MATCH (user)-[:HAS_COVER_PICTURE]->(coverPic:File)
            
            // If current user is provided, check for friendship
            OPTIONAL MATCH (currentUser:User {id: $currentUserId})
            OPTIONAL MATCH (currentUser)-[friendship:FRIEND]->(user)
            
            // First collect data without mutual friends count
            WITH user, profilePic, coverPic, currentUser, friendship
            
            // Calculate mutual friends count if current user exists
            WITH user, profilePic, coverPic, currentUser, friendship,
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
                COALESCE(user.showFriends, true) AS showFriends,
                COALESCE(user.allowFriendRequest, true) AS allowFriendRequest
            LIMIT 1
            """)
    Optional<UserProfileProjection> findProfileByUsername(String username, UUID currentUserId);

    boolean existsByUsername(String username);
}
