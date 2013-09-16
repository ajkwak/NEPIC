package nepic.gui;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nepic.util.ColoredPointList;
import nepic.util.DoubleLinkRing;
import nepic.util.ForTestingOnly;
import nepic.util.Lists;
import nepic.util.Pixel;
import nepic.util.Verify;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-9-2013-01-29 (called DrawableImage until
 *        AutoCBFinder_Alpha_v0-9-2013-02-10)
 * @version AutoCBFinder_Alpha_v0-9-2013-06-18
 */
public class AnnotatableImage {
    private BufferedImage img; // The image being annotated.
    private final Annotation[] stack; // The stack of annotations which can be applied to the image.
    private int stackTop; // position of the top non-empty element in the stack.

    /**
     * Creates an {@code AnnotatableImage} which can support the given number of annotations. Each
     * annotation in the resulting {@code AnnotatableImage} is uniquely keyed by a number of the
     * range [0, {@code maxNumAnnotations}).
     * 
     * @param maxNumAnnotations the maximum number of annotations that should be supported for this
     *        image
     */
    public AnnotatableImage(int maxNumAnnotations) {
        stack = new Annotation[maxNumAnnotations];
        for (int id = 0; id < maxNumAnnotations; id++) {
            stack[id] = new Annotation(id);
        }
        stackTop = -1;
    }

    /**
     * Sets the image of the {@link AnnotatableImage}. All annotations are automatically cleared
     * when the image is reset.
     * 
     * @param img the image to set
     * @return <code>this</code>, for chaining
     */
    public AnnotatableImage setImage(BufferedImage img) {
        Verify.notNull(img, "Image to set cannot be null");
        clearAll();
        this.img = img;
        return this;
    }

    public BufferedImage getImage() {
        return img;
    }

    /**
     * Determines if the given number is a valid ID number for an annotation in this image.
     * 
     * @param id the id number to test
     * @return true if the given ID number is valid for this image; otherwise false
     */
    public boolean isValidId(int id) {
        return id >= 0 && id < stack.length;
    }

    public void erase(int id) {
        Verify.argument(isValidId(id), "Cannot restore category with invalid ID " + id);

        int pos = findPosOfAnnotation(id);
        if (pos <= stackTop) {
            // Preprocess the stack so that the annotation to erase is at the top of the stack.
            workWithAnnotationAtPos(pos);

            // Clear the annotation to erase (already erased during the preprocessing step), and
            // decrement the top of the stack.
            stack[stackTop].clear();
            stackTop--;
        }
    }

    /**
     * Erases all of the annotations currently drawn on the image.
     */
    public void eraseAll() {
        for (int i = stackTop; i >= 0; i--) { // pop off stock consecutively
            // Erase all non-empty annotations
            stack[i].erase();
            stack[i].clear();
        }
        stackTop = -1;
    }

    /**
     * Recolors the annotation with the given ID to the given color (in RGB format).
     * 
     * @param id the unique id of the annotation to recolor.
     * @param rgb the color (in RGB format) to paint the annotation.
     * @throws IllegalStateException if the annotation to recolor is empty (i.e. if there is nothing
     *         to recolor)
     */
    public void recolor(int id, int rgb) {
        Verify.argument(isValidId(id), "Cannot restore category with invalid ID " + id);
        int pos = findPosOfAnnotation(id);
        Verify.state(pos <= stackTop, "Cannot recolor an empty annotation.");

        workWithAnnotationAtPos(pos); // Now, desired annotation is at the top of the stack.
        stack[stackTop].recolor(rgb);
    }

    /**
     * Annotates the image with the given {@link ColoredPointList} information. If the annotation
     * specified by the given ID already contains any values, the annotation is erased before these
     * new values are applied.
     * 
     * In other words, draws or re-draws the annotation on the image.
     * 
     * @param id the unique ID of the annotation
     * @param newVals the pixels with which to annotate the image, and the colors to paint those
     *        pixels on the image
     */
    public void redraw(int id, ColoredPointList... newVals) {
        Verify.argument(isValidId(id), "Cannot restore category with invalid ID " + id);
        Verify.argument(newVals != null && newVals.length > 0,
                "The given set of ColoredPixelLists must be non-empty");

        // Preprocess stack so we're ready to work with the desired annotation.
        workWithAnnotationAtPos(findPosOfAnnotation(id));

        // Clear the annotation and re-draw it with the given values.
        Annotation toRedraw = stack[stackTop];
        toRedraw.clear();
        for (ColoredPointList toAdd : newVals) {
            List<? extends Point> newPixels = toAdd.points;

            // Draw the given MonochromePixelSet.
            MonochromePixelSet pixSet = new MonochromePixelSet(toAdd.rgb);
            for (Point toDraw : newPixels) { // Draws in order
                pixSet.addPoint(toDraw); // In place
            }
            toRedraw.addPixelSet(pixSet);
        }
    }

