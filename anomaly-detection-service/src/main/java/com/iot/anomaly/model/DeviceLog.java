package com.iot.anomaly.model;

import java.time.Instant;
import java.util.UUID;

public record DeviceLog(
    UUID deviceId,
    DeviceStatus status,
    double temperature,
    Instant timestamp
) {}
