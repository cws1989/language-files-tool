package langfiles.project;

/**
 *
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public interface DigestedFileListener {

    void fileCreated(DigestedFile directory, DigestedFile fileCreated, String rootPath, String name);

    void fileDeleted(DigestedFile fileDeleted, String rootPath, String name);

    void fileModified(DigestedFile fileModified, String rootPath, String name);

    void fileRenamed(DigestedFile fileRenamed, String rootPath, String oldName, String newName);
}
