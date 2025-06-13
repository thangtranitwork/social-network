package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.projection.CountDataProjection;
import com.stu.socialnetworkapi.dto.projection.UserProfileProjection;
import com.stu.socialnetworkapi.dto.response.UserStatisticsResponse;
import com.stu.socialnetworkapi.entity.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
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
            
                OPTIONAL MATCH (currentUser:User {id: $currentUserId})
            
                // Check friendship
                OPTIONAL MATCH (currentUser)-[friendship:FRIEND]->(user)
            
                // Check friend request (outgoing or incoming)
                OPTIONAL MATCH (currentUser)-[request:REQUEST]-(user)
            
                // Check block
                OPTIONAL MATCH (currentUser)-[block:BLOCKED]->(user)
            
                // Count mutual friends
                WITH user, profilePic, currentUser, friendship, request, block,
                     size([(currentUser)-[:FRIEND]->(mutual:User)<-[:FRIEND]-(user) | mutual]) AS mutualFriendsCount
            
                // Count posts
                OPTIONAL MATCH (user)-[:POSTED]->(post:Post)
                WITH user, profilePic, currentUser, friendship, request, block, mutualFriendsCount,
                     count(post) AS postCount
            
                RETURN
                    user.id AS userId,
                    user.username AS username,
                    user.givenName AS givenName,
                    user.familyName AS familyName,
                    user.bio AS bio,
                    user.birthdate AS birthdate,
                    CASE WHEN profilePic IS NOT NULL THEN profilePic.id ELSE NULL END AS profilePictureId,
                    COALESCE(user.friendCount, 0) AS friendCount,
                    mutualFriendsCount AS mutualFriendsCount,
                    postCount AS postCount,
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

    @Query("""
                MATCH (currentUser:User {id: $currentUserId})
                CALL db.index.fulltext.queryNodes("userSearchIndex", $query + "*")
                YIELD node, score
                WHERE NOT (currentUser)-[:BLOCK]->(node)
                  AND NOT (node)-[:BLOCK]->(currentUser)
                RETURN node.id AS user
                ORDER BY score DESC
                SKIP $skip
                LIMIT $limit
            """)
    List<UUID> fullTextSearch(String query, UUID currentUserId, int limit, int skip);

    @Query("""
            MATCH (u:User {id: $userId})
            MATCH (t:User {id: $targetId})
            MERGE (u)-[r:VIEW_PROFILE]->(t)
            ON CREATE SET r.times = 1
            ON MATCH SET r.times = r.times + 1
            """)
    void increaseViewProfile(UUID userId, UUID targetId);

    @Query("""
            WITH datetime() AS today
            WITH today,
                 datetime({year: today.year, month: today.month, day: 1}) AS startOfMonth,
                 datetime({year: today.year, month: 1, day: 1}) AS startOfYear,
                 datetime($startOfWeek) AS startOfWeek
            
            MATCH (u:User)<-[:HAS_INFO]-(account:Account)
            
            RETURN
              count(u) AS totalUsers,
              count(CASE WHEN u.createdAt = today THEN 1 END) AS newUsersToday,
              count(CASE WHEN u.createdAt >= startOfWeek THEN 1 END) AS newUsersThisWeek,
              count(CASE WHEN u.createdAt >= startOfMonth THEN 1 END) AS newUsersThisMonth,
              count(CASE WHEN u.createdAt >= startOfYear THEN 1 END) AS newUsersThisYear,
              count(CASE WHEN account.isVerified = false THEN 1 END) AS notVerifiedUsers
            """)
    UserStatisticsResponse getCommonUserStatistics(ZonedDateTime startOfWeek);

    @Query("""
            WITH range(1, 7) AS dayNumbers
            UNWIND dayNumbers AS dow
            WITH dow, datetime($startOfWeek) + duration({days: dow - 1, hours: 23, minutes: 59, seconds: 59}) AS endOfDay
            MATCH (u:User)
            WHERE u.createdAt <= endOfDay
            WITH dow, count(u) AS count
            RETURN dow AS key, count
            ORDER BY key ASC
            """)
    List<CountDataProjection> getThisWeekStatistics(ZonedDateTime startOfWeek);

    @Query("""
            WITH range(1, $daysInMonth) AS dayNumbers
            UNWIND dayNumbers AS dayNum
            WITH dayNum,
                 datetime($startOfMonth) + duration({days: dayNum - 1, hours: 23, minutes: 59, seconds: 59}) AS endOfDay
            MATCH (u:User)
            WHERE u.createdAt <= endOfDay
            WITH dayNum, count(u) AS count
            RETURN dayNum AS key, count
            ORDER BY key ASC
            """)
    List<CountDataProjection> getThisMonthStatistics(ZonedDateTime startOfMonth, int daysInMonth);

    @Query("""
            WITH range(1, 12) AS monthNumbers
            UNWIND monthNumbers AS monthNum
            WITH monthNum,
                 datetime({year: $year, month: monthNum, day: 1}) + duration({months: 1, seconds: -1}) AS endOfMonth
            MATCH (u:User)
            WHERE u.createdAt <= endOfMonth
            WITH monthNum, count(u) AS count
            RETURN monthNum AS key, count
            ORDER BY key ASC
            """)
    List<CountDataProjection> getThisYearStatistics(int year);

    @Query("""
            MATCH (u:User)
            WITH min(u.createdAt.year) AS minYear, max(u.createdAt.year) AS maxYear
            WITH range(minYear, maxYear) AS yearNumbers
            UNWIND yearNumbers AS yearNum
            WITH yearNum,
                 datetime({year: yearNum + 1, month: 1, day: 1}) + duration({seconds: -1}) AS endOfYear
            MATCH (u:User)
            WHERE u.createdAt <= endOfYear
            WITH yearNum, count(u) AS count
            RETURN yearNum AS key, count
            ORDER BY key ASC
            """)
    List<CountDataProjection> getAllTimeYearlyStatistics();

    @Query("""
            MATCH (user:User {id: $userId})
            
            // Đếm số lượng bạn bè
            OPTIONAL MATCH (user)-[:FRIEND]->(:User)
            WITH user, count(*) AS friendCount
            
            // Đếm số lượng request đã gửi
            OPTIONAL MATCH (user)-[:REQUEST]->(:User)
            WITH user, friendCount, count(*) AS requestSentCount
            
            // Đếm số lượng request đã nhận
            OPTIONAL MATCH (user)<-[:REQUEST]-(:User)
            WITH user, friendCount, requestSentCount, count(*) AS requestReceivedCount
            
            // Đếm số lượng user đã block
            OPTIONAL MATCH (user)-[:BLOCK]->(:User)
            WITH user, friendCount, requestSentCount, requestReceivedCount, count(*) AS blockCount
            
            // Cập nhật tất cả các giá trị
            SET user.friendCount = friendCount,
                user.requestSentCount = requestSentCount,
                user.requestReceivedCount = requestReceivedCount,
                user.blockCount = blockCount
            """)
    void recalculateUserCounters(UUID userId);
}
