package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.RegisterRequest;
import com.stu.socialnetworkapi.dto.request.VerifyRequest;

public interface RegisterService {
    void register(RegisterRequest request);
    void verify(VerifyRequest request);
    void resend(String email);
}
