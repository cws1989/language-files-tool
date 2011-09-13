package langfiles.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import langfiles.Main;

/**
 * The menu bar of the MainWindow.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class MenuBar {

    /**
     * MainWindow
     */
    private MainWindow mainWindow;
    /**
     * The menu bar GUI component.
     */
    private JMenuBar menuBar;
    /**
     * Menu objects.
     */
    private JCheckBoxMenuItem showIconTextMenuItem;

    /**
     * Constructor.
     */
    public MenuBar(MainWindow mainWindow) {
        this.mainWindow = mainWindow;

        menuBar = new JMenuBar();

        addFileMenu();
        addProjectMenu();
        addToolsMenu();
        addWindowMenu();
        addHelpMenu();
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JMenuBar getGUI() {
        return menuBar;
    }

    /**
     * Add file menu to menu bar.
     */
    protected final void addFileMenu() {
        JMenu menu;

        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        menu.add(makeMenuItem("/langfiles/gui/images/MenuBar/exit.png", "exit", "Exit"));
    }

    /**
     * Add project menu to menu bar.
     */
    protected final void addProjectMenu() {
        JMenu menu;

        menu = new JMenu("Project");
        menu.setMnemonic(KeyEvent.VK_P);
        menuBar.add(menu);

        menu.add(makeMenuItem("/langfiles/gui/images/MenuBar/new_project.png", "new_project", "New Project"));
        menu.add(makeMenuItem("/langfiles/gui/images/MenuBar/close_project.png", "close_project", "Close Project"));
        menu.addSeparator();
        menu.add(makeMenuItem("/langfiles/gui/images/MenuBar/add_folder.png", "add_folder", "Add Folder"));
        menu.add(makeMenuItem("/langfiles/gui/images/MenuBar/add_file.png", "add_file", "Add File"));
        menu.add(makeMenuItem("/langfiles/gui/images/MenuBar/remove.png", "remove", "Remove"));
        menu.addSeparator();
        menu.add(makeMenuItem("/langfiles/gui/images/MenuBar/properties.png", "properties", "Properties"));
        menu.add(makeMenuItem("/langfiles/gui/images/MenuBar/commit.png", "commit", "Commit"));
        menu.add(makeMenuItem("/langfiles/gui/images/MenuBar/refresh.png", "refresh", "Refresh"));
    }

    /**
     * Add tools menu to menu bar.
     */
    protected final void addToolsMenu() {
        JMenu menu;

        menu = new JMenu("Tools");
        menu.setMnemonic(KeyEvent.VK_T);
        menuBar.add(menu);

        menu.add(makeMenuItem("/langfiles/gui/images/MenuBar/regular_expression_tester.png", "regular_expression_tester", "Regular Expression Tester"));
        menu.addSeparator();
        menu.add(makeMenuItem("/langfiles/gui/images/MenuBar/settings.png", "settings", "Settings"));
    }

    /**
     * Add window menu to menu bar.
     */
    protected final void addWindowMenu() {
        JMenu menu;

        menu = new JMenu("Window");
        menu.setMnemonic(KeyEvent.VK_W);
        menuBar.add(menu);

        String window_show_icon_text = Main.getInstance().getConfig().getProperty("window/show_icon_text");
        boolean isWindowShowIconText = window_show_icon_text != null ? window_show_icon_text.equals("true") : true;

        menu.add(showIconTextMenuItem = makeCheckBoxMenuItem(null, "show_icon_text", "Show Icon Text", isWindowShowIconText));
        showIconTextMenuItem.removeActionListener(mainWindow.getActionListener());
        showIconTextMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                long when = showIconTextMenuItem.isSelected() ? 1 : 0;
                ActionEvent actionEvent = new ActionEvent(e.getSource(), e.getID(), e.getActionCommand(), when, e.getModifiers());
                // centralize to MainWindow's ActionListener
                MenuBar.this.mainWindow.getActionListener().actionPerformed(actionEvent);
            }
        });

        setShowIconText(isWindowShowIconText);
    }

    /**
     * Add help menu to menu bar.
     */
    protected final void addHelpMenu() {
        JMenu menu;

        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);

        menu.add(makeMenuItem("/langfiles/gui/images/MenuBar/about.png", "about", "About"));
    }

    /**
     * Set the visibility of the tool bar text
     * @param visible true to make visible, false to make invisible
     */
    public void setShowIconText(boolean visible) {
        showIconTextMenuItem.setSelected(visible);
    }

    /**
     * Make {@see javax.swing.JMenuItem}.
     * @param imagePath the class resource path of the image, null if no image
     * @param actionCommand the action command
     * @param tooltipText tooltip text
     * @return the JMenuItem
     */
    private JMenuItem makeMenuItem(String imagePath, String actionCommand, String tooltipText) {
        JMenuItem menuItem = new JMenuItem(tooltipText);
        setMenuItem(menuItem, imagePath, actionCommand, tooltipText);
        return menuItem;
    }

    /**
     * Make {@see javax.swing.JCheckBoxMenuItem}.
     * @param imagePath the class resource path of the image, null if no image
     * @param actionCommand the action command
     * @param tooltipText tooltip text
     * @return the JMenuItem
     */
    private JCheckBoxMenuItem makeCheckBoxMenuItem(String imagePath, String actionCommand, String tooltipText, boolean isSelected) {
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(tooltipText);
        setMenuItem(menuItem, imagePath, actionCommand, tooltipText);
        menuItem.setSelected(isSelected);
        return menuItem;
    }

    /**
     * Make {@see javax.swing.JMenuItem}.
     * @param imagePath the class resource path of the image, null if no image
     * @param actionCommand the action command
     * @param tooltipText tooltip text
     * @return the JMenuItem
     */
    private JMenuItem setMenuItem(JMenuItem menuItem, String imagePath, String actionCommand, String tooltipText) {
        if (imagePath != null) {
            ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(imagePath)));
            menuItem.setIcon(icon);
        }
        if (tooltipText != null) {
            menuItem.setToolTipText(tooltipText);
        }
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(mainWindow.getActionListener());

        return menuItem;
    }
}
