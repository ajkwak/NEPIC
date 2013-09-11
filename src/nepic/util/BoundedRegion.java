package nepic.util;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-9-2013-02-10
 * @version AutoCBFinder_Alpha_v0-9-2013-02-10
 * 
 */
public interface BoundedRegion {
    public int getMinX();

    public int getMaxX();

    public int getMinY();

    public int getMaxY();

    public boolean boundsContain(BoundedRegion region);

    public boolean boundsContain(int x, int y);
}
