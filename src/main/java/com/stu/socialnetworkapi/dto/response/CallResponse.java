package com.stu.socialnetworkapi.dto.response;

import com.stu.socialnetworkapi.enums.CallAction;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CallResponse {
    CallAction action;
    UserCommonInformationResponse caller;
}
