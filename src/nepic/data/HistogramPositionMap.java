package nepic.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nepic.util.Lists;
import nepic.util.Verify;

public class HistogramPositionMap {
    /**
     * The actual histogram data.
     */
    private final List<LinkedList<Integer>> histogram;
    /**
     * The difference between the actual lower bound of the histogram's domain and zero (relates the
     * actual histogram domain to the positions in the hist array).
     */
    private final int offset;
    /**
     * The modes of the data represented by the histogram
     */
    private final List<Integer> modePositions;
    /**
     * The number of times the mode occurs in the data summarized by the histogram.
     */
    private int numModeInstances;

    /**
     * Creates a {@link HistogramPositionMap} with the given bounds.
     *
     * @param lowerBound the lower bound (inclusive) of the domain over which the built
     *        {@link HistogramPositionMap} must extend.
     * @param upperBound the upper bound (inclusive) of the domain over which the built
     *        {@link HistogramPositionMap} must extend.
     */
    public HistogramPositionMap(int lowerBound, int upperBound) {
        Verify.argument(lowerBound <= upperBound,
                "lowerBound (=" + lowerBound + ") > upperBound (=" + upperBound + ")");
        int histSize = upperBound - lowerBound + 1;
        histogram = new ArrayList<LinkedList<Integer>>(histSize);
        for (int i = 0; i < histSize; i++) {
            histogram.add(new LinkedList<Integer>());
        }
        offset = lowerBound;
        modePositions = new LinkedList<Integer>();
    }

    /**
     * Add the given value to the {@link Histogram} being built.
     *
     * @param value the value to add
     * @param the position of the value to be added
     */
    public void addValueAtPosition(int value, int position) {
        int histPos = value - offset; // The position of 'value' in the histogram matrix.
        Verify.argument(histPos >= 0 && histPos < histogram.size(), "Cannot add illegal value "
                + value + " to to the Histogram being built.  Acceptable values range from "
                + offset + " to " + (offset + histogram.size() - 1));
        LinkedList<Integer> positionsAtHistPos = histogram.get(histPos);
        positionsAtHistPos.add(position);
        int numValueInstances = positionsAtHistPos.size();
        if (numValueInstances >= numModeInstances) {
            if (numValueInstances > numModeInstances) {
                modePositions.clear();
                numModeInstances = numValueInstances;
            }
            numModeInstances = numValueInstances;
            modePositions.add(histPos);
        }
    }

    public List<Integer> getModes() {
        List<Integer> modes = new ArrayList<Integer>(modePositions.size());
        for (int modePos : modePositions) {
            modes.add(modePos + offset);
        }
        return modes;
    }

    public List<Integer> getPositionsAssociatedWith(int pi) {
        return Lists.copyOf(histogram.get(pi));
    }

    public int getNumberModeInstances() {
        return numModeInstances;
    }
}
