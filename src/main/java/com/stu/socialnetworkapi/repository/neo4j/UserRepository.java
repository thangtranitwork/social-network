package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.dto.projection.AdminUserViewProjection;
import com.stu.socialnetworkapi.dto.projection.CountDataProjection;
import com.stu.socialnetworkapi.dto.projection.UserProfileProjection;
import com.stu.socialnetworkapi.dto.projection.UserProjection;
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
            
            // Check friend request - outgoing (currentUser -> user)
            OPTIONAL MATCH (currentUser)-[requestOut:REQUEST]->(user)
            
            // Check friend request - incoming (user -> currentUser)
            OPTIONAL MATCH (user)-[requestIn:REQUEST]->(currentUser)
            
            // Check block status - currentUser blocks user
            OPTIONAL MATCH (currentUser)-[blockOut:BLOCK]->(user)
            
            // Check block status - user blocks currentUser
            OPTIONAL MATCH (user)-[blockIn:BLOCK]->(currentUser)
            
            // Count mutual friends
            WITH user, profilePic, currentUser, friendship, requestOut, requestIn, blockOut, blockIn,
                 size([(currentUser)-[:FRIEND]->(mutual:User)<-[:FRIEND]-(user) | mutual]) AS mutualFriendsCount
            
            // Count posts
            OPTIONAL MATCH (user)-[:POSTED]->(post:Post)
            WITH user, profilePic, currentUser, friendship, requestOut, requestIn, blockOut, blockIn, mutualFriendsCount,
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
            
                // Request Direction
                CASE
                    WHEN requestOut IS NOT NULL THEN 'OUT'
                    WHEN requestIn IS NOT NULL THEN 'IN'
                    ELSE 'NONE'
                END AS request,
            
                // Block Status
                CASE
                    WHEN blockOut IS NOT NULL THEN 'BLOCKED'
                    WHEN blockIn IS NOT NULL THEN 'HAS_BEEN_BLOCKED'
                    ELSE 'NORMAL'
                END AS blockStatus
            LIMIT 1
            """)
    Optional<UserProfileProjection> findProfileByUsername(String username, UUID currentUserId);

    boolean existsByUsername(String username);

    @Query("""
                MATCH (currentUser:User {id: $currentUserId})
                CALL db.index.fulltext.queryNodes("userSearchIndex", $query + "*")
                YIELD node AS targetUser, score
            
                WHERE NOT (currentUser)-[:BLOCK]->(targetUser)
                  AND NOT (targetUser)-[:BLOCK]->(currentUser)
                  AND currentUser.id <> targetUser.id
            
                // Tính shortest path
                OPTIONAL MATCH p = shortestPath((currentUser)-[*1..4]-(targetUser))
            
                // Đếm số bạn chung
                OPTIONAL MATCH (currentUser)-[:FRIEND]-(mutual:User)-[:FRIEND]-(targetUser)
            
                // Avatar
                OPTIONAL MATCH (targetUser)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
            
                RETURN
                    targetUser.id AS userId,
                    targetUser.username AS username,
                    targetUser.givenName AS givenName,
                    targetUser.familyName AS familyName,
                    profilePic.id AS profilePictureId,
                    COUNT(DISTINCT mutual) AS mutualFriendsCount,
                    EXISTS((currentUser)-[:FRIEND]-(targetUser)) AS isFriend,
                    score AS score,
                    CASE WHEN p IS NULL THEN 1000 ELSE length(p) END AS shortestPathLength
            
                ORDER BY score DESC, shortestPathLength ASC
                SKIP $skip
                LIMIT $limit
            """)
    List<UserProjection> fullTextSearch(String query, UUID currentUserId, long limit, long skip);

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
                 datetime({year: today.year, month: today.month, day: today.day, hour: 0, minute: 0, second: 0}) AS startOfDay,
                 datetime({year: today.year, month: today.month, day: 1}) AS startOfMonth,
                 datetime({year: today.year, month: 1, day: 1}) AS startOfYear,
                 datetime($startOfWeek) AS startOfWeek
            
            MATCH (u:User)<-[:HAS_INFO]-(account:Account)
            
            RETURN
              count(u) AS totalUsers,
              count(CASE WHEN u.createdAt >= startOfDay THEN 1 END) AS newUsersToday,
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
            OPTIONAL MATCH (user)-[friendRel:FRIEND]->(:User)
            WITH user, count(friendRel) AS friendCount
            
            // Đếm số lượng request đã gửi
            OPTIONAL MATCH (user)-[sentReq:REQUEST]->(:User)
            WITH user, friendCount, count(sentReq) AS requestSentCount
            
            // Đếm số lượng request đã nhận
            OPTIONAL MATCH (user)<-[receivedReq:REQUEST]-(:User)
            WITH user, friendCount, requestSentCount, count(receivedReq) AS requestReceivedCount
            
            // Đếm số lượng user đã block
            OPTIONAL MATCH (user)-[blockRel:BLOCK]->(:User)
            WITH user, friendCount, requestSentCount, requestReceivedCount, count(blockRel) AS blockCount
            
            // Cập nhật tất cả các giá trị
            SET user.friendCount = friendCount,
                user.requestSentCount = requestSentCount,
                user.requestReceivedCount = requestReceivedCount,
                user.blockCount = blockCount
            """)
    void recalculateUserCounters(UUID userId);

    @Query("""
                MATCH (user:User)
                WITH user
                ORDER BY user.createdAt DESC
                SKIP $skip LIMIT $limit
            
                OPTIONAL MATCH (user)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
                MATCH (user)<-[:HAS_INFO]-(account:Account)
            
                CALL {
                    WITH user
                    OPTIONAL MATCH (user)-[:POSTED]->(p:Post)
                    RETURN COUNT(p) AS postCount
                }
            
                CALL {
                    WITH user
                    OPTIONAL MATCH (user)-[:COMMENTED]->(c:Comment)
                    RETURN COUNT(c) AS commentCount
                }
            
                CALL {
                    WITH user
                    OPTIONAL MATCH (user)-[:UPLOAD_FILE]->(f:File)
                    RETURN COUNT(f) AS uploadedFileCount
                }
            
                CALL {
                    WITH user
                    OPTIONAL MATCH (user)-[:SENT]->(m:Message)
                    RETURN COUNT(m) AS messageCount
                }
            
                CALL {
                    WITH user
                    OPTIONAL MATCH (user)-[:SENT]->(m:Call)
                    RETURN COUNT(m) AS callCount
                }
            
                RETURN user.id AS userId,
                       user.username AS username,
                       user.givenName AS givenName,
                       user.familyName AS familyName,
                       user.bio AS bio,
                       user.birthdate AS birthdate,
                       profilePic.id AS profilePictureId,
                       user.friendCount AS friendCount,
                       user.blockCount AS blockCount,
                       user.requestSentCount AS requestSentCount,
                       user.requestReceivedCount AS requestReceivedCount,
                       postCount,
                       commentCount,
                       uploadedFileCount,
                       messageCount,
                       callCount,
                       account.email AS email,
                       account.isVerified AS isVerified,
                       user.createdAt AS registrationDate
            """)
    List<AdminUserViewProjection> getAllUsers(long skip, long limit);
}
