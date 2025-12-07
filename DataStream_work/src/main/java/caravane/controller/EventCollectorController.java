package caravane.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import caravane.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EventCollectorController {

    private static final Logger logger = LoggerFactory.getLogger(EventCollectorController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping("/events")
    public ResponseEntity<String> collectEvent(@RequestBody String eventJson) {
        try {
            // Validate JSON
            JsonNode json = objectMapper.readTree(eventJson);
            logger.info("📩 Received HTTP Event: {}", eventJson);

            // Determine Topic based on device type (simple logic)
            String topic = determineTopicFromEvent(eventJson);

            // Forward to Kafka
            kafkaTemplate.send(topic, eventJson)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.error("❌ Error sending to Kafka: {}", ex.getMessage());
                        } else {
                            logger.info("➡️ Forwarded to Kafka topic: {}", topic);
                        }
                    });

            return ResponseEntity.ok("Event received and forwarded to " + topic);

        } catch (Exception e) {
            logger.error("Invalid JSON or Internal Error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Invalid JSON or Internal Error: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("HiveMind DataStream API is running");
    }

    private String determineTopicFromEvent(String eventJson) {
        // Simple logic to determine topic based on content
        if (eventJson.contains("IOT"))
            return KafkaConfig.TOPIC_IOT;
        if (eventJson.contains("WS-"))
            return KafkaConfig.TOPIC_WORKSTATION;
        if (eventJson.contains("NET") || eventJson.contains("RTR"))
            return KafkaConfig.TOPIC_NETWORK;
        return KafkaConfig.TOPIC_SERVER; // Default to server
    }
}
