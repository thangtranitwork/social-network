package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.access-token.key}")
    private String key;
    private final JwtUtil jwtUtil;
    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) {
        jwtUtil.validateToken(token);

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HS256");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
