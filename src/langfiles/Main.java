package langfiles;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import langfiles.gui.ContentPanel;
import langfiles.gui.MenuBar;

/**
 * The main class.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class Main {

    private static Main main;
    /**
     * The directory that store the software generated files, logs etc..
     */
    private String storagePath;
    /**
     * The list of opened windows.
     */
    private final List<JFrame> windows = Collections.synchronizedList(new ArrayList<JFrame>());
    /**
     * Program event listener list.
     */
    private final List<ProgramEventListener> programEventListeners = Collections.synchronizedList(new ArrayList<ProgramEventListener>());

    private Main() {
        // storage path
        String homeDirectoryStorage = System.getProperty("user.home") + "/LanguageFilesTool/";
        String currentDirectoryStorage = System.getProperty("user.dir") + "/LanguageFilesTool/";
        String[] possibleStoragePathList = new String[]{homeDirectoryStorage, currentDirectoryStorage};

        storagePath = CommonUtil.mkdir(Arrays.asList(possibleStoragePathList));
        if (storagePath == null) {
            JOptionPane.showMessageDialog(null, "Failed to create storage path.");
            System.exit(0);
        }

        // logging strategies
        try {
            InputStream loggingPropertiesInputStream = getLoggingProperties(storagePath);
            LogManager.getLogManager().readConfiguration(loggingPropertiesInputStream);
            loggingPropertiesInputStream.close(); // not necessary
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to set logging strategies.", ex);
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Failed to set system look and feel.", ex);
        }

        // GUI
        openNewWindow();
        openNewWindow();
    }

    /**
     * Get the singleton instance of Main.
     * @return the main object
     */
    public static Main getInstance() {
//        if (main == null) {
//            synchronized (Main.class) {
//                if (main == null) {
//                    main = new Main();
//                }
//            }
//        }
        return main;
    }

    /**
     * Return the storage directory path.
     * @return the storage directory path
     */
    public String getStoragePath() {
        return this.storagePath;
    }

    /**
     * Return the list of all opened windows.
     * @return the main frame object
     */
    public List<JFrame> getWindows() {
        return new ArrayList<JFrame>(this.windows);
    }

    /**
     * Add program event listener.
     * @param listener the program event listener
     */
    public void addProgramEventListener(ProgramEventListener listener) {
        synchronized (programEventListeners) {
            programEventListeners.add(listener);
        }
    }

    /**
     * Remove program event listener.
     * @param listener the program event listener
     */
    public void removeProgramEventListener(ProgramEventListener listener) {
        synchronized (programEventListeners) {
            programEventListeners.remove(listener);
        }
    }

    /**
     * Open a new window.
     */
    public final void openNewWindow() {
        JFrame frame = new JFrame();
        frame.setTitle("Language Files Tool");
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/logo.png")));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                synchronized (programEventListeners) {
                    for (ProgramEventListener listener : programEventListeners) {
                        if (!listener.programCanCloseNow()) {
                            return;
                        }
                    }
                }
                JFrame window = ((JFrame) evt.getSource());
                window.dispose();
                windows.remove(window);
            }
        });
        frame.setJMenuBar(new MenuBar().getGUI());
        frame.setContentPane(new ContentPanel().getGUI());
        frame.pack();
        CommonUtil.centerWindow(frame);
        frame.setVisible(true);

        windows.add(frame);
    }

    /**
     * Get the logging properties {@see java.io.InpueStream}.
     * @param storagePath the path of directory that store the error log
     * @return the InputStream of the logging properties file
     */
    private InputStream getLoggingProperties(String storagePath) {
        StringBuilder sb = new StringBuilder();

        sb.append("handlers = java.util.logging.FileHandler, java.util.logging.ConsoleHandler\n");
        sb.append(".level = INFO\n");

        sb.append("java.util.logging.FileHandler.pattern = ");
        sb.append(storagePath.replace('\\', '/'));
        sb.append("/error.log\n");
        sb.append("java.util.logging.FileHandler.append = true\n");
        sb.append("java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter\n");

        sb.append("java.util.logging.ConsoleHandler.level = CONFIG\n");
        sb.append("java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n");

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Main.main = new Main();
            }
        });
    }
}
