package com.stu.socialnetworkapi.config;

import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
public class Neo4jConfig {
    private final Driver driver;

    @Bean("transactionManager")
    @Primary
    public PlatformTransactionManager neo4jTransactionManager() {
        return new Neo4jTransactionManager(driver);
    }
}
