package nepic.logging;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-8_NewLogger
 * @version AutoCBFinder_Alpha_v0-9-2013-01-16_newGui
 * 
 */
public interface LoggerObserver {

    public void verboseMessageLogged(String message);

    public void infoMessageLogged(String message);

    public void warningMessageLogged(String message);

    public void errorMessageLogged(String message);

    public void fatalErrorMessageLogged(String message);

}
