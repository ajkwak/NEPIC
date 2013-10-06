package nepic.util;

/**
 * An interface for classes that are designed to build objects of other classes.
 * 
 * @author AJ Parmidge
 *
 * @param <T> The type of object this {@link Builder} builds
 */
public interface Builder<T> {

    /**
     * Build the desired object from the data given to the {@link Builder}.
     *
     * @return the built object
     */
    public T build();
}
