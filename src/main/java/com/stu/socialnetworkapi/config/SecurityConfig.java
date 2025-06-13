package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.enums.AccountRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${front-end.origin}")
    private String frontEndOrigin;
    private final CustomJwtDecoder jwtDecoder;
    private static final String[] PUBLIC_ENDPOINTS = new String[]{
            "/v1/auth/**",
            "/v1/register/**",
            "/v1/forgot-password/**",
            "/v1/update-password/**",
            "/v1/users/{username}",
            "/v1/files/{id}",
            "/v1/notifications/send",
            "/v1/posts/newsfeed",
            "/v1/posts/{id}",
            "/v1/posts/of-user/{username}",
            "/v1/comments/of-post/{postId}",
            "/ws/**" //authorize in handshake interceptor
    };

    private static final String[] ADMIN_ENDPOINTS = new String[]{
            "/v1/statistics"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(
                        request ->
                                request.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                        .requestMatchers(ADMIN_ENDPOINTS).hasAuthority(AccountRole.ADMIN.name())
                                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer ->
                                        jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())
                                                .decoder(jwtDecoder))
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin(frontEndOrigin);
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}