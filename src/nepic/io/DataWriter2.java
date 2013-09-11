package nepic.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nepic.Nepic;
import nepic.logging.EventLogger;
import nepic.logging.EventType;
import nepic.util.Verify;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-8_NewLogger
 * @version AutoCBFinder_Alpha_v0-9_2013-01-11
 */
public class DataWriter2 {
    private final List<String> labelList;
    private final List<ArrayList<String>> dataList;

    public DataWriter2(Label[] labels) {
        Verify.notNull(labels);
        labelList = new ArrayList<String>();
        dataList = new LinkedList<ArrayList<String>>();
        flattenLabels("", labels);
    }

    private void flattenLabels(String prefix, Label[] labels) {
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] instanceof ComplexLabel) {
                ComplexLabel sLabel = (ComplexLabel) labels[i];
                flattenLabels(prefix + sLabel + "\\", sLabel.sublabels);
            } else {
                labelList.add(prefix + labels[i]);
            }
        }
    }

    public boolean addDataRow(Object[] data) {
        Verify.notNull(data);
        ArrayList<String> dataRow = new ArrayList<String>(labelList.size());
        flattenData(data, dataRow);
        if (dataRow.size() == labelList.size()) {
            dataList.add(dataRow);
            return true;
        }
        Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "Data not of expected size.  Expected",
                labelList.size(), "elements in the data.  Instead, after flattening, there were",
                dataRow.size(), " elements.  Given data row = ", dataRow);
        return false;
    }

    public boolean addDataRows(List<Object[]> data) { // TODO: keep???
        Verify.notNull(data);
        for (Object[] row : data) {
            if (!addDataRow(row)) {
                return false;
            }
        }
        return true;
    }

    private void flattenData(Object[] data, ArrayList<String> dataRow) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] instanceof Object[]) {
                flattenData((Object[]) data[i], dataRow);
            } else {
                dataRow.add(data[i].toString());
            }
        }
    }

    public boolean dataLogged() {
        return !dataList.isEmpty();
    }

    public boolean canSaveData(File file) {
        Verify.notNull(file);
        return EventLogger.getFileExtention(file.getAbsolutePath()).equals("csv");
    }

    public boolean saveData(File file) {
        Verify.notNull(file);
        String classpath = file.getAbsolutePath();
        Verify
                .argument(EventLogger.getFileExtention(classpath).equals("csv"),
                        "File must be a csv");

        try {
            PrintStream writer = new PrintStream(new FileOutputStream(classpath));
            writer.println(writeCsvLine(labelList));
            for (ArrayList<String> dataRow : dataList) {
                writer.println(writeCsvLine(dataRow));
            }
            writer.flush();
            writer.close();
            dataList.clear();
            return true;
        } catch (Exception e) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "Unable to save log to", classpath,
                    ":", EventLogger.formatException(e));
            return false;
        }// catch all exceptions
    }

    public String writeCsvLine(List<?> line) {
        StringBuilder builder = new StringBuilder();
        for (Object element : line) {
            builder.append(element).append(",");
        }
        return builder.toString();
    }

}
