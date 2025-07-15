package com.stu.socialnetworkapi.repository.sqlite;

import com.stu.socialnetworkapi.entity.sqlite.OnlineUserLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OnlineUserLogRepository extends JpaRepository<OnlineUserLog, Long> {
    List<OnlineUserLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to);
}
