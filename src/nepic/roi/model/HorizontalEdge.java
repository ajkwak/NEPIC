package nepic.roi.model;

public class HorizontalEdge implements Comparable<HorizontalEdge> {
    public int first; // first x value
    public int last; // last x value

    HorizontalEdge(int first, int last) {
        if (first < last) {
            this.first = first;
            this.last = last;
        } else {
            this.first = last;
            this.last = first;
        }
    }

    @Override
    public int compareTo(HorizontalEdge other) {
        return this.first - other.first;
    }

    @Override
    public String toString() {
        return "{" + first + ", " + last + "}";
    }
}