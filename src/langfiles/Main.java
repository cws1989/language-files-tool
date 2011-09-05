package langfiles;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import langfiles.util.CommonUtil;
import langfiles.util.LoggingPrintStream;
import langfiles.gui.MainWindowEventListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import langfiles.gui.MainWindow;

/**
 * The main class.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class Main implements MainWindowEventListener {

    /**
     * Singleton object of this class.
     */
    private static Main main;
    /**
     * The directory that store the software generated files, logs etc..
     */
    private String storagePath;
    /**
     * The list of opened windows.
     */
    private final List<MainWindow> windows = Collections.synchronizedList(new ArrayList<MainWindow>());
    /**
     * Configuration file path
     */
    private String configPath;
    /**
     * Configuration
     */
    private Properties config;

    /**
     * Constructor.
     */
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
        File systemErrorLogFile = new File(storagePath + "\\system.err.log");
        try {
            System.setErr(new LoggingPrintStream(new FileOutputStream(systemErrorLogFile, true)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        // configuration
        configPath = storagePath + "/config.ini";
        try {
            reloadConfig();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        CommonUtil.setLookAndFeel();
    }

    /**
     * Initialize those need this Main object (e.g. create GUI).
     */
    private void initialize() {
        // GUI
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
     * Get configuration properties.
     * @return the configuration properties
     */
    public Properties getConfig() {
        return config;
    }

    /**
     * Reload configuration from file.
     * @throws IOException error occured when reloading configuration
     */
    public final void reloadConfig() throws IOException {
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            configFile.createNewFile();
        }

        InputStream in = new BufferedInputStream(new FileInputStream(configPath));
        config = new Properties();
        config.load(in);
        in.close();
    }

    /**
     * Save configuration to file.
     */
    public void saveConfig() throws IOException {
        File configFile = new File(configPath);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(configFile));
        config.store(out, "You are not supposed to edit this file directly.");
        out.close();
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
    public List<MainWindow> getWindows() {
        return new ArrayList<MainWindow>(windows);
    }

    /**
     * Open a new window.
     */
    public final void openNewWindow() {
        MainWindow window = new MainWindow();
        window.addProgramEventListener(this);
        windows.add(window);
    }

    /**
     * Get the logging properties {@see java.io.InpueStream}.
     * @param storagePath the path of directory that store the error log
     * @return the InputStream of the logging properties file
     */
    private InputStream getLoggingProperties(String storagePath) {
        StringBuilder sb = new StringBuilder();

        sb.append("handlers = java.util.logging.FileHandler, langfiles.util.WrappedConsoleHandler\n");
        sb.append(".level = INFO\n");

        sb.append("java.util.logging.FileHandler.pattern = ");
        sb.append(storagePath.replace('\\', '/'));
        sb.append("/error.log\n");
        sb.append("java.util.logging.FileHandler.append = true\n");
        sb.append("java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter\n");

        sb.append("langfiles.util.WrappedConsoleHandler.level = CONFIG\n");
        sb.append("langfiles.util.WrappedConsoleHandler.formatter = java.util.logging.SimpleFormatter\n");

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    @Override
    public boolean programCanCloseNow(ChangeEvent event) {
        return true;
    }

    @Override
    public void programIsClosing(ChangeEvent event) {
        if (!(event.getSource() instanceof MainWindow)) {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Main:programIsClosing(): event.getSource() is not a MainWindow", event);
            return;
        }
        if (!windows.remove((MainWindow) event.getSource())) {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Main:programIsClosing(): event.getSource() not exist in windows list", event);
        }
    }

    /**
     * Program starter.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Main.main = new Main();
                main.initialize();
            }
        });
    }
}
