package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.PostRequest;
import com.stu.socialnetworkapi.dto.response.PostResponse;
import com.stu.socialnetworkapi.enums.PostPrivacy;
import com.stu.socialnetworkapi.service.itf.PostService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    @Override
    public PostResponse get(UUID postId) {
        return null;
    }

    @Override
    public Slice<PostResponse> getSuggestedPosts() {
        return null;
    }

    @Override
    public PostResponse post(PostRequest request) {
        return null;
    }

    @Override
    public PostResponse share(PostRequest request) {
        return null;
    }

    @Override
    public void delete(UUID postId) {

    }

    @Override
    public void updatePrivacy(UUID postId, PostPrivacy privacy) {

    }

    @Override
    public PostResponse updateContent(UUID postId, PostRequest request) {
        return null;
    }

    @Override
    public void pin(UUID postId) {

    }

    @Override
    public void unpin() {

    }

    @Override
    public void store(UUID postId) {

    }

    @Override
    public void like(UUID postId) {

    }

    @Override
    public void unlike(UUID postId) {

    }
}
