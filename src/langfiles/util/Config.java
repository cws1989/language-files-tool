package langfiles.util;

import java.io.IOException;
import java.util.List;

/**
 * The configuration interface.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public interface Config {

  /**
   * Reload the configuration from config file.
   * @throws IOException IO error when loading config file
   */
  void reload() throws IOException;

  /**
   * Save the configuration file.
   * @throws IOException IO error when saving config file
   */
  void save() throws IOException;

  /**
   * Get the property from config file.
   * @param key the key of the property
   * @return the property value
   */
  String getProperty(String key);

  /**
   * Set the property to config file. Invoke {@link #save} if need to save 
   * changes.
   * 
   * @param key the key
   * @param value the value
   * 
   * @return the previous peoperty value if exist, null if no previous value
   */
  String setProperty(String key, String value);

  /**
   * Get the list of changes before the last save.
   * @return the list of changes
   */
  List<ConfigChange> getChanges();

  /**
   * Remove the property from the config file.
   * @param key the key
   * @return the value before removing, or null if no value exist with that 
   * key before
   */
  String removeProperty(String key);

  /**
   * Get if there is any changes since last save.
   * @return true if there is any changes since last save
   */
  boolean isChanged();
}
