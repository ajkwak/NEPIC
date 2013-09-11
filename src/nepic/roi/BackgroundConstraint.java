package nepic.roi;

import nepic.image.Constraint;

/**
 * Constraints defining the Background ROI.
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_ALpha_v0-9_122212
 * 
 * @param <E> run-time class identity of the constraint value
 */
public abstract class BackgroundConstraint<E> extends Constraint<E> {

    public BackgroundConstraint(E val) {
        super(val);
    }

}
