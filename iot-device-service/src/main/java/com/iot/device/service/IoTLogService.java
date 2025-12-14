package com.iot.device.service;

import com.iot.device.model.DeviceLog;
import com.iot.device.model.DeviceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Service
public class IoTLogService {

    private static final Logger logger = LoggerFactory.getLogger(IoTLogService.class);
    private final KafkaTemplate<String, DeviceLog> kafkaTemplate;
    private final String topicName;
    private final Random random = new Random();

    public IoTLogService(KafkaTemplate<String, DeviceLog> kafkaTemplate,
            @Value("${iot.kafka.topic}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public DeviceLog generateAndSendLog() {
        DeviceLog log = createRandomLog();
        sendToKafka(log);
        return log;
    }

    public DeviceLog generateAndSendLog(double temperature) {
        DeviceLog log = new DeviceLog(
                UUID.randomUUID(),
                random.nextBoolean() ? DeviceStatus.ONLINE : DeviceStatus.OFFLINE,
                temperature,
                Instant.now());
        sendToKafka(log);
        return log;
    }

    private void sendToKafka(DeviceLog log) {
        logger.info("Generated log: {}", log);
        kafkaTemplate.send(topicName, log.deviceId().toString(), log)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Sent log to Kafka topic {}: {}", topicName, log);
                    } else {
                        logger.error("Failed to send log to Kafka", ex);
                    }
                });
    }

    private DeviceLog createRandomLog() {
        return new DeviceLog(
                UUID.randomUUID(),
                random.nextBoolean() ? DeviceStatus.ONLINE : DeviceStatus.OFFLINE,
                random.nextDouble() * 100,
                Instant.now());
    }
}
