package com.stu.socialnetworkapi.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class StringeeTokenUtil {
    private static final int DEFAULT_EXPIRE_IN_SECONDS = 2592000;

    @Value("${stringee.api.sid}")
    private String apiKeySid;

    @Value("${stringee.api.secret-key}")
    private String apiKeySecret;

    public String createAccessToken(String username) {
        try {
            long currentTime = System.currentTimeMillis() / 1000;

            String token = Jwts.builder()
                    .header()
                    .add("cty", "stringee-api;v=1")
                    .add("typ", "JWT")
                    .add("alg", "HS256")
                    .and()
                    .claim("jti", apiKeySid + "-" + currentTime)
                    .claim("iss", apiKeySid)
                    .claim("exp", currentTime + DEFAULT_EXPIRE_IN_SECONDS)
                    .claim("userId", username)
                    .signWith(Keys.hmacShaKeyFor(apiKeySecret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                    .compact();

            log.debug("Access token created successfully for user: {}", username);
            return token;

        } catch (Exception ex) {
            log.error("Error creating access token for user: {}", username, ex);
            return null;
        }
    }
}