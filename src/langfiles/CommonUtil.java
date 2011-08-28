package langfiles;

import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Common utilities/functions.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class CommonUtil {

    private CommonUtil() {
    }

    /**
     * Check if the directory path exist and is a directory or not.
     * If the directory not exist, this will attempt to create it. 
     * @param directoryPath the directory path that want to check
     * @return true if the path exist and is a directory, false if not
     */
    public static boolean mkdir(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            try {
                directory.mkdir();
            } catch (SecurityException ex) {
                Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return directory.isDirectory();
    }

    /**
     * Invoke {@see #mkdir(String)} recursively on the list until receive true.
     * @param directoryPathList the directory path list
     * @return the first succeed directory path, null if all failed
     */
    public static String mkdir(List<String> directoryPathList) {
        String succeedDirectoryPath = null;
        for (String directoryPath : directoryPathList) {
            if (mkdir(directoryPath)) {
                succeedDirectoryPath = directoryPath;
                break;
            }
        }
        return succeedDirectoryPath;
    }

    /**
     * Position the window to the center of the screen.
     * @param window the JFrame or JDialog
     */
    public static void centerWindow(Window window) {
        Toolkit toolkit = window.getToolkit();
        int Xpos = (toolkit.getScreenSize().width - window.getSize().width) / 2;
        int Ypos = (toolkit.getScreenSize().height - window.getSize().height) / 2;
        window.setBounds(Xpos, Ypos, window.getSize().width, window.getSize().height);
    }
}
