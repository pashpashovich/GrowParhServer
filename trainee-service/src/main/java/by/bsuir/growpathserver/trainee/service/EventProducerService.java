package by.bsuir.growpathserver.trainee.service;

import by.bsuir.growpathserver.trainee.model.EventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "growpath-events";

    public void sendEvent(String eventType, Map<String, Object> data, String userId) {
        EventMessage message = new EventMessage();
        message.setEventType(eventType);
        message.setTimestamp(LocalDateTime.now());
        message.setData(data);
        message.setUserId(userId);

        kafkaTemplate.send(TOPIC, message);
        log.info("Event sent: {} for user: {}", eventType, userId);
    }
}
