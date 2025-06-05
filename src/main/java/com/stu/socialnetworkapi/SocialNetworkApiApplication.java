package com.stu.socialnetworkapi;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SocialNetworkApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialNetworkApiApplication.class, args);
    }

    @PostConstruct
    public void init() {
        System.out.println("API running...");
    }
}
