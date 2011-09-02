package langfiles.gui;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
     * Constructor.
     */
    public MainWindow() {
        menuBar = new MenuBar();
        contentPanel = new ContentPanel();

        JFrame frame = new JFrame();
        frame.setTitle("Language Files Tool");
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/logo.png")));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {

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
        frame.setJMenuBar(menuBar.getGUI());
        frame.setContentPane(contentPanel.getGUI());
        frame.pack();
        CommonUtil.centerWindow(frame);
        frame.setVisible(true);
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
