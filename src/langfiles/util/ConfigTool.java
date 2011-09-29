package langfiles.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyAdapter;
import net.contentobjects.jnotify.JNotifyException;

/**
 * The configuration {@link java.util.Properties} wrapper.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class ConfigTool implements Config {

    /**
     * The path of the configuration file, must not return null for {@link java.io.File#getParent()}.
     */
    private String configPath;
    /**
     * The configuration {@link java.util.Properties}.
     */
    private Properties config;
    /**
     * Indicator that indicate whether the config has been changed or not.
     */
    private boolean isChanged;
    /**
     * The list that record the config changes.
     */
    private final List<ConfigChange> configChanges;
    /**
     * The watch id used by JNotify to listen on config file modification event. -1 means 'watch' removed or not added.
     */
    private int watchId;

    /**
     * Constructor.
     * @param configPath {@see #configPath}
     * @throws IOException read config file IO exception
     */
    public ConfigTool(String configPath) throws IOException {
        this.configPath = new File(configPath).getAbsolutePath();
        isChanged = false;
        configChanges = Collections.synchronizedList(new ArrayList<ConfigChange>());
        watchId = -1;

        reload();
    }

    /**
     * Listen on the config file modification event.
     * @throws JNotifyException error occurred when setting the event listener
     */
    protected void addWatch() throws JNotifyException {
        removeWatch();
        watchId = JNotify.addWatch(new File(configPath).getParent(), JNotify.FILE_MODIFIED, false, new JNotifyAdapter() {

            @Override
            public void fileModified(int watchId, String rootPath, String name) {
                if (configPath.equals(new File(rootPath + "/" + name).getAbsolutePath())) {
                    try {
                        reload();
                    } catch (IOException ex) {
                        Logger.getLogger(ConfigTool.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    /**
     * Remove the config file modification event listener.
     * @throws JNotifyException error occurred when removing the event listener
     */
    protected void removeWatch() throws JNotifyException {
        if (watchId != -1) {
            JNotify.removeWatch(watchId);
            watchId = -1;
        }
    }

    @Override
    public final void reload() throws IOException {
        synchronized (configChanges) {
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            InputStream in = new BufferedInputStream(new FileInputStream(configPath));
            config = new Properties();
            try {
                config.loadFromXML(in);
            } catch (InvalidPropertiesFormatException ex) {
                config = new Properties();
                // ignore
            }
            in.close();

            if (watchId == -1) {
                addWatch();
            }
        }
    }

    @Override
    public void save() throws IOException {
        synchronized (configChanges) {
            removeWatch();

            File configFile = new File(configPath);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(configFile));
            config.storeToXML(out, "You are not supposed to edit this file directly.");
            out.close();

            configChanges.clear();
            isChanged = false;

            addWatch();
        }
    }

    @Override
    public String getProperty(String key) {
        return config.getProperty(key);
    }

    @Override
    public String setProperty(String key, String value) {
        String returnObject = null;
        synchronized (configChanges) {
            returnObject = (String) config.setProperty(key, value);
            if (returnObject == null || !returnObject.equals(value)) {
                configChanges.add(new ConfigChange(key, value));
                isChanged = true;
            }
        }
        return returnObject;
    }

    @Override
    public String removeProperty(String key) {
        String returnObject = null;
        synchronized (configChanges) {
            returnObject = (String) config.remove(key);
            if (returnObject != null) {
                configChanges.add(new ConfigChange(key, returnObject));
                isChanged = true;
            }
        }
        return returnObject;
    }

    @Override
    public List<ConfigChange> getChanges() {
        return new ArrayList<ConfigChange>(configChanges);
    }

    @Override
    public boolean isChanged() {
        return isChanged;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (watchId != -1) {
            JNotify.removeWatch(watchId);
            watchId = -1;
        }
    }
}