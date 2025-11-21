package com.example.ballkeeper.controller;

import com.example.ballkeeper.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // SSE 연결은 반드시 produces = MediaType.TEXT_EVENT_STREAM_VALUE 설정 필요
    @GetMapping(value = "/api/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam Long userId) {
        return notificationService.subscribe(userId);
    }
}