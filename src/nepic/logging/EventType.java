package nepic.logging;

/**
 * The type of event being logged
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_ALpha_v0-9_122212
 */
public enum EventType {
    /**
     * Message that should only be recorded if an error is reported during the session recorded by
     * this {@link EventLogger}. Cannot be displayed to the user
     */
    VERBOSE,
    /**
     * Generic info message
     */
    INFO,
    /**
     * Generic warning message
     */
    WARNING,
    /**
     * Generic error message
     */
    ERROR,
    /**
     * Message describing a severe error that necessitates this application to close
     */
    ERROR_FATAL;
}
