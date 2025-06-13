package com.stu.socialnetworkapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BlockResponse {
    UUID uuid;
    UserCommonInformationResponse user;
}
