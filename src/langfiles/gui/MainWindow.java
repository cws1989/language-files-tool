package langfiles.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import langfiles.Main;

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
                    boolean showIconText = e.getWhen() == 1;
                    Main main = Main.getInstance();
                    main.getConfig().setProperty("window_show_icon_text", Boolean.toString(showIconText));
                    try {
                        main.saveConfig();
                    } catch (IOException ex) {
                        Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    getToolBar().setShowIconText(showIconText);
                    menuBar.setShowIconText(showIconText);
                }
            }
        };

        menuBar = new MenuBar(this);

        //<editor-fold defaultstate="collapsed" desc="content panel">
        JPanel contentPanel = new JPanel();
        contentPanel.setPreferredSize(new Dimension(1000, 630));
        contentPanel.setLayout(new BorderLayout());

        toolBar = new ToolBar(this);
        contentPanel.add(toolBar.getGUI(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane();
        Color panelBackground = splitPane.getBackground();
        splitPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, panelBackground.brighter()), BorderFactory.createMatteBorder(1, 0, 0, 0, panelBackground.darker())));
        splitPane.setDividerSize(2);
        splitPane.setDividerLocation(250);
        contentPanel.add(splitPane, BorderLayout.CENTER);

        projectPanel = new ProjectPanel();
        splitPane.setLeftComponent(projectPanel.getGUI());

        codePanel = new CodePanel();
        splitPane.setRightComponent(codePanel.getGUI());
        //</editor-fold>

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
        window.setContentPane(contentPanel);
        window.pack();
        window.setLocationByPlatform(true);
        //CommonUtil.centerWindow(window);
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
