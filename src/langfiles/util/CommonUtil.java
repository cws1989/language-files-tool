package langfiles.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

/**
 * Common utilities/functions.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class CommonUtil {

  private static final Logger LOG = Logger.getLogger(CommonUtil.class.getName());

  protected CommonUtil() {
  }

  /**
   * Check if the input represent a binary file or not.
   * 
   * This is mainly used for distinguish between binary and text file.
   * Currently it check for 3 continuous null (0x00). It should work fine with 
   * up to UTF-16. If the encoding is UTF-32, it should be check for at least 6 
   * continuous null but some small binary file do not have so much continuous 
   * null but 3 to 5 (sure, no proof, no research). UTF-32 worse case (hex): 00 
   * XX 00 00  00 00 00 XX.
   * 
   * @param b the byte array to check
   * @param offset the offset
   * @param length the length
   * 
   * @return true if it is highly likely to be a binary file
   */
  public static boolean isBinaryFile(byte[] b, int offset, int length) {
    int continuousNull = 0;
    for (int i = offset, iEnd = offset + length; i < iEnd; i++) {
      if (b[i] == 0) {
        continuousNull++;
        if (continuousNull >= 3) {
          return true;
        }
      } else {
        if (continuousNull != 0) {
          continuousNull = 0;
        }
      }
    }

    return false;
  }

  /**
   * Bold font.
   * @param font the font
   * @param bold true to bold, false to unbold
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
   * @param bold true to bold, false to unbold
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
      LOG.log(Level.INFO, "Failed to set system look and feel.", ex);
    }
  }

  /**
   * Read the whole file and return the content in byte array.
   * @param file the file to read
   * @return the content of the file in byte array
   * @throws IOException error occurred when reading the content from the file
   */
  public static byte[] readFile(File file) throws IOException {
    if (file == null) {
      throw new NullPointerException("argument 'file' cannot be null");
    }

    long fileLength = file.length();
    byte[] content = new byte[(int) fileLength];

    FileInputStream fin = null;
    try {
      fin = new FileInputStream(file);

      int byteRead = 0, cumulateByteRead = 0;
      while ((byteRead = fin.read(content, cumulateByteRead, content.length - cumulateByteRead)) != -1) {
        cumulateByteRead += byteRead;
        if (cumulateByteRead >= fileLength) {
          break;
        }
      }

      if (cumulateByteRead != fileLength) {
        throw new IOException(String.format("The total number of bytes read does not match the file size. Actual file size: %1$d, bytes read: %2$d, path: %3$s",
                fileLength, cumulateByteRead, file.getAbsolutePath()));
      }
    } finally {
      closeQuietly(fin);
    }

    return content;
  }

  /**
   * Close the stream quietly without throwing any IO exception.
   * @param closeable the stream to close, accept null
   */
  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException ex) {
      }
    }
  }

  /**
   * If the file is a directory, return the directory path; if the file is not 
   * a directory, return the directory path that contain the file.
   * 
   * @param file the file
   * 
   * @return the file parent path
   */
  public static String getFileDirectory(File file) {
    if (file == null) {
      throw new NullPointerException("argument 'file' cannot be null");
    }
    if (file.isDirectory()) {
      return file.getAbsolutePath();
    } else {
      String filePath = file.getAbsolutePath();
      int pos = filePath.replace(File.separator, "/").lastIndexOf('/');
      return pos != -1 ? filePath.substring(0, pos) : filePath;
    }
  }

  /**
   * Return the file file extension of the filePath. Directory part and file 
   * name part will be removed.
   * <p>
   * e.g. C:\Program Files\Language Files Tool\test.txt -> txt,<br />
   * e.g. C:/ProgramFiles/LanguageFilesTool/test.txt -> txt,<br />
   * e.g. test.temp.php -> temp.php
   * </p>
   * @param filePath the file path
   * @return the file extension
   */
  public static String getFileExtension(String filePath) {
    String fileName = removeFileDirectory(filePath);

    int pos = fileName.indexOf('.');
    if (pos != -1) {
      return fileName.substring(pos + 1);
    }

    return fileName;
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
    String fileName = filePath.replace((CharSequence) "\\", (CharSequence) "/");

    int pos = fileName.lastIndexOf('/');
    if (pos != -1) {
      return fileName.substring(pos + 1);
    }

    return fileName;
  }
  /**
   * The graphics to be used to get the FontMetrics, see 
   * {@link {#getFontMetrics(java.awt.Font)}. Not lazy initialization.
   */
  protected final static Graphics graphicsForFontMetrics = (new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)).getGraphics();

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
   * Invoke {@link #mkdir(String)} recursively on the list until receive true.
   * @param directoryPathList the directory path list
   * @return the first succeed directory path, null if any failed
   */
  public static String mkdir(List<String> directoryPathList) {
    String succeedDirectoryPath = null;
    for (String directoryPath : directoryPathList) {
      File directory = new File(directoryPath);
      if (directory.isDirectory() || new File(directoryPath).mkdirs()) {
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
    Dimension screenSize = toolkit.getScreenSize();
    Dimension windowSize = window.getSize();
    int Xpos = (screenSize.width - windowSize.width) / 2;
    int Ypos = (screenSize.height - windowSize.height) / 2;
    window.setBounds(Xpos, Ypos, windowSize.width, windowSize.height);
  }

  /**
   * Set tooltip dismiss delay to delayTimeInMilli temporarily when mouseover 
   * the component, reset to default after mouseout.
   * 
   * @param component the component to set tooltip dismiss delay on
   * @param delayTimeInMilli the delay time in milli second
   * 
   * @return the MouseListener that added to the component
   */
  public static MouseListener setTooltipDismissDelay(Component component, final int delayTimeInMilli) {
    MouseListener mouseListener = new MouseAdapter() {

      private int defaultDismissDelay;

      @Override
      public void mouseEntered(MouseEvent me) {
        defaultDismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
        ToolTipManager.sharedInstance().setDismissDelay(delayTimeInMilli);
      }

      @Override
      public void mouseExited(MouseEvent me) {
        ToolTipManager.sharedInstance().setDismissDelay(defaultDismissDelay);
      }
    };
    component.addMouseListener(mouseListener);
    return mouseListener;
  }

  /**
   * Set undo (Ctrl + Z) and redo (Ctrl + Y) function on the component.
   * @param component the text component
   * @return the KeyListener that added to the component
   */
  public static KeyListener setUndoManager(final JTextComponent component) {
    final UndoManager undoManager = new UndoManager();
    KeyListener listener = new KeyAdapter() {

      private boolean initialized = false;
      private boolean canUndo;
      private boolean canRedo;

      @Override
      public void keyPressed(KeyEvent e) {
        if (!e.getSource().equals(component) || e.getModifiers() != KeyEvent.CTRL_MASK) {
          return;
        }

        // no synchronization needed
        if (!initialized) {
          canUndo = undoManager.canUndo();
          canRedo = undoManager.canRedo();
          initialized = true;
        }

        int keyCode = e.getKeyCode();
        switch (keyCode) {
          case KeyEvent.VK_Z:
            if (canUndo) {
              undoManager.undo();
            }
            break;
          case KeyEvent.VK_Y:
            if (canRedo) {
              undoManager.redo();
            }
            break;
        }
      }
    };
    component.getDocument().addUndoableEditListener(undoManager);
    component.addKeyListener(listener);
    return listener;
  }
}
