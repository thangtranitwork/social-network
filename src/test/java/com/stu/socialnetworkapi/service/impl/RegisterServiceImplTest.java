package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.RegisterRequest;
import com.stu.socialnetworkapi.dto.request.VerifyRequest;
import com.stu.socialnetworkapi.entity.Account;
import com.stu.socialnetworkapi.entity.VerifyCode;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.neo4j.AccountRepository;
import com.stu.socialnetworkapi.repository.neo4j.VerifyCodeRepository;
import com.stu.socialnetworkapi.service.itf.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterServiceImplTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MailService mailService;
    @Mock
    private VerifyCodeRepository verifyCodeRepository;

    @InjectMocks
    private RegisterServiceImpl registerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(registerService, "frontEndOrigin", "http://localhost:3000");
    }

    @Test
    void register_ShouldSaveAccountAndSendEmail_WhenSuccess() {
        // Given
        String email = "user@example.com";
        String password = "password";
        String givenName = "John";
        String familyName = "Doe";
        LocalDate now = LocalDate.now();
        String encodedPassword = "encodedPassword";

        RegisterRequest request = new RegisterRequest(
                email, password, givenName, familyName, now
        );

        when(accountRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);

        // When
        registerService.register(request);

        // Then
        verify(accountRepository).save(any(Account.class));
        verify(mailService).sendHTML(
                eq(email),
                eq("Email Verification"),
                eq("register-verify-email"),
                anyMap()
        );
    }

    @Test
    void register_ShouldThrowException_WhenEmailIsUsed() {
        // Given
        String email = "user@example.com";
        String password = "password";
        String givenName = "John";
        String familyName = "Doe";
        LocalDate now = LocalDate.now();
        String encodedPassword = "encodedPassword";
        UUID uuid = UUID.randomUUID();

        ErrorCode expectedCode = ErrorCode.ACCOUNT_ALREADY_EXISTS;

        RegisterRequest request = new RegisterRequest(
                email, password, givenName, familyName, now
        );

        Account account = Account.builder()
                .id(uuid)
                .email(email)
                .password(encodedPassword)
                .isVerified(true)
                .build();

        when(accountRepository.findByEmail(request.email())).thenReturn(Optional.of(account));

        // When
        ApiException ex = assertThrows(ApiException.class, () -> registerService.register(request));

        // Then
        assertNotNull(ex);
        assertEquals(expectedCode, ex.getErrorCode());
    }

    @Test
    void register_ShouldThrowException_WhenEmailIsUsedAndNotVerified() {
        // Given
        String email = "user@example.com";
        String password = "password";
        String givenName = "John";
        String familyName = "Doe";
        LocalDate now = LocalDate.now();
        String encodedPassword = "encodedPassword";
        UUID uuid = UUID.randomUUID();

        ErrorCode expectedCode = ErrorCode.EMAIL_NOT_VERIFIED;

        RegisterRequest request = new RegisterRequest(
                email, password, givenName, familyName, now
        );

        Account account = Account.builder()
                .id(uuid)
                .email(email)
                .password(encodedPassword)
                .isVerified(false)
                .build();

        when(accountRepository.findByEmail(request.email())).thenReturn(Optional.of(account));

        // When
        ApiException ex = assertThrows(ApiException.class, () -> registerService.register(request));

        // Then
        assertNotNull(ex);
        assertEquals(expectedCode, ex.getErrorCode());
    }

    @Test
    void verify_ShouldMarkAccountAsVerified_WhenSuccess() {
        //Given
        String email = "user@example.com";
        UUID code = UUID.randomUUID();
        VerifyRequest request = new VerifyRequest(email, code);

        VerifyCode verifyCode = VerifyCode.builder()
                .code(code)
                .expiryTime(LocalDateTime.now().plusMinutes(15))
                .build();
        Account account = Account.builder()
                .email(email)
                .isVerified(false)
                .verifyCode(verifyCode)
                .build();

        when(accountRepository.findByEmail(request.email())).thenReturn(Optional.of(account));
        //When
        registerService.verify(request);
        //Then
        verify(accountRepository).save(account);
        verify(verifyCodeRepository).deleteById(request.code());
        assertTrue(account.isVerified());
    }

    @Test
    void verify_ShouldThrowException_WhenAccountNotFound() {
        //Given
        String email = "user@example.com";
        UUID code = UUID.randomUUID();
        ErrorCode expectedCode = ErrorCode.ACCOUNT_NOT_FOUND;

        VerifyRequest request = new VerifyRequest(email, code);

        when(accountRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        //When
        ApiException ex = assertThrows(ApiException.class, () -> registerService.verify(request));
        //Then
        assertEquals(expectedCode, ex.getErrorCode());
    }

    @Test
    void verify_ShouldThrowException_WhenVerifyCodeNotFound() {
        //Given
        String email = "user@example.com";
        UUID code = UUID.randomUUID();
        ErrorCode expectedCode = ErrorCode.VERIFICATION_CODE_NOT_FOUND;

        VerifyRequest request = new VerifyRequest(email, code);

        Account account = new Account();

        when(accountRepository.findByEmail(request.email())).thenReturn(Optional.of(account));
        //When
        ApiException ex = assertThrows(ApiException.class, () -> registerService.verify(request));
        //Then
        assertEquals(expectedCode, ex.getErrorCode());
    }

    @Test
    void verify_ShouldThrowException_WhenVerifyCodeNotMatch() {
        //Given
        String email = "user@example.com";
        UUID code = UUID.randomUUID();
        UUID actualCode = UUID.randomUUID();
        ErrorCode expectedCode = ErrorCode.VERIFICATION_CODE_NOT_MATCHED_OR_EXPIRED;

        VerifyRequest request = new VerifyRequest(email, code);

        VerifyCode verifyCode = VerifyCode.builder()
                .code(actualCode)
                .expiryTime(LocalDateTime.now().plusMinutes(15))
                .build();
        Account account = Account.builder()
                .verifyCode(verifyCode)
                .build();

        when(accountRepository.findByEmail(request.email())).thenReturn(Optional.of(account));
        //When
        ApiException ex = assertThrows(ApiException.class, () -> registerService.verify(request));
        //Then
        assertEquals(expectedCode, ex.getErrorCode());
    }

    @Test
    void verify_ShouldThrowException_WhenVerifyCodeIsExpired() {
        //Given
        String email = "user@example.com";
        UUID code = UUID.randomUUID();
        ErrorCode expectedCode = ErrorCode.VERIFICATION_CODE_NOT_MATCHED_OR_EXPIRED;

        VerifyRequest request = new VerifyRequest(email, code);

        VerifyCode verifyCode = VerifyCode.builder()
                .code(code)
                .expiryTime(LocalDateTime.now().minusMinutes(15))
                .build();
        Account account = Account.builder()
                .verifyCode(verifyCode)
                .build();

        when(accountRepository.findByEmail(request.email())).thenReturn(Optional.of(account));
        //When
        ApiException ex = assertThrows(ApiException.class, () -> registerService.verify(request));
        //Then
        assertEquals(expectedCode, ex.getErrorCode());
    }

    @Test
    void resend_ShouldSendEmail_WhenSuccess() {
        //Given
        String email = "user@example.com";
        UUID code = UUID.randomUUID();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15);

        VerifyCode verifyCode = VerifyCode.builder()
                .code(code)
                .expiryTime(expiryTime)
                .build();
        Account account = Account.builder()
                .email(email)
                .isVerified(false)
                .verifyCode(verifyCode)
                .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        //When
        registerService.resend(email);

        //Then
        verify(verifyCodeRepository).save(verifyCode);
        verify(mailService).sendHTML(
                eq(email),
                eq("Email Verification"),
                eq("register-verify-email"),
                anyMap()
        );
    }

    @Test
    void resend_ShouldThrowException_WhenAccountNotFound() {
        //Given
        String email = "user@example.com";
        ErrorCode expectedCode = ErrorCode.ACCOUNT_NOT_FOUND;
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());
        //When
        ApiException ex = assertThrows(ApiException.class, () -> registerService.resend(email));
        //Then
        assertNotNull(ex);
        assertEquals(expectedCode, ex.getErrorCode());
    }

    @Test
    void resend_ShouldThrowException_WhenAccountIsVerified() {
        //Given
        String email = "user@example.com";
        Account account = Account.builder()
                .email(email)
                .isVerified(true)
                .build();
        ErrorCode expectedCode = ErrorCode.ACCOUNT_VERIFIED;

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        //When
        ApiException ex = assertThrows(ApiException.class, () -> registerService.resend(email));
        //Then
        assertNotNull(ex);
        assertEquals(expectedCode, ex.getErrorCode());
    }
}