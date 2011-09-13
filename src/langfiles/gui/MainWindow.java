package langfiles.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
     * Constructor.
     */
    public MainWindow() {
        mainWindowEventListenerList = Collections.synchronizedList(new ArrayList<MainWindowEventListener>());

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
                    main.getConfig().setProperty("window/show_icon_text", Boolean.toString(showIconText));

                    getToolBar().setShowIconText(showIconText);
                    getMenuBar().setShowIconText(showIconText);
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

        projectPanel = new ProjectPanel(this);
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
        window.setJMenuBar(menuBar.getGUI());
        window.setContentPane(contentPanel);
        window.pack();
        //window.setLocationByPlatform(true);
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
