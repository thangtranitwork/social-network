package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.LoginRequest;
import com.stu.socialnetworkapi.dto.response.AuthenticationResponse;
import com.stu.socialnetworkapi.entity.Account;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.AccountRole;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.neo4j.AccountRepository;
import com.stu.socialnetworkapi.repository.redis.LoginAttemptRepository;
import com.stu.socialnetworkapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LoginAttemptRepository loginAttemptRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private HttpServletResponse httpResponse;

    @InjectMocks
    private AuthenticationServiceImpl authenticationServiceImpl;

    @Test
    void authenticate_ShouldReturnAccessToken_WhenSuccess() {
        // Given
        String email = "username@gmail.com";
        String password = "password";
        UUID uuid = UUID.randomUUID();
        AccountRole role = AccountRole.USER;
        String username = "username";
        String encodedPassword = "encoded_password";
        boolean isVerified = true;
        String accessToken = "access-token";

        LoginRequest loginRequest = new LoginRequest(email, password);
        User user = User.builder()
                .username(username)
                .build();

        Account account = Account.builder()
                .id(uuid)
                .email(email)
                .password(encodedPassword)
                .role(role)
                .isVerified(isVerified)
                .user(user)
                .build();

        when(loginAttemptRepository.isAccountLocked(uuid)).thenReturn(false);
        when(accountRepository.findByEmailAndRoleIs(loginRequest.email(), role))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches(password, account.getPassword()))
                .thenReturn(true);
        when(jwtUtil.generateAccessToken(uuid, username, account.getRole()))
                .thenReturn(accessToken);
        doNothing().when(jwtUtil).generateAndStoreRefreshToken(uuid, role, username, httpResponse);

        //When
        AuthenticationResponse authenticationResponse = authenticationServiceImpl.authenticate(loginRequest, httpResponse);

        //Then
        assertNotNull(authenticationResponse);
        assertEquals(accessToken, authenticationResponse.token());
        verify(loginAttemptRepository).loginSucceeded(uuid);
        verify(jwtUtil).generateAndStoreRefreshToken(uuid, role, username, httpResponse);
    }

    @Test
    void authenticate_ShouldThrowException_WhenAccountNotFound() {
        // Given
        String email = "username@gmail.com";
        String password = "password";
        AccountRole role = AccountRole.USER;

        ErrorCode expectedCode = ErrorCode.ACCOUNT_NOT_FOUND;

        LoginRequest loginRequest = new LoginRequest(email, password);

        when(accountRepository.findByEmailAndRoleIs(loginRequest.email(), role))
                .thenReturn(Optional.empty());

        //When
        ApiException ex = assertThrows(ApiException.class,
                () -> authenticationServiceImpl.authenticate(loginRequest, httpResponse));
        //Then
        assertNotNull(ex);
        assertEquals(expectedCode, ex.getErrorCode());
    }

    @Test
    void authenticate_ShouldThrowException_WhenAccountNotVerify() {
        // Given
        String email = "username@gmail.com";
        String password = "password";
        UUID uuid = UUID.randomUUID();
        AccountRole role = AccountRole.USER;
        String username = "username";
        String encodedPassword = "encoded_password";
        boolean isVerified = false;


        ErrorCode expectedCode = ErrorCode.ACCOUNT_NOT_VERIFIED;

        LoginRequest loginRequest = new LoginRequest(email, password);
        User user = User.builder()
                .username(username)
                .build();


        Account account = Account.builder()
                .id(uuid)
                .email(email)
                .password(encodedPassword)
                .role(role)
                .isVerified(isVerified)
                .user(user)
                .build();

        when(accountRepository.findByEmailAndRoleIs(loginRequest.email(), role))
                .thenReturn(Optional.of(account));

        //When
        ApiException ex = assertThrows(ApiException.class,
                () -> authenticationServiceImpl.authenticate(loginRequest, httpResponse));
        //Then
        assertNotNull(ex);
        assertEquals(expectedCode, ex.getErrorCode());
    }

    @Test
    void authenticate_ShouldThrowException_WhenWrongPassword() {
        // Given
        String email = "username@gmail.com";
        String password = "password";
        UUID uuid = UUID.randomUUID();
        AccountRole role = AccountRole.USER;
        String username = "username";
        String encodedPassword = "encoded_password";
        boolean isVerified = true;
        ErrorCode expectedCode = ErrorCode.AUTHENTICATION_FAILED;
        int failedAttempts = 0;

        LoginRequest loginRequest = new LoginRequest(email, password);
        User user = User.builder()
                .username(username)
                .build();

        Account account = Account.builder()
                .id(uuid)
                .email(email)
                .password(encodedPassword)
                .role(role)
                .isVerified(isVerified)
                .user(user)
                .build();

        when(loginAttemptRepository.isAccountLocked(uuid)).thenReturn(false);
        when(accountRepository.findByEmailAndRoleIs(loginRequest.email(), role))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches(password, account.getPassword()))
                .thenReturn(false);
        when(loginAttemptRepository.getFailedAttempts(account.getId())).thenReturn(failedAttempts);

        //When
        ApiException ex = assertThrows(ApiException.class,
                () -> authenticationServiceImpl.authenticate(loginRequest, httpResponse));
        //Then
        assertNotNull(ex);
        assertEquals(expectedCode, ex.getErrorCode());
    }

    @Test
    void authenticate_ShouldThrowException_WhenWrongPasswordAndFailedAttemptsEquals5() {
        // Given
        String email = "username@gmail.com";
        String password = "password";
        UUID uuid = UUID.randomUUID();
        AccountRole role = AccountRole.USER;
        String username = "username";
        String encodedPassword = "encoded_password";
        boolean isVerified = true;
        ErrorCode expectedCode = ErrorCode.ACCOUNT_LOCKED;
        int failedAttempts = 5;
        ZonedDateTime lockoutTime = ZonedDateTime.now();

        LoginRequest loginRequest = new LoginRequest(email, password);
        User user = User.builder()
                .username(username)
                .build();

        Account account = Account.builder()
                .id(uuid)
                .email(email)
                .password(encodedPassword)
                .role(role)
                .isVerified(isVerified)
                .user(user)
                .build();

        when(loginAttemptRepository.isAccountLocked(uuid)).thenReturn(false);
        when(accountRepository.findByEmailAndRoleIs(loginRequest.email(), role))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches(password, account.getPassword()))
                .thenReturn(false);
        when(loginAttemptRepository.getFailedAttempts(account.getId())).thenReturn(failedAttempts);
        when(loginAttemptRepository.getLockoutTime(account.getId())).thenReturn(lockoutTime);
        //When
        ApiException ex = assertThrows(ApiException.class,
                () -> authenticationServiceImpl.authenticate(loginRequest, httpResponse));
        //Then
        assertNotNull(ex);
        assertEquals(expectedCode, ex.getErrorCode());
        assertEquals(lockoutTime, ex.getAttributes().get("time"));
    }


    @Test
    void authenticateAdmin_ShouldReturnAccessToken_WhenSuccess() {
        // Given
        String email = "username@gmail.com";
        String password = "password";
        UUID uuid = UUID.randomUUID();
        AccountRole role = AccountRole.ADMIN;
        String username = "username";
        String encodedPassword = "encoded_password";
        boolean isVerified = true;
        String accessToken = "access-token";

        LoginRequest loginRequest = new LoginRequest(email, password);
        User user = User.builder()
                .username(username)
                .build();

        Account account = Account.builder()
                .id(uuid)
                .email(email)
                .password(encodedPassword)
                .role(role)
                .isVerified(isVerified)
                .user(user)
                .build();

        when(loginAttemptRepository.isAccountLocked(uuid)).thenReturn(false);
        when(accountRepository.findByEmailAndRoleIs(loginRequest.email(), role))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches(password, account.getPassword()))
                .thenReturn(true);
        when(jwtUtil.generateAccessToken(uuid, username, account.getRole()))
                .thenReturn(accessToken);
        doNothing().when(jwtUtil).generateAndStoreRefreshToken(uuid, role, username, httpResponse);

        //When
        AuthenticationResponse authenticationResponse = authenticationServiceImpl.authenticateAdmin(loginRequest, httpResponse);

        //Then
        assertNotNull(authenticationResponse);
        assertEquals(accessToken, authenticationResponse.token());
        verify(loginAttemptRepository).loginSucceeded(uuid);
        verify(jwtUtil).generateAndStoreRefreshToken(uuid, role, username, httpResponse);
    }

    @Test
    void authenticateAdmin_ShouldThrowException_WhenWrongPassword() {
        // Given
        String email = "username@gmail.com";
        String password = "password";
        UUID uuid = UUID.randomUUID();
        AccountRole role = AccountRole.ADMIN;
        String username = "username";
        String encodedPassword = "encoded_password";
        boolean isVerified = true;
        ErrorCode expectedCode = ErrorCode.AUTHENTICATION_FAILED;
        int failedAttempts = 0;

        LoginRequest loginRequest = new LoginRequest(email, password);
        User user = User.builder()
                .username(username)
                .build();

        Account account = Account.builder()
                .id(uuid)
                .email(email)
                .password(encodedPassword)
                .role(role)
                .isVerified(isVerified)
                .user(user)
                .build();

        when(loginAttemptRepository.isAccountLocked(uuid)).thenReturn(false);
        when(accountRepository.findByEmailAndRoleIs(loginRequest.email(), role))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches(password, account.getPassword()))
                .thenReturn(false);
        when(loginAttemptRepository.getFailedAttempts(account.getId())).thenReturn(failedAttempts);

        //When
        ApiException ex = assertThrows(ApiException.class,
                () -> authenticationServiceImpl.authenticateAdmin(loginRequest, httpResponse));
        //Then
        assertNotNull(ex);
        assertEquals(expectedCode, ex.getErrorCode());
    }

    @Test
    void authenticateAdmin_ShouldThrowException_WhenWrongPasswordAndFailedAttemptsEquals5() {
        // Given
        String email = "username@gmail.com";
        String password = "password";
        UUID uuid = UUID.randomUUID();
        AccountRole role = AccountRole.ADMIN;
        String username = "username";
        String encodedPassword = "encoded_password";
        boolean isVerified = true;
        ErrorCode expectedCode = ErrorCode.ACCOUNT_LOCKED;
        int failedAttempts = 5;
        ZonedDateTime lockoutTime = ZonedDateTime.now();

        LoginRequest loginRequest = new LoginRequest(email, password);
        User user = User.builder()
                .username(username)
                .build();

        Account account = Account.builder()
                .id(uuid)
                .email(email)
                .password(encodedPassword)
                .role(role)
                .isVerified(isVerified)
                .user(user)
                .build();

        when(loginAttemptRepository.isAccountLocked(uuid)).thenReturn(false);
        when(accountRepository.findByEmailAndRoleIs(loginRequest.email(), role))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches(password, account.getPassword()))
                .thenReturn(false);
        when(loginAttemptRepository.getFailedAttempts(account.getId())).thenReturn(failedAttempts);
        when(loginAttemptRepository.getLockoutTime(account.getId())).thenReturn(lockoutTime);
        //When
        ApiException ex = assertThrows(ApiException.class,
                () -> authenticationServiceImpl.authenticateAdmin(loginRequest, httpResponse));
        //Then
        assertNotNull(ex);
        assertEquals(expectedCode, ex.getErrorCode());
        assertEquals(lockoutTime, ex.getAttributes().get("time"));
    }
}