package com.stu.socialnetworkapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;
import java.util.UUID;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean(name = "uuidListRedisTemplate")
    public RedisTemplate<String, List<UUID>> uuidListRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, List<UUID>> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Dùng Jackson để serialize List<UUID>
        Jackson2JsonRedisSerializer<List<UUID>> serializer = new Jackson2JsonRedisSerializer<>(
                (Class<List<UUID>>) (Class<?>) List.class
        );
        template.setDefaultSerializer(serializer);

        return template;
    }
}

