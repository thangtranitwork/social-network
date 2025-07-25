package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.projection.CommentProjection;
import com.stu.socialnetworkapi.dto.request.CommentRequest;
import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.request.ReplyCommentRequest;
import com.stu.socialnetworkapi.dto.response.CommentResponse;
import com.stu.socialnetworkapi.entity.*;
import com.stu.socialnetworkapi.enums.NotificationAction;
import com.stu.socialnetworkapi.enums.ObjectType;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.CommentMapper;
import com.stu.socialnetworkapi.repository.neo4j.CommentRepository;
import com.stu.socialnetworkapi.repository.neo4j.KeywordRepository;
import com.stu.socialnetworkapi.repository.neo4j.PostRepository;
import com.stu.socialnetworkapi.service.itf.*;
import com.stu.socialnetworkapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final KeywordRepository keywordRepository;
    private final NotificationService notificationService;

    @Override
    public CommentResponse comment(CommentRequest request) {
        User author = userService.getCurrentUserRequiredAuthentication();
        validateCommentContent(request.content(), request.file());
        postService.validateViewPost(request.postId(), author.getUsername());
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
        keywordRepository.interact(post.getId(), author.getId(), Keyword.COMMENT_SCORE);
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
        postService.validateViewPost(post.getId(), author.getUsername());
        blockService.validateBlock(post.getAuthor().getUsername(), author.getUsername());
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
        keywordRepository.interact(post.getId(), author.getId(), Keyword.COMMENT_SCORE);
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
        Comment originalComment = comment.getOriginalComment();
        Post post = !comment.isRepliedComment()
                ? comment.getPost()
                : originalComment.getPost();
        boolean isAdmin = jwtUtil.isAdmin();
        boolean isPostAuthor = post.getAuthor().getId().equals(user.getId());
        boolean isCommentAuthor = user.getId().equals(comment.getAuthor().getId());

        if (!isAdmin && !isPostAuthor && !isCommentAuthor) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (comment.isRepliedComment()) {
            File attachment = comment.getAttachedFile();
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
            if ((isAdmin || isPostAuthor) && !isCommentAuthor) sendNotificationWhenDeleteComment(user, comment);
        }
    }

    @Override
    public CommentResponse updateContent(UUID commentId, CommentRequest request) {
        Comment comment = getCommentById(commentId);
        String trimmed = request.content().trim();
        validateUpdateContent(comment, trimmed);
        comment.setContent(trimmed);
        commentRepository.save(comment);
        return commentMapper.toCommentResponse(comment);
    }

    @Override
    public List<CommentResponse> getComments(UUID postId, Neo4jPageable pageable) {
        String currentUsername = userService.getCurrentUsernameRequiredAuthentication();
        postService.validateViewPost(postId, currentUsername);
        List<CommentProjection> projections = (switch (pageable.getType()) {
            case RELEVANT ->
                    commentRepository.getSuggestedComments(postId, currentUsername, pageable.getSkip(), pageable.getLimit());
            case FRIEND_ONLY ->
                    commentRepository.getFriendComments(postId, currentUsername, pageable.getSkip(), pageable.getLimit());
            case TIME ->
                    commentRepository.getCommentsOrderByCreatedAtDesc(postId, currentUsername, pageable.getSkip(), pageable.getLimit());
        });
        return projections.stream()
                .map(commentMapper::toCommentResponse)
                .toList();
    }

    @Override
    public List<CommentResponse> getRepliedComments(UUID commentId, Neo4jPageable pageable) {
        UUID currentUserId = userService.getCurrentUserId();
        return commentRepository.findRepliedCommentByOriginalCommentId(commentId, currentUserId, pageable.getSkip(), pageable.getLimit()).stream()
                .map(commentMapper::toCommentResponse)
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

    private void validateUpdateContent(Comment comment, String trimmedContent) {
        UUID currentUserId = userService.getCurrentUserId();
        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (trimmedContent == null || trimmedContent.isEmpty() || trimmedContent.length() > Comment.MAX_CONTENT_LENGTH) {
            throw new ApiException(ErrorCode.INVALID_COMMENT_CONTENT_LENGTH);
        }
        if (trimmedContent.equals(comment.getContent())) {
            throw new ApiException(ErrorCode.COMMENT_CONTENT_UNCHANGED);
        }
    }

    private void sendNotificationWhenDeleteComment(User user, Comment comment) {
        Notification notification = Notification.builder()
                .creator(user)
                .receiver(comment.getAuthor())
                .targetType(ObjectType.COMMENT)
                .targetId(comment.getId())
                .action(NotificationAction.DELETE_COMMENT)
                .build();
        notificationService.send(notification);
    }
}
