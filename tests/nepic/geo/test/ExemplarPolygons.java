package nepic.geo.test;

import java.awt.Point;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import nepic.geo.Polygon;

// TODO: Javadoc
public class ExemplarPolygons {
    private static final List<Builder> EXEMPLAR_POLYGONS = Lists.newArrayList(
            new Builder("STAR", new Point[] {
                    new Point(12, 0),
                    new Point(15, 7),
                    new Point(24, 7),
                    new Point(17, 12),
                    new Point(20, 21),
                    new Point(12, 15),
                    new Point(4, 21),
                    new Point(7, 12),
                    new Point(0, 7),
                    new Point(9, 7) }),
            new Builder("SQUARE", new Point[] {
                    new Point(0, 0),
                    new Point(8, 0),
                    new Point(8, 8),
                    new Point(0, 8) }),
            new Builder("UTAH", new Point[] {
                    new Point(0, 0),
                    new Point(8, 0),
                    new Point(8, 8),
                    new Point(20, 8),
                    new Point(20, 20),
                    new Point(0, 20) }),
            new Builder("TIME_TURNER", new Point[] {
                    new Point(0, 0),
                    new Point(10, 0),
                    new Point(0, 10),
                    new Point(10, 10) }),
            new Builder("DIAMOND", new Point[] {
                    new Point(4, 0),
                    new Point(8, 4),
                    new Point(4, 8),
                    new Point(0, 4) }),
            new Builder("SCALENE_TRIANGLE", new Point[] {
                    new Point(20, 0),
                    new Point(40, 8),
                    new Point(60, 8), }),
            new Builder("BITTEN_HOUSE", new Point[] {
                    new Point(4, 0),
                    new Point(8, 3),
                    new Point(5, 6),
                    new Point(8, 9),
                    new Point(0, 9),
                    new Point(0, 3) }),
            new Builder("STAIRS", new Point[] {
                    new Point(0, 5),
                    new Point(1, 5),
                    new Point(1, 4),
                    new Point(2, 4),
                    new Point(2, 3),
                    new Point(3, 3),
                    new Point(3, 2),
                    new Point(4, 2),
                    new Point(4, 1),
                    new Point(5, 1),
                    new Point(5, 0),
                    new Point(0, 0) }),
            new Builder("U", new Point[] {
                    new Point(0, 0),
                    new Point(4, 0),
                    new Point(4, 10),
                    new Point(8, 10),
                    new Point(8, 0),
                    new Point(12, 0),
                    new Point(12, 14),
                    new Point(0, 14) }),
            new Builder("MINI_U", new Point[] {
                    new Point(0, 0),
                    new Point(2, 0),
                    new Point(2, 2),
                    new Point(4, 2),
                    new Point(4, 0),
                    new Point(6, 0),
                    new Point(6, 4),
                    new Point(0, 4) }),
            new Builder("MINI_H", new Point[] {
                    new Point(0, 0),
                    new Point(2, 0),
                    new Point(2, 2),
                    new Point(4, 2),
                    new Point(4, 0),
                    new Point(6, 0),
                    new Point(6, 6),
                    new Point(4, 6),
                    new Point(4, 4),
                    new Point(2, 4),
                    new Point(2, 6),
                    new Point(0, 6) }),
            new Builder("CROSS", new Point[] {
                    new Point(2, 2),
                    new Point(2, 0),
                    new Point(4, 0),
                    new Point(4, 2),
                    new Point(6, 2),
                    new Point(6, 4),
                    new Point(4, 4),
                    new Point(4, 6),
                    new Point(2, 6),
                    new Point(2, 4),
                    new Point(0, 4),
                    new Point(0, 2) }),
            new Builder("INCREMENT", new Point[] {
                    new Point(0, 2),
                    new Point(2, 2),
                    new Point(2, 0),
                    new Point(4, 0),
                    new Point(4, 2),
                    new Point(6, 2),
                    new Point(6, 0),
                    new Point(8, 0),
                    new Point(8, 2),
                    new Point(10, 2),
                    new Point(10, 4),
                    new Point(8, 4),
                    new Point(8, 6),
                    new Point(6, 6),
                    new Point(6, 4),
                    new Point(4, 4),
                    new Point(4, 6),
                    new Point(2, 6),
                    new Point(2, 4),
                    new Point(0, 4), }),
            new Builder("MINI_I", new Point[] {
                    new Point(0, 0),
                    new Point(6, 0),
                    new Point(6, 2),
                    new Point(4, 2),
                    new Point(4, 3),
                    new Point(6, 3),
                    new Point(6, 5),
                    new Point(0, 5),
                    new Point(0, 3),
                    new Point(2, 3),
                    new Point(2, 2),
                    new Point(0, 2) }),
            new Builder("MINI_C", new Point[] {
                    new Point(0, 0),
                    new Point(4, 0),
                    new Point(4, 2),
                    new Point(2, 2),
                    new Point(2, 4),
                    new Point(4, 4),
                    new Point(4, 6),
                    new Point(0, 6), }),
            new Builder("MINIER_C", new Point[] {
                    new Point(0, 0),
                    new Point(4, 0),
                    new Point(4, 2),
                    new Point(2, 2),
                    new Point(2, 3),
                    new Point(4, 3),
                    new Point(4, 5),
                    new Point(0, 5), }),
            new Builder("CASTLE", new Point[] {
                    new Point(0, 0),
                    new Point(5, 0),
                    new Point(5, 10),
                    new Point(10, 5),
                    new Point(15, 10),
                    new Point(20, 5),
                    new Point(25, 10),
                    new Point(25, 0),
                    new Point(30, 0),
                    new Point(30, 30),
                    new Point(0, 30) }),
            new Builder("PENTAGON", new Point[] {
                    new Point(0, 0),
                    new Point(10, 6),
                    new Point(6, 14),
                    new Point(-6, 14),
                    new Point(-10, 6) }),
            new Builder("CROSS_OVER_STAR", new Point[] {
                    new Point(0, 0),
                    new Point(6, 14),
                    new Point(-10, 6),
                    new Point(10, 6),
                    new Point(-6, 14) }));

    public static ImmutableList<ExemplarPolygons.Builder> getExemplarPolygonBuilders() {
        return ImmutableList.copyOf(EXEMPLAR_POLYGONS);
    }

    public static class Builder {
        private final String polygonName;
        private final Point[] polygonCtorParams;

        private Builder(String polygonName, Point[] polygonCtorParams) {
            this.polygonName = polygonName;
            this.polygonCtorParams = polygonCtorParams;
        }

        public String getPolygonName() {
            return polygonName;
        }

        public Polygon buildPolygon() {
            return new Polygon(polygonCtorParams);
        }
    }
}
