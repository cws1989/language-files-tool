package langfiles.project;

/**
 * Code Viewer interface.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public interface CodeViewer {

    /**
     * Set the code that this viewer should show.
     * @param digestedFile the digested file
     */
    void setCode(DigestedFile digestedFile);
}
