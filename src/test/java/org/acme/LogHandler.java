package org.acme;

import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogHandler extends Handler {

    ArrayList<LogRecord> logEntries = new ArrayList<LogRecord>();

    @Override
    public void publish(LogRecord logRecord) {
        logEntries.add(logRecord);
    }

    @Override
    public void flush() {
        logEntries.clear();
    }

    @Override
    public void close() throws SecurityException {

    }

    public ArrayList<LogRecord> getLogEntries() {
        return logEntries;
    }
}
