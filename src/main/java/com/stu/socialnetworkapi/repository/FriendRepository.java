package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.projection.FriendProjection;
import com.stu.socialnetworkapi.dto.projection.UserProjection;
import com.stu.socialnetworkapi.entity.relationship.Friend;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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
            MATCH (:User {id: $userId})-[friend:FRIEND]->(:User {id: $targetId})
            RETURN friend.uuid
            """)
    Optional<UUID> getFriendId(UUID userId, UUID targetId);

    void deleteByUuid(UUID uuid);


    @Query("""
            MATCH (user1:User {id: $userId1})-[friend:FRIEND]->(user2:User {id: $userId2})
            return COUNT(friend) > 0
            """)
    boolean isFriend(UUID userId1, UUID userId2);

    /**
     * Hệ thống gợi ý bạn bè
     * - Số lượng bạn chung: 5 điểm 1 bạn chung
     * - Thông qua quan hệ (User)-[view:VIEW_PROFILE]->(User)
     * dùng thuộc tính view.times * 2 điểm, ở chiều ngược lại view.times * 1 điểm
     * - Với mỗi đường từ user -> target (2 cạnh): 2 điểm
     * - Độ tuổi chênh lệch mỗi 1 tuổi chênh lệch -2 điểm * số tuổi chênh lệch
     * - Đã từng chat với nhau nhưng chưa kết bạn: 30 điểm
     */
    @Query("""
            MATCH (currentUser:User {id: $userId})
            MATCH (target:User)
            WHERE target.id <> $userId
              AND NOT EXISTS((currentUser)-[:FRIEND|BLOCK|REQUEST]-(target))
            
            // Bạn chung
            OPTIONAL MATCH (currentUser)-[:FRIEND]->(mutual:User)-[:FRIEND]->(target)
            WHERE mutual.id <> target.id
            
            // Lượt xem profile
            OPTIONAL MATCH (currentUser)-[viewOut:VIEW_PROFILE]->(target)
            OPTIONAL MATCH (target)-[viewIn:VIEW_PROFILE]->(currentUser)
            
            // Ảnh đại diện
            OPTIONAL MATCH (target)-[:HAS_PROFILE_PICTURE]->(pic:File)
            
            // Cùng chat (chưa kết bạn)
            OPTIONAL MATCH (currentUser)-[:IS_MEMBER_OF]->(chat)<-[:IS_MEMBER_OF]-(target)
            
            OPTIONAL MATCH path = (currentUser)-[*2..2]-(target)
            
            WITH currentUser, target, pic, chat,
                 COUNT(DISTINCT mutual) AS mutualFriendsCount,
                 COALESCE(viewOut.times, 0) AS viewOutTimes,
                 COALESCE(viewIn.times, 0) AS viewInTimes,
                 COUNT(path) AS numPaths,
            
                 currentUser.birthdate.year - target.birthdate.year AS ageDiff
            
            WITH target, pic, mutualFriendsCount, viewOutTimes, viewInTimes, ageDiff, numPaths, chat,
                 mutualFriendsCount * 5
                 + viewOutTimes * 2
                 + viewInTimes
                 - abs(ageDiff) * 2
                 + CASE WHEN chat IS NOT NULL THEN 30 ELSE 0 END
                 + numPaths * 2 AS score
            
            RETURN
                target.id AS userId,
                target.username AS username,
                target.givenName AS givenName,
                target.familyName AS familyName,
                CASE WHEN pic IS NOT NULL THEN pic.id ELSE NULL END AS profilePictureId,
                mutualFriendsCount AS mutualFriendsCount,
                false AS isFriend,
                score
            
            ORDER BY score DESC
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
