package langfiles.gui;

import langfiles.gui.component.JTitledPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import langfiles.Main;
import langfiles.project.Project;
import langfiles.project.ProjectListener;
import langfiles.util.CommonUtil;
import langfiles.util.Config;
import langfiles.util.Splash;

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
     * The tool bar of the window.
     */
    private ToolBar toolBar;
    /**
     * The project panel of the window.
     */
    private ProjectPanel projectPanel;
    /**
     * The code panel of the window.
     */
    private CodePanel codePanel;
    /**
     * Program event listener list.
     */
    private final List<MainWindowEventListener> mainWindowEventListenerList;
    /**
     * The action listener for menu bar and tool bar.
     */
    private ActionListener actionListener;
    /**
     * The project list.
     */
    private final List<Project> projectList;
    private final List<ProjectListener> projectEventListenerList;

    /**
     * Constructor.
     */
    public MainWindow(List<Project> projectList) {
        Splash.updateMessage("Initializing GUI ...");

        mainWindowEventListenerList = Collections.synchronizedList(new ArrayList<MainWindowEventListener>());
        this.projectList = Collections.synchronizedList(new ArrayList<Project>(projectList));
        projectEventListenerList = Collections.synchronizedList(new ArrayList<ProjectListener>());

        // should be initialized before initialize menu bar, content panel, ...
        actionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                /**
                 * file menu
                 */
                if (cmd.equals("exit")) {
                    WindowListener[] windowListenerList = window.getWindowListeners();
                    for (WindowListener windowListener : windowListenerList) {
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
                    final JFrame frame = new JFrame();
                    frame.setTitle("Regular Expression Tester");
                    frame.setIconImage(Toolkit.getDefaultToolkit().getImage(JTitledPanel.class.getResource("/langfiles/gui/images/RegularExpressionTester/logo.png")));
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    final RegularExpressionTester regularExpressionTester = new RegularExpressionTester();
                    frame.setContentPane(regularExpressionTester.getGUI());
                    frame.pack();
                    frame.setLocationRelativeTo(window);

                    final MainWindowEventListener mainWindowEventListener = new MainWindowEventListener() {

                        @Override
                        public boolean windowCanCloseNow(ChangeEvent event) {
                            return true;
                        }

                        @Override
                        public void windowIsClosing(ChangeEvent event) {
                            frame.dispose();
                        }
                    };
                    addMainWindowEventListener(mainWindowEventListener);

                    frame.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosed(WindowEvent e) {
                            regularExpressionTester.close();
                            removeMainWindowEventListener(mainWindowEventListener);
                        }
                    });

                    frame.setVisible(true);
                } else if (cmd.equals("settings")) {
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
                    main.getPreference().setProperty("window/show_icon_text", Boolean.toString(showIconText));

                    getToolBar().setShowIconText(showIconText);
                    getMenuBar().setShowIconText(showIconText);
                }
            }
        };

        Splash.updateMessage("Initializing Menu Bar ...");

        menuBar = new MenuBar(this);

        Splash.updateMessage("Initializing Content Panel ...");

        //<editor-fold defaultstate="collapsed" desc="content panel">
        toolBar = new ToolBar(this);
        projectPanel = new ProjectPanel(this);
        codePanel = new CodePanel(this);

        final JSplitPane splitPane = new JSplitPane();
        Color panelBackground = splitPane.getBackground();
        splitPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, panelBackground.brighter()), BorderFactory.createMatteBorder(1, 0, 0, 0, panelBackground.darker())));
        splitPane.setDividerSize(2);
        splitPane.setLeftComponent(projectPanel.getGUI());
        splitPane.setRightComponent(codePanel.getGUI());
        String splitPaneDividerLocation = Main.getInstance().getPreference().getProperty("main_window/split_pane_divider_location");
        splitPane.setDividerLocation(splitPaneDividerLocation != null ? Integer.parseInt(splitPaneDividerLocation) : 250);

        JPanel contentPanel = new JPanel();
        contentPanel.setPreferredSize(new Dimension(1000, 630));
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(toolBar.getGUI(), BorderLayout.NORTH);
        contentPanel.add(splitPane, BorderLayout.CENTER);

        Main.getInstance().addShutdownEvent(100, new Runnable() {

            @Override
            public void run() {
                Config preference = Main.getInstance().getPreference();
                preference.setProperty("main_window/split_pane_divider_location", Integer.toString(splitPane.getDividerLocation()));
            }
        });
        //</editor-fold>

        Splash.updateMessage("Initializing Main Window ...");

        //<editor-fold defaultstate="collapsed" desc="frame">
        window = new JFrame();
        window.setTitle("Language Files Tool");
        window.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/logo.png")));
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                ChangeEvent event = new ChangeEvent(MainWindow.this);
                synchronized (mainWindowEventListenerList) {
                    for (MainWindowEventListener listener : mainWindowEventListenerList) {
                        if (!listener.windowCanCloseNow(event)) {
                            return;
                        }
                    }
                    for (MainWindowEventListener listener : mainWindowEventListenerList) {
                        listener.windowIsClosing(event);
                    }
                }
                Window window = ((Window) evt.getSource());
                if (window != null) {
                    window.dispose();
                }
            }
        });
        window.addWindowStateListener(new WindowAdapter() {

            @Override
            public void windowStateChanged(WindowEvent e) {
                if (window.getExtendedState() == JFrame.NORMAL) {
                    Dimension windowLastDimension = window.getSize();
                    Point windowPosOnScreen = window.getLocationOnScreen();

                    Config preference = Main.getInstance().getPreference();
                    preference.setProperty("window/last_width", Integer.toString(windowLastDimension.width));
                    preference.setProperty("window/last_height", Integer.toString(windowLastDimension.height));
                    preference.setProperty("window/last_pos_x", Integer.toString(windowPosOnScreen.x));
                    preference.setProperty("window/last_pos_y", Integer.toString(windowPosOnScreen.y));
                }
            }
        });

        window.setJMenuBar(menuBar.getGUI());
        window.setContentPane(contentPanel);
        window.pack();

        Splash.updateMessage("Getting GUI Preference ...");

        Config preference = Main.getInstance().getPreference();

        String windowLastWidthString = preference.getProperty("window/last_width");
        String windowLastHeightString = preference.getProperty("window/last_height");
        if (windowLastHeightString != null && windowLastWidthString != null) {
            window.setSize(new Dimension(Integer.parseInt(windowLastWidthString), Integer.parseInt(windowLastHeightString)));
        }

        String windowLastPosX = preference.getProperty("window/last_pos_x");
        String windowLastPosY = preference.getProperty("window/last_pos_y");
        if (windowLastHeightString != null && windowLastWidthString != null) {
            window.setLocation(Integer.parseInt(windowLastPosX), Integer.parseInt(windowLastPosY));
        } else {
            //window.setLocationByPlatform(true);
            CommonUtil.centerWindow(window);
        }

        window.setVisible(true);
        //</editor-fold>

        for (Project project : projectList) {
            addProject(project);
        }
    }

    public List<Project> addProjectEventListener(ProjectListener listener) {
        synchronized (projectList) {
            synchronized (projectEventListenerList) {
                projectEventListenerList.add(listener);
                return getProjectList();
            }
        }
    }

    public boolean removeProjectEventListener(ProjectListener listener) {
        return projectEventListenerList.remove(listener);
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
     * Add project.
     * @param project the project
     */
    public void addProject(Project project) {
        synchronized (projectEventListenerList) {
            synchronized (projectList) {
                projectList.add(project);
                for (ProjectListener listener : projectEventListenerList) {
                    listener.projectAdded(project);
                }
            }
        }
    }

    public void removeProject(Project project) {
        synchronized (projectEventListenerList) {
            synchronized (projectList) {
                projectList.remove(project);
                for (ProjectListener listener : projectEventListenerList) {
                    listener.projectRemoved(project);
                }
            }
        }
    }

    public List<Project> getProjectList() {
        List<Project> returnList = null;
        synchronized (projectList) {
            returnList = new ArrayList<Project>(projectList);
        }
        return returnList;
    }

//    public SyncFile getSyncFileByAbsolutePath(String path) {
//        synchronized (projectList) {
//            for (Project project : projectList) {
//                SyncFile returnFile = null;
//                if ((returnFile = project.getSyncFileByAbsolutePath(path)) != null) {
//                    return returnFile;
//                }
//            }
//            return null;
//        }
//    }
    /**
     * Add program event listener.
     * @param listener the program event listener
     */
    public void addMainWindowEventListener(MainWindowEventListener listener) {
        synchronized (mainWindowEventListenerList) {
            mainWindowEventListenerList.add(listener);
        }
    }

    /**
     * Remove program event listener.
     * @param listener the program event listener
     */
    public void removeMainWindowEventListener(MainWindowEventListener listener) {
        synchronized (mainWindowEventListenerList) {
            mainWindowEventListenerList.remove(listener);
        }
    }
}
