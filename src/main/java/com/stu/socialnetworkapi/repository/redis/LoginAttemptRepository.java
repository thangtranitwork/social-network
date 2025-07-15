package com.stu.socialnetworkapi.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LoginAttemptRepository {
    private static final String LOCKOUT_POSTFIX = ":lockoutTime";
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final int MAX_ATTEMPTS = 5;  // Số lần đăng nhập sai tối đa
    private static final int LOCKOUT_TIME = 15;  // Thời gian khóa tài khoản (phút)

    private final RedisTemplate<String, String> redisTemplate;

    // Kiểm tra tài khoản có đang bị khóa không
    public boolean isAccountLocked(UUID userId) {
        String lockoutKey = LOGIN_FAIL_PREFIX + userId + LOCKOUT_POSTFIX;  // Khóa lưu thời gian khóa
        String lockoutTimeStr = redisTemplate.opsForValue().get(lockoutKey);  // Lấy thời gian khóa tài khoản

        if (lockoutTimeStr != null) {
            long lockoutTime = Long.parseLong(lockoutTimeStr);
            // Kiểm tra xem thời gian hiện tại có trước thời gian khóa tài khoản không
            return ZonedDateTime.now().isBefore(ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(lockoutTime), java.time.ZoneId.systemDefault()));
        }
        return false;  // Nếu không có khóa tài khoản, trả về false
    }

    // Cập nhật số lần đăng nhập sai
    public void loginFailed(UUID userId) {
        String failKey = LOGIN_FAIL_PREFIX + userId;
        String lockoutKey = failKey + LOCKOUT_POSTFIX;  // Khóa thời gian khóa tài khoản

        // Tăng số lần đăng nhập sai trong Redis
        Long attempts = redisTemplate.opsForValue().increment(failKey);

        // Kiểm tra nếu số lần đăng nhập sai đạt giới hạn
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            // Tạo thời gian khóa tài khoản, 15 phút từ bây giờ
            ZonedDateTime lockoutTime = ZonedDateTime.now().plusMinutes(LOCKOUT_TIME);
            // Lưu thời gian khóa tài khoản vào Redis
            redisTemplate.opsForValue().set(lockoutKey, String.valueOf(lockoutTime.toInstant().toEpochMilli()), Duration.ofMinutes(LOCKOUT_TIME));
        }
    }

    // Xóa số lần đăng nhập sai khi đăng nhập thành công
    public void loginSucceeded(UUID userId) {
        String failKey = LOGIN_FAIL_PREFIX + userId;
        String lockoutKey = failKey + LOCKOUT_POSTFIX;

        // Xóa thông tin đăng nhập sai và khóa tài khoản khỏi Redis
        redisTemplate.delete(failKey);  // Xóa số lần sai
        redisTemplate.delete(lockoutKey);  // Xóa thời gian khóa tài khoản
    }

    // Kiểm tra số lần sai mật khẩu
    public int getFailedAttempts(UUID userId) {
        String failKey = LOGIN_FAIL_PREFIX + userId;
        String value = redisTemplate.opsForValue().get(failKey);
        return value != null ? Integer.parseInt(value) : 0;  // Trả về số lần sai hoặc 0 nếu không có
    }

    // Lấy thời gian khóa tài khoản nếu có
    public ZonedDateTime getLockoutTime(UUID userId) {
        String lockoutKey = LOGIN_FAIL_PREFIX + userId + LOCKOUT_POSTFIX;
        String value = redisTemplate.opsForValue().get(lockoutKey);

        if (value != null) {
            long lockoutTime = Long.parseLong(value);
            return ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(lockoutTime), java.time.ZoneId.systemDefault());
        }
        return null;
    }
}
