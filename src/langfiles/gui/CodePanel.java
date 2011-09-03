package langfiles.gui;

import javax.swing.JPanel;

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
        codePanel.setBackground(codePanel.getBackground().brighter());
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JPanel getGUI() {
        return codePanel;
    }
}
