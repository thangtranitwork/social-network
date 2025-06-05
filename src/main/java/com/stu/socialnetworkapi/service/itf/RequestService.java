package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.RequestResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface RequestService {
    void sendAddFriendRequest(String username);

    Slice<RequestResponse> getSentRequests(Pageable pageable);

    Slice<RequestResponse> getReceivedRequests(Pageable pageable);

    void deleteRequest(UUID uuid);

    void acceptRequest(UUID uuid);
}
