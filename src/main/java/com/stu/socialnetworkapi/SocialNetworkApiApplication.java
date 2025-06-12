package com.stu.socialnetworkapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class SocialNetworkApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialNetworkApiApplication.class, args);
    }
}
