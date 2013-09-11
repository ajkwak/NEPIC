package nepic.io;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
public class DataWriter_orig {
    public final List<String> labelList;
    public final List<ArrayList<Object>> dataList;

    public DataWriter_orig(Label[] labels) {
        Verify.notNull(labels);
        labelList = new ArrayList<String>();
        dataList = new LinkedList<ArrayList<Object>>();
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

    public boolean addData(Object[] data) {
        Verify.notNull(data);
        ArrayList<Object> dataRow = new ArrayList<Object>(labelList.size());
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

    public void flattenData(Object[] data, ArrayList<Object> dataRow) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] instanceof Object[]) {
                flattenData((Object[]) data[i], dataRow);
            } else {
                dataRow.add(data[i]);
            }
        }
    }

    public boolean dataLogged() {
        return !dataList.isEmpty();
    }

    public boolean canSaveData(JFrame gui, File file) {
        Verify.notNull(file);
        if (!EventLogger.getFileExtention(file.getAbsolutePath()).equals("csv")) {
            return false;
        }

        // if (!file.canWrite()) {
        boolean writeFile = !file.exists();
        if (!writeFile) {
            int overwrite = JOptionPane.showConfirmDialog(gui,
                    "The indicated file already exists.  Would you like to overwrite it?",
                    "Overwrite File?", JOptionPane.YES_NO_OPTION);
            if (overwrite == JOptionPane.YES_OPTION) {
                writeFile = true;
            }
        }
        return writeFile;
        // } else {
        // JOptionPane.showMessageDialog(gui, "Unable to write to the selected file.",
        // "Unable to Save File", JOptionPane.ERROR_MESSAGE);
        // }
        // return false;
    }

    public boolean saveData(JFrame gui, File file) {
        Verify.notNull(file);
        String classpath = file.getAbsolutePath();
        Verify
                .argument(EventLogger.getFileExtention(classpath).equals("csv"),
                        "File must be a csv");

        try {
            PrintStream writer = new PrintStream(new FileOutputStream(classpath));
            writer.println(writeCsvLine(labelList));
            for (ArrayList<Object> dataRow : dataList) {
                writer.println(writeCsvLine(dataRow));
            }
            writer.flush();
            writer.close();
            dataList.clear();
            if (file.exists()) {
                int open = JOptionPane.showConfirmDialog(gui,
                        "Would you like to open the data file you just saved?", "Open Data File?",
                        JOptionPane.YES_NO_OPTION);
                if (open == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(file);
                }
            } else {
                Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "After saving, file", classpath,
                        "does not exist");
            }
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
