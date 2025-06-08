package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.File;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends Neo4jRepository<File, String> {
}
