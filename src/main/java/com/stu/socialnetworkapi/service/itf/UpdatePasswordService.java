package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.UpdatePasswordRequest;
import com.stu.socialnetworkapi.dto.request.VerifyRequest;

public interface UpdatePasswordService {
    void send(String email, String continueUrl);
    void verify(VerifyRequest request);
    void updatePassword(UpdatePasswordRequest request);
}
