package langfiles.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import langfiles.util.SortedArrayList;
import langfiles.util.SyncFile;

/**
 * The project handler.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class Project implements Comparable<Object> {

    /**
     * The project name.
     */
    protected String projectName;
    /**
     * The list of allowed file extensions.
     */
    private final List<String> allowedExtensionList;
    /**
     * The list of disallowed file extensions.
     */
    private final List<String> disallowedExtensionList;
    /**
     * The list of ignored files.
     */
    private final List<String> ignoreFileList;
    /**
     * The list of syncFiles. It is a sorted ArrayList.
     */
    private final List<SyncFile> syncFileList;
    private final List<ProjectFileListener> projectFileListenerList;

    /**
     * Constructor.
     */
    public Project(String projectName) {
        this.projectName = projectName;
        allowedExtensionList = Collections.synchronizedList(new ArrayList<String>());
        disallowedExtensionList = Collections.synchronizedList(new ArrayList<String>());
        ignoreFileList = Collections.synchronizedList(new ArrayList<String>());
        syncFileList = Collections.synchronizedList(new SortedArrayList<SyncFile>());
        projectFileListenerList = Collections.synchronizedList(new ArrayList<ProjectFileListener>());
    }

    /**
     * Get the name of the project.
     * @return the project name
     */
    public String getName() {
        return projectName;
    }

    public List<SyncFile> addFileListener(ProjectFileListener listener) {
        synchronized (syncFileList) {
            synchronized (projectFileListenerList) {
                projectFileListenerList.add(listener);
                return getSyncFileList();
            }
        }
    }

    public void removeFileListener(ProjectFileListener listener) {
        projectFileListenerList.remove(listener);
    }

    /**
     * Get the list of allowed file extensions.
     * @return the allowed file extension list
     */
    public List<String> getAllowedExtensions() {
        List<String> returnList = null;
        synchronized (allowedExtensionList) {
            returnList = new ArrayList<String>(allowedExtensionList);
        }
        return returnList;
    }

    /**
     * Set the list of allowed file extensions.
     * @param extensionList the allowed file extension list
     */
    public void setAllowedExtensions(List<String> extensionList) {
        synchronized (syncFileList) {
            synchronized (allowedExtensionList) {
                // remove the '.' at the beginning of the extension
                ListIterator<String> iterator = extensionList.listIterator();
                while (iterator.hasNext()) {
                    String extension = iterator.next();
                    if (extension.charAt(0) == '.') {
                        iterator.set(extension.substring(1));
                    }
                }

                allowedExtensionList.clear();
                allowedExtensionList.addAll(extensionList);
            }

            // remove files with extension not within the allowed extension list and add back those within the allowed extension list
            for (SyncFile syncFile : syncFileList) {
                syncFile.setAllowedFileExtensionList(extensionList, true);
            }
        }
    }

    /**
     * Get the list of disallowed file extensions.
     * @return the allowed file extension list
     */
    public List<String> getDisallowedExtensions() {
        List<String> returnList = null;
        synchronized (disallowedExtensionList) {
            returnList = new ArrayList<String>(disallowedExtensionList);
        }
        return returnList;
    }

    /**
     * Set the list of disallowed file extensions.
     * @param extensionList the disallowed file extension list
     */
    public void setDisallowedExtensions(List<String> extensionList) {
        synchronized (syncFileList) {
            synchronized (disallowedExtensionList) {
                // remove the '.' at the beginning of the extension
                ListIterator<String> iterator = extensionList.listIterator();
                while (iterator.hasNext()) {
                    String extension = iterator.next();
                    if (extension.charAt(0) == '.') {
                        iterator.set(extension.substring(1));
                    }
                }

                disallowedExtensionList.clear();
                disallowedExtensionList.addAll(extensionList);
            }

            for (SyncFile syncFile : syncFileList) {
                syncFile.setDisallowedFileExtensionList(extensionList, true);
            }
        }
    }

    /**
     * Get ignore file list.
     * @return the list
     */
    public List<String> getIgnoreFileList() {
        List<String> returnList;
        synchronized (ignoreFileList) {
            returnList = new ArrayList<String>(ignoreFileList);
        }
        return returnList;
    }

    /**
     * Add file to ignore file list.
     * @param file the file to ignore
     */
    public void addIgnoreFile(List<File> files) {
        synchronized (syncFileList) {
            synchronized (ignoreFileList) {
                for (File file : files) {
                    if (ignoreFileList.indexOf(file.getAbsolutePath()) == -1) {
                        ignoreFileList.add(file.getAbsolutePath());
                    }
                }
            }

            for (SyncFile syncFile : syncFileList) {
                syncFile.setIgnoreFileList(ignoreFileList, true);
            }
        }
    }

    /**
     * Remove the file from the ignore file list.
     * @param file the file to remove from ignore file list
     */
    public void removeIgnoreFile(File file) {
        synchronized (syncFileList) {
            boolean itemExistBeforeRemove = ignoreFileList.remove(file.getAbsolutePath());

            if (itemExistBeforeRemove) {
                for (SyncFile syncFile : syncFileList) {
                    syncFile.setIgnoreFileList(ignoreFileList, true);
                }
            }
        }
    }

    /**
     * Get the list of SyncFiles.
     * @return the list of SyncFiles
     */
    public List<SyncFile> getSyncFileList() {
        List<SyncFile> returnList = new ArrayList<SyncFile>();
        synchronized (syncFileList) {
            returnList = new ArrayList<SyncFile>(syncFileList);
        }
        return returnList;
    }

    /**
     * Add file/folder and all files inside recursively to the project.
     * @param file the file/folder
     * @return 
     */
    public boolean add(File file) {
        synchronized (syncFileList) {
            // compare to existing file list (root level only) to check duplication
            for (SyncFile syncFile : syncFileList) {
                if (syncFile.getAbsolutePath().equals(file.getAbsolutePath())) {
                    return false;
                }
            }

            SyncFile syncFile = null;
            try {
                syncFile = new SyncFile(null, file);
                syncFile.setInheritUserObject("project", this);
                syncFile.addWatch();
                syncFile.setAllowedFileExtensionList(allowedExtensionList, false);
                syncFile.setDisallowedFileExtensionList(disallowedExtensionList, false);
                syncFile.setIgnoreFileList(ignoreFileList, true);
            } catch (IOException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            // syncFile should not be null here

            syncFileList.add(syncFile);

            synchronized (projectFileListenerList) {
                for (ProjectFileListener listener : projectFileListenerList) {
                    listener.projectFileAdded(syncFile);
                }
            }

            return true;
        }
    }

    public void remove(SyncFile syncFile) {
        synchronized (syncFileList) {
            syncFileList.remove(syncFile);
            synchronized (projectFileListenerList) {
                for (ProjectFileListener listener : projectFileListenerList) {
                    listener.projectFileRemoved(syncFile);
                }
            }
        }
    }

    public SyncFile getSyncFileByAbsolutePath(String path) {
        String absolutePath = new File(path).getAbsolutePath();

        SyncFile returnSyncFile = null;
        synchronized (syncFileList) {
            for (SyncFile _syncFile : syncFileList) {
                String replacedString = absolutePath.replace(_syncFile.getAbsolutePath(), "");
                if (replacedString.isEmpty()) {
                    return _syncFile;
                }
                replacedString = replacedString.substring(1);
                if ((returnSyncFile = _syncFile.findSyncFile(replacedString)) != null) {
                    return returnSyncFile;
                }
            }
        }

        return returnSyncFile;
    }

    /**
     * Commit the changes.
     */
    public void commit() {
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Project) {
            return getName().compareTo(((Project) o).getName());
        } else {
            throw new ClassCastException();
        }
    }
}