    public void addPoints(int id, ColoredPointList... newVals) {
        Verify.argument(isValidId(id));
        Verify.argument(newVals != null && newVals.length > 0,
                "The given set of ColoredPixelLists must be non-empty");

        // Preprocess stack so we're ready to work with the desired annotation.
        workWithAnnotationAtPos(findPosOfAnnotation(id));

        Annotation addTo = stack[stackTop];
        for (ColoredPointList toAdd : newVals) {
            // Draw the given MonochromePixelSet.
            MonochromePixelSet pixSet = new MonochromePixelSet(toAdd.rgb);
            for (Point toDraw : toAdd.points) { // Draws in order
                pixSet.addPoint(toDraw); // In place
            }
            addTo.addPixelSet(pixSet);
        }
    }

    private int findPosOfAnnotation(int id) {
        int pos = stack.length - 1;
        while (pos >= 0 && id != stack[pos].id) {
            pos--;
        }
        return pos;
    }

    // Puts the annotation to work with at the top of the stack (without restoring that annotation),
    // so that the annotation can be manipulated without affecting any other annotation in the
    // stack.
    private void workWithAnnotationAtPos(int pos) {
        if (pos > stackTop) { // Working with a previously empty annotation.
            stackTop++;
            swapAnnotationsAt(pos, stackTop);
        } else { // Working with an existing annotation
            // Erase all items >= pos in the stack.
            for (int i = stackTop; i >= pos; i--) {
                stack[i].erase();
            }

            // Restore all items formerly above the annotation to redraw in the stack.
            Annotation annotation = stack[pos];
            for (int i = pos + 1; i <= stackTop; i++) {
                stack[i].restore();
                stack[i - 1] = stack[i]; // Move down (so that 'annotation' becomes the stack top).
            }

            // Put the annotation to draw at the top of the stack, and clear its pixels, since
            // these will be reset.
            stack[stackTop] = annotation;
        }
    }

    private void swapAnnotationsAt(int pos1, int pos2) {
        if (pos1 != pos2) { // If actually have to swap.
            Annotation temp = stack[pos1];
            stack[pos1] = stack[pos2];
            stack[pos2] = temp;
        }
    }

    private void clearAll() {
        for (int i = stackTop; i >= 0; i--) { // pop off stock consecutively
            stack[i].clear();
        }
        stackTop = -1;
    }

    private class Annotation {
        private final int id; // The unique id of the annotation
        private final DoubleLinkRing<MonochromePixelSet> pixelLists;

        private Annotation(int id) {
            this.id = id;
            pixelLists = new DoubleLinkRing<MonochromePixelSet>();
        }

        private void addPixelSet(MonochromePixelSet toRestore) {
            pixelLists.addLast(toRestore);
        }

        private void clear() {
            pixelLists.clear();
        }

        private void erase() {
            // Must erase in reverse order added
            Iterator<MonochromePixelSet> reverseItr = pixelLists.reverseIterator();
            while (reverseItr.hasNext()) {
                reverseItr.next().erase();
            }
        }

        // // erase the last-added RestoreList in the annotation
        // private void eraseLast() {
        // pixelSets.getFirst().erase();
        // }
        //
        // // clear the last RestoreList in the annotation
        // private void clearLast() {
        // pixelSets.getFirst().clear();
        // }

        private void restore() {
            for (MonochromePixelSet pixelSet : pixelLists) {
                pixelSet.recolor(pixelSet.rgb);
            }
        }

        // TODO: If never use eraseLast() or clearLast(), then make this method collapse all
        // pixelSets together (b/c now have the same RGB value)
        private void recolor(int rgb) {
            for (MonochromePixelSet pixelSet : pixelLists) {
                pixelSet.recolor(rgb);
            }
        }
    }

    private class MonochromePixelSet {
        private int rgb;
        private final DoubleLinkRing<Pixel> pixels;

