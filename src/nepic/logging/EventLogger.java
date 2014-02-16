package nepic.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import nepic.Nepic;
import nepic.util.Verify;

/**
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-8_NewLogger
 * @version AutoCBFinder_Alpha_v0-9-2013-01-29
 */
public class EventLogger {

    public static final String LOG_ONLY = null;
    private static final int NUM_EVENTS_BEFORE_SAVE = 100;
    private final List<Log> events;
    private boolean errorRecorded = false;
    private boolean warningRecorded = false;

    private File logFile;
    private PrintWriter writer = null;

    public EventLogger(String name) {
        Verify.notNull(name, "Name for event log cannot be null");

        // Set up observers
        observers = new LinkedList<LoggerObserver>();
        events = new ArrayList<Log>(NUM_EVENTS_BEFORE_SAVE);

        // Set up event logging for this session
        logFile = getLogFile(name);
        if (logFile != null) {
            try {
                writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
                writer.println(makeStartingLogLine());
            } catch (IOException e) {
                logFile = null;
                logEvent(EventType.ERROR,
                        "Unable to initialize log!  All log events will be displayed in their "
                                + "entirety during this session.", formatException(e));
            }
        } else {
            logEvent(EventType.ERROR,
                    "Unable to initialize log!  All log events will be displayed in their "
                            + "entirety during this session.");
        }
    }

    public boolean haveLoggingFile() {
        return logFile != null;
    }

    public String getLogFileName() {
        if (logFile == null) {
            return null;
        }
        return logFile.getName();
    }

    public File getLogFile(String name) {
        int logNum = 1;
        String logName;
        File logFile;
        boolean fileExists;
        do { // Don't write to file over 500kB
            logName = name + "[" + logNum + "].log";
            logFile = new File(logName);
            logNum++;

            fileExists = logFile.exists();
        } while (fileExists && (logFile.length() > 512000L || !logFile.canWrite()));

        // Try to make the file if it doesn't yet exist.
        if (!fileExists) {
            try {
                if (!logFile.createNewFile()) {
                    Nepic.log(EventType.ERROR, "Attempt to create new log file "
                            + logFile.getName() + " has failed.");
                    return null;
                }
            } catch (IOException e) {
                Nepic.log(EventType.ERROR,
                        "Exception occurred while trying to create new log file "
                                + logFile.getName(), formatException(e));
                return null;
            }
        }

        return logFile;
    }

    public boolean errorsRecorded() {
        return errorRecorded;
    }

    public boolean warningRecorded() {
        return warningRecorded;
    }

    private String makeStartingLogLine() {
        return new StringBuilder("\n\n*****************************************************\n\n# ")
                .append(Nepic.getName())
                .append(' ')
                .append(Nepic.getFullVersion())
                .append(" ")
                .append(this.getClass().getName())
                .append(" initialized ")
                .append(new Date())
                .toString();
    }

    public EventLogger logEvent(EventType eventType, String messageForUser, Object... furtherInfo) {
        boolean haveLoggingFile = haveLoggingFile();

        if (haveLoggingFile && events.size() >= NUM_EVENTS_BEFORE_SAVE) {
            saveLog();
        }

        Log currentEvent = new Log(eventType, messageForUser, furtherInfo);
        if (haveLoggingFile || observers.isEmpty()) {
            events.add(currentEvent);
        }

        // If error, record that error has occurred
        if (eventType.equals(EventType.ERROR) || eventType.equals(EventType.FATAL_ERROR)) {
            errorRecorded = true;
        } else if (eventType.equals(EventType.WARNING)) {
            warningRecorded = true;
        }

        // Inform observers
        if (!observers.isEmpty()) {
            if (!haveLoggingFile && !events.isEmpty()) {
                for (Log event : events) {
                    notifyObservers(event.type, event.toString());
                }
                events.clear();
            }
            notifyObservers(eventType, (haveLoggingFile ? messageForUser : currentEvent.toString()));
        }

        return this;
    }

