-- Cassandra Schema Definition
-- For PowerShell: Get-Content create_tables.cql | docker exec -i cassandra-dev cqlsh
-- For CMD/Bash: docker exec -i cassandra-dev cqlsh < create_tables.cql

CREATE KEYSPACE IF NOT EXISTS security_keyspace
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE security_keyspace;

-- Alerts table for critical warnings
CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY,
    type text,
    message text,
    severity text,
    status text,
    source text,
    device_id text,
    timestamp timestamp,
    created_at timestamp,
    updated_at timestamp
);

-- Anomaly alerts from AI/ML analysis
CREATE TABLE IF NOT EXISTS anomaly_alerts (
    alert_id text PRIMARY KEY,
    device_id text,
    description text,
    detected_value double,
    timestamp timestamp
);

-- Laptops registry
CREATE TABLE IF NOT EXISTS laptops (
    id UUID PRIMARY KEY,
    mac_address text,
    name text,
    ip_address text,
    status text,
    os_version text,
    owner text,
    last_seen timestamp,
    created_at timestamp,
    updated_at timestamp
);

-- Raw security events
CREATE TABLE IF NOT EXISTS security_events (
    id UUID PRIMARY KEY,
    event_type text,
    device_id text,
    severity text,
    timestamp timestamp,
    metadata text
);

-- Backend services status
CREATE TABLE IF NOT EXISTS services (
    id UUID PRIMARY KEY,
    name text,
    status text,
    port int,
    version text,
    timestart timestamp,
    timeend timestamp
);

-- System users
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username text,
    password text,
    role text,
    created_at timestamp,
    updated_at timestamp
);

-- IoT Device Logs (Time-series data)
CREATE TABLE IF NOT EXISTS iot_device_logs (
    device_id uuid,
    timestamp timestamp,
    status text,
    temperature double,
    PRIMARY KEY (device_id, timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);
