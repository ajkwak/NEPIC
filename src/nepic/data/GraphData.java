package nepic.data;

import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nepic.gui.Graph;
import nepic.util.BoundedRegion;
import nepic.util.Verify;

public class GraphData implements BoundedRegion, Iterable<DataSet> {
    /**
     * The name of the independent variable for all the included data sets.
     */
    private String independentVariable = "";
    /**
     * The name of the dependent variable for all the included data sets.
     */
    private String dependentVariable = "";
    /**
     * The global minimum x-value of all included data sets.
     */
    private int minX = Integer.MAX_VALUE;
    /**
     * The global maximum x-value of all included data sets.
     */
    private int maxX = Integer.MIN_VALUE;
    /**
     * The global minimum y-value of all included data sets.
     */
    private int minY = Integer.MAX_VALUE;
    /**
     * The global maximum y-value of all included data sets.
     */
    private int maxY = Integer.MIN_VALUE;
    /**
     * The collection of all the data sets included in this {@link GraphData}.
     */
    private final DataSet[] dataSets;

    /**
     * Creates an empty {@link GraphData} with the ability to handle the given number of data sets.
     *
     * @param numDataSets the maximum number of data sets this {@link GraphData} object is expected
     *        to handle.
     */
    public GraphData(int numDataSets) {
        Verify.argument(numDataSets > 0, "The maximum number of supported data sets must be >= 1");
        dataSets = new MutableDataSet[numDataSets];
    }

    /**
     * Returns the maximum number of data sets this {@link GraphData} can handle.
     */
    public int getMaxNumDataSetsSupported() {
        return dataSets.length;
    }

    /**
     * Gets the name of the independent variable for all the data sets to be graphed.
     *
     * @return the name of the independent variable of the data sets to be graphed; otherwise empty
     *         string
     */
    public String getIndependentVariable() {
        return independentVariable;
    }

    /**
     * Sets the name of the independent variable to the given value. If the given value is
     * {@code null}, sets the name of the independant variable to an empty string.
     *
     * @param independentVariable the name of the independent variable to set
     * @return {@code this}, for chaining
     */
    public GraphData setIndependentVariable(String independentVariable) {
        if (independentVariable == null) {
            independentVariable = "";
        }
        this.independentVariable = independentVariable;
        return this;
    }

    public String getDependentVariable() {
        return dependentVariable;
    }

    public GraphData setDependentVariable(String dependentVariable) {
        if (dependentVariable == null) {
            dependentVariable = "";
        }
        this.dependentVariable = dependentVariable;
        return this;
    }

    @Override
    public int getMinX() {
        verifyContainsDataSets();
        return minX;
    }

    @Override
    public int getMaxX() {
        verifyContainsDataSets();
        return maxX;
    }

    @Override
    public int getMinY() {
        verifyContainsDataSets();
        return minY;
    }

    @Override
    public int getMaxY() {
        verifyContainsDataSets();
        return maxY;
    }

    @Override
    public boolean boundsContain(BoundedRegion region) {
        verifyContainsDataSets();
        return minX <= region.getMinX() && maxX >= region.getMaxX()
                && minY <= region.getMinY() && maxY >= region.getMaxY();
    }

    @Override
    public boolean boundsContain(int x, int y) {
        return minX <= x && maxX >= x && minY <= y && maxY >= y;
    }

    /**
     * Adds the given values as a new data set on the graph.
     *
     * @param name the name of the data set to add
     * @param values the data values to add
     * @param rgb the color in which to draw the new data set on the graph
     * @return the unique ID number of the newly added data set
     */
    public int addDataSet(String name, Collection<? extends Point> values, int rgb) {
        Verify.nonEmpty(name, "label");
        Verify.nonEmpty(values, "values");

        // Make a DataSet out of the newVals
        int id = findNextAvailableDataSetId();
        MutableDataSet newDataSet = new MutableDataSet();
        newDataSet.setName(name);
        newDataSet.setRgb(rgb);
        newDataSet.addAll(values);
        dataSets[id] = newDataSet;
        adjustGlobalBounds(newDataSet);
        return id;
    }

    public void redefineDataSetName(int id, String newName) {
        verifyValidDataSetId(id);
        DataSet dataSet = dataSets[id];
        Verify.argument(dataSet != null, "No data set with given id = " + id + " exists");
        dataSet.setName(newName);
    }

    public void redefineDataSetValues(int id, Collection<? extends Point> newValues) {
        verifyValidDataSetId(id);
        DataSet dataSet = dataSets[id];
        Verify.argument(dataSet != null, "No data set with given id = " + id + " exists");
        dataSet.clear();
        dataSet.addAll(newValues);
    }

    public void redefineDataSetColor(int id, int newRgb) {
        verifyValidDataSetId(id);
        DataSet dataSet = dataSets[id];
        Verify.argument(dataSet != null, "No data set with given id = " + id + " exists");
        dataSet.setRgb(newRgb);
    }

    public DataSet getDataSet(int id) {
        verifyValidDataSetId(id);
        DataSet dataSet = dataSets[id];
        Verify.argument(dataSet != null, "No data set with given id = " + id + " exists");
        return new ImmutableDataSet(dataSet);
    }

    /**
     * Removes the data set with the given {@code id}.
     *
     * @param id the ID of the data set to remove.
     */
    public void removeDataSet(int id) {
        verifyValidDataSetId(id);
        DataSet removedDataSet = dataSets[id];
        dataSets[id] = null;
        if (removedDataSet != null) {
            reviseGlobalBoundsAfterRemoval(removedDataSet);
        }
    }

