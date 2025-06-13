package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.RequestResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RequestService {
    void sendAddFriendRequest(String username);

    List<RequestResponse> getSentRequests(Pageable pageable);

    List<RequestResponse> getReceivedRequests(Pageable pageable);

    void deleteRequest(String username);

    void acceptRequest(String username);
}
