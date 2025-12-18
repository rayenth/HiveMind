# Database Schema Documentation

## 1. Table: `alerts`
Stores critical system alerts and notifications.
- **`id`** (UUID): Unique alert identifier.
- **`type`** (Text): Category (e.g., "SECURITY", "SYSTEM").
- **`message`** (Text): Description of the alert.
- **`severity`** (Text): LOW, MEDIUM, HIGH, CRITICAL.
- **`status`** (Text): NEW, READ, RESOLVED.
- **`source`** (Text): Origin of alert (SYSTEM, AI_MODULE, MANUAL).
- **`device_id`** (Text): ID of the related device.
- **`timestamp`** (Timestamp): Event time.

## 2. Table: `anomaly_alerts`
AI-detected anomalies from log analysis.
- **`alert_id`** (Text): Unique identifier.
- **`device_id`** (Text): Target device.
- **`description`** (Text): What went wrong.
- **`detected_value`** (Double): Confidence or anomaly score.
- **`timestamp`** (Timestamp): Time of detection.

## 3. Table: `laptops`
Registry of laptop computers on the network.
- **`id`** (UUID): Unique laptop ID.
- **`mac_address`** (Text): Physical hardware address.
- **`name`** (Text): Device name.
- **`ip_address`** (Text): Network IP address.
- **`status`** (Text): Operational state (e.g., "ONLINE", "COMPROMISED").
- **`os_version`** (Text): Operating system version.
- **`owner`** (Text): Primary user assigned to the device.
- **`last_seen`** (Timestamp): Last communication time.
- **`created_at`** / **`updated_at`**: Metadata timestamps.

## 4. Table: `security_events`
Raw logs of system activities and security checks.
- **`id`** (UUID): Unique event ID.
- **`event_type`** (Text): Type of the event (e.g., "LOGIN_FAILURE").
- **`device_id`** (Text): ID of the reporting device.
- **`severity`** (Text): LOW, MEDIUM, HIGH, CRITICAL.
- **`timestamp`** (Timestamp): Time of occurrence.
- **`metadata`** (Text): Extra JSON or text data.

## 5. Table: `services`
Tracks status of internal backend services.
- **`id`** (UUID): Unique service ID.
- **`name`** (Text): Service name (e.g., "Kafka", "DataStream").
- **`status`** (Text): Health status (e.g., "UP", "DOWN").
- **`port`** (Int): Port number the service runs on.
- **`version`** (Text): Version string.
- **`timestart`**: When the service started.
- **`timeend`**: When the service stopped (if applicable).

## 7. Table: `users`
System users and administrators.
- **`id`** (UUID): Unique user ID.
- **`username`** (Text): Login name.
- **`password`** (Text): Hashed password.
- **`role`** (Text): Permissions level ("ADMIN", "USER", "SECURITY_ENGINEER").
- **`created_at`** / **`updated_at`**: Metadata.

## 8. Table: `iot_device_logs`
Time-series logs for IoT devices.
- **`device_id`** (UUID): ID of the device.
- **`timestamp`** (Timestamp): Time of the log entry.
- **`status`** (Text): ONLINE, OFFLINE.
- **`temperature`** (Double): Recorded temperature.
