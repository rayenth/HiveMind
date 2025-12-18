# Kafka Configuration and Implementation Explanation

This document explains how Kafka is configured and implemented in the **IoT Device Service**, and details the traffic flow.

## 1. Configuration Files

### `src/main/resources/application.yml`
This file contains the base properties for connecting to the Kafka broker.
- **`spring.kafka.bootstrap-servers`**: Points to the Kafka broker (default: `localhost:9092`).
- **`spring.kafka.producer.key-serializer`**: Serializes the message key (UUID string) as a String.
- **`spring.kafka.producer.value-serializer`**: Serializes the message body (`DeviceLog` object) as JSON.
- **`iot.kafka.topic`**: Custom property defining the target topic name (`iot-logs`).

### `src/main/java/com/iot/device/config/KafkaConfig.java`
This Java configuration class explicitly defines the Spring Beans needed for Kafka interaction.
- **`producerFactory()`**: Creates a `ProducerFactory` using the config from `application.yml`. It ensures that the `JsonSerializer` is correctly set up to convert Java objects into JSON bytes.
- **`kafkaTemplate()`**: Creates the `KafkaTemplate` bean. This is the high-level abstraction we use in our code to send messages. It wraps the producer factory.

## 2. Implementation Files

### `src/main/java/com/iot/device/service/IoTLogService.java`
This is where the actual sending logic resides.
- **Injection**: It injects the `KafkaTemplate<String, DeviceLog>` and the topic name.
- **`generateAndSendLog()`**:
    1.  Creates a random `DeviceLog` object.
    2.  Calls `kafkaTemplate.send(topicName, key, value)`.
    3.  **Key**: `log.deviceId().toString()` (ensures logs from the same device go to the same partition).
    4.  **Value**: The `DeviceLog` object itself (serialized to JSON).
    5.  **Async Handling**: Uses `.whenComplete()` to log success or failure asynchronously without blocking the main thread.

## 3. Kafka Traffic Flow

Here is exactly what happens when you trigger the service:

1.  **Trigger**: You send a `POST` request to `http://localhost:8080/iot/send-log`.
2.  **Controller**: `IoTLogController` receives the request and calls `IoTLogService.generateAndSendLog()`.
3.  **Generation**: The service creates a `DeviceLog` (e.g., `{ "deviceId": "...", "temp": 45.2, ... }`).
4.  **Serialization**: The `KafkaTemplate` uses the `JsonSerializer` to convert that `DeviceLog` object into a JSON string/byte array.
5.  **Publishing**: The Producer sends this JSON payload to the Kafka Broker at `localhost:9092`.
    -   **Topic**: `iot-logs`
    -   **Key**: The Device UUID.
6.  **Storage**: Kafka receives the message and stores it in the `iot-logs` topic.
7.  **Result**: The service returns the generated log to the HTTP client immediately, while the message is asynchronously persisted in Kafka.

## Summary
- **We are the Producer**: This microservice only *sends* data.
- **Data Format**: JSON.
- **Topic**: `iot-logs`.
- **Purpose**: To simulate a stream of telemetry data coming from IoT devices.
