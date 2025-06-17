package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.entity.File;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentStaticVariableLoader {
    @Value("${origin.self}")
    private String origin;

    @PostConstruct
    public void init() {
        File.setSelfOrigin(origin);
    }
}
