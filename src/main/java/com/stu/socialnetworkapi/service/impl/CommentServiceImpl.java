package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.CommentRequest;
import com.stu.socialnetworkapi.dto.request.ReplyCommentRequest;
import com.stu.socialnetworkapi.dto.response.CommentResponse;
import com.stu.socialnetworkapi.entity.*;
import com.stu.socialnetworkapi.enums.NotificationAction;
import com.stu.socialnetworkapi.enums.ObjectType;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.CommentMapper;
import com.stu.socialnetworkapi.repository.CommentRepository;
import com.stu.socialnetworkapi.repository.PostRepository;
import com.stu.socialnetworkapi.service.itf.*;
import com.stu.socialnetworkapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final FileService fileService;
    private final PostService postService;
    private final BlockService blockService;
    private final CommentMapper commentMapper;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    @Override
    public CommentResponse comment(CommentRequest request) {
        User author = userService.getCurrentUserRequiredAuthentication();
        validateCommentContent(request.content(), request.file());
        postService.validateViewPost(request.postId(), author.getId());
        Post post = postService.getPostById(request.postId());
        File attachment = request.file() != null
                ? fileService.upload(request.file())
                : null;
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content(request.content().trim())
                .attachedFile(attachment)
                .build();
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        commentRepository.save(comment);
        sendNotificationWhenComment(post, comment);
        return commentMapper.toCommentResponse(comment);
    }

    @Override
    public CommentResponse reply(ReplyCommentRequest request) {
        User author = userService.getCurrentUserRequiredAuthentication();
        validateCommentContent(request.content(), request.file());
        Comment originalComment = getCommentById(request.originalCommentId());
        if (originalComment.isRepliedComment())
            throw new ApiException(ErrorCode.CAN_NOT_REPLY_REPLIED_COMMENT);
        Post post = originalComment.getPost();
        postService.validateViewPost(post.getId(), author.getId());
        blockService.validateBlock(post.getId(), author.getId());
        File attachment = request.file() != null
                ? fileService.upload(request.file())
                : null;
        Comment comment = Comment.builder()
                .author(author)
                .content(request.content().trim())
                .attachedFile(attachment)
                .originalComment(originalComment)
                .build();
        originalComment.setReplyCount(originalComment.getReplyCount() + 1);
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        commentRepository.saveAll(List.of(originalComment, comment));
        sendNotificationWhenReply(post, comment, originalComment);
        return commentMapper.toCommentResponse(comment);
    }

    @Override
    public void like(UUID id) {
        User liker = userService.getCurrentUserRequiredAuthentication();
        if (commentRepository.isLiked(id, liker.getId()))
            throw new ApiException(ErrorCode.LIKED_COMMENT);
        Comment comment = getCommentById(id);
        comment.getLiker().add(liker);
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
        sendNotificationWhenLikeComment(id, liker);
    }

    @Override
    public void unlike(UUID id) {
        User liker = userService.getCurrentUserRequiredAuthentication();
        if (!commentRepository.isLiked(id, liker.getId()))
            throw new ApiException(ErrorCode.NOT_LIKED_COMMENT);
        Comment comment = getCommentById(id);
        comment.getLiker().remove(liker);
        comment.setLikeCount(comment.getLikeCount() - 1);
        commentRepository.save(comment);
    }

    @Override
    public void delete(UUID id) {
        User user = userService.getCurrentUserRequiredAuthentication();
        Comment comment = getCommentById(id);
        Post post = comment.getPost();
        boolean havePermission = jwtUtil.isAdmin()
                || post.getAuthor().getId().equals(user.getId())
                || user.getId().equals(comment.getAuthor().getId());
        if (!havePermission) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (comment.isRepliedComment()) {
            File attachment = comment.getAttachedFile();
            Comment originalComment = comment.getOriginalComment();
            originalComment.setReplyCount(originalComment.getReplyCount() - 1);
            post.setCommentCount(post.getCommentCount() - 1);

            postRepository.save(post);
            commentRepository.delete(comment);
            commentRepository.save(originalComment);
            if (attachment != null) {
                fileService.deleteFile(attachment);
            }
        } else {
            List<Comment> needDeleteComments = comment.getRepliedComments();
            List<File> attachments = needDeleteComments.stream()
                    .map(Comment::getAttachedFile)
                    .collect(Collectors.toList());
            needDeleteComments.add(comment);
            if (comment.getAttachedFile() != null) {
                attachments.add(comment.getAttachedFile());
            }
            post.setCommentCount(post.getCommentCount() - needDeleteComments.size());
            postRepository.save(post);
            commentRepository.deleteAll(needDeleteComments);
            if (!attachments.isEmpty()) {
                fileService.deleteFiles(attachments);
            }
        }
    }

    @Override
    public List<CommentResponse> getComments(UUID postId, Pageable pageable) {
        UUID currentUserId = userService.getCurrentUserId();
        postService.validateViewPost(postId, currentUserId);
        return commentRepository.findAllByPostId(postId, pageable).stream()
                .map(commentMapper::toCommentResponse)
                .map(response -> mapIsLiked(response, currentUserId))
                .toList();
    }

    private Comment getCommentById(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void sendNotificationWhenComment(Post post, Comment comment) {
        if (post.getAuthor().getId().equals(comment.getAuthor().getId())) return;
        Notification notification = Notification.builder()
                .creator(comment.getAuthor())
                .receiver(post.getAuthor())
                .action(NotificationAction.COMMENT)
                .targetId(comment.getId())
                .targetType(ObjectType.COMMENT)
                .build();
        notificationService.send(notification);
    }

    private void sendNotificationWhenReply(Post post, Comment comment, Comment originalComment) {
        sendNotificationWhenComment(post, comment);
        if (comment.getAuthor().getId().equals(originalComment.getAuthor().getId())) return;
        Notification notification = Notification.builder()
                .creator(comment.getAuthor())
                .receiver(originalComment.getAuthor())
                .action(NotificationAction.REPLY_COMMENT)
                .targetId(comment.getId())
                .targetType(ObjectType.COMMENT)
                .build();
        notificationService.send(notification);
    }

    private void sendNotificationWhenLikeComment(UUID id, User liker) {
        Notification notification = Notification.builder()
                .creator(liker)
                .targetId(id)
                .targetType(ObjectType.COMMENT)
                .action(NotificationAction.LIKE_COMMENT)
                .build();
        notificationService.sendToFriends(notification);
    }

    private void validateCommentContent(String content, MultipartFile attachment) {
        boolean isContentEmpty = (content == null || content.trim().isEmpty());
        boolean hasNoAttachment = attachment == null || attachment.isEmpty();

        if (isContentEmpty && hasNoAttachment) {
            throw new ApiException(ErrorCode.COMMENT_CONTENT_AND_ATTACH_FILE_BOTH_EMPTY);
        }

        if (content != null && content.trim().length() > Comment.MAX_CONTENT_LENGTH) {
            throw new ApiException(ErrorCode.INVALID_COMMENT_CONTENT_LENGTH);
        }
    }

    private CommentResponse mapIsLiked(CommentResponse response, UUID userId) {
        response.setLiked(commentRepository.isLiked(response.getId(), userId));
        return response;
    }
}
