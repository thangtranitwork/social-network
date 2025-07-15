package com.stu.socialnetworkapi.entity.sqlite;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "online_user_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnlineUserLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    private Integer onlineCount;
}
