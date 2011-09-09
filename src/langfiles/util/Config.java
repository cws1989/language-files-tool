package langfiles.util;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author cws1989
 */
public interface Config {

    void reload() throws IOException;

    void save() throws IOException;

    String getProperty(String key);

    Object setProperty(String key, String value);

    List<ConfigChange> getChanges();

    boolean isChanged();

    public static class ConfigChange {

        private String key;
        private String value;

        public ConfigChange(String key, String value) {
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
}
