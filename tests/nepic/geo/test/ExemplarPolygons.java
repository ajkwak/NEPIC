package nepic.geo.test;

import java.awt.Point;

import nepic.geo.Polygon;

public class ExemplarPolygons {

    public static Polygon makeStar() {
        return new Polygon(new Point[] {
                new Point(12, 0),
                new Point(15, 7),
                new Point(24, 7),
                new Point(17, 12),
                new Point(20, 21),
                new Point(12, 15),
                new Point(4, 21),
                new Point(7, 12),
                new Point(0, 7),
                new Point(9, 7) });
    }

    public static Polygon makeSquare() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(8, 0),
                new Point(8, 8),
                new Point(0, 8), });
    }

    public static Polygon makeUtah() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(8, 0),
                new Point(8, 8),
                new Point(20, 8),
                new Point(20, 20),
                new Point(0, 20), });
    }

    public static Polygon makeTimeTurner() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(10, 0),
                new Point(0, 10),
                new Point(10, 10) });
    }

    public static Polygon makeDiamond() {
        return new Polygon(new Point[] {
                new Point(4, 0),
                new Point(8, 4),
                new Point(4, 8),
                new Point(0, 4) });
    }

    public static Polygon makeScaleneTriangle() {
        return new Polygon(new Point[] { new Point(20, 0), new Point(40, 8), new Point(60, 8), });
    }

    public static Polygon makeBittenHouse() {
        return new Polygon(new Point[] {
                new Point(4, 0),
                new Point(8, 3),
                new Point(5, 6),
                new Point(8, 9),
                new Point(0, 9),
                new Point(0, 3) });
    }

    public static Polygon makeStairs() {
        return new Polygon(new Point[] {
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
                new Point(0, 0) });
    }

    public static Polygon makeU() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(4, 0),
                new Point(4, 10),
                new Point(8, 10),
                new Point(8, 0),
                new Point(12, 0),
                new Point(12, 14),
                new Point(0, 14) });
    }

    public static Polygon makeMiniU() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(2, 0),
                new Point(2, 2),
                new Point(4, 2),
                new Point(4, 0),
                new Point(6, 0),
                new Point(6, 4),
                new Point(0, 4) });
    }

    public static Polygon makeMiniH() {
        return new Polygon(new Point[] {
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
                new Point(0, 6) });
    }

    public static Polygon makeCross() {
        return new Polygon(new Point[] {
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
                new Point(0, 2) });
    }

    public static Polygon makeIncrement() {
        return new Polygon(new Point[] {
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
                new Point(0, 4), });
    }

    public static Polygon makeMiniI() { // TODO
        return new Polygon(new Point[] {
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
                new Point(0, 2) });
    }

    public static Polygon makeMiniC() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(4, 0),
                new Point(4, 2),
                new Point(2, 2),
                new Point(2, 4),
                new Point(4, 4),
                new Point(4, 6),
                new Point(0, 6), });
    }

    public static Polygon makeMinierC() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(4, 0),
                new Point(4, 2),
                new Point(2, 2),
                new Point(2, 3),
                new Point(4, 3),
                new Point(4, 5),
                new Point(0, 5), });
    }

    public static Polygon makeCastle() {
        return new Polygon(new Point[] {
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
                new Point(0, 30) });
    }

    public static Polygon makePentagon() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(10, 6),
                new Point(6, 14),
                new Point(-6, 14),
                new Point(-10, 6) });
    }

    public static Polygon makeCrossOverStar() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(6, 14),
                new Point(-10, 6),
                new Point(10, 6),
                new Point(-6, 14) });
    }
}
