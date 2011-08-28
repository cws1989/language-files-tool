package langfiles;

/**
 * Listen to program event.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public interface ProgramEventListener {

    /**
     * Check if the listener can close now. If return false, the program will stop closing.
     * @return true if the listener can close now, false if not
     */
    boolean programCanCloseNow();

    /**
     * Notify the event that the program is closing.
     */
    void programIsClosing();
}