    public static String formatException(Throwable e) {
        StringBuilder builder = new StringBuilder(e.toString());
        boolean includedNepicMethods = false;
        for (StackTraceElement el : e.getStackTrace()) {
            String elString = el.toString();
            if (elString.startsWith("nepic")) { // NOTE: THIS IS NEPIC-SPECIFIC (nepic packages)
                includedNepicMethods = true;
            } else if (includedNepicMethods) {
                break;
            }
            builder.append("\n\tat ");
            builder.append(elString);

        }
        return builder.toString();
    }

    private LinkedList<LoggerObserver> observers;

    public boolean registerObserver(LoggerObserver o) {
        return observers.add(o);
    }

    public boolean removeObserver(LoggerObserver o) {
        return observers.remove(o);
    }

    private void notifyObservers(EventType type, String message) {
        for (LoggerObserver observer : observers) {
            if (type.equals(EventType.INFO)) {
                observer.respondToInfo(message);
            } else if (type.equals(EventType.WARNING)) {
                observer.respondToWarning(message);
            } else if (type.equals(EventType.ERROR)) {
                observer.respondToError(message);
            } else if (type.equals(EventType.FATAL_ERROR)) {
                observer.respondToFatalError(message);
            }
        }
    }

    // Should only be called if saveLog() fails
    public String getLog() {
        StringBuilder logBuilder = new StringBuilder().append("\n");
        for (Log event : events) {
            logBuilder.append(event).append("\n");
        }
        return logBuilder.toString();
    }

    private boolean saveLog() { // will only be called if have logging file
        try {
            for (Log event : events) {
                if (!event.type.equals(EventType.VERBOSE)
                        || Nepic.getPrefs().isVerboseEventLogged()) {
                    writer.println(event);
                }
            }
            events.clear();
            writer.flush();
            return true;
        } catch (Exception e) {
            Nepic.log(EventType.ERROR, "Unable to save logged events" + ": " + formatException(e));
            return false;
        }// catch all exceptions
    }

    public void endLog() {
        if (haveLoggingFile()) {
            saveLog(); // save any unsaved Log events before closing the out stream
            writer.print("# Session ended and log successfully completed at " + new Date());
            writer.flush();
            writer.close();
        }
    }

    private class Log {
        private static final String separator = " ";
        private static final String usrMsgToken = "\"";
        private final EventType type;
        private String message;

        private Log(EventType type, String messageForUser, Object[] furtherInfo) {
            // initial verification
            Verify.notNull(type, "type");
            Verify.notNull(furtherInfo, "futher info");

            // Type of event being logged
            this.type = type;

            createLogMessage(messageForUser, furtherInfo);
        }

        private void createLogMessage(String messageForUser, Object[] furtherInfo) {
            StringBuilder builder = new StringBuilder(type.toString()).append(";\t");

            // Info about method generating message to be logged
            appendMessage(builder, beginLog());
            builder.append(" ::");

            // Include message to user, if necessary
            if (messageForUser != null && !messageForUser.isEmpty()) {
                builder.append(separator).append(usrMsgToken).append(messageForUser).append(
                        usrMsgToken);
            }

            // Include all given further info
            appendMessage(builder, furtherInfo);

            message = builder.toString();
        }

        private void appendMessage(StringBuilder messageBuilder, Object[] furtherInfo) {
            for (Object o : furtherInfo) {
                messageBuilder.append(separator).append(o);
            }
        }

        private Object[] beginLog() {
            StackTraceElement callerMethodInfo = Thread.currentThread().getStackTrace()[7];
            Object[] eventInfo = new Object[5];
            eventInfo[0] = callerMethodInfo.getClassName();
            eventInfo[1] = callerMethodInfo.getMethodName() + "()";
            eventInfo[2] = "[line";
            eventInfo[3] = callerMethodInfo.getLineNumber();
            eventInfo[4] = "]";
            return eventInfo;
        }

        @Override
        public String toString() {
            return message;
        }

    }
}
