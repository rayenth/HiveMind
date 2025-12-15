package com.security.backend.controller;

import com.security.backend.model.Device;
import com.security.backend.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public List<Device> getAllDevices() {
        return deviceService.getAllDevices();
    }

    @PostMapping
    public Device registerDevice(@RequestBody Device device) {
        return deviceService.registerDevice(device);
    }

    @PatchMapping("/{id}/status")
    public void updateDeviceStatus(@PathVariable UUID id, @RequestParam String status) {
        deviceService.updateDeviceStatus(id, status);
    }
}
