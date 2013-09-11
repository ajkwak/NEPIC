package nepic.image;

import java.awt.image.BufferedImage;

/**
 * Includes all of the information NEPIC loads or calculates for a given image page, including the
 * {@link ImagePage} itself, as well as the {@link PageInfo} for that page.
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-9_120112 (Called ImgModel until AutoCBFinder_Alpha_v0-9-2013-01-29)
 * @version AutoCBFinder_Alpha_v0-9_2013-02-10
 */
@Deprecated
public class Page {
    public ImagePage img;
    public PageInfo pageInfo;

    public Page(String imgName, int pgNum, ImagePage img) {
        // Verify.notNull(img, "ImageMatrix for image page cannot be null");
        // this.img = img;
        // pageInfo = new PageInfo(imgName, pgNum, img.makeHistogram());
    }

    public BufferedImage displayImg(int whichImg) {
        return img.displayImg(whichImg);
    }// displayImg

    public int getRelLum(int x, int y) {
        return img.getPixelIntensity(x, y);
    }// getRelLum

    public int[] getDimensions() {
        return img.getDimensions();
    }// getDimensions

}
