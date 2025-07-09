package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import com.stu.socialnetworkapi.dto.request.*;
import com.stu.socialnetworkapi.dto.response.MessageCommand;
import com.stu.socialnetworkapi.dto.response.MessageResponse;
import com.stu.socialnetworkapi.entity.Chat;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.Message;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.exception.WebSocketException;
import com.stu.socialnetworkapi.mapper.MessageMapper;
import com.stu.socialnetworkapi.repository.InChatRedisRepository;
import com.stu.socialnetworkapi.repository.IsTypingRedisRepository;
import com.stu.socialnetworkapi.repository.MessageRepository;
import com.stu.socialnetworkapi.service.itf.ChatService;
import com.stu.socialnetworkapi.service.itf.FileService;
import com.stu.socialnetworkapi.service.itf.MessageService;
import com.stu.socialnetworkapi.service.itf.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final UserService userService;
    private final FileService fileService;
    private final ChatService chatService;
    private final MessageMapper messageMapper;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final InChatRedisRepository inChatRedisRepository;
    private final IsTypingRedisRepository isTypingRedisRepository;

    @Override
    public MessageResponse sendMessage(TextMessageRequest request) {
        User sender = userService.getCurrentUserRequiredAuthentication();
        User receiver = userService.getUser(request.username());
        Chat chat = chatService.getOrCreateDirectChat(sender, receiver);
        String content = request.text().trim();
        if (content.isEmpty()) throw new ApiException(ErrorCode.TEXT_MESSAGE_CONTENT_REQUIRED);
        if (content.length() > Message.MAX_CONTENT_LENGTH)
            throw new ApiException(ErrorCode.INVALID_MESSAGE_CONTENT_LENGTH);

        Message message = Message.builder()
                .chat(chat)
                .content(content)
                .sender(sender)
                .build();

        messageRepository.save(message);
        MessageResponse response = messageMapper.toMessageResponse(message);
        sendMessageNotification(chat.getId(), receiver.getId(), response);
        return response;
    }

    @Override
    public MessageResponse sendMessage(TextMessageRequest request, UUID userId) {
        User sender = userService.getUser(userId);
        User receiver = userService.getUser(request.username());
        Chat chat = chatService.getOrCreateDirectChat(sender, receiver);
        String content = request.text().trim();
        if (content.isEmpty()) throw new WebSocketException(ErrorCode.TEXT_MESSAGE_CONTENT_REQUIRED);
        if (content.length() > Message.MAX_CONTENT_LENGTH)
            throw new WebSocketException(ErrorCode.INVALID_MESSAGE_CONTENT_LENGTH);

        Message message = Message.builder()
                .chat(chat)
                .content(content)
                .sender(sender)
                .build();

        messageRepository.save(message);
        MessageResponse response = messageMapper.toMessageResponse(message);
        sendMessageNotification(chat.getId(), receiver.getId(), response);
        return response;
    }

    @Override
    public MessageResponse sendFile(FileMessageRequest request) {
        User sender = userService.getCurrentUserRequiredAuthentication();
        User receiver = userService.getUser(request.username());
        Chat chat = chatService.getOrCreateDirectChat(sender, receiver);
        File file = fileService.upload(request.attachment());
        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .attachedFile(file)
                .build();
        messageRepository.save(message);
        MessageResponse response = messageMapper.toMessageResponse(message);
        sendMessageNotification(chat.getId(), receiver.getId(), response);
        return response;
    }

    @Override
    public List<MessageResponse> getHistory(UUID chatId, Neo4jPageable pageable) {
        UUID userId = userService.getCurrentUserIdRequiredAuthentication();
        if (!inChatRedisRepository.isInChat(userId, chatId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        List<MessageResponse> messages = messageRepository.findAllByChatIdOrderBySentAtDesc(chatId, pageable.getSkip(), pageable.getLimit()).stream()
                .map(messageMapper::toMessageResponse)
                .toList();

        if (pageable.getSkip() <= pageable.getLimit())
            messageRepository.markAsRead(chatId, userId);
        return messages;
    }

    @Override
    public void editMessage(EditMessageRequest request) {
        Message message = messageRepository.findById(request.messagesId())
                .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND));
        validateEditMessage(message, request.text());
        String content = request.text().trim();
        message.setContent(content);
        message.setUpdateAt(ZonedDateTime.now());
        messageRepository.save(message);
        MessageCommand command = MessageCommand.builder()
                .id(message.getId())
                .command(MessageCommand.Command.EDIT)
                .message(content)
                .build();
        sendMessageCommand(message.getChat().getId(), command);
    }

    private void sendMessageCommand(UUID chatId, MessageCommand command) {
        messagingTemplate.convertAndSend(WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX + "/" + chatId, command);
    }

    @Override
    public void deleteMessage(UUID messageId) {
        User user = userService.getCurrentUserRequiredAuthentication();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND));
        Chat chat = message.getChat();
        validateDeleteMessage(message, user);
        message.setDeleteAt(ZonedDateTime.now());
        message.setContent("deleted");
        messageRepository.save(message);
        if (message.getAttachedFile() != null) {
            fileService.deleteFile(message.getAttachedFile());
        }
        MessageCommand command = MessageCommand.builder()
                .id(messageId)
                .command(MessageCommand.Command.DELETE)
                .build();
        sendMessageCommand(chat.getId(), command);
    }

    @Override
    public void typing(UserTypingRequest request) {
        MessageCommand command = MessageCommand.builder()
                .command(request.isTyping() ? MessageCommand.Command.TYPING : MessageCommand.Command.STOP_TYPING)
                .id(request.userId())
                .build();
        if (request.isTyping())
            isTypingRedisRepository.save(request.userId(), request.chatId());
        else isTypingRedisRepository.delete(request.userId());
        sendMessageCommand(request.chatId(), command);
    }

    private static void validateDeleteMessage(Message message, User user) {
        if (!message.getSender().getId().equals(user.getId())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (ZonedDateTime.now().isAfter(message.getSentAt().plusMinutes(Message.MINUTES_TO_DELETE_MESSAGE))) {
            throw new ApiException(ErrorCode.CAN_NOT_DELETE_MESSAGE);
        }
    }

    private void validateEditMessage(Message message, String newContent) {
        User user = userService.getCurrentUserRequiredAuthentication();
        String content = newContent.trim();
        if (!message.getSender().getId().equals(user.getId()))
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        if (message.getContent() == null && message.getAttachedFile() != null)
            throw new ApiException(ErrorCode.CAN_NOT_EDIT_FILE_MESSAGE);
        if (ZonedDateTime.now().isAfter(message.getSentAt().plusMinutes(Message.MINUTES_TO_EDIT_MESSAGE)))
            throw new ApiException(ErrorCode.CAN_NOT_EDIT_MESSAGE);
        if (content.isEmpty())
            throw new ApiException(ErrorCode.TEXT_MESSAGE_CONTENT_REQUIRED);
        if (content.length() > Message.MAX_CONTENT_LENGTH)
            throw new ApiException(ErrorCode.INVALID_MESSAGE_CONTENT_LENGTH);
        if (content.equals(message.getContent()))
            throw new ApiException(ErrorCode.TEXT_MESSAGE_CONTENT_UNCHANGED);
    }

    private void sendMessageNotification(UUID chatId, UUID targetId, MessageResponse response) {
        // Gửi tin lên đoạn chat (người dùng đang mở đoạn chat trên màn hình)
        messagingTemplate.convertAndSend(WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX + "/" + chatId, response);
        // Gửi thông báo tin nhắn cho người nhận
        messagingTemplate.convertAndSend(WebSocketChannelPrefix.MESSAGE_CHANNEL_PREFIX + "/" + targetId, response);
    }
}
