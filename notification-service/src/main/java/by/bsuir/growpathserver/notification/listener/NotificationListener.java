package by.bsuir.growpathserver.notification.listener;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.model.EventMessage;
import by.bsuir.growpathserver.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;
    private static final String TOPIC = "growpath-events";

    @KafkaListener(topics = TOPIC, groupId = "notification-service-group")
    public void consume(EventMessage event) {
        log.info("Received event: {} for user: {}", event.getEventType(), event.getUserId());

        switch (event.getEventType()) {
            case "APPLICATION_CREATED":
                handleApplicationCreated(event);
                break;
            case "TASK_COMPLETED":
                handleTaskCompleted(event);
                break;
            default:
                log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void handleApplicationCreated(EventMessage event) {
        Map<String, Object> data = event.getData();
        String email = (String) data.get("email");
        String subject = "Новая заявка на стажировку";
        String text = String.format("Здравствуйте! Ваша заявка на стажировку была создана. " +
                                            "ID заявки: %s", data.get("applicationId"));

        if (email != null) {
            notificationService.sendEmail(email, subject, text);
        }
    }

    private void handleTaskCompleted(EventMessage event) {
        Map<String, Object> data = event.getData();
        String email = (String) data.get("email");
        String subject = "Задача выполнена";
        String text = String.format("Здравствуйте! Задача '%s' была выполнена.",
                                    data.get("taskName"));

        if (email != null) {
            notificationService.sendEmail(email, subject, text);
        }
    }
}
