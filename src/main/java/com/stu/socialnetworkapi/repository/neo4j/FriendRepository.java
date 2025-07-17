package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.dto.projection.UserProjection;
import com.stu.socialnetworkapi.entity.relationship.Friend;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FriendRepository extends Neo4jRepository<Friend, Long> {
    @Query("""
             MATCH (user:User {username: $username})-[friend:FRIEND]->(target:User)
             RETURN target.username
             LIMIT $limit
            """)
    Set<String> getFriendUsernames(String username, long limit);

    @Query("""
            MATCH (:User {username: $username})-[friend:FRIEND]->(:User {username: $targetUsername})
            RETURN friend.uuid
            """)
    Optional<UUID> getFriendId(String username, String targetUsername);

    @Query("""
                MATCH (u:User)-[r:FRIEND {uuid: $uuid}]->(t:User)
                OPTIONAL MATCH (u)<-[rv:FRIEND]-(t)
                DELETE r, rv
            """)
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
            MATCH (currentUser:User {username: $username})
            MATCH (target:User)
            WHERE target.id <> currentUser.id
              AND NOT (currentUser)-[:FRIEND|BLOCK|REQUEST]-(target)
            
            // Bạn chung
            OPTIONAL MATCH (currentUser)-[:FRIEND]->(mutual:User)-[:FRIEND]->(target)
            
            // Lượt xem profile
            OPTIONAL MATCH (currentUser)-[viewOut:VIEW_PROFILE]->(target)
            OPTIONAL MATCH (target)-[viewIn:VIEW_PROFILE]->(currentUser)
            
            // Cùng chat (chưa kết bạn)
            OPTIONAL MATCH (currentUser)-[:IS_MEMBER_OF]->(chat)<-[:IS_MEMBER_OF]-(target)
            
            OPTIONAL MATCH path = (currentUser)-[]->(:Post|Comment)<-[]-(target)
            
            WITH currentUser, target, chat,
                 COUNT(DISTINCT mutual) AS mutualFriendsCount,
                 COALESCE(viewOut.times, 0) AS viewOutTimes,
                 COALESCE(viewIn.times, 0) AS viewInTimes,
                 COUNT(path) AS numPaths,
            
                 currentUser.birthdate.year - target.birthdate.year AS ageDiff
            
            WITH target, mutualFriendsCount, viewOutTimes, viewInTimes, ageDiff, numPaths, chat,
                 mutualFriendsCount * 5
                 + viewOutTimes * 2
                 + viewInTimes
                 - abs(ageDiff) * 2
                 + CASE WHEN chat IS NOT NULL THEN 30 ELSE 0 END
                 + numPaths * 2 AS score
            
            RETURN
                target.username AS username
            ORDER BY score DESC
            LIMIT $limit
            """)
    Set<String> getSuggestedFriendUsernames(String username, long limit);


    @Query("""
            // Match both users
            MATCH (user1:User {username: $username}), (user2:User {username: $username})
            
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
            LIMIT $limit
            """)
    List<UserProjection> getMutualFriends(String username, String targetUsername, long limit);
}
