package Caravane.subscriber;

import Caravane.events.FileChangedEvent;
import Caravane.events.LogEntryEvent;
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
        if (event.getFilename().endsWith(".txt") == true) {
            // Send to DataStream Kafka topic with proper JSON format
            String json = String.format(
                    "{\"eventType\":\"FILE_CHANGED\",\"deviceId\":\"WS-AGENT\",\"severity\":\"LOW\",\"filename\":\"%s\",\"changeType\":\"%s\",\"timestamp\":%d}",
                    event.getFilename(),
                    event.getChngetype(),
                    System.currentTimeMillis());
            kp.send("device-events-workstation", json);
        }
    }

    /**
     * Handles log entry events and forwards them to Kafka
     */
    @EventListener
    public void handleLogEntry(LogEntryEvent event) {
        // Send log data to Kafka with proper JSON format
        String json = String.format(
                "{\"eventType\":\"LOG_ENTRY\",\"deviceId\":\"WS-AGENT\",\"severity\":\"%s\",\"logFile\":\"%s\",\"logLine\":\"%s\",\"timestamp\":%d}",
                event.getSeverity(),
                event.getLogFile(),
                escapeJson(event.getLogLine()),
                event.getEventTimestamp());
        kp.send("device-events-workstation", json);
    }

    /**
     * Escape special characters in JSON strings
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
