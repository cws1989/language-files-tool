package langfiles.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import langfiles.Main;
import langfiles.util.CommonUtil;

/**
 * The tool bar of the MainWindow.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class ToolBar {

    /**
     * MainWindow
     */
    private MainWindow mainWindow;
    /**
     * The tool bar.
     */
    private JToolBar toolBar;
    /**
     * Exclusive use for {@link #setTextVisible(Boolean)} to save the text of the button before removing the text.
     */
    private Map<Component, String> componentsText;
    /**
     * Popup menu of the tool bar.
     */
    private JPopupMenu popupMenu;
    /**
     * The popup menu item in the {@link #popupMenu}.
     */
    private JCheckBoxMenuItem showIconTextMenuItem;

    /**
     * Constructor.
     */
    public ToolBar(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        componentsText = new HashMap<Component, String>();

        String window_show_icon_text = Main.getInstance().getPreference().getProperty("window/show_icon_text");
        boolean isWindowShowIconText = window_show_icon_text != null ? window_show_icon_text.equals("true") : true;

        //<editor-fold defaultstate="collapsed" desc="popup menu">
        showIconTextMenuItem = new JCheckBoxMenuItem("Show Icon Text");
        showIconTextMenuItem.setActionCommand("show_icon_text");
        showIconTextMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                long when = showIconTextMenuItem.isSelected() ? 1 : 0;
                ActionEvent actionEvent = new ActionEvent(e.getSource(), e.getID(), e.getActionCommand(), when, e.getModifiers());
                // centralize to MainWindow's ActionListener
                ToolBar.this.mainWindow.getActionListener().actionPerformed(actionEvent);
            }
        });

        popupMenu = new JPopupMenu();
        popupMenu.add(showIconTextMenuItem);
        //</editor-fold>

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        addButtons();

        setShowIconText(isWindowShowIconText);
    }

    /**
     * Add buttons to tool bar.
     */
    private void addButtons() {
        toolBar.add(createButton("/langfiles/gui/images/ToolBar/new_project.png", "new_project", "New Project", "New Project"));
        toolBar.add(createButton("/langfiles/gui/images/ToolBar/close_project.png", "close_project", "Close Project", "Close Project"));
        toolBar.addSeparator();
        toolBar.add(createButton("/langfiles/gui/images/ToolBar/add_folder.png", "add_folder", "Add Folder to Project", "Add Folder"));
        toolBar.add(createButton("/langfiles/gui/images/ToolBar/add_file.png", "add_file", "Add File to Project", "Add File"));
        toolBar.add(createButton("/langfiles/gui/images/ToolBar/remove.png", "remove", "Remove from Project", "Remove"));
        toolBar.addSeparator();
        toolBar.add(createButton("/langfiles/gui/images/ToolBar/properties.png", "properties", "Project Properties", "Properties"));
        toolBar.add(createButton("/langfiles/gui/images/ToolBar/commit.png", "commit", "Commit Project", "Commit"));
        toolBar.add(createButton("/langfiles/gui/images/ToolBar/refresh.png", "refresh", "Refresh", "Refresh"));
        toolBar.addSeparator();
        toolBar.add(createButton("/langfiles/gui/images/ToolBar/regular_expression_tester.png", "regular_expression_tester", "Regular Expression Tester", "RegExp Tester"));
        toolBar.add(createButton("/langfiles/gui/images/ToolBar/settings.png", "settings", "Settings", "Settings"));
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(createButton("/langfiles/gui/images/ToolBar/about.png", "about", "About", "About"));
    }

    /**
     * Set the visibility of the tool bar text
     * @param visible true to make visible, false to make invisible
     */
    public final void setShowIconText(boolean visible) {
        showIconTextMenuItem.setSelected(visible);

        Component[] components = toolBar.getComponents();
        for (Component component : components) {
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                String buttonText = button.getText();
                if (visible) {
                    String buttonTextPreserved = componentsText.remove(button);
                    if (buttonTextPreserved != null) {
                        button.setText(buttonTextPreserved);
                    }
                } else {
                    if (buttonText != null && !buttonText.isEmpty()) {
                        componentsText.put(button, buttonText);
                    }
                    button.setText(null);
                }
            }
        }
    }

    /**
     * Get the GUI display bar.
     * @return the GUI component
     */
    public JToolBar getGUI() {
        return toolBar;
    }

    /**
     * Create toggle button for tool bar.
     * @param imgLocation the image location (e.g. /langfiles/logo.png) or null
     * @param actionCommand the action command or null
     * @param toolTipText the tool tip text or null
     * @param altText the alternate text or null
     * @return the toggle button
     */
    protected final JToggleButton createToggleButton(String imgLocation, String actionCommand, String toolTipText, String altText) {
        JToggleButton button = new JToggleButton();

        button.setVerticalTextPosition(JButton.BOTTOM);
        button.setHorizontalTextPosition(JButton.CENTER);
        button.setFont(CommonUtil.deriveFont(button.getFont(), false, 10));
        button.setFocusable(false);
        button.addActionListener(mainWindow.getActionListener());

        if (imgLocation != null) {
            if (altText != null) {
                button.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(imgLocation)), altText));
            } else {
                button.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(imgLocation))));
            }
        }
        if (actionCommand != null) {
            button.setActionCommand(actionCommand);
        }
        if (toolTipText != null) {
            button.setToolTipText(toolTipText);
        }
        if (altText != null) {
            button.setText(altText);
        }

        return button;
    }

    /**
     * Create button for tool bar.
     * @param imgLocation the image location (e.g. /langfiles/logo.png) or null
     * @param actionCommand the action command or null
     * @param toolTipText the tool tip text or null
     * @param altText the alternate text or null
     * @return the button
     */
    protected final JButton createButton(String imgLocation, String actionCommand, String toolTipText, String altText) {
        JButton button = new JButton();

        button.setVerticalTextPosition(JButton.BOTTOM);
        button.setHorizontalTextPosition(JButton.CENTER);
        button.setFont(CommonUtil.deriveFont(button.getFont(), false, 10));
        button.setFocusable(false);
        button.addActionListener(mainWindow.getActionListener());

        if (imgLocation != null) {
            if (altText != null) {
                button.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(imgLocation)), altText));
            } else {
                button.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(imgLocation))));
            }
        }
        if (actionCommand != null) {
            button.setActionCommand(actionCommand);
        }
        if (toolTipText != null) {
            button.setToolTipText(toolTipText);
        }
        if (altText != null) {
            button.setText(altText);
        }

        return button;
    }
}
