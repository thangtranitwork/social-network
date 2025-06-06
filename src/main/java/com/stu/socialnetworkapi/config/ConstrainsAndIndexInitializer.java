package com.stu.socialnetworkapi.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConstrainsAndIndexInitializer {

    private final Driver neo4jDriver;

    @PostConstruct
    public void createConstraintsAndIndexes() {
        try (Session session = neo4jDriver.session()) {
            session.run("CREATE CONSTRAINT user_id_unique IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE");
            session.run("CREATE CONSTRAINT user_username_unique IF NOT EXISTS FOR (u:User) REQUIRE u.username IS UNIQUE");
            session.run("CREATE CONSTRAINT account_email_unique IF NOT EXISTS FOR (a:Account) REQUIRE a.email IS UNIQUE");
            session.run("CREATE CONSTRAINT post_id_unique IF NOT EXISTS FOR (p:Post) REQUIRE p.id IS UNIQUE");
            session.run("CREATE CONSTRAINT file_id_unique IF NOT EXISTS FOR (f:File) REQUIRE f.id IS UNIQUE");

            session.run("""
                    CREATE FULLTEXT INDEX userSearchIndex IF NOT EXISTS
                    FOR (u:User)
                    ON EACH [u.username, u.givenName, u.familyName]
                    """);

            session.run("""
                    CREATE FULLTEXT INDEX postSearchIndex IF NOT EXISTS
                    FOR (p:Post)
                    ON EACH [p.content]
                    """);
            session.run("""
                                SHOW INDEXES YIELD name, type
                                RETURN name, type
                            """)
                    .forEachRemaining(System.out::println);
        } catch (Exception e) {
            log.error("Failed to create constraints and indexes", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}