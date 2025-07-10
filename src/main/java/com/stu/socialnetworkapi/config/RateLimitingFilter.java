package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Integer> redisTemplate;

    private static final int MAX_REQUESTS = 50;        // Giới hạn
    private static final int TIME_WINDOW_SECONDS = 60;  // Mỗi 60 giây

    public RateLimitingFilter(@Qualifier("integerRedisTemplate") RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/v1/files") || request.getRequestURI().startsWith("/ws"); // Bỏ qua endpoint /abc
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            String username = authentication.getName();
            String redisKey = "rate_limit:" + username;

            Integer currentCount = redisTemplate.opsForValue().get(redisKey);
            if (currentCount == null) {
                redisTemplate.opsForValue().set(redisKey, 1, Duration.ofSeconds(TIME_WINDOW_SECONDS));
                log.debug("Request from {}: {}, name: {}, times {}", request.getRemoteAddr(), request.getRequestURI(), username, redisTemplate.opsForValue().get("rate_limit:" + authentication.getName()));
            } else if (currentCount < MAX_REQUESTS) {
                redisTemplate.opsForValue().increment(redisKey);
                log.debug("Request from {}: {}, name: {}, times {}", request.getRemoteAddr(), request.getRequestURI(), username, redisTemplate.opsForValue().get("rate_limit:" + authentication.getName()));
            } else {
                ErrorCode errorCode = ErrorCode.TOO_MANY_REQUESTS;
                response.setStatus(errorCode.getHttpStatus().value());
                response.setContentType("application/json");
                String body = """
                        {
                          "code": "%s",
                          "message": "%s"
                        }
                        """.formatted(errorCode.getCode(), errorCode.getMessage());
                response.getWriter().write(body);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
