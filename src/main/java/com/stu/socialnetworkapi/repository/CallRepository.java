package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.Call;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CallRepository extends Neo4jRepository<Call, UUID> {
    Optional<Call> findByCallId(String callId);
}
