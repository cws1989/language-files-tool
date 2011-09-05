package langfiles.gui;

import java.awt.BorderLayout;
import java.io.File;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import langfiles.handler.DigestedFile;
import langfiles.handler.Handler;

/**
 * The code panel.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class CodePanel {

    /**
     * The project panel.
     */
    private JPanel codePanel;

    /**
     * Constructor
     */
    public CodePanel() {
        codePanel = new JPanel();
        codePanel.setLayout(new BorderLayout());
        codePanel.setBackground(codePanel.getBackground().brighter());

        //<editor-fold defaultstate="collapsed" desc="for test purpose">
        Handler handler = new Handler();
        handler.addFile(new File("build.xml"));
        handler.addFile(new File("manifest.mf"));
        List<DigestedFile> digestedFileLis = handler.getDigestedData();

        SwingCodeViewer codeViewer = new SwingCodeViewer();
        codeViewer.setCode(digestedFileLis.get(0));

        SwingCodeViewer codeViewer2 = new SwingCodeViewer();
        codeViewer2.setCode(digestedFileLis.get(1));

        JTabbedPane tab = new JTabbedPane();
        tab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tab.setFocusable(false);
        tab.addTab("build.xml", codeViewer.getGUI());
        tab.addTab("manifest.mf", codeViewer2.getGUI());
        codePanel.add(tab, BorderLayout.CENTER);
        //</editor-fold>
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JPanel getGUI() {
        return codePanel;
    }
}
