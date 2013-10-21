package nepic.roi;

import nepic.image.Constraint;

/**
 *
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_ALpha_v0-9_122212
 *
 * @param <E>
 */
public abstract class CellBodyConstraint<E> extends Constraint<E> {

    public CellBodyConstraint(E val) {
        super(val);
    }

    public static class Hello extends CellBodyConstraint<String> {

        public Hello(String val) {
            super(val);
            // TODO Auto-generated constructor stub
        }

    }
}