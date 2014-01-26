package nepic.util;

public class Range {
    public final int min;
    public final int max;

    public Range(int min, int max) {
        // Make sure that the min and max are saved in the correct variables.
        if (min <= max) {
            this.min = min;
            this.max = max;
        } else {
            this.min = max;
            this.max = min;
        }
    }

    @Override
    public String toString() {
        return min + "-" + max;
    }
}
