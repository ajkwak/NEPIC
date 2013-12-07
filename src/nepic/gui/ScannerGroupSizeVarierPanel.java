package nepic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JTextField;

import nepic.roi.DataScanner;
import nepic.util.Verify;

public class ScannerGroupSizeVarierPanel extends JPanel {
    private static final long serialVersionUID = 1L; // Default
    private Graph graph;
    private DataScanner scanner;
    private JTextField groupSizeTF;

    public ScannerGroupSizeVarierPanel(DataScanner scanner) {
        Verify.notNull(scanner, "OneDimensionalScanner");
        this.scanner = scanner;

        int graphWidth = 800;
        int graphHeight = 400;

        graph = new Graph(graphWidth, graphHeight, 0x000000 /* Black */)
                .setData(scanner.getGraphData());
        graph.redraw(true, true, true);
        graph.setLocation(0, 0);
        graph.setVisible(true);
        add(graph);

        groupSizeTF = new JTextField(50);
        // groupSizeTF.setEditable(true);
        // groupSizeTF.setSize(50, 25);
        groupSizeTF.setLocation(graphWidth - 55, graphHeight + 5);
        groupSizeTF.setVisible(true);
        groupSizeTF.addActionListener(new SetGroupSizeHandler());
        add(groupSizeTF);

        // setLayout(null);
        setBorder(UtilityMethods.PANEL_BORDERS);
        this.setSize(graphWidth + 5, graphHeight + 45);
        this.setMinimumSize(getSize());
        this.setPreferredSize(getSize());
        setVisible(true);
    }

    private class SetGroupSizeHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int groupSize = Integer.parseInt(groupSizeTF.getText());
                // scanner.smoothData(groupSize);
                graph.redraw(true, true, true);
                // repaint();
            } catch (NumberFormatException nfe) {
                groupSizeTF.setText("");
            }

        }
    }
}