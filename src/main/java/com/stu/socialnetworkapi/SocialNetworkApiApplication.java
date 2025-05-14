package com.stu.socialnetworkapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SocialNetworkApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialNetworkApiApplication.class, args);
    }

}