    private void verifyContainsDataSets() {
        Verify.state(maxX >= minX, /* This will only be true when there is 1+ data sets */
                "There must be at least one data set in this graph data.");
    }

    /**
     * Gets a currently-unused unique ID for a data set to be graphed.
     *
     * @return a currently unused unique data set ID
     * @throws IllegalStateException if all of the unique data set IDs available in the graph are in
     *         use.
     */
    private int findNextAvailableDataSetId() {
        for (int i = 0; i < dataSets.length; i++) {
            if (dataSets[i] == null) { // i.e. if this position is free
                return i;
            }
        }
        throw new IllegalStateException("There are no free data set IDs available.");
    }

    /**
     * Verifies that the given ID is a valid data set ID.
     *
     * @param id the ID to check
     * @throws IllegalArgumentException if the ID is not a valid data set ID
     */
    private void verifyValidDataSetId(int id) {
        Verify.argument(id >= 0 && id < dataSets.length,
                "Illegal data set ID.  Data set ID must be between 0 and " + (dataSets.length - 1)
                + " inclusive.");
    }

    /**
     * Modifies the global bounds of this {@link Graph} to include the given data set.
     *
     * <p>
     * i.e. if the given data set has a max x-value of 13, but the current bounds max out at 10 in
     * the x-direction, this method modifies the global max x-value to 13 (so that the global bounds
     * now include the bounds of the given data set).
     *
     * @param dataSet the data set to include in the graph bounds
     * @return {@code true} if the global bounds of the {@link Graph} are adjusted due to the
     *         addition of the given data set; otherwise {@code false}
     */
    private boolean adjustGlobalBounds(DataSet dataSet) {
        boolean boundsAdjusted = false;
        if (dataSet.getMinX() < minX) {
            minX = dataSet.getMinX();
            boundsAdjusted = true;
        }
        if (dataSet.getMaxX() > maxX) {
            maxX = dataSet.getMaxX();
            boundsAdjusted = true;
        }
        if (dataSet.getMinY() < minY) {
            minY = dataSet.getMinY();
            boundsAdjusted = true;
        }
        if (dataSet.getMaxY() > maxY) {
            maxY = dataSet.getMaxY();
            boundsAdjusted = true;
        }
        return boundsAdjusted;
    }

    /**
     * Revises the global bounds of this {@link Graph}, assuming that the given data set has been
     * previously removed from the graph.
     *
     * @param removedDataSet the data set that has been removed from the graph
     * @return {@code true} if the global bounds of the {@link Graph} are revised due to the removal
     *         of the given data set; otherwise {@code false}
     */
    private boolean reviseGlobalBoundsAfterRemoval(DataSet removedDataSet) {
        if (removedDataSet.getMinX() == minX || removedDataSet.getMaxX() == maxX
                || removedDataSet.getMinY() == minY || removedDataSet.getMaxY() == maxY) {
            // Then need to adjust the global bounds.
            minX = Integer.MAX_VALUE;
            maxX = Integer.MIN_VALUE;
            minY = Integer.MAX_VALUE;
            maxY = Integer.MIN_VALUE;
            boolean boundsRevised = false;
            for (int i = 0; i < dataSets.length; i++) {
                DataSet dataSet = dataSets[i];
                if (dataSet != null) {
                    boundsRevised = adjustGlobalBounds(dataSet);
                }
            }
            return boundsRevised;
        }
        return false;
    }

    /**
     * Returns all of the ID values of existing {@link DataSet}s in this {@link GraphData}.
     */
    public Collection<Integer> getValidIds() {
        List<Integer> ids = new LinkedList<Integer>();
        for (int id = 0; id < dataSets.length; id++) {
            if (dataSets[id] != null) { // Then this is the ID of an existing DataSet
                ids.add(id);
            }
        }
        return ids;
    }

    /**
     * Determines if the given value is the ID of an existing {@link DataSet} in this
     * {@link GraphData}.
     *
     * @param id the ID to check
     * @return {@code true} if the given ID is for an existing {@link DataSet} in this
     *         {@link GraphData}; otherwise {@code false}
     */
    public boolean datasetExists(int id) {
        return id >= 0 && id < dataSets.length && dataSets[id] != null;
    }

    @Override
    public Iterator<DataSet> iterator() {
        return new Iterator<DataSet>() {
            private Iterator<Integer> idItr = getValidIds().iterator();

            @Override
            public boolean hasNext() {
                return idItr.hasNext();
            }

            @Override
            public DataSet next() {
                int id = idItr.next();
                return new ImmutableDataSet(dataSets[id]);
            }

            /**
             * Not supported.
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    public GraphData copy() {
        GraphData copy = new GraphData(dataSets.length);
        copy.setIndependentVariable(independentVariable);
        copy.setDependentVariable(dependentVariable);
        for (DataSet dataSet : this) {
            copy.addDataSet(dataSet.getName(), dataSet, dataSet.getRgb());
        }
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("Independent Variable: ").append(independentVariable)
                .append("\nDependent Variable: ").append(dependentVariable)
                .append("\nData Sets:");
        for (DataSet dataSet : this) {
            builder.append("\n\t").append(dataSet);
        }
        return builder.toString();
    }
}
