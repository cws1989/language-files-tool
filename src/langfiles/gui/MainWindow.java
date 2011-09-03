package langfiles.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import langfiles.util.CommonUtil;

/**
 * The main window of the program.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class MainWindow {

    /**
     * The GUI frame of the main window.
     */
    private JFrame window;
    /**
     * The menu bar of the window.
     */
    private MenuBar menuBar;
    /**
     * The content panel of the window.
     */
    private ContentPanel contentPanel;
    /**
     * Program event listener list.
     */
    private final List<MainWindowEventListener> programEventListeners = Collections.synchronizedList(new ArrayList<MainWindowEventListener>());
    /**
     * The action listener for menu bar and tool bar.
     */
    private ActionListener actionListener;

    /**
     * Constructor.
     */
    public MainWindow() {
        // should be initialized before initialize menu bar, content panel, ...
        actionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                /**
                 * file menu
                 */
                if (cmd.equals("exit")) {
                    WindowListener[] windowListeners = window.getWindowListeners();
                    for (WindowListener windowListener : windowListeners) {
                        windowListener.windowClosing(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
                    }
                }/**
                 * project menu
                 */
                else if (cmd.equals("new_project")) {
                } else if (cmd.equals("open_project")) {
                } else if (cmd.equals("add_folder")) {
                } else if (cmd.equals("add_file")) {
                } else if (cmd.equals("remove")) {
                } else if (cmd.equals("properties")) {
                } else if (cmd.equals("commit")) {
                } else if (cmd.equals("refresh")) {
                }/**
                 * tools menu
                 */
                else if (cmd.equals("regular_expression_tester")) {
                } else if (cmd.equals("option")) {
                }/**
                 * help menu
                 */
                else if (cmd.equals("about")) {
                    About about = new About(window);
                    about.show();
                }/**
                 * window menu && tool bar
                 */
                else if (cmd.equals("show_icon_text")) {
                    contentPanel.getToolBar().setShowIconText(e.getWhen() == 1);
                    menuBar.setShowIconText(e.getWhen() == 1);
                }
            }
        };

        menuBar = new MenuBar(this);
        contentPanel = new ContentPanel(this);

        window = new JFrame();
        window.setTitle("Language Files Tool");
        window.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/logo.png")));
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                ChangeEvent event = new ChangeEvent(MainWindow.this);
                synchronized (programEventListeners) {
                    for (MainWindowEventListener listener : programEventListeners) {
                        if (!listener.programCanCloseNow(event)) {
                            return;
                        }
                    }
                    for (MainWindowEventListener listener : programEventListeners) {
                        listener.programIsClosing(event);
                    }
                }
                JFrame window = ((JFrame) evt.getSource());
                window.dispose();
            }
        });
        window.setJMenuBar(menuBar.getGUI());
        window.setContentPane(contentPanel.getGUI());
        window.pack();
        CommonUtil.centerWindow(window);
        window.setVisible(true);
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JFrame getGUI() {
        return window;
    }

    /**
     * Get the menu bar.
     * @return the menu bar
     */
    public MenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Get the content panel.
     * @return the content panel
     */
    public ContentPanel getContentPanel() {
        return contentPanel;
    }

    /**
     * Get the action listener.
     * @return the action listener
     */
    public ActionListener getActionListener() {
        return actionListener;
    }

    /**
     * Add program event listener.
     * @param listener the program event listener
     */
    public void addProgramEventListener(MainWindowEventListener listener) {
        synchronized (programEventListeners) {
            programEventListeners.add(listener);
        }
    }

    /**
     * Remove program event listener.
     * @param listener the program event listener
     */
    public void removeProgramEventListener(MainWindowEventListener listener) {
        synchronized (programEventListeners) {
            programEventListeners.remove(listener);
        }
    }
}
