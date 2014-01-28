package nepic.util;

import java.awt.Point;
import java.util.List;

/**
 *
 * @author AJ Parmidge
 * @since Nepic_Alpha_v1-1-2013-06-18
 *
 */
public class ColoredPointList {
    public final List<? extends Point> points;
    public final int rgb;

    public ColoredPointList(List<? extends Point> points, int rgb) {
        Verify.nonEmpty(points, "annotation points");
        this.points = points;
        this.rgb = rgb;
    }

}
