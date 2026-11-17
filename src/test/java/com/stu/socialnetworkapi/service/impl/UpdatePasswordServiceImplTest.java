package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.UpdatePasswordRequest;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePasswordServiceImplTest {

    @Mock
    private MailService mailService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private VerifyCodeRepository verifyCodeRepository;

    @InjectMocks
    private UpdatePasswordServiceImpl updatePasswordService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(updatePasswordService, "frontEndOrigin", "https://app.example.com");
    }

    @Test
    void send_ShouldThrowInvalidInput_WhenContinueUrlHasUntrustedOrigin() {
        ApiException ex = assertThrows(ApiException.class,
                () -> updatePasswordService.send("user@example.com", "https://evil.com/reset"));
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
        verifyNoInteractions(accountRepository, mailService);
    }

    @Test
    void send_ShouldReplaceExistingVerifyCode_WhenCalledAgain() {
        UUID oldCode = UUID.randomUUID();
        VerifyCode existing = VerifyCode.builder()
                .code(oldCode)
                .verified(true)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .verifyCode(existing)
                .build();

        when(accountRepository.findByEmail("user@example.com")).thenReturn(Optional.of(account));

        updatePasswordService.send("user@example.com", "https://app.example.com/reset-password");

        verify(verifyCodeRepository).deleteById(oldCode);
        verify(verifyCodeRepository).save(argThat(vc -> !vc.getCode().equals(oldCode) && !vc.isVerified()));
        verify(mailService).sendHTML(eq("user@example.com"), anyString(), eq("update-password-email"), anyMap());
    }

    @Test
    void verify_ShouldThrow_WhenEmailDoesNotMatchVerifyCodeAccount() {
        UUID code = UUID.randomUUID();
        Account owner = Account.builder().email("owner@example.com").build();
        VerifyCode verifyCode = VerifyCode.builder()
                .code(code)
                .account(owner)
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .build();

        when(verifyCodeRepository.findById(code)).thenReturn(Optional.of(verifyCode));

        ApiException ex = assertThrows(ApiException.class,
                () -> updatePasswordService.verify(new VerifyRequest("attacker@example.com", code)));
        assertEquals(ErrorCode.VERIFICATION_CODE_NOT_MATCHED_OR_EXPIRED, ex.getErrorCode());
    }

    @Test
    void updatePassword_ShouldUpdatePasswordAndDeleteVerifyCode_WhenVerifiedAndNotExpired() {
        UUID code = UUID.randomUUID();
        VerifyCode verifyCode = VerifyCode.builder()
                .code(code)
                .verified(true)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .verifyCode(verifyCode)
                .build();

        when(accountRepository.findByEmail("user@example.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.encode("NewPass123!")).thenReturn("encoded-pass");

        updatePasswordService.updatePassword(new UpdatePasswordRequest("user@example.com", "NewPass123!"));

        assertEquals("encoded-pass", account.getPassword());
        assertNull(account.getVerifyCode());
        verify(accountRepository).save(account);
        verify(verifyCodeRepository).deleteById(code);
    }

    @Test
    void updatePassword_ShouldThrow_WhenVerifyCodeIsExpired() {
        VerifyCode verifyCode = VerifyCode.builder()
                .code(UUID.randomUUID())
                .verified(true)
                .expiryTime(LocalDateTime.now().minusMinutes(1))
                .build();
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .verifyCode(verifyCode)
                .build();

        when(accountRepository.findByEmail("user@example.com")).thenReturn(Optional.of(account));

        ApiException ex = assertThrows(ApiException.class,
                () -> updatePasswordService.updatePassword(new UpdatePasswordRequest("user@example.com", "NewPass123!")));
        assertEquals(ErrorCode.VERIFICATION_CODE_NOT_MATCHED_OR_EXPIRED, ex.getErrorCode());
        verify(accountRepository, never()).save(any());
    }
}

