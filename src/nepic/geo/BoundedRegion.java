package nepic.geo;

/**
 * A region in two-dimensional Cartesian space that can be
 *
 * @author AJ Parmidge
 */
public interface BoundedRegion {
    /**
     * Returns the minimum x-coordinate value in the {@link BoundedRegion}.
     */
    public int getMinX();

    /**
     * Returns the maximum x-coordinate value in the {@link BoundedRegion}.
     */
    public int getMaxX();

    /**
     * Returns the minimum y-coordinate value in the {@link BoundedRegion}.
     */
    public int getMinY();

    /**
     * Returns the maximum y-coordinate value in the {@link BoundedRegion}.
     */
    public int getMaxY();

    /**
     * Determine whether the bounds of this {@link BoundedRegion} inclusively contain the bounds of
     * the parameter {@link BoundedRegion}.
     * <p>
     * NOTE: since this method returns whether or not the bounds of this {@link BoundedRegion}
     * contain the bounds of the given {@link BoundedRegion}, this method should return {@code true}
     * on the call {@code boundsContain(this)}.
     *
     * @param region the potentially contained bounded region
     * @return {@code true} if the bounds of {@code this} {@link BoundedRegion} contain the bounds
     *         of the given {@link BoundedRegion}; otherwise {@code false}
     */
    public boolean boundsContain(BoundedRegion region);

    /**
     * Determine whether the bounds of this {@link BoundedRegion} inclusively contain the given
     * coordinate.
     *
     * @param region the potentially contained bounded region
     * @return {@code true} if the bounds of {@code this} {@link BoundedRegion} contain the bounds
     *         of the given {@link BoundedRegion}; otherwise {@code false}
     */
    public boolean boundsContain(int x, int y);
}
