package nepic.logging;

/**
 * The type of event being logged by an {@link EventLogger}.
 */
public enum EventType {
    /**
     * Message that should only be recorded if an error is reported during the session recorded by
     * this {@link EventLogger}. Cannot be displayed to the user.
     */
    VERBOSE,
    /**
     * An {@code INFO}-level event. This {@link EventType} should be used when some important or
     * complicated
     * procedure has been completed successfully.
     */
    INFO,
    /**
     * A {@code WARNING}-level event. This {@link EventType} should be used when a not-unexpected
     * and fully-handled error occurs.
     */
    WARNING,
    /**
     * An {@code ERROR}-level event. This {@link EventType} should be used when an unexpected error
     * occurs.
     */
    ERROR,
    /**
     * A {@code FATAL_ERROR}-level event. This {@link EventType} should be used ONLY if an error
     * occurs that is so severe that NEPIC cannot recover and must close immediately.
     */
    FATAL_ERROR;
}
