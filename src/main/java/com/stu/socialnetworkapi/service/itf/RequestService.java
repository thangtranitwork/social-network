package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;

import java.util.List;

public interface RequestService {
    void sendAddFriendRequest(String username);

    List<UserCommonInformationResponse> getSentRequests(Neo4jPageable pageable);

    List<UserCommonInformationResponse> getReceivedRequests(Neo4jPageable pageable);

    void deleteRequest(String username);

    void acceptRequest(String username);
}
