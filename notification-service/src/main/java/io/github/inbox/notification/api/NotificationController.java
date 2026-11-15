package io.github.inbox.notification.api;

import io.github.inbox.notification.mail.NotificationQueryService;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationQueryService service;

    public NotificationController(NotificationQueryService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationResponse> findByOrder(@RequestParam("orderId") @Positive Long orderId) {
        return service.findByOrder(orderId);
    }
}
