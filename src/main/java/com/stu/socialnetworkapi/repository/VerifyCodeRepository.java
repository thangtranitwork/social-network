package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.VerifyCode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VerifyCodeRepository extends Neo4jRepository<VerifyCode, UUID> {
}
