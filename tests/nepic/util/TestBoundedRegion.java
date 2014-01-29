package nepic.util;

import nepic.geo.BoundedRegion;

/**
 * A test implementation of the {@link BoundedRegion} interface.
 *
 * @author AJ Parmidge
 */
public class TestBoundedRegion implements BoundedRegion {
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    /**
     * Creates a {@link TestBoundedRegion} bounded by the given values.
     *
     * @param minX the minimum x-value to set
     * @param maxX the maximum x-value to set
     * @param minY the minimum y-value to set
     * @param maxY the maximum y-value to set
     */
    public TestBoundedRegion(int minX, int maxX, int minY, int maxY) {
        Verify.argument(minX <= maxX, "minX must be <= maxX");
        Verify.argument(minY <= maxY, "minY must be <= maxY");
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public int getMinX() {
        return minX;
    }

    @Override
    public int getMaxX() {
        return maxX;
    }

    @Override
    public int getMinY() {
        return minY;
    }

    @Override
    public int getMaxY() {
        return maxY;
    }

    /**
     * Currently unsupported.
     */
    @Override
    public boolean boundsContain(BoundedRegion region) {
        throw new UnsupportedOperationException();
    }

    /**
     * Currently unsupported.
     */
    @Override
    public boolean boundsContain(int x, int y) {
        throw new UnsupportedOperationException();
    }

}
