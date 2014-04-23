package nepic.geo.test;

import java.util.HashMap;

import com.google.common.collect.Maps;

import nepic.geo.BoundedRegion;
import nepic.geo.BoundingBox;
import nepic.image.IdTaggedImage;
import nepic.util.Verify;

/**
 * A simple implementation of {@link IdTaggedImage} for use by tests. The main advantage of this
 * implementation for tests (particularly for manual tests) is that the {@link #toString()} method
 * returns a string representation of the image in which every pixel in the
 * {@link TestIdTaggedImage} is represented by a single character, and each {@code ID} is
 * represented as a different character. This makes it easy to see exaclty what the image looks like
 * (what pixels map to what ID), which can be useful for debugging.
 *
 * @author AJ Parmidge
 */
public class TestIdTaggedImage implements IdTaggedImage {
    /**
     * The default ID of all pixels in this {@link TestIdTaggedImage}. Do not set any of the other
     * IDs in this {@link TestIdTaggedImage} to this value.
     * <p>
     * Value = {@value #DEFAULT_ID}
     */
    public static final int DEFAULT_ID = 0;
    /**
     * The character that represents the {@link #DEFAULT_ID} when this {@link TestIdTaggedImage} is
     * printed using its {@link #toString()} method.
     * <p>
     * Value = '{@value #DEFAULT_ID_CHAR}'
     */
    public static final char DEFAULT_ID_CHAR = '-';

    private final BoundingBox bounds;
    private final int[][] img;
    private final HashMap<Integer, Character> idMap = Maps.newHashMap();

    /**
     * Create a new {@link TestIdTaggedImage} with the given boundaries.
     *
     * @param minX the minimum x-value of the {@link TestIdTaggedImage} to create
     * @param maxX the maximum x-value of the {@link TestIdTaggedImage} to create
     * @param minY the minimum y-value of the {@link TestIdTaggedImage} to create
     * @param maxY the maximum y-value of the {@link TestIdTaggedImage} to create
     */
    public TestIdTaggedImage(int minX, int maxX, int minY, int maxY) {
        idMap.put(DEFAULT_ID, DEFAULT_ID_CHAR);
        bounds = new BoundingBox(minX, maxX, minY, maxY);
        img = new int[maxX - minX + 1][maxY - minY + 1];
    }

    @Override
    public int getMinX() {
        return bounds.getMinX();
    }

    @Override
    public int getMaxX() {
        return bounds.getMaxX();
    }

    @Override
    public int getMinY() {
        return bounds.getMinY();
    }

    @Override
    public int getMaxY() {
        return bounds.getMaxY();
    }

    @Override
    public boolean boundsContain(BoundedRegion region) {
        return bounds.boundsContain(region);
    }

    @Override
    public boolean boundsContain(int x, int y) {
        return bounds.boundsContain(x, y);
    }

    /**
     * Sets up the given ID in this {@link TestIdTaggedImage} to be represented by the given
     * character when the {@link #toString()} method of this {@link TestIdTaggedImage} is called.
     *
     * @param id the ID to set
     * @param idChar the character to set with the given ID
     * @return the character that used to represent the given ID, or {@code null} if no character
     *         was represented by this ID before
     */
    public Character createId(int id, char idChar) {
        return idMap.put(id, idChar);
    }

    /**
     * Equivalent to {@link #createId(int, char)}, except that an exception is thrown if the given
     * ID already is already represented by a character. This can be useful to help prevent
     * accidentally duplicating IDs when using this {@link TestIdTaggedImage}.
     *
     * @param id the ID to set
     * @param idChar the character to set with the given ID
     * @throws IllegalStateException if the given ID was already represented by a different
     *         character
     */
    public void createIdOrThrow(int id, char idChar) {
        Character displacedChar = createId(id, idChar);
        System.out.println("displacedChar = " + displacedChar);
        Verify.state(displacedChar == null, "Displacing non-null character " + displacedChar
                + " for ID " + id);
    }

    /**
     * Sets the pixel at the given coordinate to the given ID.
     * <p>
     * Note that before this method is called, the given ID must have been set up for this image
     * using the {@link #createId(int, char)} method or the {@link #createIdOrThrow(int, char)}
     * method.
     *
     * @param x the x-value of the pixel to set to the given ID
     * @param y the y-value of the pixel to set to the given ID
     * @param id the ID to set
     * @return the former ID of the pixel
     * @throws IllegalArgumentException if the character for the given ID has not yet been set with
     *         {{@link #createId(int, char)} or {@link #createIdOrThrow(int, char)}
     */
    public int setId(int x, int y, int id) {
        Verify.argument(idMap.get(id) != null, "Char for given id (=" + id + ") not set");
        x = x - getMinX();
        y = y - getMinY();
        int oldId = img[x][y];
        img[x][y] = id;
        return oldId;
    }

    /**
     * Equivalent to {@link #setId(int, int, int)}, except that an exception is thrown if the given
     * pixel already has an ID other than the {@link #DEFAULT_ID}. This can be useful for verifying
     * that two regions with different IDs do not overlap, e.g.
     *
     * @param x the x-value of the pixel to set to the given ID
     * @param y the y-value of the pixel to set to the given ID
     * @param id the ID to set
     * @throws IllegalArgumentException if the character for the given ID has not yet been set with
     *         {{@link #createId(int, char)} or {@link #createIdOrThrow(int, char)}
     */
    public void setIdOrThrow(int x, int y, int id) {
        int displacedId = setId(x, y, id);
        Verify.state(displacedId == DEFAULT_ID, "Displacing non-default ID " + displacedId);
    }

    @Override
    public int getId(int x, int y) {
        x = x - getMinX();
        y = y - getMinY();
        return img[x][y];
    }

    @Override
    public boolean contains(int x, int y) {
        return boundsContain(x, y);
    }

    /**
     * Creates a String-representation of this {@link TestIdTaggedImage} in which every pixel in the
     * image is represented by a single character. Each character represents the ID of that pixel,
     * as set up in the {@link #createId(int, char)} method on this {@link TestIdTaggedImage}.
     */
    @Override
    public String toString() {
        int width = img.length;
        int height = img[0].length;
        StringBuilder builder = new StringBuilder((width + 1) * height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                builder.append(idMap.get(img[x][y]));
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
