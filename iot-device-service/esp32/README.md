# ESP32 IoT Logger - Testing Guide

This guide explains how to configure the ESP32 to send logs to the `iot-device-service` and verify the data flow through to Kafka.

## Prerequisites

- **Hardware**: ESP32 Development Board.
- **Software**:
    - Arduino IDE (with ESP32 board support installed).
    - Java 17+ & Maven (for the backend).
    - Docker (for Kafka, if running locally).

## 1. Backend Setup

Before running the ESP32, ensure your backend and Kafka are running.

1.  **Start Kafka**: Ensure your Kafka broker is up (e.g., via Docker Compose).
2.  **Start the Service**:
    Navigate to the `iot-device-service` directory and run:
    ```bash
    mvn spring-boot:run
    ```
3.  **Find Your IP**:
    The ESP32 needs to connect to your computer's IP address, not `localhost`.
    - Linux/Mac: Run `ifconfig` or `ip a` to find your local IP (e.g., `192.168.1.100`).

## 2. ESP32 Configuration

1.  Open `esp32_iot_logger.ino` in Arduino IDE.
2.  Update the following variables at the top of the file:

    ```cpp
    // WiFi credentials
    const char* ssid = "YOUR_WIFI_SSID";     // Your WiFi Name
    const char* password = "YOUR_WIFI_PASSWORD"; // Your WiFi Password

    // Backend endpoint
    // REPLACE 192.168.X.X with your computer's local IP address
    const char* serverUrl = "http://192.168.X.X:8080/iot/ingest-log";
    ```

## 3. Flashing & Running

1.  Connect your ESP32 to your computer via USB.
2.  Select your board and port in Arduino IDE (**Tools > Board** / **Port**).
3.  Click **Upload** (Right Arrow icon).
4.  Open the **Serial Monitor** (**Tools > Serial Monitor**) and set the baud rate to **115200**.

## 4. Verification

### Step A: Serial Monitor
You should see output indicating successful connection and data sending:
```text
Connected to WiFi
200
{"deviceId":"esp32-sensor-01", ...}
```
If you see `Error on sending POST: -1`, check your IP address and ensure the backend is reachable (firewall might block it).

### Step B: Backend Logs
Check the terminal where `iot-device-service` is running. You should see:
```text
INFO ... IoTLogController : Received log from input: DeviceLogInput[...]
INFO ... IoTLogService    : Sent log to Kafka topic iot-logs: DeviceLog[...]
```

### Step C: Kafka Verification
To verify the message reached Kafka, you can run a console consumer.

**If using Docker:**
```bash
docker exec -it <kafka-container-name> kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic iot-logs --from-beginning
```
*(Replace `<kafka-container-name>` with your actual container name, e.g., `kafka` or `broker`)*.

You should see the JSON logs appearing in real-time.
