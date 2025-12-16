package com.iot.device.service;

import com.iot.device.model.DeviceLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IoTLogServiceTest {

    @Mock
    private KafkaTemplate<String, DeviceLog> kafkaTemplate;

    private IoTLogService ioTLogService;
    private final String topicName = "test-topic";

    @BeforeEach
    void setUp() {
        ioTLogService = new IoTLogService(kafkaTemplate, topicName);
    }

    @Test
    void generateAndSendLog_shouldGenerateLogAndSendToKafka() {
        // Arrange
        when(kafkaTemplate.send(eq(topicName), any(), any(DeviceLog.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        DeviceLog log = ioTLogService.generateAndSendLog();

        // Assert
        assertNotNull(log);
        assertNotNull(log.deviceId());
        assertNotNull(log.status());
        assertNotNull(log.timestamp());

        verify(kafkaTemplate).send(eq(topicName), eq(log.deviceId().toString()), eq(log));
    }
}
