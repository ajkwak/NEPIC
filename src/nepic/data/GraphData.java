package nepic.data;

import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import nepic.gui.Graph;
import nepic.util.BoundedRegion;
import nepic.util.Pair;
import nepic.util.Verify;

public class GraphData implements BoundedRegion, Iterable<Pair<String, ? extends DataSet>> {
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
     * The collection of all the data sets included in this {@link GraphData}. Maps the name of the
     * {@link DataSet} to the set of data itself.
     */
    private Map<String, DataSet> dataSetMap;

    /**
     * Creates an empty {@link GraphData} with the ability to handle the given number of data sets.
     *
     * @param numDataSets the maximum number of data sets this {@link GraphData} object is expected
     *        to handle.
     */
    public GraphData() {
        dataSetMap = new TreeMap<String, DataSet>();
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

    public boolean isEmpty() {
        return dataSetMap.isEmpty();
    }

    public int size() {
        return dataSetMap.size();
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

    public void setDataSet(String name, Collection<? extends Point> values, int rgb) {
        Verify.nonEmpty(name, "name");
        Verify.nonEmpty(values, "values");

        removeAndAdjustBounds(name);
        putAndAdjustBounds(name, new MutableDataSet().setRgb(rgb).setData(values));
    }

    public void renameDataSet(String currentName, String newName) {
        Verify.notNull(currentName, "currentName");
        Verify.notNull(newName, "newName");
        Verify.argument(dataSetMap.get(newName) == null,
                "Cannot clobber existing DataSet with name '" + newName + "'");
        DataSet toRename = dataSetMap.remove(currentName);
        Verify.argument(toRename != null, "No data set with name '" + currentName + "' exists.");
        dataSetMap.put(newName, toRename);
    }

    public void revalueDataSet(String name, Collection<? extends Point> values) {
        Verify.notNull(name);
        Verify.nonEmpty(values, "values");
        DataSet dataSet = removeAndAdjustBounds(name);
        Verify.argument(dataSet != null, "No data set with name '" + name + "' exists.");
        dataSet.setData(values);
        putAndAdjustBounds(name, dataSet);
    }

    public void recolorDataSet(String name, int rgb) {
        get(name).setRgb(rgb);
    }

    private DataSet get(String name) {
        DataSet dataSet = dataSetMap.get(name);
        Verify.argument(dataSet != null, "No data set with name '" + name + "' exists.");
        return dataSet;
    }

    private DataSet removeAndAdjustBounds(String name) {
        DataSet removedDataSet = dataSetMap.remove(name);
        if (removedDataSet != null) {
            reviseGlobalBoundsAfterRemoval(removedDataSet);
        }
        return removedDataSet;
    }

    private void putAndAdjustBounds(String name, DataSet dataSet) {
        dataSetMap.put(name, dataSet);
        adjustGlobalBounds(dataSet);
    }

    /**
     *
     * @param name
     * @return
     * @throws IllegalArgumentException if no {@link DataSet} with the given {@code name} exists in
     *         this {@link GraphData}
     */
    public DataSet getDataSet(String name) {
        Verify.notNull(name, "name");
        DataSet dataSet = dataSetMap.get(name);
        Verify.argument(dataSet != null, "No data set with name '" + name + "' exists.");
        return new ImmutableDataSet(dataSet);
    }

    public boolean containsDataSet(String name) {
        Verify.notNull(name, "name");
        return dataSetMap.get(name) != null;
    }

    /**
     * Removes the data set with the given {@code id}.
     *
     * @param id the ID of the data set to remove.
     */
    public DataSet removeDataSet(String name) {
        Verify.notNull(name, "name");
        DataSet dataSet = dataSetMap.remove(name);
        Verify.argument(dataSet != null, "No data set with name '" + name + "' exists.");
        return dataSet;
    }

    private void verifyContainsDataSets() {
        Verify.state(maxX >= minX, /* This will only be true when there is 1+ data sets */
                "There must be at least one data set in this graph data.");
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
            for (DataSet dataSet : dataSetMap.values()) {
                adjustGlobalBounds(dataSet);
            }
            return boundsRevised;
        }
        return false;
    }

    @Override
    public Iterator<Pair<String, ? extends DataSet>> iterator() {
        return new Iterator<Pair<String, ? extends DataSet>>() {
            private Iterator<Entry<String, DataSet>> entryItr = dataSetMap.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return entryItr.hasNext();
            }

            @Override
            public Pair<String, ? extends DataSet> next() {
                Entry<String, DataSet> entry = entryItr.next();
                return Pair.newPair(entry.getKey(), new ImmutableDataSet(entry.getValue()));
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("Independent Variable: ").append(independentVariable)
                .append("\nDependent Variable: ").append(dependentVariable)
                .append("\nData Sets:");
        for (Pair<String, ? extends DataSet> entry : this) {
            builder.append("\n\t").append(entry.first).append(": ").append(entry.second);
        }
        return builder.toString();
    }
}
