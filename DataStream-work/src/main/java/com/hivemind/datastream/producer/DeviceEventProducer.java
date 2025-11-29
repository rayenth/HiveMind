package com.hivemind.datastream.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hivemind.datastream.model.*;
import com.hivemind.datastream.config.KafkaConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DeviceEventProducer {
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    public DeviceEventProducer(String bootstrapServers) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        this.producer = new KafkaProducer<>(props);
        System.out.println("✅ Kafka Producer initialized");
    }

    private void sendEvent(String topic, DeviceEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, event.getEventId(), json);

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("❌ Error sending event: " + exception.getMessage());
                } else {
                    System.out.println("✓ Sent " + event.getDeviceType() + " event to " +
                            topic + " [partition=" + metadata.partition() + "]");
                }
            });
        } catch (Exception e) {
            System.err.println("❌ Error serializing event: " + e.getMessage());
        }
    }

    public void generateWorkstationEvents(int count, long delayMs) {
        System.out.println("\n🖥️  Generating " + count + " Workstation events...");
        String[] eventTypes = { "LOGIN_SUCCESS", "LOGIN_FAILURE", "FILE_ACCESS",
                "PROCESS_START", "SUSPICIOUS_ACTIVITY", "FILE_DOWNLOAD" };
        String[] users = { "alice", "bob", "admin", "john", "guest", "david" };
        String[] processes = { "chrome.exe", "notepad.exe", "cmd.exe", "powershell.exe",
                "python.exe", "java.exe" };
        String[] files = { "document.pdf", "config.ini", "data.xlsx", "script.ps1" };

        for (int i = 0; i < count; i++) {
            WorkstationEvent event = new WorkstationEvent();
            event.setDeviceId("WS-" + (random.nextInt(10) + 1));

            String eventType = eventTypes[random.nextInt(eventTypes.length)];
            event.setEventType(eventType);
            event.setSourceIp("192.168.1." + (random.nextInt(200) + 1));
            event.setUserId(users[random.nextInt(users.length)]);
            event.setProcessName(processes[random.nextInt(processes.length)]);
            event.setFileName(files[random.nextInt(files.length)]);

            boolean success = !eventType.contains("FAILURE");
            event.setLoginSuccess(success);

            // Set severity based on event type
            if (eventType.contains("SUSPICIOUS") || eventType.contains("FAILURE")) {
                event.setSeverity("HIGH");
            } else if (eventType.contains("cmd") || eventType.contains("powershell")) {
                event.setSeverity("MEDIUM");
            } else {
                event.setSeverity("LOW");
            }

            // Set new fields
            event.setUsername(event.getUserId());
            event.setAuthenticationStatus(success ? "SUCCESS" : "FAILURE");

            sendEvent(KafkaConfig.TOPIC_WORKSTATION, event);
            sleep(delayMs);
        }
    }

    public void generateIoTEvents(int count, long delayMs) {
        System.out.println("\n🔌 Generating " + count + " IoT events...");
        String[] sensorTypes = { "TEMPERATURE", "HUMIDITY", "MOTION", "DOOR_SENSOR",
                "SMOKE_DETECTOR", "CAMERA" };
        String[] locations = { "Building-A-Floor1", "Building-A-Floor2", "Building-B",
                "Warehouse", "Parking-Lot" };

        for (int i = 0; i < count; i++) {
            IoTEvent event = new IoTEvent();
            event.setDeviceId("IOT-" + (random.nextInt(50) + 1));
            event.setEventType("SENSOR_READING");

            String sensorType = sensorTypes[random.nextInt(sensorTypes.length)];
            event.setSensorType(sensorType);
            event.setLocation(locations[random.nextInt(locations.length)]);
            event.setSourceIp("10.0.0." + (random.nextInt(254) + 1));

            // Generate realistic sensor values
            switch (sensorType) {
                case "TEMPERATURE":
                    event.setSensorValue(15.0 + random.nextDouble() * 20); // 15-35°C
                    event.setUnit("celsius");
                    break;
                case "HUMIDITY":
                    event.setSensorValue(30.0 + random.nextDouble() * 50); // 30-80%
                    event.setUnit("percent");
                    break;
                case "MOTION":
                    event.setSensorValue(random.nextBoolean() ? 1.0 : 0.0);
                    event.setUnit("boolean");
                    break;
                default:
                    event.setSensorValue(random.nextDouble() * 100);
                    event.setUnit("units");
            }

            event.setSeverity("LOW");

            sendEvent(KafkaConfig.TOPIC_IOT, event);
            sleep(delayMs);
        }
    }

    public void generateNetworkEvents(int count, long delayMs) {
        System.out.println("\n🌐 Generating " + count + " Network events...");
        String[] eventTypes = { "NORMAL_TRAFFIC", "PORT_SCAN", "TRAFFIC_SPIKE",
                "UNAUTHORIZED_ACCESS", "DDoS_ATTEMPT" };
        String[] protocols = { "TCP", "UDP", "ICMP", "HTTP", "HTTPS" };
        String[] actions = { "ALLOW", "BLOCK", "DROP" };

        for (int i = 0; i < count; i++) {
            NetworkDeviceEvent event = new NetworkDeviceEvent();
            event.setDeviceId("RTR-" + (random.nextInt(5) + 1));
            event.setDeviceName("Router-Main-" + (random.nextInt(3) + 1));

            String eventType = eventTypes[random.nextInt(eventTypes.length)];
            event.setEventType(eventType);
            event.setPortNumber(random.nextInt(65535));
            event.setProtocol(protocols[random.nextInt(protocols.length)]);
            event.setBytesTransferred((long) (random.nextDouble() * 10000000));
            event.setSourceIp("172.16.0." + (random.nextInt(254) + 1));
            event.setDestinationIp("8.8.8." + (random.nextInt(10) + 1));
            event.setAction(actions[random.nextInt(actions.length)]);

            // Set severity
            if (eventType.contains("SCAN") || eventType.contains("DDoS") ||
                    eventType.contains("UNAUTHORIZED")) {
                event.setSeverity("CRITICAL");
            } else if (eventType.contains("SPIKE")) {
                event.setSeverity("MEDIUM");
            } else {
                event.setSeverity("LOW");
            }

            sendEvent(KafkaConfig.TOPIC_NETWORK, event);
            sleep(delayMs);
        }
    }

    public void generateServerEvents(int count, long delayMs) {
        System.out.println("\n🖥️  Generating " + count + " Server events...");
        String[] eventTypes = { "NORMAL_OPERATION", "CPU_HIGH", "MEMORY_HIGH",
                "DISK_FULL", "SERVICE_RESTART", "CRASH" };
        String[] services = { "web-server", "database", "api-gateway", "cache",
                "auth-service", "email-service" };

        for (int i = 0; i < count; i++) {
            ServerEvent event = new ServerEvent();
            event.setDeviceId("SRV-" + (random.nextInt(10) + 1));
            event.setServerName("Server-" + (char) ('A' + random.nextInt(5)));

            String eventType = eventTypes[random.nextInt(eventTypes.length)];
            event.setEventType(eventType);
            event.setCpuUsage(20.0 + random.nextDouble() * 70);
            event.setMemoryUsage(30.0 + random.nextDouble() * 60);
            event.setDiskUsage(40.0 + random.nextDouble() * 50);
            event.setActiveConnections(random.nextInt(1000));
            event.setService(services[random.nextInt(services.length)]);
            event.setSourceIp("192.168.100." + (random.nextInt(254) + 1));

            // Set severity based on metrics
            if (event.getCpuUsage() > 80 || event.getMemoryUsage() > 85 ||
                    eventType.contains("CRASH")) {
                event.setSeverity("CRITICAL");
            } else if (event.getCpuUsage() > 60 || event.getMemoryUsage() > 70) {
                event.setSeverity("HIGH");
            } else {
                event.setSeverity("LOW");
            }

            // Set new fields for Server events (simulating admin access)
            if (eventType.contains("SERVICE") || eventType.contains("CRASH")) {
                event.setUsername("system");
                event.setAuthenticationStatus("NONE");
            } else {
                event.setUsername("admin");
                event.setAuthenticationStatus("SUCCESS");
            }

            sendEvent(KafkaConfig.TOPIC_SERVER, event);
            sleep(delayMs);
        }
    }

    private void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void close() {
        System.out.println("\n🛑 Closing Kafka Producer...");
        producer.close();
    }

    public static void main(String[] args) {
        DeviceEventProducer producer = new DeviceEventProducer(KafkaConfig.BOOTSTRAP_SERVERS);

        System.out.println("🚀 Starting HiveMind Multi-Device Event Simulator\n");
        System.out.println("=".repeat(60));

        // Generate events from all device types in parallel threads
        Thread workstationThread = new Thread(() -> producer.generateWorkstationEvents(20, 1000));

        Thread iotThread = new Thread(() -> producer.generateIoTEvents(30, 800));

        Thread networkThread = new Thread(() -> producer.generateNetworkEvents(15, 1200));

        Thread serverThread = new Thread(() -> producer.generateServerEvents(25, 1000));

        // Start all threads
        workstationThread.start();
        iotThread.start();
        networkThread.start();
        serverThread.start();

        // Wait for all to complete
        try {
            workstationThread.join();
            iotThread.join();
            networkThread.join();
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ Event generation completed!");
        producer.close();
    }
}