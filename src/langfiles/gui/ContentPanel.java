package langfiles.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * The content panel of the main frame.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class ContentPanel {

    /**
     * MainWindow
     */
    private MainWindow mainWindow;
    /**
     * The content panel.
     */
    private JPanel contentPanel;
    /**
     * Tool bar
     */
    private ToolBar toolBar;
    /**
     * The project panel
     */
    private ProjectPanel projectPanel;
    /**
     * The code panel
     */
    private CodePanel codePanel;

    /**
     * Constructor.
     */
    public ContentPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;

        contentPanel = new JPanel();
        contentPanel.setPreferredSize(new Dimension(1000, 630));
        contentPanel.setLayout(new BorderLayout());

        toolBar = new ToolBar(this);
        contentPanel.add(toolBar.getGUI(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane();
        Color panelBackground = splitPane.getBackground();
        splitPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, panelBackground.brighter()), BorderFactory.createMatteBorder(1, 0, 0, 0, panelBackground.darker())));
        splitPane.setDividerSize(3);
        splitPane.setDividerLocation(250);
        contentPanel.add(splitPane, BorderLayout.CENTER);

        projectPanel = new ProjectPanel();
        splitPane.setLeftComponent(projectPanel.getGUI());

        codePanel = new CodePanel();
        splitPane.setRightComponent(codePanel.getGUI());
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JPanel getGUI() {
        return contentPanel;
    }

    /**
     * Get the tool bar.
     * @return the tool bar
     */
    public ToolBar getToolBar() {
        return toolBar;
    }

    /**
     * Get the project panel.
     * @return the project panel
     */
    public ProjectPanel getProjectPanel() {
        return projectPanel;
    }

    /**
     * Get the code panel.
     * @return the code panel
     */
    public CodePanel getCodePanel() {
        return codePanel;
    }

    /**
     * Get the action listener.
     * @return the action listener
     */
    public ActionListener getActionListener() {
        return mainWindow.getActionListener();
    }
}
