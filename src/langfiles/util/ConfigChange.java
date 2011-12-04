package langfiles.util;

/**
 * Config change.
 */
public class ConfigChange {

  protected String key;
  protected String value;

  public ConfigChange(String key, String value) {
    if (key == null) {
      throw new NullPointerException("argument 'key' cannot be null");
    }
    if (value == null) {
      throw new NullPointerException("argument 'value' cannot be null");
    }
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
}