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

            // NODE KEY and RELATIONSHIP KEY are only supported by Neo4j Enterprise Edition
            session.run("CREATE CONSTRAINT user_id_unique IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE");
            session.run("CREATE CONSTRAINT user_username_unique IF NOT EXISTS FOR (u:User) REQUIRE u.username IS UNIQUE");
            session.run("CREATE CONSTRAINT account_id_unique IF NOT EXISTS FOR (a:Account) REQUIRE a.id IS UNIQUE");
            session.run("CREATE CONSTRAINT account_email_unique IF NOT EXISTS FOR (a:Account) REQUIRE a.email IS UNIQUE");
            session.run("CREATE CONSTRAINT chat_id_unique IF NOT EXISTS FOR (c:Chat) REQUIRE c.id IS UNIQUE");
            session.run("CREATE CONSTRAINT comment_id_unique IF NOT EXISTS FOR (c:Comment) REQUIRE c.id IS UNIQUE");
            session.run("CREATE CONSTRAINT post_id_unique IF NOT EXISTS FOR (p:Post) REQUIRE p.id IS UNIQUE");
            session.run("CREATE CONSTRAINT file_id_unique IF NOT EXISTS FOR (f:File) REQUIRE f.id IS UNIQUE");
            session.run("CREATE CONSTRAINT message_id_unique IF NOT EXISTS FOR (m:Message) REQUIRE m.id IS UNIQUE");
            session.run("CREATE CONSTRAINT call_id_unique IF NOT EXISTS FOR (m:Call) REQUIRE m.callId IS UNIQUE");
            session.run("CREATE CONSTRAINT notification_id_unique IF NOT EXISTS FOR (n:Notification) REQUIRE n.id IS UNIQUE");
            session.run("CREATE CONSTRAINT verify_code_id_unique IF NOT EXISTS FOR (v:VerifyCode) REQUIRE v.id IS UNIQUE");
            session.run("CREATE CONSTRAINT keyword_text_unique IF NOT EXISTS FOR (k:Keyword) REQUIRE k.text IS UNIQUE");

            session.run("CREATE CONSTRAINT friend_uuid_unique IF NOT EXISTS FOR ()-[r:FRIEND]-() REQUIRE r.uuid IS UNIQUE");
            session.run("CREATE CONSTRAINT block_uuid_unique IF NOT EXISTS FOR ()-[r:BLOCK]-() REQUIRE r.uuid IS UNIQUE");
            session.run("CREATE CONSTRAINT request_uuid_unique IF NOT EXISTS FOR ()-[r:REQUEST]-() REQUIRE r.uuid IS UNIQUE");

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

            log.info("Constraints and indexes created for Neo4j");

        } catch (Exception e) {
            log.error("Failed to create constraints and indexes", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}
