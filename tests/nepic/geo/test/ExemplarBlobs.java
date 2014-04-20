package nepic.geo.test;

import java.awt.Point;

import com.google.common.collect.Lists;

import nepic.geo.Blob;

public class ExemplarBlobs {

    public static Blob makeShallowHeart() {
        return Blob.newBlobFromTracedEdges(Lists.newArrayList(
                new Point(1, 0),
                new Point(2, 0),
                new Point(3, 1),
                new Point(4, 1),
                new Point(5, 0),
                new Point(6, 0),
                new Point(7, 1),
                new Point(7, 2),
                new Point(6, 3),
                new Point(5, 3),
                new Point(4, 4),
                new Point(3, 4),
                new Point(2, 3),
                new Point(1, 3),
                new Point(0, 2),
                new Point(0, 1)));
    }

    public static Blob makeNarrowHeart() {
        return Blob.newBlobFromTracedEdges(Lists.newArrayList(
                new Point(1, 0),
                new Point(2, 1),
                new Point(3, 0),
                new Point(4, 1),
                new Point(4, 2),
                new Point(3, 3),
                new Point(3, 4),
                new Point(2, 5),
                new Point(1, 4),
                new Point(1, 3),
                new Point(0, 2),
                new Point(0, 1)));
    }

    public static Blob makeOrnament() {
        return Blob.newBlobFromTracedEdges(Lists.newArrayList(
                new Point(2, 5),
                new Point(2, 4),
                new Point(3, 3),
                new Point(4, 2),
                new Point(4, 1),
                new Point(4, 2),
                new Point(5, 3),
                new Point(6, 4),
                new Point(6, 5),
                new Point(6, 6),
                new Point(5, 7),
                new Point(4, 7),
                new Point(3, 7),
                new Point(2, 6)));
    }

    public static Blob makeSidewaysOrnament() {
        return Blob.newBlobFromTracedEdges(Lists.newArrayList(
                new Point(1, 3),
                new Point(2, 3),
                new Point(3, 2),
                new Point(4, 1),
                new Point(5, 1),
                new Point(6, 1),
                new Point(7, 2),
                new Point(7, 3),
                new Point(7, 4),
                new Point(6, 5),
                new Point(5, 5),
                new Point(4, 5),
                new Point(3, 4),
                new Point(2, 3)));
    }

    public static Blob makeSidewaysOrnament2() {
        return Blob.newBlobFromTracedEdges(Lists.newArrayList(
                new Point(4, 1),
                new Point(5, 1),
                new Point(6, 1),
                new Point(7, 2),
                new Point(7, 3),
                new Point(7, 4),
                new Point(6, 5),
                new Point(5, 5),
                new Point(4, 5),
                new Point(3, 4),
                new Point(2, 3),
                new Point(1, 3),
                new Point(2, 3),
                new Point(3, 2)));
    }

    public static Blob makeHorizTwoPointLine() {
        return Blob.newBlobFromTracedEdges(Lists.newArrayList(
                new Point(2, 1),
                new Point(1, 1)));
    }

    public static Blob makeVertTwoPointLine() {
        return Blob.newBlobFromTracedEdges(Lists.newArrayList(
                new Point(1, 1),
                new Point(1, 2)));
    }

    public static Blob makeSinglePoint() {
        return Blob.newBlobFromTracedEdges(Lists.newArrayList(new Point(1, 1)));
    }
}
