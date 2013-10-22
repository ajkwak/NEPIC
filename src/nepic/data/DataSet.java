package nepic.data;

import java.awt.Point;
import java.util.Collection;

import nepic.util.BoundedRegion;

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
    public DataSet setName(String name);

    /**
     * Returns the color of this data set, in RGB format.
     */
    public int getRgb();

    /**
     * Sets the color of this data set to the given RGB value.
     *
     * @param rgb the color to set
     */
    public DataSet setRgb(int rgb);

    public DataSet setData(Point... data);

    public DataSet setData(Collection<Point> data);
}
