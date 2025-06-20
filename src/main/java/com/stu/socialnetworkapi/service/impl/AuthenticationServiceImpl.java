package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.LoginRequest;
import com.stu.socialnetworkapi.dto.response.AuthenticationResponse;
import com.stu.socialnetworkapi.entity.Account;
import com.stu.socialnetworkapi.enums.AccountRole;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.AccountRepository;
import com.stu.socialnetworkapi.repository.LoginAttemptRepository;
import com.stu.socialnetworkapi.service.itf.AuthenticationService;
import com.stu.socialnetworkapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final int MAX_ATTEMPTS = 5;

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptRepository loginAttemptRepository;
    private final JwtUtil jwtUtil;

    @Override
    public AuthenticationResponse authenticate(LoginRequest request, HttpServletResponse response) {
        Account account = accountRepository.findByEmailAndRoleIs(request.email(), AccountRole.USER)
                .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
        validateLogin(account);
        boolean matches = passwordEncoder.matches(request.password(), account.getPassword());
        if (!matches) processLoginFailed(account);

        return processLoginSucceed(account, response);
    }

    @Override
    public AuthenticationResponse authenticateAdmin(LoginRequest request, HttpServletResponse response) {
        Account account = accountRepository.findByEmailAndRoleIs(request.email(), AccountRole.ADMIN)
                .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
        validateLogin(account);
        boolean matches = passwordEncoder.matches(request.password(), account.getPassword());
        if (!matches) processLoginFailed(account);

        return processLoginSucceed(account, response);
    }

    @Override
    public AuthenticationResponse refresh(String refreshToken) {
        String accessToken = jwtUtil.refreshAccessToken(refreshToken);
        return new AuthenticationResponse(accessToken);
    }

    @Override
    public void logout(String token, HttpServletResponse response) {
        jwtUtil.revokeRefreshToken(token, response);
    }

    private void validateLogin(Account account) {
        if (!account.isVerified())
            throw new ApiException(ErrorCode.ACCOUNT_NOT_VERIFIED);

        boolean isLocked = loginAttemptRepository.isAccountLocked(account.getId());
        if (isLocked)
            throw new ApiException(
                    ErrorCode.ACCOUNT_LOCKED,
                    Map.of("time", loginAttemptRepository.getLockoutTime(account.getId())));
    }

    private AuthenticationResponse processLoginSucceed(Account account, HttpServletResponse response) {
        loginAttemptRepository.loginSucceeded(account.getId());
        String username = account.getUser().getUsername();
        String accessToken = jwtUtil.generateAccessToken(account.getId(), username, account.getRole());
        jwtUtil.generateAndStoreRefreshToken(account.getId(), account.getRole(), username, response);
        return new AuthenticationResponse(accessToken);
    }

    private void processLoginFailed(Account account) {
        loginAttemptRepository.loginFailed(account.getId());
        int failedAttempts = loginAttemptRepository.getFailedAttempts(account.getId());

        if (failedAttempts == 5)
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED, Map.of("time", loginAttemptRepository.getLockoutTime(account.getId())));
        else
            throw new ApiException(ErrorCode.AUTHENTICATION_FAILED, Map.of("remainingAttempts", MAX_ATTEMPTS - failedAttempts));
    }

}
