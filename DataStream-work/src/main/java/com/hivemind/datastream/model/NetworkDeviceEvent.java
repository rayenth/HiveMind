package com.hivemind.datastream.model;

public class NetworkDeviceEvent extends DeviceEvent {
    private String deviceName;
    private Integer portNumber;
    private String protocol;
    private Long bytesTransferred;
    private String destinationIp;
    private String action; // ALLOW, BLOCK, DROP

    public NetworkDeviceEvent() {
        super();
        setDeviceType("NETWORK");
    }

    // Getters and Setters
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public Integer getPortNumber() { return portNumber; }
    public void setPortNumber(Integer portNumber) { this.portNumber = portNumber; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public Long getBytesTransferred() { return bytesTransferred; }
    public void setBytesTransferred(Long bytesTransferred) { this.bytesTransferred = bytesTransferred; }

    public String getDestinationIp() { return destinationIp; }
    public void setDestinationIp(String destinationIp) { this.destinationIp = destinationIp; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}