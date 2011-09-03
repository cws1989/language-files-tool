package langfiles.gui;

import javax.swing.JPanel;

/**
 * The project panel.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class ProjectPanel {

    /**
     * The project panel.
     */
    private JPanel projectPanel;

    /**
     * Constructor
     */
    public ProjectPanel() {
        projectPanel = new JPanel();
        projectPanel.setBackground(projectPanel.getBackground().brighter());
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JPanel getGUI() {
        return projectPanel;
    }
}
