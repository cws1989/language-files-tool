package langfiles.util;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 * Common utilities/functions.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class CommonUtil {

    private CommonUtil() {
    }

    /**
     * Bold font.
     * @param font the font
     * @param bold true to bold, false unbold
     * @return the bolded/unbolded font
     */
    public static Font boldFont(Font font, boolean bold) {
        if ((font.getStyle() & Font.BOLD) != 0) {
            if (!bold) {
                return font.deriveFont(font.getStyle() ^ Font.BOLD);
            }
        } else {
            if (bold) {
                return font.deriveFont(font.getStyle() | Font.BOLD);
            }
        }
        return font;
    }

    /**
     * Change font size.
     * @param font the font
     * @param fontSize the font size to change to
     * @return the font that changed size
     */
    public static Font changeFontSize(Font font, int fontSize) {
        return font.deriveFont((float) fontSize);
    }

    /**
     * Change font size and bold/unbold font
     * @param font the font
     * @param bold true to bold, false unbold
     * @param fontSize the font size to change to
     * @return the changed font
     */
    public static Font deriveFont(Font font, boolean bold, int fontSize) {
        return changeFontSize(boldFont(font, bold), fontSize);
    }

    /**
     * Set UI look & feel to system look & feel.
     */
    public static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            Logger.getLogger(CommonUtil.class.getName()).log(Level.INFO, "Failed to set system look and feel.", ex);
        }
    }

    /**
     * Read the whole file and return the data as string.
     * @param file the file to read
     * @return the data in the file as string, null if the file is not valid
     * @throws IOException error occurred when reading the file
     */
    public static String readFile(File file) throws IOException {
        if (!file.isFile() || !file.exists()) {
            return null;
        }
        byte[] buffer = new byte[(int) file.length()];
        try {
            FileInputStream fileIn = new FileInputStream(file);
            fileIn.read(buffer);
            fileIn.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new String(buffer);
    }

    /**
     * Get the files recursively from the directory and filter the result with specified allowed extensions.
     * @param directory the directory to loop through
     * @param allowedExtensions allowed file extensions in list form; if it is empty, that means allow all file extensions
     * @return the files that match the requirements
     */
    public static List<File> getFiles(File directory, List<String> allowedExtensions) {
        int extensionsLength = allowedExtensions.size();
        for (int i = 0; i < extensionsLength; i++) {
            ListIterator<String> iterator = allowedExtensions.listIterator();
            while (iterator.hasNext()) {
                String ext = iterator.next();
                if (ext.charAt(0) == '.') {
                    iterator.set(ext.substring(1));
                }
            }
        }

        List<File> returnList = new ArrayList<File>();

        List<File> tempList = getFiles(directory);
        for (File file : tempList) {
            String fileExtension = getFileExtension(file.getName());
            if (allowedExtensions.isEmpty() || allowedExtensions.indexOf(fileExtension) != -1) {
                returnList.add(file);
            }
        }

        return returnList;
    }

    /**
     * Private use for {@link #getFiles(java.io.File, java.util.List)}.
     * @param directory the directory to get files from
     * @return a list of all the files with that directory recursively
     */
    private static List<File> getFiles(File directory) {
        List<File> returnList = new ArrayList<File>();
        getFilesRecusive(returnList, directory);
        return returnList;
    }

    /**
     * Private use for {@link #getFiles(java.io.File)}.
     * @param existingList a list of existing files, for redundancy/loop checking
     * @param directory the directory to get files from
     */
    private static void getFilesRecusive(List<File> existingList, File directory) {
        if (!directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            if (existingList.contains(file)) {
                continue;
            }

            if (file.isDirectory()) {
                getFilesRecusive(existingList, file);
            } else {
                existingList.add(file);
            }
        }
    }

    /**
     * Return the file file extension of the filePath. Directory part and file name part will be removed.
     * <p>
     * e.g. C:\Program Files\Language Files Tool\test.txt -> txt,<br />
     * e.g. C:/ProgramFiles/LanguageFilesTool/test.txt -> txt,<br />
     * e.g. test.temp.php -> temp.php
     * </p>
     * @param filePath the file path
     * @return the file extension
     */
    public static String getFileExtension(String filePath) {
        filePath = removeFileDirectory(filePath);

        int pos = filePath.indexOf('.');
        if (pos != -1) {
            return filePath.substring(pos + 1);
        }

        return filePath;
    }

    /**
     * Return the file name of the filePath. Directory part and file extension will be removed.
     * <p>
     * e.g. C:\Program Files\Language Files Tool\test.txt -> test,<br />
     * e.g. C:/ProgramFiles/LanguageFilesTool/test.txt -> test,<br />
     * e.g. test.temp.php -> temp
     * </p>
     * @param filePath the file path
     * @return the file name
     */
    public static String getFileName(String filePath) {
        filePath = removeFileDirectory(filePath);

        int pos = filePath.indexOf('.');
        if (pos != -1) {
            return filePath.substring(0, pos);
        }

        return filePath;
    }

    /**
     * Remove the directory part of the filePath if exist.
     * <p>
     * e.g. C:\Program Files\Language Files Tool\test.txt -> test.txt,<br />
     * e.g. C:/ProgramFiles/LanguageFilesTool/test.txt -> test.txt
     * </p>
     * @param filePath the file path
     * @return the file name
     */
    private static String removeFileDirectory(String filePath) {
        filePath = filePath.replace((CharSequence) "\\", (CharSequence) "/");

        int pos = filePath.lastIndexOf('/');
        if (pos != -1) {
            return filePath.substring(pos + 1);
        }

        return filePath;
    }
    /**
     * The graphics to be used to get the FontMetrics, see {@link {#getFontMetrics(java.awt.Font)}.
     */
    private final static Graphics graphicsForFontMetrics = (new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)).getGraphics();

    /**
     * Get the FontMetrics for the font.
     * @param font the Font
     * @return the FontMetrics
     */
    public static FontMetrics getFontMetrics(Font font) {
        FontMetrics fontMetrics = null;
        synchronized (graphicsForFontMetrics) {
            fontMetrics = graphicsForFontMetrics.getFontMetrics(font);
        }
        return fontMetrics;
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
     * Invoke {@link #mkdir(String)} recursively on the list until receive true.
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
