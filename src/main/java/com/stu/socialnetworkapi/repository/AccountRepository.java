package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.Account;
import com.stu.socialnetworkapi.enums.AccountRole;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends Neo4jRepository<Account, UUID> {
    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailAndRoleIs(String email, AccountRole role);
}
