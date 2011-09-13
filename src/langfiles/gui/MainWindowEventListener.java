package langfiles.gui;

import javax.swing.event.ChangeEvent;

/**
 * Main window event listener.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public interface MainWindowEventListener {

    /**
     * Check if the listener can close now. If return false, the main window will stop closing.
     * @return true if the listener can close now, false if not
     */
    boolean windowCanCloseNow(ChangeEvent event);

    /**
     * Notify the event that the main window is closing.
     */
    void windowIsClosing(ChangeEvent event);
}
