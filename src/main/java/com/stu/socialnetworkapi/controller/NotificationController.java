package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.NotificationsResponse;
import com.stu.socialnetworkapi.service.itf.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<NotificationsResponse> getNotifications(Neo4jPageable pageable) {
        return ApiResponse.success(notificationService.getNotifications(pageable));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount() {
        return ApiResponse.success(notificationService.getUnreadNotificationCount());
    }

    @PatchMapping("mark-as-read")
    public ApiResponse<Void> markAsRead(Neo4jPageable pageable) {
        System.out.println(pageable);
        notificationService.markLatestNotificationsAsRead(pageable.getLimit());
        return ApiResponse.success();
    }
}
