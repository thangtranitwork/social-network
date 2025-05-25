package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.RegisterRequest;
import com.stu.socialnetworkapi.dto.request.VerifyRequest;
import com.stu.socialnetworkapi.entity.Account;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.entity.VerifyCode;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.AccountRepository;
import com.stu.socialnetworkapi.repository.VerifyCodeRepository;
import com.stu.socialnetworkapi.service.itf.MailService;
import com.stu.socialnetworkapi.service.itf.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {
    private static final long VERIFY_EMAIL_VALIDITY_SECONDS = 900L; // 15 minutes
    @Value("${front-end.origin}")
    private String frontEndOrigin;

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final VerifyCodeRepository verifyCodeRepository;

    @Override
    public void register(RegisterRequest request) {
        validateRegister(request);
        String encodedPassword = passwordEncoder.encode(request.password());
        UUID code = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        VerifyCode verifyCode = VerifyCode.builder()
                .code(code)
                .expiryTime(LocalDateTime.now().plusSeconds(VERIFY_EMAIL_VALIDITY_SECONDS))
                .build();
        User user = User.builder()
                .id(accountId)
                .username(accountId.toString())
                .givenName(request.givenName())
                .familyName(request.familyName())
                .birthdate(request.birthdate())
                .build();
        Account account = Account.builder()
                .id(accountId)
                .email(request.email())
                .password(encodedPassword)
                .verifyCode(verifyCode)
                .user(user)
                .build();
        sendVerifyEmail(account.getEmail(), verifyCode);
        accountRepository.save(account);
    }

    @Override
    public void verify(VerifyRequest request) {
        Account account = accountRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
        VerifyCode verifyCode = account.getVerifyCode();
        if (verifyCode == null)
            throw new ApiException(ErrorCode.VERIFICATION_CODE_NOT_FOUND);
        else if (!verifyCode.getCode().equals(request.code()) || verifyCode.getExpiryTime().isBefore(LocalDateTime.now()))
            throw new ApiException(ErrorCode.VERIFICATION_CODE_NOT_MATCHED_OR_EXPIRED);
        else {
            account.setVerified(true);
            account.setVerifyCode(null);
            accountRepository.save(account);
            verifyCodeRepository.deleteById(request.code());
        }
    }

    @Override
    public void resend(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
        VerifyCode code = account.getVerifyCode();
        if (account.isVerified() || code == null) throw new ApiException(ErrorCode.ACCOUNT_VERIFIED);
        code.setExpiryTime(LocalDateTime.now().plusSeconds(VERIFY_EMAIL_VALIDITY_SECONDS));
        verifyCodeRepository.save(code);
        sendVerifyEmail(email, code);
    }

    private void validateRegister(RegisterRequest request) {
        accountRepository.findByEmail(request.email())
                .ifPresent(account -> {
                    if (account.isVerified()) throw new ApiException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
                    else throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED);
                });
    }

    private void sendVerifyEmail(String email, VerifyCode verifyCode) {
        Map<String, Object> data = new HashMap<>();
        data.put("verificationLink", String.format("%s/register&email=%s&code=%s", frontEndOrigin, email, verifyCode.getCode()));
        mailService.sendHTML(
                email,
                "Email Verification",
                "register-verify-email",
                data);
    }
}
