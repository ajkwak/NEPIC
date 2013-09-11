package nepic.gui;

import static org.junit.Assert.*;

import java.awt.Point;
import java.awt.image.BufferedImage;

import nepic.util.ColoredPointList;
import nepic.util.Lists;

import org.junit.Test;

public class AnnotatableImageTest {
    private final int numAnnotations = 5;
    private AnnotatableImage ai;
    private BufferedImage img;

    private final int imgWidth = 25;
    private final int imgHeight = 25;
    private final int backgroundColor = 0x000000;

    private BufferedImage makeBlankBufferedImage() {
        BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < imgWidth; x++) {
            for (int y = 0; y < imgHeight; y++) {
                img.setRGB(x, y, backgroundColor);
            }
        }
        return img;
    }

    private AnnotatableImage makeEmptyAnnotatableImage() {
        img = makeBlankBufferedImage();
        return new AnnotatableImage(numAnnotations).setImage(img);
    }

    @Test
    public void redraw_IllegalId_Fails() {
        ai = makeEmptyAnnotatableImage();

        // ID is too small (negative)
        int illegalId = -1;
        ColoredPointList valsToDraw = new ColoredPointList(Lists.newArrayList(new Point(0, 0)),
                0xff0000);
        try {
            ai.redraw(illegalId, valsToDraw);
            fail("Redraw with illegal ID " + illegalId + " succeeded.");
        } catch (IllegalArgumentException expected) {
            // Expected
        }

        // ID is too large
        illegalId = numAnnotations; // should be one too large
        try {
            ai.redraw(illegalId, valsToDraw);
            fail("Redraw with illegal ID " + illegalId + " succeeded.");
        } catch (IllegalArgumentException expected) {
            // Expected
        }
    }

    @Test
    public void redraw_Succeeds() {
        // Create an empty AnnotatableImage
        ai = makeEmptyAnnotatableImage();

        // Draw the first annotation on the image.
        int annotationId = 4;
        // ai.redraw(4, newVals);

        fail("Not yet implemented");
    }
}
