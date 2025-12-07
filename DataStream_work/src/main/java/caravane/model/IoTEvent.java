package caravane.model;

public class IoTEvent extends DeviceEvent {
    private String sensorType;
    private Double sensorValue;
    private String location;
    private String unit;

    public IoTEvent() {
        super();
        setDeviceType("IOT");
    }

    // Getters and Setters
    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }

    public Double getSensorValue() { return sensorValue; }
    public void setSensorValue(Double sensorValue) { this.sensorValue = sensorValue; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}