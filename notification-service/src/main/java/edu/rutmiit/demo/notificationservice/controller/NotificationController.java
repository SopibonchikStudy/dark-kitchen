// controller/NotificationController.java
package edu.rutmiit.demo.notificationservice.controller;

import edu.rutmiit.demo.notificationservice.listener.NotificationListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationListener listener;

    public NotificationController(NotificationListener listener) {
        this.listener = listener;
    }

    @GetMapping
    public List<NotificationListener.NotificationRecord> getNotifications() {
        return listener.getNotifications();
    }
}