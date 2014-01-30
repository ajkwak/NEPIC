package nepic.util;

/**
 * Interface for objects that can exit in a valid or a non-valid state.
 * 
 * @author AJ Parmidge
 */
public interface Validatable {

    /**
     * Determines whether this object is currently in a valid state.
     *
     * @return {@code true} if this object is in a currently valid state; otherwise {@code false}
     */
    public boolean isValid();

}
