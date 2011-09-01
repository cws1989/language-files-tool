package langfiles.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * The menu bar of the main frame.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class MenuBar {

    /**
     * The menu bar GUI component.
     */
    private JMenuBar menuBar;
    /**
     * The action listener for this menu bar
     */
    private ActionListener actionListener;

    /**
     * Constructor
     */
    public MenuBar() {
        actionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                if (cmd.equals("new_project")) {
                } else if (cmd.equals("open_project")) {
                } else if (cmd.equals("about")) {
                }
            }
        };

        menuBar = new JMenuBar();
        JMenu menu;

        // File menu
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        menu.add(makeMenuItem(null, "new_project", "New Project"));
        menu.addSeparator();
        menu.add(makeMenuItem(null, "open_project", "Open Project"));

        // Help menu
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);

        menu.add(makeMenuItem(null, "about", "About"));
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JMenuBar getGUI() {
        return menuBar;
    }

    /**
     * Make {@see javax.swing.JMenuItem}.
     * @param imagePath the class resource path of the image, null if no image
     * @param actionCommand the action command
     * @param altText alternative text
     * @return the JMenuItem
     */
    private JMenuItem makeMenuItem(String imagePath, String actionCommand, String altText) {
        JMenuItem menuItem = new JMenuItem(altText);

        if (imagePath != null) {
            ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(imagePath)));
            menuItem.setIcon(icon);
        }
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(actionListener);

        return menuItem;
    }
}
