package Caravane.events;

import org.springframework.context.ApplicationEvent;

/**
 * Event triggered when a new log entry is detected in system logs
 */
public class LogEntryEvent extends ApplicationEvent {
    private final String logFile;
    private final String logLine;
    private final String severity;
    private final long eventTimestamp;

    public LogEntryEvent(Object source, String logFile, String logLine, String severity) {
        super(source);
        this.logFile = logFile;
        this.logLine = logLine;
        this.severity = severity;
        this.eventTimestamp = System.currentTimeMillis();
    }

    public String getLogFile() {
        return logFile;
    }

    public String getLogLine() {
        return logLine;
    }

    public String getSeverity() {
        return severity;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }
}
