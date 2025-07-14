package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.projection.ChatProjection;
import com.stu.socialnetworkapi.entity.Chat;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRepository extends Neo4jRepository<Chat, UUID> {

    @Query("""
            MATCH (u:User {id: $userId})-[:IS_MEMBER_OF]->(chat:Chat)<-[:IS_MEMBER_OF]-(target:User {id: $targetId})
            RETURN chat.id
            """)
    Optional<UUID> getDirectChatIdByMemberIds(UUID userId, UUID targetId);

    @Query("""
            MATCH (u:User {id: $userId})-[:IS_MEMBER_OF]->(chat:Chat)
            RETURN chat.id
            """)
    List<UUID> getChatIdsByUserId(UUID userId);

    @Query("""
            MATCH (currentUser:User {id: $userId})-[:IS_MEMBER_OF]->(chat:Chat)<-[:IS_MEMBER_OF]-(target:User)
            WHERE target.id <> $userId
            RETURN target.id
            """)
    List<UUID> getTargetIds(UUID userId);

    @Query("""
            MATCH (currentUser:User {id: $userId})-[:IS_MEMBER_OF]->(chat:Chat)<-[:IS_MEMBER_OF]-(target:User)
            WHERE target.id <> $userId
            
            // Get latest message
            OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(message:Message)
            WITH currentUser, chat, target, message
            ORDER BY message.sentAt DESC
            WITH currentUser, chat, target, COLLECT(message)[0] AS latestMessage
            
            // Get message sender info
            OPTIONAL MATCH (latestMessage)<-[:SENT]-(sender:User)
            OPTIONAL MATCH (latestMessage)-[:ATTACH_FILE]->(latestMessageFile:File)
            OPTIONAL MATCH (sender)-[:HAS_PROFILE_PICTURE]->(senderProfilePic:File)
            
            // Get target profile picture
            OPTIONAL MATCH (target)-[:HAS_PROFILE_PICTURE]->(targetProfilePic:File)
            
            // Count unread messages
            OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(unreadMsg:Message)
            WHERE NOT EXISTS((currentUser)-[:SENT]->(unreadMsg)) AND unreadMsg.isRead = false
            
            // Check friendship status
            OPTIONAL MATCH (currentUser)-[friendRel:FRIEND]->(target)
            
            // Check block status
            OPTIONAL MATCH (currentUser)-[blockOut:BLOCK]->(target)
            OPTIONAL MATCH (currentUser)<-[blockIn:BLOCK]-(target)
            
            RETURN
                chat.id AS chatId,
                target.givenName + ' ' + target.familyName AS name,
            
                latestMessage.id AS latestMessageId,
                latestMessage.content AS latestMessageContent,
                latestMessageFile.id AS latestMessageFileId,
                COALESCE(latestMessage.sentAt, chat.createdAt) AS latestMessageSentAt,
                latestMessage.deleteAt IS NOT NULL AS latestMessageDeleted,
                latestMessage.type AS latestMessageType,
                latestMessage.callId AS latestMessageCallId,
                latestMessage.callAt AS latestMessageCallAt,
                latestMessage.endAt AS latestMessageEndAt,
                latestMessage.isAnswered AS latestMessageAnswered,
                latestMessage.isVideoCall AS latestMessageIsVideoCall,
            
                sender.id AS latestMessageSenderId,
                sender.username AS latestMessageSenderUsername,
                sender.givenName AS latestMessageSenderGivenName,
                sender.familyName AS latestMessageSenderFamilyName,
                senderProfilePic.id AS latestMessageSenderProfilePictureId,
            
                target.id AS targetId,
                target.username AS targetUsername,
                target.givenName AS targetGivenName,
                target.familyName AS targetFamilyName,
                targetProfilePic.id AS targetProfilePictureId,
                COUNT(unreadMsg) AS notReadMessageCount,
                CASE WHEN friendRel IS NOT NULL THEN true ELSE false END AS isFriend,
                CASE
                    WHEN blockOut IS NOT NULL THEN 'BLOCKED'
                    WHEN blockIn IS NOT NULL THEN 'HAS_BEEN_BLOCKED'
                    ELSE 'NORMAL'
                END AS blockStatus
            
            ORDER BY latestMessageSentAt DESC
            """)
    List<ChatProjection> getChatListOrderByLatestMessageSentTimeDesc(UUID userId);

    @Query("""
            MATCH (currentUser:User {id: $userId})-[:IS_MEMBER_OF]->(chat:Chat)<-[:IS_MEMBER_OF]-(target:User)
            WHERE target.id <> $userId
            AND
                  toLower(target.givenName + ' ' + target.familyName) CONTAINS toLower($query) OR
                  toLower(target.username) CONTAINS toLower($query)
            
            // Get latest message
            OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(message:Message)
            WITH currentUser, chat, target, message
            ORDER BY message.sentAt DESC
            WITH currentUser, chat, target, COLLECT(message)[0] AS latestMessage
            
            // Get message sender info
            OPTIONAL MATCH (latestMessage)<-[:SENT]-(sender:User)
            OPTIONAL MATCH (latestMessage)-[:ATTACH_FILE]->(latestMessageFile:File)
            OPTIONAL MATCH (sender)-[:HAS_PROFILE_PICTURE]->(senderProfilePic:File)
            
            // Get target profile picture
            OPTIONAL MATCH (target)-[:HAS_PROFILE_PICTURE]->(targetProfilePic:File)
            
            // Count unread messages
            OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(unreadMsg:Message)<-[:SENT]-(sender2:User)
            WHERE sender.id <> currentUser.id AND unreadMsg.isRead
            
            // Check friendship status
            OPTIONAL MATCH (currentUser)-[friendRel:FRIEND]->(target)
            
            // Check block status
            OPTIONAL MATCH (currentUser)-[blockOut:BLOCK]->(target)
            OPTIONAL MATCH (currentUser)<-[blockIn:BLOCK]-(target)
            
            RETURN
                chat.id AS chatId,
                target.givenName + ' ' + target.familyName AS name,
            
                latestMessage.id AS latestMessageId,
                latestMessage.content AS latestMessageContent,
                latestMessageFile.id AS latestMessageFileId,
                latestMessage.sentAt AS latestMessageSentAt,
                latestMessage.deleteAt IS NOT NULL AS latestMessageDeleted,
                latestMessage.type AS latestMessageType,
                latestMessage.callId AS latestMessageCallId,
                latestMessage.callAt AS latestMessageCallAt,
                latestMessage.endAt AS latestMessageEndAt,
                latestMessage.isAnswered AS latestMessageAnswered,
                latestMessage.isVideoCall AS latestMessageIsVideoCall,
            
                sender.id AS latestMessageSenderId,
                sender.username AS latestMessageSenderUsername,
                sender.givenName AS latestMessageSenderGivenName,
                sender.familyName AS latestMessageSenderFamilyName,
                senderProfilePic.id AS latestMessageSenderProfilePictureId,
                target.id AS targetId,
                target.username AS targetUsername,
                target.givenName AS targetGivenName,
                target.familyName AS targetFamilyName,
                targetProfilePic.id AS targetProfilePictureId,
                COUNT(unreadMsg) AS notReadMessageCount,
                CASE WHEN friendRel IS NOT NULL THEN true ELSE false END AS isFriend,
                CASE
                    WHEN blockOut IS NOT NULL THEN 'BLOCKED'
                    WHEN blockIn IS NOT NULL THEN 'HAS_BEEN_BLOCKED'
                    ELSE 'NORMAL'
                END AS blockStatus
            
            ORDER BY latestMessage.sentAt DESC
            """)
    List<ChatProjection> searchChats(UUID userId, String query);
}