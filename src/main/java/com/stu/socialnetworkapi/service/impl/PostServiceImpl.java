package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.projection.PostProjection;
import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.request.PostRequest;
import com.stu.socialnetworkapi.dto.request.PostUpdateContentRequest;
import com.stu.socialnetworkapi.dto.request.SharePostRequest;
import com.stu.socialnetworkapi.dto.response.PostResponse;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.Notification;
import com.stu.socialnetworkapi.entity.Post;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.NotificationAction;
import com.stu.socialnetworkapi.enums.ObjectType;
import com.stu.socialnetworkapi.enums.PostPrivacy;
import com.stu.socialnetworkapi.event.PostCreatedEvent;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.PostMapper;
import com.stu.socialnetworkapi.repository.neo4j.FileRepository;
import com.stu.socialnetworkapi.repository.neo4j.KeywordRepository;
import com.stu.socialnetworkapi.repository.neo4j.PostRepository;
import com.stu.socialnetworkapi.service.itf.*;
import com.stu.socialnetworkapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final JwtUtil jwtUtil;
    private final PostMapper postMapper;
    private final UserService userService;
    private final FileService fileService;
    private final FriendService friendService;
    private final BlockService blockService;
    private final PostRepository postRepository;
    private final FileRepository fileRepository;
    private final KeywordRepository keywordRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public PostResponse get(UUID postId) {
        String currentUsername = userService.getCurrentUserUsername();
        PostProjection projection = postRepository.findPostProjectionById(postId, currentUsername)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        validateViewPost(projection, currentUsername);
        if (currentUsername != null) keywordRepository.interact(postId, currentUsername, 1);
        return postMapper.toPostResponse(projection);
    }

    @Override
    public List<PostResponse> getPostsOfUser(String authorUsername, Neo4jPageable pageable) {
        String currentUsername = userService.getCurrentUsernameRequiredAuthentication();
        userService.validateUserExists(authorUsername);
        blockService.validateBlock(currentUsername, authorUsername);
        return postRepository
                .findAllByAuthorUsername(authorUsername, currentUsername, pageable.getSkip(), pageable.getLimit()).stream()
                .map(postMapper::toPostResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public List<PostResponse> getAllPosts(Neo4jPageable pageable) {
        return postRepository.getAllOrderByCreatedAtDesc(pageable.getSkip(), pageable.getLimit()).stream()
                .map(postMapper::toPostResponse)
                .toList();
    }

    @Override
    public List<PostResponse> getSuggestedPosts(Neo4jPageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();

        List<PostProjection> projections = switch (pageable.getType()) {
            case RELEVANT -> postRepository.getSuggestedPosts(currentUserId, pageable.getSkip(), pageable.getLimit());
            case FRIEND_ONLY ->
                    postRepository.getFriendPostsOnly(currentUserId, pageable.getSkip(), pageable.getLimit());
            case TIME ->
                    postRepository.getPostsOrderByCreatedAtDesc(currentUserId, pageable.getSkip(), pageable.getLimit());
        };

        return projections.stream()
                .map(postMapper::toPostResponse)
                .toList();
    }


    @Override
    public PostResponse post(PostRequest request) {
        String content = request.content();
        List<MultipartFile> files = request.files();
        validatePostRequest(content, files);
        User author = userService.getCurrentUserRequiredAuthentication();
        List<File> uploadedFiles = files != null && !files.isEmpty()
                ? fileService.upload(files)
                : null;

        Post post = Post.builder()
                .author(author)
                .content(content.trim())
                .attachedFiles(uploadedFiles)
                .privacy(request.privacy())
                .build();
        PostResponse response = postMapper.toPostResponse(postRepository.save(post));
        eventPublisher.publishEvent(new PostCreatedEvent(post.getId()));
        sendNotificationWhenPost(author, post);
        return response;
    }

    @Override
    public PostResponse share(SharePostRequest request) {
        String content = request.content();

        User author = userService.getCurrentUserRequiredAuthentication();
        Post originalPost = getPostById(request.originalPostId());
        PostPrivacy originalPostPrivacy = originalPost.getPrivacy();
        validateSharePost(content, author.getUsername(), originalPost.getAuthor().getUsername(), originalPostPrivacy);
        Post post = Post.builder()
                .author(author)
                .content(content.trim())
                .privacy(request.privacy())
                .originalPost(originalPost)
                .build();

        originalPost.setShareCount(originalPost.getShareCount() + 1);
        postRepository.saveAll(List.of(post, originalPost));
        keywordRepository.interact(originalPost.getId(), author.getId(), 5);
        eventPublisher.publishEvent(new PostCreatedEvent(post.getId()));
        sendNotificationWhenSharePost(author, originalPost, post);

        return postMapper.toPostResponse(post);
    }

    @Override
    public void delete(UUID postId) {
        User user = userService.getCurrentUserRequiredAuthentication();
        Post post = getPostById(postId);
        boolean isAdmin = jwtUtil.isAdmin();
        boolean isAuthor = post.getAuthor().getId().equals(user.getId());
        if (!isAdmin && !isAuthor) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        post.setContent("deleted");
        post.setDeletedAt(ZonedDateTime.now());
        List<File> files = post.getAttachedFiles();
        post.setAttachedFiles(null);
        postRepository.save(post);
        fileService.deleteFiles(files);
        if (isAdmin) {
            Notification notification = Notification.builder()
                    .creator(user)
                    .receiver(post.getAuthor())
                    .targetId(post.getId())
                    .targetType(ObjectType.POST)
                    .action(NotificationAction.DELETE_POST)
                    .build();
            notificationService.send(notification);
        }
    }

    @Override
    public void updatePrivacy(UUID postId, PostPrivacy privacy) {
        Post post = getPostById(postId);
        validateAuthor(post);
        if (post.getPrivacy().equals(privacy)) {
            throw new ApiException(ErrorCode.PRIVACY_UNCHANGED);
        }
        post.setPrivacy(privacy);
        postRepository.save(post);
    }

    @Override
    public void updateContent(UUID postId, PostUpdateContentRequest request) {
        Post post = getPostById(postId);
        validateAuthor(post);
        validateUpdateContentPost(post, request);
        if (post.isSharedPost()) {
            post.setContent(request.content() != null ? request.content().trim() : "");
        } else {
            processUpdateFile(request, post);
            String trimmedContent = request.content() != null
                    ? request.content().trim()
                    : null;
            post.setContent(trimmedContent);
        }
        post.setUpdatedAt(ZonedDateTime.now());
        postRepository.save(post);
    }

    @Override
    public void like(UUID postId) {
        User user = userService.getCurrentUserRequiredAuthentication();
        Post post = getPostById(postId);

        blockService.validateBlock(user.getUsername(), post.getAuthor().getUsername());
        if (postRepository.isLiked(post.getId(), user.getId())) {
            throw new ApiException(ErrorCode.LIKED_POST);
        }
        post.getLiker().add(user);
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
        keywordRepository.interact(postId, user.getId(), 2);
        sendNotificationWhenLikePost(postId, user, post);
    }

    @Override
    public void unlike(UUID postId) {
        User user = userService.getCurrentUserRequiredAuthentication();
        Post post = getPostById(postId);

        blockService.validateBlock(user.getUsername(), post.getAuthor().getUsername());
        if (!postRepository.isLiked(post.getId(), user.getId())) {
            throw new ApiException(ErrorCode.NOT_LIKED_POST);
        }
        post.getLiker().remove(user);
        post.setLikeCount(post.getLikeCount() - 1);
        postRepository.save(post);
    }

    @Override
    public void validateViewPost(UUID postId, String viewerUsername) {
        Post post = getPostById(postId);
        validateViewPost(post, viewerUsername);
    }

    @Override
    public Post getPostById(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
        if (post.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.DELETED_POST);
        }
        return post;
    }

    @Override
    public List<String> getFilesInPostsOfUser(String username, Neo4jPageable pageable) {
        String currentUsername = userService.getCurrentUsernameRequiredAuthentication();
        userService.validateUserExists(username);
        blockService.validateBlock(currentUsername, username);
        return fileRepository.findFileInPostByUsername(username, currentUsername, pageable.getSkip(), pageable.getLimit())
                .stream().map(File::getPath)
                .toList();
    }

    private static void validatePostRequest(String content, List<MultipartFile> files) {
        boolean isContentEmpty = (content == null || content.trim().isEmpty());
        boolean hasNoAttachment = (files == null || files.isEmpty());
        if (isContentEmpty && hasNoAttachment) {
            throw new ApiException(ErrorCode.POST_CONTENT_AND_ATTACH_FILES_BOTH_EMPTY);
        }

        if (content != null && content.trim().length() > Post.MAX_CONTENT_LENGTH) {
            throw new ApiException(ErrorCode.INVALID_POST_CONTENT_LENGTH);
        }

        if (files != null && files.size() > Post.MAX_ATTACH_FILES) {
            throw new ApiException(ErrorCode.INVALID_NUMBER_OF_POST_ATTACHMENTS);
        }
    }

    private void validateUpdateContentPost(Post post, PostUpdateContentRequest request) {
        if (post.isSharedPost()) validateSharedPost(post, request);
        else validateUpdateContentNormalPost(post, request);
    }

    private void validateViewPost(Post post, String viewerUsername) {
        PostPrivacy privacy = post.getPrivacy();
        String authorUsername = post.getAuthor().getUsername();
        boolean isAuthenticated = viewerUsername != null;
        boolean isAuthor = isAuthenticated && viewerUsername.equals(authorUsername);

        if (isAuthor) {
            return;
        }
        if (PostPrivacy.PUBLIC.equals(privacy)) {
            if (isAuthenticated) blockService.validateBlock(viewerUsername, authorUsername);
            return;
        }
        // Friend can not be blocked
        if (PostPrivacy.FRIEND.equals(privacy)) {
            if (!isAuthenticated || !friendService.isFriend(viewerUsername, authorUsername)) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }
        //Other case: PRIVATE, ...
        throw new ApiException(ErrorCode.UNAUTHORIZED);
    }

    private void validateViewPost(PostProjection projection, String viewerUsername) {
        PostPrivacy privacy = projection.privacy();

        boolean isAuthenticated = viewerUsername != null;
        boolean isAuthor = isAuthenticated && viewerUsername.equals(projection.authorUsername());

        if (isAuthor) {
            return;
        }
        if (PostPrivacy.PUBLIC.equals(privacy)) {
            if (isAuthenticated) blockService.validateBlock(viewerUsername, projection.authorUsername());
            return;
        }
        // Friend can not be blocked
        if (PostPrivacy.FRIEND.equals(privacy)) {
            if (!isAuthenticated || !projection.isFriend()) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }
        //Other case: PRIVATE, ...
        throw new ApiException(ErrorCode.UNAUTHORIZED);
    }

    private void validateSharePost(String content, String currentUserUsername, String originalPostAuthorUsername, PostPrivacy originalPostPrivacy) {
        if (content != null && content.length() > Post.MAX_CONTENT_LENGTH) {
            throw new ApiException(ErrorCode.INVALID_POST_CONTENT_LENGTH);
        }
        if (!PostPrivacy.PUBLIC.equals(originalPostPrivacy)) {
            throw new ApiException(ErrorCode.ONLY_PUBLIC_POST_CAN_BE_SHARED);
        }
        blockService.validateBlock(currentUserUsername, originalPostAuthorUsername);
    }


    private static void validateSharedPost(Post post, PostUpdateContentRequest request) {
        if (post.getContent() == null && request.content() == null)
            throw new ApiException(ErrorCode.POST_CONTENT_UNCHANGED);
        if (request.content() != null) {
            String trimmedContent = request.content().trim();
            if (trimmedContent.length() > Post.MAX_CONTENT_LENGTH) {
                throw new ApiException(ErrorCode.INVALID_POST_CONTENT_LENGTH);
            }
            if (post.getContent() != null && post.getContent().equals(trimmedContent)) {
                throw new ApiException(ErrorCode.POST_CONTENT_UNCHANGED);
            }
        }
    }

    private static void validateUpdateContentNormalPost(Post post, PostUpdateContentRequest request) {
        int totalFileCountAfterUpdate = validateUpdateAttachment(post, request);

        String trimmedContent = (request.content() != null) ? request.content().trim() : null;
        boolean isContentEmpty = (trimmedContent == null || trimmedContent.isEmpty());
        boolean hasNoAttachment = totalFileCountAfterUpdate == 0;
        if (hasNoAttachment && isContentEmpty) {
            throw new ApiException(ErrorCode.POST_CONTENT_AND_ATTACH_FILES_BOTH_EMPTY);
        }
        if (trimmedContent != null && trimmedContent.length() > Post.MAX_CONTENT_LENGTH) {
            throw new ApiException(ErrorCode.INVALID_POST_CONTENT_LENGTH);
        }
    }

    private static int validateUpdateAttachment(Post post, PostUpdateContentRequest request) {
        Set<String> currentFileIds = post.getAttachedFiles().stream()
                .map(File::getId)
                .collect(Collectors.toSet());

        List<String> deleteFileIds = Optional.ofNullable(request.deleteOldFileUrls())
                .orElse(Collections.emptyList())
                .stream()
                .map(File::getId)
                .toList();

        if (!currentFileIds.containsAll(deleteFileIds)) {
            throw new ApiException(ErrorCode.INVALID_DELETE_ATTACHMENT);
        }

        int remainingFileCount = currentFileIds.size() - deleteFileIds.size();
        int newFileCount = Optional.ofNullable(request.newFiles())
                .orElse(Collections.emptyList())
                .size();
        int totalFileCount = remainingFileCount + newFileCount;

        if (totalFileCount > Post.MAX_ATTACH_FILES) {
            throw new ApiException(ErrorCode.INVALID_NUMBER_OF_POST_ATTACHMENTS);
        }
        return totalFileCount;
    }

    private void processUpdateFile(PostUpdateContentRequest request, Post post) {
        List<File> addedFiles = new ArrayList<>();
        if (request.newFiles() != null && !request.newFiles().isEmpty()) {
            addedFiles.addAll(fileService.upload(request.newFiles()));
        }

        if (request.deleteOldFileUrls() != null && !request.deleteOldFileUrls().isEmpty()) {
            Set<String> deleteFileIds = request.deleteOldFileUrls().stream()
                    .map(File::getId)
                    .collect(Collectors.toSet());

            List<File> deleteFiles = post.getAttachedFiles().stream()
                    .filter(file -> deleteFileIds.contains(file.getId()))
                    .toList();

            fileService.deleteFiles(deleteFiles);

            post.getAttachedFiles().removeAll(deleteFiles);
        }

        if (!addedFiles.isEmpty()) {
            post.getAttachedFiles().addAll(addedFiles);
        }
    }

    private void validateAuthor(Post post) {
        UUID currentUserId = userService.getCurrentUserRequiredAuthentication().getId();
        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void sendNotificationWhenPost(User author, Post post) {
        Notification notification = Notification.builder()
                .creator(author)
                .targetId(post.getId())
                .targetType(ObjectType.POST)
                .action(NotificationAction.POST)
                .sentAt(post.getCreatedAt())
                .build();
        notificationService.sendToFriends(notification);
    }

    private void sendNotificationWhenSharePost(User author, Post originalPost, Post post) {
        Notification notificationToAuthor = Notification.builder()
                .creator(author)
                .receiver(originalPost.getAuthor())
                .targetId(post.getId())
                .targetType(ObjectType.POST)
                .action(NotificationAction.SHARE)
                .sentAt(post.getCreatedAt())
                .build();

        Notification notificationToFriend = Notification.builder()
                .creator(author)
                .targetId(post.getId())
                .targetType(ObjectType.POST)
                .action(NotificationAction.POST)
                .sentAt(post.getCreatedAt())
                .build();

        notificationService.send(notificationToAuthor);
        notificationService.sendToFriends(notificationToFriend);
    }

    private void sendNotificationWhenLikePost(UUID postId, User user, Post post) {
        Notification notification = Notification.builder()
                .targetId(postId)
                .targetType(ObjectType.POST)
                .creator(user)
                .receiver(post.getAuthor())
                .action(NotificationAction.LIKE_POST)
                .build();
        notificationService.send(notification);
    }
}