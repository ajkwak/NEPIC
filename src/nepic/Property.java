package nepic;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_ALpha_v0-9_122212
 * @param <E>
 */
public class Property<E> {
    public final String name;
    private E value;

    public Property(String name) {
        this.name = name;
    }

    public Property(String name, E defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;
    }

}
