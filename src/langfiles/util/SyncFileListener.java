package langfiles.util;

/**
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public interface SyncFileListener {

  void fileCreated(SyncFile directory, SyncFile fileCreated, String rootPath, String name);

  void fileDeleted(SyncFile fileDeleted, String rootPath, String name);

  void fileModified(SyncFile fileModified, String rootPath, String name);

  void fileRenamed(SyncFile fileRenamed, String rootPath, String oldName, String newName);
}
