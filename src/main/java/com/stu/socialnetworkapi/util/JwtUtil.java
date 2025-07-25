package com.stu.socialnetworkapi.util;

import com.stu.socialnetworkapi.enums.AccountRole;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.redis.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.access-token.duration}")
    private int accessTokenValiditySeconds;

    @Value("${jwt.refresh-token.duration}")
    private int refreshTokenValidityDays;

    @Value("${jwt.access-token.key}")
    private String secret;

    private final TokenRepository tokenRepository;
    private static final String SCOPE_CLAIM_KEY = "scope";
    private static final String USERNAME_CLAIM_KEY = "username";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "token";

    /**
     * Tạo Access Token có thời hạn ngắn, ký bằng secret.
     */
    public String generateAccessToken(UUID userId, String username, AccountRole role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValiditySeconds * 1000L);
        String jit = UUID.randomUUID().toString();

        // Specify the algorithm explicitly to match the decoder
        return Jwts.builder()
                .id(jit)
                .subject(userId.toString())
                .claim(SCOPE_CLAIM_KEY, role.name())
                .claim(USERNAME_CLAIM_KEY, username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Tạo refresh token (UUID), lưu vào Redis, gửi qua cookie HTTPOnly.
     */
    public void generateAndStoreRefreshToken(UUID userId, AccountRole role, String username, HttpServletResponse response) {
        String refreshToken = UUID.randomUUID().toString();
        Duration ttl = Duration.ofDays(refreshTokenValidityDays);

        tokenRepository.save(userId, refreshToken, role.name(), username, ttl);

        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // bật nếu dùng HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) ttl.getSeconds());

        response.addCookie(cookie);
    }

    /**
     * Xóa refresh token trong Redis + cookie.
     */
    public void revokeRefreshToken(String token, HttpServletResponse response) {
        tokenRepository.delete(token);
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Tạo mới access token từ refresh token nếu hợp lệ.
     */
    public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        UUID userId = tokenRepository.findUserIdByToken(refreshToken)
                .map(UUID::fromString)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_OR_EXPIRED_REFRESH_TOKEN));

        AccountRole role = AccountRole.valueOf(
                tokenRepository.getRole(userId)
                        .orElseThrow(() -> new ApiException(ErrorCode.INVALID_OR_EXPIRED_REFRESH_TOKEN)));
        String username = tokenRepository.getUsername(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_OR_EXPIRED_REFRESH_TOKEN));
        return generateAccessToken(userId, username, role);
    }

    public UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return UUID.fromString((jwtAuthenticationToken).getToken().getSubject());
        }
        return null;
    }

    public UUID getUserIdRequiredAuthentication() {
        UUID userId = getUserId();
        if (userId == null) throw new ApiException(ErrorCode.UNAUTHENTICATED);
        return userId;
    }

    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return (String) jwtAuthenticationToken.getToken().getClaims().get(USERNAME_CLAIM_KEY);
        }
        return null;
    }

    public String getUsernameRequiredAuthentication() {
        String username = getUsername();
        if (username == null) throw new ApiException(ErrorCode.UNAUTHENTICATED);
        return username;
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new ApiException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
    }

    public boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(authority -> authority.getAuthority().equals(AccountRole.ADMIN.name()));
    }
}
