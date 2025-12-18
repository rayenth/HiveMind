package com.iot.device.model;

public record DeviceLogInput(
        String deviceId,
        double temperature,
        DeviceStatus status,
        String timestamp) {
}
