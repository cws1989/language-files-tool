package langfiles.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import langfiles.project.DigestedFile;

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
     * The tabbed pane that place the code panels.
     */
    private JTabbedPane tabbedPane;

    /**
     * Constructor.
     */
    public CodePanel() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFocusable(false);

        codePanel = new JPanel();
        codePanel.setLayout(new BorderLayout());
        codePanel.setBackground(codePanel.getBackground().brighter());
        codePanel.add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JPanel getGUI() {
        return codePanel;
    }

    /**
     * Add file to code panel.
     * @param digestedFile the file to add to code panel
     */
    public void add(DigestedFile digestedFile) {
        SwingCodeViewer codeViewer = new SwingCodeViewer();
        codeViewer.setCode(digestedFile);

        tabbedPane.addTab(digestedFile.getFile().getName(), codeViewer.getGUI());
        tabbedPane.setSelectedComponent(codeViewer.getGUI());
        tabbedPane.setTabComponentAt(tabbedPane.getSelectedIndex(), new JTabComponent(tabbedPane));
    }
}
