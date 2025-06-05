package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.NotificationResponse;
import com.stu.socialnetworkapi.service.itf.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> getNotifications(Pageable pageable) {
        return notificationService.getNotifications(pageable);
    }
}
