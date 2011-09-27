package langfiles.project;

import javax.swing.JComponent;
import langfiles.util.SyncFile;

/**
 * Code Viewer interface.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public interface CodeViewer {

    /**
     * Set the code that this viewer should show.
     * @param digestedFile the digested file
     */
    void setCode(SyncFile syncFile);

    JComponent getGUI();
}
