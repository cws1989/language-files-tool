package langfiles.project;

import langfiles.util.SyncFile;

/**
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public interface ProjectFileListener {

    void projectFileAdded(SyncFile syncFile);

    void projectFileRemoved(SyncFile syncFile);
}
