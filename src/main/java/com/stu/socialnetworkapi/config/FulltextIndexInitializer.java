package com.stu.socialnetworkapi.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FulltextIndexInitializer {

    private final Driver neo4jDriver;

    private static final String USER_INDEX = "userSearchIndex";
    private static final String POST_INDEX = "postSearchIndex";

    @PostConstruct
    public void createFulltextIndexesIfNotExist() {
        try (Session session = neo4jDriver.session()) {
            createIndexIfMissing(session, USER_INDEX,
                    """
                            CREATE FULLTEXT INDEX userSearchIndex
                            FOR (u:User)
                            ON EACH [u.username, u.givenName, u.familyName]
                            """);

            createIndexIfMissing(session, POST_INDEX,
                    """
                            CREATE FULLTEXT INDEX postSearchIndex
                            FOR (p:Post)
                            ON EACH [p.content]
                            """);
        } catch (Exception e) {
            log.error("Failed to create fulltext indexes", e);
        }
    }

    private void createIndexIfMissing(Session session, String indexName, String createCypher) {
        boolean exists = session.run("""
                            SHOW INDEXES YIELD name, type
                            WHERE name = $indexName AND type = "FULLTEXT"
                            RETURN count(*) > 0 AS exists
                        """, Values.parameters("indexName", indexName))
                .single()
                .get("exists")
                .asBoolean();

        if (!exists) {
            session.run(createCypher);
            System.out.println("Index " + indexName + " created");
        } else {
            System.out.println("Index " + indexName + " already exists");
        }
    }
}
