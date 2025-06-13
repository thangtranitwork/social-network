package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RequestService {
    void sendAddFriendRequest(String username);

    List<UserCommonInformationResponse> getSentRequests(Pageable pageable);

    List<UserCommonInformationResponse> getReceivedRequests(Pageable pageable);

    void deleteRequest(String username);

    void acceptRequest(String username);
}
