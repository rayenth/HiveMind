package Caravane.subscriber;

import Caravane.events.FileChangedEvent;
import Caravane.service.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * this class objective is to make the "packaging" of the event to make it
 * publishable in the kafka topics
 */
@Component
public class KafkaForwarderService implements EventSubscriber {

    @Autowired
    private KafkaProducer kp;

    @EventListener
    @Override
    public void handlefilechanged(FileChangedEvent event) {
        // Keep your original check
        if (event.getFilename().endsWith(".log") == true) {

            // LOGIC ADDED: We must include event.getNewcontent() in the JSON
            // so the subscriber actually receives the log data.
            String json = String.format(
                    "{\"eventType\":\"FILE_CHANGED\",\"deviceId\":\"WS-AGENT\",\"severity\":\"LOW\",\"filename\":\"%s\",\"changeType\":\"%s\",\"content\":\"%s\",\"timestamp\":%d}",
                    event.getFilename(),
                    event.getTypechange(), // Ensure this getter name matches your Event class
                    event.getNewcontent().replace("\"", "\\\""), // Sanitize for JSON
                    System.currentTimeMillis());

            kp.send("device-events-workstation", json);
            System.out.println("Forwarded to Kafka: " + event.getFilename());
        }
    }
}