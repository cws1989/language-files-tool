package langfiles.gui;

import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class StatusBar {

    private JPanel statusBar;
    private JPanel leftPanel;
    private JPanel rightPanel;

    public StatusBar() {
        statusBar = new JPanel();
        statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
        statusBar.setPreferredSize(new Dimension(100, 22));
        statusBar.setMinimumSize(new Dimension(100, 22));
        statusBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, statusBar.getBackground().darker()), BorderFactory.createMatteBorder(1, 0, 0, 0, statusBar.getBackground().brighter())));
    }

    public JPanel getGUI() {
        return statusBar;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new StatusBar().getGUI());
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);
    }
}
