package com.hivemind.datastream.model;

public class ServerEvent extends DeviceEvent {
    private String serverName;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double diskUsage;
    private Integer activeConnections;
    private String service;

    public ServerEvent() {
        super();
        setDeviceType("SERVER");
    }

    // Getters and Setters
    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }

    public Double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }

    public Double getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(Double memoryUsage) { this.memoryUsage = memoryUsage; }

    public Double getDiskUsage() { return diskUsage; }
    public void setDiskUsage(Double diskUsage) { this.diskUsage = diskUsage; }

    public Integer getActiveConnections() { return activeConnections; }
    public void setActiveConnections(Integer activeConnections) { this.activeConnections = activeConnections; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
}