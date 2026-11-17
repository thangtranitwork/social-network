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
import com.stu.socialnetworkapi.service.itf.UpdatePasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdatePasswordServiceImpl implements UpdatePasswordService {
    private static final long VERIFY_EMAIL_VALIDITY_SECONDS = 900L; // 15 minutes

    @Value("${origin.front-end}")
    private String frontEndOrigin;

    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final VerifyCodeRepository verifyCodeRepository;

    @Override
    public void send(String email, String continueUrl) {
        validateContinueUrl(continueUrl);
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
        VerifyCode existingCode = account.getVerifyCode();
        if (existingCode != null) {
            account.setVerifyCode(null);
            accountRepository.save(account);
            verifyCodeRepository.deleteById(existingCode.getCode());
        }
        VerifyCode code = VerifyCode.builder()
                .code(UUID.randomUUID())
                .account(account)
                .verified(false)
                .expiryTime(LocalDateTime.now().plusSeconds(VERIFY_EMAIL_VALIDITY_SECONDS))
                .build();
        verifyCodeRepository.save(code);
        sendVerifyEmail(email, code, continueUrl);
    }

    @Override
    public void verify(VerifyRequest request) {
        VerifyCode verifyCode = verifyCodeRepository.findById(request.code())
                .orElseThrow(() -> new ApiException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));
        if (verifyCode.getAccount() == null
                || !verifyCode.getAccount().getEmail().equals(request.email())
                || !verifyCode.getCode().equals(request.code())
                || verifyCode.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.VERIFICATION_CODE_NOT_MATCHED_OR_EXPIRED);
        }
        verifyCode.setVerified(true);
        verifyCodeRepository.save(verifyCode);
    }

    @Override
    public void updatePassword(UpdatePasswordRequest request) {
        Account account = accountRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));

        VerifyCode verifyCode = account.getVerifyCode();
        if (verifyCode == null
                || !verifyCode.isVerified()
                || verifyCode.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.VERIFICATION_CODE_NOT_MATCHED_OR_EXPIRED);
        }
        account.setPassword(passwordEncoder.encode(request.password()));
        account.setVerifyCode(null);
        accountRepository.save(account);
        verifyCodeRepository.deleteById(verifyCode.getCode());
    }

    private void validateContinueUrl(String continueUrl) {
        try {
            URI continueUri = URI.create(continueUrl);
            URI allowedUri = URI.create(frontEndOrigin);
            boolean sameOrigin = continueUri.getScheme() != null
                    && continueUri.getScheme().equalsIgnoreCase(allowedUri.getScheme())
                    && continueUri.getHost() != null
                    && continueUri.getHost().equalsIgnoreCase(allowedUri.getHost())
                    && continueUri.getPort() == allowedUri.getPort();
            if (!sameOrigin) {
                throw new ApiException(ErrorCode.INVALID_INPUT);
            }
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
    }

    private void sendVerifyEmail(String email, VerifyCode verifyCode, String continueUrl) {
        Map<String, Object> data = new HashMap<>();
        data.put("resetLink", String.format("%s?email=%s&code=%s", continueUrl, email, verifyCode.getCode()));
        mailService.sendHTML(
                email,
                "Update Password Verification",
                "update-password-email",
                data);
    }
}
