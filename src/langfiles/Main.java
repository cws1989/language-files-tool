package langfiles;

import java.awt.Font;
import java.awt.Point;
import langfiles.util.CommonUtil;
import langfiles.util.LoggingPrintStream;
import langfiles.gui.MainWindowEventListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import langfiles.gui.MainWindow;
import langfiles.project.Project;
import langfiles.util.Config;
import langfiles.util.ConfigTool;
import langfiles.util.Splash;

/**
 * The main class.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class Main {

    /**
     * Singleton object of this class.
     */
    private static Main main;
    /**
     * The directory that store the software generated files (settings, preference, logs etc.).
     */
    private String storageDirectoryPath;
    /**
     * The list of opened {@link MainWindow}.
     */
    private final List<MainWindow> mainWindowList;
    /**
     * The path of the configuration/setting file.
     */
    private String configFilePath;
    /**
     * Configuration, {@link java.util.Properties} through {@link langfiles.util.Config} interface. It is loaded from {@link #configFilePath}.
     */
    private Config config;
    /**
     * The path of the configuration/setting file.
     */
    private String preferenceFilePath;
    /**
     * Preference, {@link java.util.Properties} through {@link langfiles.util.Config} interface. It is loaded from {@link #preferenceFilePath}.
     */
    private Config preference;
    /**
     * Shutdown hook list. Things that has to be done before program exit/shutdown.
     */
    final Map<Integer, List<Runnable>> shutdownHookList;
    /**
     * The MainWindow event listener that will be added to every MainWindow. 
     * It is used to remove the MainWindow from {@link #mainWindowList}.
     */
    private MainWindowEventListener mainWindowEventListener;

    /**
     * Constructor. Should invoke {@link #initialize} after construction.
     */
    private Main() {
        Splash.setPosition(new Point(140, 25));
        Splash.setFont(new Font("Tahoma", Font.PLAIN, 12));

        Splash.updateMessage("Initializing ...");

        mainWindowList = Collections.synchronizedList(new ArrayList<MainWindow>());

        //<editor-fold defaultstate="collapsed" desc="storage path">
        String homeDirectoryStorage = System.getProperty("user.home") + "/LanguageFilesTool/";
        String currentDirectoryStorage = System.getProperty("user.dir") + "/LanguageFilesTool/";
        String[] possibleStoragePathList = new String[]{homeDirectoryStorage, currentDirectoryStorage};

        storageDirectoryPath = CommonUtil.mkdir(Arrays.asList(possibleStoragePathList));
        if (storageDirectoryPath == null) {
            JOptionPane.showMessageDialog(null, "Failed to create storage path.");
            System.exit(0);
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="logging strategies">
        try {
            InputStream loggingPropertiesInputStream = getLoggingProperties(storageDirectoryPath);
            LogManager.getLogManager().readConfiguration(loggingPropertiesInputStream);
            loggingPropertiesInputStream.close(); // not necessary
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to set logging strategies.", ex);
        }
        // record System.err
        File systemErrorLogFile = new File(storageDirectoryPath + "\\system.err.log");
        try {
            System.setErr(new LoggingPrintStream(new FileOutputStream(systemErrorLogFile, true)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="shutdown hook">
        shutdownHookList = Collections.synchronizedMap(new TreeMap<Integer, List<Runnable>>());
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (shutdownHookList) {
                    for (List<Runnable> taskList : shutdownHookList.values()) {
                        for (Runnable task : taskList) {
                            task.run();
                        }
                    }
                }
            }
        }));
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="configuration">
        configFilePath = new File(storageDirectoryPath + "/config.xml").getAbsolutePath();
        try {
            config = new ConfigTool(configFilePath);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        addShutdownEvent(101, new Runnable() {

            @Override
            public void run() {
                if (config.isChanged()) {
                    try {
                        config.save();
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="preference">
        preferenceFilePath = new File(storageDirectoryPath + "/preference.xml").getAbsolutePath();
        try {
            preference = new ConfigTool(preferenceFilePath);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        addShutdownEvent(101, new Runnable() {

            @Override
            public void run() {
                if (preference.isChanged()) {
                    try {
                        preference.save();
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="uncaught exception handle">
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Uncaught Exception", e);
            }
        });
        //</editor-fold>

        // remove the closing MainWindow from  {@link #mainWindowList}
        mainWindowEventListener = new MainWindowEventListener() {

            @Override
            public boolean windowCanCloseNow(ChangeEvent event) {
                return true;
            }

            @Override
            public void windowIsClosing(ChangeEvent event) {
                if (!(event.getSource() instanceof MainWindow)) {
                    Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Main:programIsClosing(): event.getSource() is not a MainWindow", event);
                    return;
                }
                if (!mainWindowList.remove((MainWindow) event.getSource())) {
                    Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Main:programIsClosing(): event.getSource() not exist in windows list", event);
                }
            }
        };

//        try {
//            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (Exception e) {
//        }
        CommonUtil.setLookAndFeel();
    }

    /**
     * Get the singleton instance of Main.
     * @return the main object
     */
    public static Main getInstance() {
        if (main == null) {
            synchronized (Main.class) {
                if (main == null) {
                    main = new Main();
                }
            }
        }
        return main;
    }

    /**
     * Get configuration.
     * @return the configuration
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Get preference.
     * @return the preference
     */
    public Config getPreference() {
        return preference;
    }

    /**
     * Return the storage directory path.
     * @return the storage directory path
     */
    public String getStoragePath() {
        return storageDirectoryPath;
    }

    /**
     * Return the list of all opened windows.
     * @return the main frame object
     */
    public List<MainWindow> getWindows() {
        return new ArrayList<MainWindow>(mainWindowList);
    }

    /**
     * Open a new MainWindow.
     */
    public void openNewWindow(List<Project> projectList) {
        MainWindow window = new MainWindow(projectList);
        window.addMainWindowEventListener(mainWindowEventListener);
        mainWindowList.add(window);
    }

    /**
     * Get the logging properties {@see java.io.InpueStream}.
     * @param storageDirectoryPath {@see #storageDirectoryPath}
     * @return the InputStream of the logging properties file
     */
    private InputStream getLoggingProperties(String storageDirectoryPath) {
        StringBuilder sb = new StringBuilder(512);

        sb.append("handlers = java.util.logging.FileHandler, langfiles.util.WrappedConsoleHandler\n");
        sb.append(".level = INFO\n");

        sb.append("java.util.logging.FileHandler.pattern = ");
        sb.append(storageDirectoryPath.replace('\\', '/'));
        sb.append("/error.log\n");
        sb.append("java.util.logging.FileHandler.append = true\n");
        sb.append("java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter\n");

        sb.append("langfiles.util.WrappedConsoleHandler.level = CONFIG\n");
        sb.append("langfiles.util.WrappedConsoleHandler.formatter = java.util.logging.SimpleFormatter\n");

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    /**
     * Add shutdown event.
     * @param priorityValue smaller value will be executed first
     * @param task the task to execute before shutdown
     */
    public void addShutdownEvent(int priorityValue, Runnable task) {
        synchronized (shutdownHookList) {
            List<Runnable> taskList = shutdownHookList.get(priorityValue);
            if (taskList == null) {
                taskList = new ArrayList<Runnable>();
                shutdownHookList.put(priorityValue, taskList);
            }
            taskList.add(task);
        }
    }

    /**
     * Remove shutdown event.
     * @param task remove the task from {@link #shutdownHookList}
     */
    public boolean removeShutdownEvent(Runnable task) {
        synchronized (shutdownHookList) {
            for (Integer priorityValue : shutdownHookList.keySet()) {
                List<Runnable> taskList = shutdownHookList.get(priorityValue);
                if (taskList.indexOf(task) != -1) {
                    return taskList.remove(task);
                }
            }
        }
        return false;
    }

    /**
     * Program starter.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Main.main = new Main();

                // for test purpose
                Project project = new Project("Language Files Tool");
                project.setAllowedExtensions(Arrays.asList(new String[]{".java"}));
                project.add(new File(System.getProperty("user.dir")));
                main.openNewWindow(Arrays.asList(new Project[]{project}));
            }
        });
    }
}