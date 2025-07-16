package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;

import java.util.List;

public interface RequestService {
    void sendAddFriendRequest(String username);

    List<UserCommonInformationResponse> getSentRequests();

    List<UserCommonInformationResponse> getReceivedRequests();

    void deleteRequest(String username);

    void acceptRequest(String username);
}
