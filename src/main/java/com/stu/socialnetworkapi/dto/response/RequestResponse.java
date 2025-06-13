package com.stu.socialnetworkapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class RequestResponse {
    UUID uuid;
    ZonedDateTime sentAt;
    UserCommonInformationResponse user;
}
