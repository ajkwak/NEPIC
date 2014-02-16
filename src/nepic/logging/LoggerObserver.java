package nepic.logging;

/**
 * Interface that allows implementing classes to observe and respond to {@link EventLogger} logs.
 */
public interface LoggerObserver {

    /**
     * Respond to an {@link EventType#INFO} message being logged.
     *
     * @param message the {@code INFO} message
     */
    public void respondToInfo(String message);

    /**
     * Respond to an {@link EventType#WARNING} message being logged.
     *
     * @param message the {@code WARNING} message
     */
    public void respondToWarning(String message);

    /**
     * Respond to an {@link EventType#ERROR} message being logged.
     *
     * @param message the {@code ERROR} message
     */
    public void respondToError(String message);

    /**
     * Respond to an {@link EventType#FATAL_ERROR} message being logged. Most {@link LoggerObserver}
     * s will
     * have no body to this method (only if it is TRULY necessary should a {@link LoggerObserver}
     * attempt to run code after a fatal error has been logged.
     * 
     * @param message the {@code FATAL_ERROR} message
     */
    public void respondToFatalError(String message);

}
