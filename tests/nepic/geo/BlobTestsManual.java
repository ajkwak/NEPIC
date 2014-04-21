package nepic.geo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nepic.geo.test.ExemplarBlobs;

public class BlobTestsManual {

    static List<Point> convertToList(Point[] pts) {
        List<Point> edges = new ArrayList<Point>(pts.length);
        for (int i = 0; i < pts.length; i++) {
            edges.add(pts[i]);
        }
        return edges;
    }

    public void printDrawInnards(Blob blob) { // Terribly inefficient!
        System.out.println("\n\n");
        ArrayList<LinkedList<Integer>> innardsList = blob.makeInnardsList();

        for (int i = 0; i < innardsList.size(); i++) {
            LinkedList<Integer> row = innardsList.get(i);
            System.out.print(i + "\t");
            for (int x = blob.getMinX(); x <= blob.getMaxX(); x++) {
                if (row.remove((Object) x)) {
                    System.out.print(Blob.INNARD_POINT);
                } else {
                    System.out.print(Blob.OUTSIDE_POINT);
                }
            }
            System.out.println();
        }
    }

    public void printDraw() {
        System.out.println("\n\n" + this);
    }

    public static void main(String[] args) throws IllegalAccessException {
        // Test all Blob exemplars.
        for (ExemplarBlobs.Builder blobBldr : ExemplarBlobs.getExemplarBlobBuilders()) {
            System.out.println("\n\n" + blobBldr.getBlobName());
            Blob blob = blobBldr.buildBlob();
            System.out.println(blob);
        }
    }
}
