package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Integer> redisTemplate;

    private static final int MAX_REQUESTS = 100;        // Giới hạn
    private static final int TIME_WINDOW_SECONDS = 60;  // Mỗi 60 giây
    private static final String RATE_LIMIT_BY_IP = "rate_limit:ip:";

    public RateLimitingFilter(@Qualifier("integerRedisTemplate") RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/v1/files")
                || request.getRequestURI().startsWith("/ws")
                || request.getRequestURI().startsWith("/v1/stringee");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIP(request);
        if (clientIp != null && clientIp.startsWith("0:0:0:0:0:0:0:")) {
            // Skip localhost
            filterChain.doFilter(request, response);
            return;
        }
        String redisKey = RATE_LIMIT_BY_IP + clientIp;

        Integer currentCount = redisTemplate.opsForValue().get(redisKey);
        if (currentCount == null) {
            redisTemplate.opsForValue().set(redisKey, 1, Duration.ofSeconds(TIME_WINDOW_SECONDS));
        } else if (currentCount < MAX_REQUESTS) {
            redisTemplate.opsForValue().increment(redisKey);
        } else {
            // Trả lỗi 429 Too Many Requests
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

        filterChain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        String remoteAddr = request.getRemoteAddr();

        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }
        return remoteAddr;
    }
}
