package com.iot.device.controller;

import com.iot.device.model.DeviceLog;
import com.iot.device.service.IoTLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/iot")
public class IoTLogController {

    private final IoTLogService ioTLogService;

    public IoTLogController(IoTLogService ioTLogService) {
        this.ioTLogService = ioTLogService;
    }

    @PostMapping("/send-log")
    public ResponseEntity<DeviceLog> sendLog() {
        DeviceLog log = ioTLogService.generateAndSendLog();
        return ResponseEntity.ok(log);
    }

    @PostMapping("/send-log/custom")
    public ResponseEntity<DeviceLog> sendCustomLog(@org.springframework.web.bind.annotation.RequestParam double temperature) {
        DeviceLog log = ioTLogService.generateAndSendLog(temperature);
        return ResponseEntity.ok(log);
    }
}
