package nepic.image;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_ALpha_v0-9_122212
 * 
 * @param <E>
 */
public class Constraint<E> {
    private E val;

    public Constraint(E val) {
        this.val = val;
    }

    public E getVal() {
        return val;
    }

    public Constraint<E> setVal(E val) {
        this.val = val;
        return this;
    }

    @Override
    public String toString() {
        return "Constraint[val = " + val + "]";
    }

}