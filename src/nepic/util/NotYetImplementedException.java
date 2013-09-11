package nepic.util;

/**
 * Convenience exception for programmer during project development to indicate that methods (in an
 * API, e.g.) hare not yet been implemented. This exception should never be present in a product
 * when it is pushed to users.
 * 
 * @author AJ Parmidge
 * 
 */
public class NotYetImplementedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

}
