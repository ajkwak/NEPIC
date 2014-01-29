package nepic.image;

import nepic.geo.BoundedRegion;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-9-2013-01-29
 * @version AutoCBFinder_Alpha_v0-9-2013-01-29
 * 
 */
public interface IdTaggedImage extends BoundedRegion {

    public int getId(int x, int y);

    public boolean contains(int x, int y);

}
