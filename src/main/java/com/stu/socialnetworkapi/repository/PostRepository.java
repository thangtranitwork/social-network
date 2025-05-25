package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.Post;
import com.stu.socialnetworkapi.enums.PostPrivacy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface PostRepository extends Neo4jRepository<Post, UUID> {
    Slice<Post> findAllByAuthorIdAndPrivacyIsIn(UUID authorId, Collection<PostPrivacy> privacies, Pageable pageable);

    @Query("""
            MATCH (p:Post {id: $postId})<-[like:LIKED]-(u:User {id: $likerId})
            RETURN COUNT(like) > 0
            """)
    boolean isLiked(UUID postId, UUID likerId);

    @Query("""
            MATCH (u:User {id: $userId})
            MATCH (p:Post {id: $postId})
            CREATE (u)-[:PINNED]->(p)
            """)
    void pinPost(UUID postId, UUID userId);

    @Query("""
            MATCH (u:User {id: $userId})-[pin:PINNED]-()
            DELETE pin
            """)
    void unpinPost(UUID userId);

    @Query("""
            MATCH (p:Post {id: $postId})<-[store:STORED]-(u:User {id: $userId})
            RETURN COUNT(store) > 0
            """)
    boolean isStored(UUID postId, UUID userId);

    @Query("""
            MATCH (u:User {id: $userId})
            MATCH (p:Post {id: $postId})
            CREATE (u)-[:STORED]->(p)
            """)
    void storePost(UUID postId, UUID userId);
}