        private MonochromePixelSet(int rgb) {
            this.rgb = rgb;
            pixels = new DoubleLinkRing<Pixel>();
        }

        private void addPoint(Point pt) {
            final int x = pt.x;
            final int y = pt.y;
            pixels.addLast(new Pixel(x, y, img.getRGB(x, y)));
            img.setRGB(x, y, rgb);
        }

        private void erase() {
            // Must iterate through in reverse order so that erase in opposite order painted.
            Iterator<Pixel> reverseItr = pixels.reverseIterator();
            while (reverseItr.hasNext()) {
                Pixel pixel = reverseItr.next();
                img.setRGB(pixel.x, pixel.y, pixel.relLum);
            }
        }

        // Restores (redraws) the pixel list with its pre-defined points but a new RGB (i.e.
        // recolors
        // the pixel list).
        // NOTE: Assumes that the pixel list has previously been erased!!!
        private void recolor(int rgb) {
            for (Pixel pixel : pixels) {
                final int x = pixel.x;
                final int y = pixel.y;
                pixel.relLum = img.getRGB(x, y);
                img.setRGB(x, y, rgb);
            }
            this.rgb = rgb;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("0x")
                    .append(Integer.toHexString(rgb))
                    .append(": ")
                    .append(pixels)
                    .toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = stack.length - 1; i >= 0; i--) {
            builder.append(stack[i].id).append(i == stackTop ? "*" : " ").append(" ").append(
                    stack[i].pixelLists);
            builder.append("\n");
        }
        return builder.toString();
    }

    @ForTestingOnly
    int getNumberOfAnnotations() {
        return stackTop + 1;
    }

    @ForTestingOnly
    int getIdOfTopAnnotation() {
        return stack[stackTop].id;
    }

    @ForTestingOnly
    List<ColoredPointList> getIterableCopyOfTopAnnotation() {
        Annotation annotation = stack[stackTop];
        List<ColoredPointList> annotationCopy = new ArrayList<ColoredPointList>(
                annotation.pixelLists.size());
        for (MonochromePixelSet list : annotation.pixelLists) {
            List<Pixel> pixelListCopy = new ArrayList<Pixel>(list.pixels.size());
            for (Pixel pixel : list.pixels) {
                pixelListCopy.add(new Pixel(pixel.x, pixel.y, pixel.relLum));
            }
            annotationCopy.add(new ColoredPointList(pixelListCopy, list.rgb));
        }
        return annotationCopy;
    }

    public BufferedImage zoom(int zoomFactor) {
        int origWidth = img.getWidth();
        int origHeight = img.getHeight();
        BufferedImage zoomedImg = new BufferedImage(origWidth * zoomFactor,
                origHeight * zoomFactor, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < origWidth; x++) {
            for (int y = 0; y < origHeight; y++) {
                int color = img.getRGB(x, y);
                // Color the relevant portion of the zoomed image.
                for (int zoomedX = zoomFactor * x; zoomedX < zoomFactor * (x + 1); zoomedX++) {
                    for (int zoomedY = zoomFactor * y; zoomedY < zoomFactor * (y + 1); zoomedY++) {
                        zoomedImg.setRGB(zoomedX, zoomedY, color);
                    }
                }
            }
        }
        return zoomedImg;
    }

    /**
     * Returns the width of the image being annotated.
     */
    public int getWidth() {
        return img.getWidth();
    }

    /**
     * Returns the height of the image being annotated.
     */
    public int getHeight() {
        return img.getHeight();
    }

    public static void main(String[] args) {
        AnnotatableImage ai = new AnnotatableImage(5);
        ai.setImage(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB));
        ai.redraw(2, new ColoredPointList(Lists.newArrayList(new Point(0, 0), new Point(1, 1)),
                0xffffff));
        ai.redraw(4, new ColoredPointList(Lists.newArrayList(new Point(0, 0)), 0xffffff));
        ai.redraw(0, new ColoredPointList(Lists.newArrayList(new Point(1, 1)), 0xffffff));
        ai.redraw(2, new ColoredPointList(Lists.newArrayList(new Point(15, 15)), 0x333333));
        ai.erase(1);
        ai.erase(1);
        ai.erase(3);
        ai.erase(2);
        ai.erase(4);
        ai.recolor(4, 0x333333);
        System.out.println(ai);
        System.out.println("stackTop = " + ai.stackTop);
    }
}