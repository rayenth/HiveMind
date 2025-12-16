package com.iot.anomaly.service;

import com.iot.anomaly.model.AnomalyAlert;
import com.iot.anomaly.model.DeviceLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LogConsumerService {

    @Autowired
    private AnomalyDetectorService anomalyDetectorService;

    @Autowired
    private AnomalyProducerService anomalyProducerService;

    @KafkaListener(topics = "iot-logs", groupId = "anomaly-group")
    public void consume(DeviceLog log) {
        System.out.println("Received log: " + log);

        if (anomalyDetectorService.isAnomaly(log)) {
            AnomalyAlert alert = new AnomalyAlert();
            alert.setAlertId(UUID.randomUUID().toString());
            alert.setDeviceId(log.deviceId().toString());
            alert.setDescription(anomalyDetectorService.getAnomalyDescription(log));
            alert.setDetectedValue(log.temperature());
            alert.setTimestamp(LocalDateTime.now());

            anomalyDetectorService.addAnomaly(alert);
            anomalyProducerService.sendAlert(alert);
        }
    }
}
