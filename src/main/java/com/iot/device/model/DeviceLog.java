package com.iot.device.model;

import java.time.Instant;
import java.util.UUID;

public record DeviceLog(
    //UUID deviceId,
    String deviceId,
    DeviceStatus status,
    double temperature,
    Instant timestamp
) {}
