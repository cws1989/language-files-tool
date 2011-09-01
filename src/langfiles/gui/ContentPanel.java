package langfiles.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;

/**
 * The content panel of the main frame.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class ContentPanel {

    /**
     * The content panel.
     */
    private JPanel contentPanel;

    /**
     * Constructor.
     */
    public ContentPanel() {
        contentPanel = new JPanel();
        contentPanel.setPreferredSize(new Dimension(1000, 630));
        contentPanel.setLayout(new BorderLayout());
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JPanel getGUI() {
        return contentPanel;
    }
}
