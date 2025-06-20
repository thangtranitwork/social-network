package com.stu.socialnetworkapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

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
    public RedisTemplate<String, List<String>> uuidListRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, List<String>> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());

        // Use Jackson serializer for List<String> values
        Jackson2JsonRedisSerializer<List<String>> serializer = new Jackson2JsonRedisSerializer<>(
                (Class<List<String>>) (Class<?>) List.class
        );

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }
}