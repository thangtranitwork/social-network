package com.stu.socialnetworkapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class OnlineResponse {
    boolean isOnline;
    ZonedDateTime lastOnlineAt;
}
