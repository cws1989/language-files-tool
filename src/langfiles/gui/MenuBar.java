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
@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {

    private ActionListener actionListener;

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

        JMenu menu;

        // File menu
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        add(menu);

        menu.add(makeMenuItem(null, "new_project", "New Project"));
        menu.addSeparator();
        menu.add(makeMenuItem(null, "open_project", "Open Project"));

        // Help menu
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        add(menu);

        menu.add(makeMenuItem(null, "about", "About"));
    }

    /**
     * Make {@see javax.swing.JMenuItem}.
     * @param imagePath the class resource path of the image, null if no image
     * @param actionCommand the action command
     * @param altText alternative text
     * @return the JMenuItem
     */
    private JMenuItem makeMenuItem(String imagePath, String actionCommand, String altText) {
        JMenuItem menuItem;
        if (imagePath != null) {
            ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(imagePath)));
            menuItem = new JMenuItem(altText, icon);
        } else {
            menuItem = new JMenuItem(altText);
        }
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(actionListener);
        return menuItem;
    }
}
