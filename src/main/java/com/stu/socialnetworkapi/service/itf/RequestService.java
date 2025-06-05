package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.RequestResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RequestService {
    void sendAddFriendRequest(String username);

    List<RequestResponse> getSentRequests(Pageable pageable);

    List<RequestResponse> getReceivedRequests(Pageable pageable);

    void deleteRequest(UUID uuid);

    void acceptRequest(UUID uuid);
}
