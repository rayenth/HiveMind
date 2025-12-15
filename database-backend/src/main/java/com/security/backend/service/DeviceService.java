package com.security.backend.service;

import com.security.backend.model.Device;
import com.security.backend.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Optional<Device> getDeviceById(UUID id) {
        return deviceRepository.findById(id);
    }

    public Device registerDevice(Device device) {
        device.setLastSeen(LocalDateTime.now());
        device.setStatus("ONLINE");
        return deviceRepository.save(device);
    }

    public void updateDeviceStatus(UUID id, String status) {
        Optional<Device> deviceOpt = deviceRepository.findById(id);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setStatus(status);
            device.setLastSeen(LocalDateTime.now());
            deviceRepository.save(device);
        }
    }
}
