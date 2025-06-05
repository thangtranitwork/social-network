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
            MATCH (u:User {id: $userId})-[isMember:IS_MEMBER_OF]->(c:Chat {id: $chatId})
            RETURN COUNT(isMember) > 0
            """)
    boolean existInChat(UUID chatId, UUID userId);

    @Query("""
                MATCH (u:User {id: $userId})-[:IS_MEMBER_OF]->(chat:Chat)
                OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(message:Message)
                WITH u, chat, message
                ORDER BY message.sentAt DESC
                WITH u, chat, COLLECT(message)[0] AS latestMessage
            
                OPTIONAL MATCH (latestMessage)<-[:SENT]-(sender:User)
                OPTIONAL MATCH (latestMessage)-[:ATTACH_FILE]->(latestMessageFile:File)
                OPTIONAL MATCH (sender)-[:HAS_PROFILE_PICTURE]->(senderProfilePic:File)
            
                OPTIONAL MATCH (chat)<-[:IS_MEMBER_OF]-(otherMember:User)
                WHERE otherMember.id <> $userId
                OPTIONAL MATCH (otherMember)-[:HAS_PROFILE_PICTURE]->(targetProfilePic:File)
            
                OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(unreadMsg:Message)
                OPTIONAL MATCH (reader:User {id: $userId})-[r:READ]->(unreadMsg)
                WHERE r IS NULL
            
                RETURN
                    chat.id AS chatId,
                    CASE
                        WHEN chat.type = 'DIRECT' THEN otherMember.givenName + ' ' + otherMember.familyName
                        ELSE chat.name
                    END AS name,
                    latestMessage.id AS latestMessageId,
                    latestMessage.content AS latestMessageContent,
                    latestMessageFile.id AS latestMessageFileId,
                    latestMessage.sentAt AS latestMessageSentAt,
                    sender.id AS latestMessageSenderId,
                    sender.username AS latestMessageSenderUsername,
                    sender.givenName AS latestMessageSenderGivenName,
                    sender.familyName AS latestMessageSenderFamilyName,
                    senderProfilePic.id AS latestMessageSenderProfilePictureId,
                    CASE
                        WHEN chat.type = 'DIRECT' THEN otherMember.id
                        ELSE null
                    END AS targetId,
                    CASE
                        WHEN chat.type = 'DIRECT' THEN otherMember.username
                        ELSE null
                    END AS targetUsername,
                    CASE
                        WHEN chat.type = 'DIRECT' THEN otherMember.givenName
                        ELSE null
                    END AS targetGivenName,
                    CASE
                        WHEN chat.type = 'DIRECT' THEN otherMember.familyName
                        ELSE null
                    END AS targetFamilyName,
                    CASE
                        WHEN chat.type = 'DIRECT' THEN targetProfilePic.id
                        ELSE null
                    END AS targetProfilePictureId,
                    COUNT(unreadMsg) AS notReadMessageCount
            
                ORDER BY latestMessage.sentAt DESC
            """)
    List<ChatProjection> getChatListOrderByLatestMessageSentTimeDesc(UUID userId);


    @Query("""
                MATCH (u:User {id: $userId})-[:IS_MEMBER_OF]->(chat:Chat)
                OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(message:Message)
                WITH u, chat, message
                ORDER BY message.sentAt DESC
                WITH u, chat, COLLECT(message)[0] AS latestMessage
            
                OPTIONAL MATCH (chat)-[:HAS_CHAT_IMAGE]->(chatImage:File)
                OPTIONAL MATCH (latestMessage)<-[:SENT]-(sender:User)
                OPTIONAL MATCH (latestMessage)-[:ATTACH_FILE]->(latestMessageFile:File)
                OPTIONAL MATCH (sender)-[:HAS_PROFILE_PICTURE]->(senderProfilePic:File)
            
                OPTIONAL MATCH (chat)<-[:IS_MEMBER_OF]-(otherMember:User)
                WHERE chat.type = 'DIRECT' AND otherMember.id <> $userId
                OPTIONAL MATCH (otherMember)-[:HAS_PROFILE_PICTURE]->(targetProfilePic:File)
            
                OPTIONAL MATCH (chat)-[:HAS_MESSAGE]->(unreadMsg:Message)
                OPTIONAL MATCH (reader:User {id: $userId})-[r:READ]->(unreadMsg)
                WHERE r IS NULL
            
                // === Filtering by query ===
                WITH chat, chatImage, latestMessage, latestMessageFile, sender, senderProfilePic,
                     otherMember, targetProfilePic, unreadMsg, u
                WHERE
                    $query IS NULL OR
                    (
                        chat.type = 'GROUP' AND toLower(chat.name) CONTAINS toLower($query)
                    ) OR (
                        chat.type = 'DIRECT' AND (
                            toLower(otherMember.givenName + ' ' + otherMember.familyName) CONTAINS toLower($query)
                            OR toLower(otherMember.username) CONTAINS toLower($query)
                        )
                    )
            
                RETURN
                    chat.id AS chatId,
                    CASE
                        WHEN chat.type = 'DIRECT' THEN otherMember.givenName + ' ' + otherMember.familyName
                        ELSE chat.name
                    END AS name,
                    latestMessage.id AS latestMessageId,
                    latestMessage.content AS latestMessageContent,
                    latestMessageFile.id AS latestMessageFileId,
                    latestMessage.sentAt AS latestMessageSentAt,
                    sender.id AS latestMessageSenderId,
                    sender.username AS latestMessageSenderUsername,
                    sender.givenName AS latestMessageSenderGivenName,
                    sender.familyName AS latestMessageSenderFamilyName,
                    senderProfilePic.id AS latestMessageSenderProfilePictureId,
                    CASE
                        WHEN chat.type = 'DIRECT' THEN otherMember.id
                        ELSE null
                    END AS targetId,
                    CASE
                        WHEN chat.type = 'DIRECT' THEN otherMember.username
                        ELSE null
                    END AS targetUsername,
                    CASE
                        WHEN chat.type = 'DIRECT' THEN otherMember.givenName
                        ELSE null
                    END AS targetGivenName,
                    CASE
                        WHEN chat.type = 'DIRECT' THEN otherMember.familyName
                        ELSE null
                    END AS targetFamilyName,
                    CASE
                        WHEN chat.type = 'DIRECT' THEN targetProfilePic.id
                        ELSE null
                    END AS targetProfilePictureId,
                    COUNT(unreadMsg) AS notReadMessageCount
            
                ORDER BY latestMessage.sentAt DESC
            """)
    List<ChatProjection> searchChats(UUID userId, String query);
}
