package nepic.util;

import java.awt.Point;
import java.util.Collection;

/**
 * Interface representing a named set of {@link Point} values bounded in physical (Cartesian) space.
 *
 * @author AJ Parmidge
 */
public interface DataSet extends BoundedRegion, Collection<Point> {

    /**
     * Returns the name of this data set.
     */
    public String getName();

    /**
     * Sets the name of this data set to the given value.
     *
     * @param name the name to set
     */
    public void setName(String name);

    /**
     * Returns the color of this data set, in RGB format.
     */
    public int getRgb();

    /**
     * Sets the color of this data set to the given RGB value.
     *
     * @param rgb the color to set
     */
    public void setRgb(int rgb);
}
