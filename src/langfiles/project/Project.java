package langfiles.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import langfiles.util.CommonUtil;
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
     * The list of digested data/files. It is a sorted ArrayList.
     */
    private final List<SyncFile> syncFileList;
    private final Map<SyncFile, Map<String, SyncFile>> digestedDataFileList;
    /**
     * Exclusive use for {@link #revalidateFiles()}.
     */
    private boolean revalidatingFiles;
    private final Object revalidateFilesLock = new Object();

    /**
     * Constructor.
     */
    public Project(String projectName) {
        this.projectName = projectName;
        allowedExtensionList = Collections.synchronizedList(new ArrayList<String>());
        disallowedExtensionList = Collections.synchronizedList(new ArrayList<String>());
        ignoreFileList = Collections.synchronizedList(new ArrayList<String>());
        syncFileList = Collections.synchronizedList(new SortedArrayList<SyncFile>());
        digestedDataFileList = Collections.synchronizedMap(new HashMap<SyncFile, Map<String, SyncFile>>());
        revalidatingFiles = false;
    }

    /**
     * Get the name of the project.
     * @return the project name
     */
    public String getName() {
        return projectName;
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
        synchronized (syncFileList) {
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

        // remove files with extension within the allowed extension list and add back those not within the allowed extension list
        synchronized (syncFileList) {
            for (SyncFile syncFile : syncFileList) {
                syncFile.setDisallowedFileExtensionList(extensionList, true);
            }
        }
    }

    /**
     * Add file to ignore file list.
     * @param file the file to ignore
     */
    public void addIgnoreFile(File file) {
        synchronized (ignoreFileList) {
            if (ignoreFileList.indexOf(file.getAbsolutePath()) == -1) {
                ignoreFileList.add(file.getAbsolutePath());
            }
        }
    }

    /**
     * Remove the file from the ignore file list.
     * @param file the file to remove from ignore file list
     */
    public void removeIgnoreFile(File file) {
        ignoreFileList.remove(file.getAbsolutePath());
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
     * Get the list of digested data/files.
     * @return the list of digested data/files
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
     */
    public void add(File file) {
        synchronized (syncFileList) {
            // compare to existing file list (root level only) to check duplication
            boolean existAlready = false;
            for (SyncFile syncFile : syncFileList) {
                if (syncFile.getFile().getAbsolutePath().equals(file.getAbsolutePath())) {
                    existAlready = true;
                    break;
                }
            }
            if (existAlready) {
                return;
            }

            try {
                SyncFile syncFile = new SyncFile(null, file);
                syncFile.setInheritUserObject("project", this);
                syncFile.addWatch();
                syncFile.setAllowedFileExtensionList(allowedExtensionList, false);
                syncFile.setDisallowedFileExtensionList(disallowedExtensionList, false);
                syncFile.setIgnoreFileList(ignoreFileList, true);
                syncFileList.add(syncFile);
                digestedDataFileList.put(syncFile, getFileList(syncFile));
            } catch (IOException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void remove(SyncFile syncFile) {
        synchronized (syncFileList) {
            syncFileList.remove(syncFile);
            digestedDataFileList.remove(syncFile);
        }
    }

    protected Map<String, SyncFile> getFileList(SyncFile syncFile) {
        Map<String, SyncFile> returnMap = new HashMap<String, SyncFile>();

        if (syncFile.isDirectory()) {
            returnMap.put(syncFile.getFile().getAbsolutePath(), syncFile);
            List<SyncFile> _fileList = syncFile.getChildSyncFileList();
            for (SyncFile _syncFile : _fileList) {
                returnMap.putAll(getFileList(_syncFile));
            }
        } else {
            returnMap.put(syncFile.getFile().getAbsolutePath(), syncFile);
        }

        return returnMap;
    }

    /**
     * Check whether the directory fufil those filters.
     * @param file the directory to check
     * @return true if fufil, false if not
     */
    public boolean isDirectoryFufilFilter(File file) {
        return !(ignoreFileList.indexOf(file.getAbsolutePath()) != -1);
    }

    /**
     * Check whether the file fufil those filters.
     * @param file the file to check
     * @return true if fufil, false if not
     */
    public boolean isFileFufilFilter(File file) {
        return !((!allowedExtensionList.isEmpty() && allowedExtensionList.indexOf(CommonUtil.getFileExtension(file.getName())) == -1)
                || (!disallowedExtensionList.isEmpty() && disallowedExtensionList.indexOf(CommonUtil.getFileExtension(file.getName())) != -1)
                || ignoreFileList.indexOf(file.getAbsolutePath()) != -1);
    }

    public SyncFile getSyncFileByAbsolutePath(String path) {
        String absolutePath = new File(path).getAbsolutePath();

        SyncFile returnSyncFile = null;
        synchronized (digestedDataFileList) {
            for (Map<String, SyncFile> fileMap : digestedDataFileList.values()) {
                if ((returnSyncFile = fileMap.get(absolutePath)) != null) {
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