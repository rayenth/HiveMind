package caravane.model;

public class WorkstationEvent extends DeviceEvent {
    private String userId;
    private String processName;
    private String fileName;
    private boolean loginSuccess;

    public WorkstationEvent() {
        super();
        setDeviceType("WORKSTATION");
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public boolean isLoginSuccess() { return loginSuccess; }
    public void setLoginSuccess(boolean loginSuccess) { this.loginSuccess = loginSuccess; }
}