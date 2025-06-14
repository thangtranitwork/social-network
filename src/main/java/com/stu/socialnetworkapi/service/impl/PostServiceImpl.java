package com.stu.socialnetworkapi.service.impl;

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
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.PostMapper;
import com.stu.socialnetworkapi.repository.PostRepository;
import com.stu.socialnetworkapi.service.itf.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostMapper postMapper;
    private final UserService userService;
    private final FileService fileService;
    private final FriendService friendService;
    private final BlockService blockService;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Override
    public PostResponse get(UUID postId) {
        Post post = getPostById(postId);
        validateViewPost(post, userService.getCurrentUserId());
        return postMapper.toPostResponse(post);
    }

    @Override
    public List<PostResponse> getPostsOfUser(String authorUsername, Pageable pageable) {
        UUID currentUserId = userService.getCurrentUserId();
        UUID targetId = userService.getUserId(authorUsername);
        boolean isAuthenticated = currentUserId != null;
        boolean isAuthor = isAuthenticated && currentUserId.equals(targetId);

        if (isAuthenticated) {
            blockService.validateBlock(currentUserId, targetId);
        }

        List<PostPrivacy> visiblePrivacies;

        if (isAuthor) {
            // Tác giả có thể xem mọi bài viết của mình
            visiblePrivacies = List.of(PostPrivacy.PUBLIC, PostPrivacy.FRIEND, PostPrivacy.PRIVATE);
        } else if (isAuthenticated && friendService.isFriend(currentUserId, targetId)) {
            // Nếu là bạn bè ⇒ được xem bài public + friend
            visiblePrivacies = List.of(PostPrivacy.PUBLIC, PostPrivacy.FRIEND);
        } else {
            // Người lạ ⇒ chỉ được xem bài public
            visiblePrivacies = List.of(PostPrivacy.PUBLIC);
        }

        return postRepository
                .findAllByAuthorIdAndPrivacyIsIn(targetId, visiblePrivacies, pageable).stream()
                .map(postMapper::toPostResponse)
                .map(post -> mapIsLiked(post, currentUserId))
                .toList();
    }

    @Override
    public List<PostResponse> getSuggestedPosts(Pageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        return postRepository.findAllById(postRepository.getSuggestedPosts(currentUserId, pageable)).stream()
                .map(postMapper::toPostResponse)
                .map(post -> mapIsLiked(post, currentUserId))
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

        sendNotificationWhenPost(author, post);

        return response;
    }

    @Override
    public PostResponse share(SharePostRequest request) {
        String content = request.content();

        User author = userService.getCurrentUserRequiredAuthentication();
        Post originalPost = getPostById(request.originalPostId());
        UUID currentUserId = author.getId();
        UUID originalPostAuthorId = originalPost.getAuthor().getId();
        PostPrivacy originalPostPrivacy = originalPost.getPrivacy();
        validateSharePost(content, currentUserId, originalPostAuthorId, originalPostPrivacy);
        Post post = Post.builder()
                .author(author)
                .content(content.trim())
                .privacy(request.privacy())
                .originalPost(originalPost)
                .build();

        originalPost.setShareCount(originalPost.getShareCount() + 1);
        postRepository.saveAll(List.of(post, originalPost));

        sendNotificationWhenSharePost(author, originalPost, post);

        return postMapper.toPostResponse(post);
    }

    @Override
    public void delete(UUID postId) {
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
    public PostResponse updateContent(UUID postId, PostUpdateContentRequest request) {
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
        PostResponse response = postMapper.toPostResponse(postRepository.save(post));
        mapIsLiked(response, post.getAuthor().getId());
        return response;
    }

    @Override
    public void like(UUID postId) {
        User user = userService.getCurrentUserRequiredAuthentication();
        Post post = getPostById(postId);

        blockService.validateBlock(user.getId(), post.getId());
        if (postRepository.isLiked(post.getId(), user.getId())) {
            throw new ApiException(ErrorCode.LIKED_POST);
        }
        post.getLiker().add(user);
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
        sendNotificationWhenLikePost(postId, user, post);
    }

    @Override
    public void unlike(UUID postId) {
        User user = userService.getCurrentUserRequiredAuthentication();
        Post post = getPostById(postId);

        blockService.validateBlock(user.getId(), post.getId());
        if (!postRepository.isLiked(post.getId(), user.getId())) {
            throw new ApiException(ErrorCode.NOT_LIKED_POST);
        }
        post.getLiker().remove(user);
        post.setLikeCount(post.getLikeCount() - 1);
        postRepository.save(post);
    }

    @Override
    public void validateViewPost(UUID postId, UUID viewerId) {
        Post post = getPostById(postId);
        validateViewPost(post, viewerId);
    }

    @Override
    public Post getPostById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
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

    private void validateViewPost(Post post, UUID viewerId) {
        PostPrivacy privacy = post.getPrivacy();
        UUID authorId = post.getAuthor().getId();

        boolean isAuthenticated = viewerId != null;
        boolean isAuthor = isAuthenticated && viewerId.equals(authorId);

        if (isAuthor) {
            return;
        }
        if (PostPrivacy.PUBLIC.equals(privacy)) {
            if (isAuthenticated) blockService.validateBlock(viewerId, authorId);
            return;
        }
        // Friend can not be blocked
        if (PostPrivacy.FRIEND.equals(privacy)) {
            if (!isAuthenticated || !friendService.isFriend(viewerId, authorId)) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }
        //Other case: PRIVATE, ...
        throw new ApiException(ErrorCode.UNAUTHORIZED);
    }

    private void validateSharePost(String content, UUID currentUserId, UUID originalPostAuthorId, PostPrivacy originalPostPrivacy) {
        if (content != null && content.length() > Post.MAX_CONTENT_LENGTH) {
            throw new ApiException(ErrorCode.INVALID_POST_CONTENT_LENGTH);
        }
        if (!PostPrivacy.PUBLIC.equals(originalPostPrivacy)) {
            throw new ApiException(ErrorCode.ONLY_PUBLIC_POST_CAN_BE_SHARED);
        }
        blockService.validateBlock(currentUserId, originalPostAuthorId);
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

    private PostResponse mapIsLiked(PostResponse post, UUID userId) {
        post.setLiked(postRepository.isLiked(post.getId(), userId));
        return post;
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