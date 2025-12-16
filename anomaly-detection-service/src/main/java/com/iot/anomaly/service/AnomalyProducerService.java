package com.iot.anomaly.service;

import com.iot.anomaly.model.AnomalyAlert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnomalyProducerService {

    private static final String TOPIC = "anomaly-alerts";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendAlert(AnomalyAlert alert) {
        kafkaTemplate.send(TOPIC, alert.getDeviceId(), alert);
        System.out.println("Sent anomaly alert: " + alert);
    }
}
