package by.bsuir.growpathserver.notification.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage {
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("data")
    private Map<String, Object> data;
    
    @JsonProperty("userId")
    private String userId;
}
