package langfiles.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyAdapter;
import net.contentobjects.jnotify.JNotifyException;

/**
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class SyncFile implements Comparable<Object> {

    protected static final String fileSeperator = System.getProperty("file.separator");
    /**
     * The parent SyncFile of this SyncFile.
     */
    private SyncFile parent;
    /**
     * Information about the {@link java.io.File} of this SyncFile.
     */
    protected File file;
    protected boolean fileIsDirectory;
    protected String fileAbsolutePath;
    protected String fileName;
    protected long fileLastModified;
    /**
     * The watch id of this file. If this is a file instead a directory, it will listen to the directory that contain the file.
     */
    protected int watchId;
    //
    protected List<File> childFileList;
    protected List<SyncFile> childSyncFileList;
    protected Map<String, SyncFile> childSyncFileMap;
    /**
     * Conditions that determine whether the child file has a SyncFile or not.
     */
    protected List<String> allowedFileExtensionList;
    protected List<String> disallowedFileExtensionList;
    protected List<String> ignoreFileList;
    /**
     * User defined objects.
     */
    protected final Map<String, Object> userObjectList;
    /**
     * User defined objects. This will be inherited when creating child.
     */
    protected final Map<String, Object> inheritUserObjectList;
    /**
     * SyncFileListener list.
     */
    protected final List<SyncFileListener> listenerList;
    /**
     * Mutex lock.
     */
    protected final Object syncFileLock = new Object();
    //
    private boolean debugMode = false;

    public SyncFile(SyncFile parent, File file) throws IOException {
        this(parent, file, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), true);
    }

    protected SyncFile(SyncFile parent, File file, List<String> allowedFileExtensionList, List<String> disallowedFileExtensionList, List<String> ignoreFileList, boolean checkAndCreateChildren) throws IOException {
        this.parent = parent;

        watchId = -1;

        childFileList = new ArrayList<File>();
        childSyncFileList = new ArrayList<SyncFile>();
        childSyncFileMap = new HashMap<String, SyncFile>();

        this.allowedFileExtensionList = allowedFileExtensionList;
        this.disallowedFileExtensionList = disallowedFileExtensionList;
        this.ignoreFileList = ignoreFileList;

        userObjectList = Collections.synchronizedMap(new HashMap<String, Object>());
        inheritUserObjectList = Collections.synchronizedMap(new HashMap<String, Object>());
        listenerList = Collections.synchronizedList(new ArrayList<SyncFileListener>());

        setFile(file);
        if (checkAndCreateChildren) {
            updateChildFileList();
            updateChildSyncFileList(true);
        }
    }

    protected void setFile(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException();
        }
        synchronized (syncFileLock) {
            this.file = file;
            fileIsDirectory = file.isDirectory();
            fileAbsolutePath = file.getAbsolutePath();
            fileName = file.getName();
            fileLastModified = file.lastModified();
        }
    }

    protected void updateChildFileList() {
        if (!isDirectory()) {
            return;
        }
        synchronized (syncFileLock) {
            childFileList.clear();

            File[] _files = file.listFiles();
            if (_files != null) {
                childFileList.addAll(Arrays.asList(_files));
            }
        }
    }

    /**
     * childFileList is assumed to be updated
     */
    protected void updateChildSyncFileList(boolean updateChild) {
        if (!isDirectory()) {
            return;
        }
        synchronized (syncFileLock) {
            List<File> _childFileList = new ArrayList<File>(childFileList);
            SyncFile[] _childSyncFileList = childSyncFileList.toArray(new SyncFile[childSyncFileList.size()]);

            for (SyncFile _childSyncFile : _childSyncFileList) {
                // remove _childSyncFile's File from _childFileList
                boolean syncFileExistInChildFileList = false;
                Iterator<File> iterator2 = _childFileList.iterator();
                while (iterator2.hasNext()) {
                    File _file = iterator2.next();
                    if (_file.getAbsolutePath().equals(_childSyncFile.getAbsolutePath())) {
                        syncFileExistInChildFileList = true;
                        iterator2.remove();
                        break;
                    }
                }

                if (!syncFileExistInChildFileList || !isFileFufilFilter(_childSyncFile.getFile())) {
                    // SyncFile not exist in _childFileList or not fufil filter
                    _childSyncFile.fireDeleteEvent(_childSyncFile.getAbsolutePath(), "", true, true);
                } else {
                    // SyncFile exist in _childFileList
                    // if it is a directory, validate its child SyncFile list
                    if (_childSyncFile.isDirectory() && updateChild) {
                        _childSyncFile.updateChildSyncFileList(updateChild);
                    }
                }
            }

            // check the remaining File in _childFileList that do not have a SyncFile coresponding to it yet
            for (File _file : _childFileList) {
                if (_file.isHidden()) {
                    continue;
                }
                // if File fufil the filter, add it to SyncFile list
                if (isFileFufilFilter(_file)) {
                    fireCreateEvent(_file.getAbsolutePath(), "", true);
                }
            }
        }
    }

    /**
     * Rename a file that do not have a SyncFile relate to it.
     * update childFileList
     */
    protected void renameFile(File oldFile, File newFile) {
        synchronized (syncFileLock) {
            String oldFileAbsolutePath = oldFile.getAbsolutePath();

            ListIterator<File> iterator = childFileList.listIterator();
            while (iterator.hasNext()) {
                File _file = iterator.next();
                if (_file.getAbsolutePath().equals(oldFileAbsolutePath)) {
                    iterator.set(newFile);
                    break;
                }
            }
        }
    }

    /**
     * Rename a file that have a SyncFile related to it.
     * update childFileList, childSyncFileList, childSyncFileMap
     */
    protected void renameFile(File oldFile, File newFile, SyncFile child) {
        synchronized (syncFileLock) {
            String oldFileAbsPath = oldFile.getAbsolutePath();

            ListIterator<File> iterator = childFileList.listIterator();
            while (iterator.hasNext()) {
                File _file = iterator.next();
                if (_file.getAbsolutePath().equals(oldFileAbsPath)) {
                    iterator.set(newFile);
                    break;
                }
            }

            childSyncFileMap.remove(oldFile.getName());
            childSyncFileMap.put(newFile.getName(), child);
        }
    }

    /**
     * update childFileList, childSyncFileList, childSyncFileMap
     */
    protected void removeFile(final SyncFile child, boolean deleteSyncFileOnly) {
        synchronized (syncFileLock) {
            if (!deleteSyncFileOnly) {
                Iterator<File> iterator = childFileList.iterator();
                while (iterator.hasNext()) {
                    File _file = iterator.next();
                    if (_file.getAbsolutePath().equals(child.getAbsolutePath())) {
                        iterator.remove();
                        break;
                    }
                }
            }
            childSyncFileList.remove(child);
            childSyncFileMap.remove(child.getFileName());
        }
    }

    public void refresh() {
        if (!isDirectory()) {
            return;
        }
        synchronized (syncFileLock) {
            updateChildFileList();
            updateChildSyncFileList(false);

            SyncFile[] _childSyncFileList = childSyncFileList.toArray(new SyncFile[childSyncFileList.size()]);
            for (SyncFile _syncFile : _childSyncFileList) {
                _syncFile.refresh();
            }
        }
    }

    /**
     * @param path should remove the first part and the first file separator
     */
    public SyncFile findSyncFile(String path) {
        if (path.isEmpty()) {
            return this;
        }

        int pos = path.indexOf(fileSeperator);
        String _fileName = pos != -1 ? path.substring(0, pos) : path;

        SyncFile target = childSyncFileMap.get(_fileName);
        if (target == null) {
            return null;
        }

        return pos == -1 ? target : target.findSyncFile(path.substring(pos + 1));
    }

    public void addWatch() throws JNotifyException {
        synchronized (syncFileLock) {
            removeWatch();

            String watchPath = isDirectory() ? getAbsolutePath() : CommonUtil.getFileDirectory(file);
            watchId = JNotify.addWatch(watchPath, JNotify.FILE_ANY, true, new JNotifyAdapter() {

                @Override
                public void fileCreated(int watchId, String rootPath, String name) {
                    if (debugMode) {
                        System.out.println("c: " + rootPath + "/" + name);
                    }

                    if (!isDirectory()) {
                        return;
                    }

                    File file = new File(rootPath + "/" + name);
                    if (!file.exists()) {
                        // should not reach here
                        return;
                    }

                    synchronized (syncFileLock) {
                        for (File _file : childFileList) {
                            if (_file.getAbsolutePath().equals(file.getAbsolutePath())) {
                                return;
                            }
                        }

                        String replacePath = file.getParentFile().getAbsolutePath().replace(getAbsolutePath(), "");
                        if (!replacePath.isEmpty()) {
                            replacePath = replacePath.substring(1);
                        }

                        SyncFile parentOfnewSyncFile = findSyncFile(replacePath);
                        if (parentOfnewSyncFile == null) {
                            return;
                        }
                        parentOfnewSyncFile.childFileList.add(file);
                        if (!file.isHidden()) {
                            parentOfnewSyncFile.fireCreateEvent(rootPath, name, false);
                        }
                    }

                    if (debugMode) {
                        System.out.println(SyncFile.this);
                    }
                }

                @Override
                public void fileDeleted(int watchId, String rootPath, String name) {
                    if (debugMode) {
                        System.out.println("d: " + rootPath + "/" + name);
                    }

                    File file = new File(rootPath + "/" + name);

                    synchronized (syncFileLock) {
                        SyncFile fileDeleted = findSyncFile(file.getAbsolutePath().replace(getAbsolutePath(), "").substring(1));
                        if (fileDeleted == null) {
                            return;
                        }
                        fileDeleted.fireDeleteEvent(rootPath, name, true, false);
                    }

                    if (debugMode) {
                        System.out.println(SyncFile.this);
                    }
                }

                @Override
                public void fileModified(int watchId, String rootPath, String name) {
                    if (debugMode) {
                        System.out.println("m: " + rootPath + "/" + name);
                    }

                    File file = new File(rootPath + "/" + name);
                    if (!file.exists()) {
                        // should not reach here
                        return;
                    }

                    synchronized (syncFileLock) {
                        SyncFile fileModified = findSyncFile(file.getAbsolutePath().replace(getAbsolutePath(), "").substring(1));
                        if (fileModified == null) {
                            return;
                        }
                        fileModified.fireModifyEvent(rootPath, name);
                    }

                    if (debugMode) {
                        System.out.println(SyncFile.this);
                    }
                }

                @Override
                public void fileRenamed(int watchId, String rootPath, String oldName, String newName) {
                    if (debugMode) {
                        System.out.println("r: " + rootPath + "/" + oldName + " > " + newName);
                    }

                    File oldFile = new File(rootPath + "/" + oldName);
                    File newFile = new File(rootPath + "/" + newName);
                    if (!newFile.exists()) {
                        // should not reach here
                        return;
                    }

                    synchronized (syncFileLock) {
                        String oldFileRelativePath = oldFile.getAbsolutePath().replace(getAbsolutePath(), "").substring(1);

                        SyncFile fileRenamed = findSyncFile(oldFileRelativePath);
                        if (fileRenamed != null) {
                            fileRenamed.fireRenameEvent(rootPath, oldName, newName);
                        }

                        int pos = oldFileRelativePath.lastIndexOf(fileSeperator);
                        String parentPath = pos != -1 ? oldFileRelativePath.substring(0, pos) : "";
                        SyncFile fileRenamedParent = findSyncFile(parentPath);
                        if (fileRenamedParent != null) {
                            fileRenamedParent.renameFile(oldFile, newFile);
                            fileRenamedParent.updateChildSyncFileList(false);
                        }
                    }

                    if (debugMode) {
                        System.out.println(SyncFile.this);
                    }
                }
            });
        }
    }

    public void removeWatch() throws JNotifyException {
        synchronized (syncFileLock) {
            if (watchId != -1) {
                JNotify.removeWatch(watchId);
                watchId = -1;
            }
        }
    }

    public FileContent getFileContent() throws IOException, SecurityException {
        if (isDirectory()) {
            return null;
        }
        synchronized (syncFileLock) {
            FileContent returnObject = null;
            while (true) {
                fileLastModified = file.lastModified();
                String _content = CommonUtil.readFile(getFile());
                long _fileLastModified = file.lastModified();
                if (fileLastModified == _fileLastModified) {
                    returnObject = new FileContent(fileLastModified, _content);
                    break;
                }
            }
            return returnObject;
        }
    }

    public List<File> getChildFileList() {
        return new ArrayList<File>(childFileList);
    }

    public List<SyncFile> getChildSyncFileList() {
        return new ArrayList<SyncFile>(childSyncFileList);
    }

    public void setAllowedFileExtensionList(List<String> allowedFileExtensionList, boolean validate) {
        synchronized (syncFileLock) {
            this.allowedFileExtensionList.clear();
            this.allowedFileExtensionList.addAll(allowedFileExtensionList);
            if (validate) {
                updateChildSyncFileList(true);
            }
        }
    }

    public void setDisallowedFileExtensionList(List<String> disallowedFileExtensionList, boolean validate) {
        synchronized (syncFileLock) {
            this.disallowedFileExtensionList.clear();
            this.disallowedFileExtensionList.addAll(disallowedFileExtensionList);
            if (validate) {
                updateChildSyncFileList(true);
            }
        }
    }

    public void setIgnoreFileList(List<String> ignoreFileList, boolean validate) {
        synchronized (syncFileLock) {
            this.ignoreFileList.clear();
            this.ignoreFileList.addAll(ignoreFileList);
            if (validate) {
                updateChildSyncFileList(true);
            }
        }
    }

    protected boolean isFileFufilFilter(File file) {
        synchronized (syncFileLock) {
            try {
                if (ignoreFileList.indexOf(file.getAbsolutePath()) != -1) {
                    return false;
                }
                if (file.isDirectory()) {
                } else {
                    String _fileName = file.getName();
                    if (!allowedFileExtensionList.isEmpty() && allowedFileExtensionList.indexOf(CommonUtil.getFileExtension(_fileName)) == -1) {
                        return false;
                    }
                    if (!disallowedFileExtensionList.isEmpty() && disallowedFileExtensionList.indexOf(CommonUtil.getFileExtension(_fileName)) != -1) {
                        return false;
                    }
                }
            } catch (SecurityException ex) {
                return false;
            }
            return true;
        }
    }

    protected void setParent(SyncFile parent) {
        synchronized (syncFileLock) {
            this.parent = parent;
        }
    }

    public SyncFile getParent() {
        synchronized (syncFileLock) {
            return parent;
        }
    }

    public File getFile() {
        synchronized (syncFileLock) {
            return file;
        }
    }

    public boolean isDirectory() {
        synchronized (syncFileLock) {
            return fileIsDirectory;
        }
    }

    public String getAbsolutePath() {
        synchronized (syncFileLock) {
            return fileAbsolutePath;
        }
    }

    public String getFileName() {
        synchronized (syncFileLock) {
            return fileName;
        }
    }

    public long getLastModified() {
        synchronized (syncFileLock) {
            return fileLastModified;
        }
    }

    public void addListener(SyncFileListener listener) {
        synchronized (listenerList) {
            listenerList.add(listener);
        }
    }

    public void removeListener(SyncFileListener listener) {
        synchronized (listenerList) {
            listenerList.remove(listener);
        }
    }

    public void setInheritUserObject(String key, Object object) {
        inheritUserObjectList.put(key, object);
        synchronized (syncFileLock) {
            for (SyncFile syncFile : childSyncFileList) {
                syncFile.setInheritUserObject(key, object);
            }
        }
    }

    public Object getInheritUserObject(String key) {
        return inheritUserObjectList.get(key);
    }

    public void removeInheritUserObject(String key) {
        inheritUserObjectList.remove(key);
        synchronized (syncFileLock) {
            for (SyncFile syncFile : childSyncFileList) {
                syncFile.removeInheritUserObject(key);
            }
        }
    }

    public void setUserObject(String key, Object object) {
        userObjectList.put(key, object);
    }

    public Object getUserObject(String key) {
        return userObjectList.get(key);
    }

    public void removeUserObject(String key) {
        userObjectList.remove(key);
    }

    /**
     * Fire file created event.
     * @return the SyncFile of the new file
     */
    protected SyncFile fireCreateEvent(String rootPath, String name, boolean checkAndCreateChildren) {
        if (debugMode) {
            System.out.println("fc: " + rootPath + (name.isEmpty() ? "" : "/") + name);
        }

        if (!isDirectory()) {
            return null;
        }

        File newFile = new File(rootPath + "/" + name);
        if (!newFile.exists()) {
            // should not reach here
            System.out.println("SyncFile:fireCreateEvent(): file not exist: " + newFile.getAbsolutePath());
            return null;
        }

        SyncFile newSyncFile = null;
        synchronized (syncFileLock) {
            if (isFileFufilFilter(newFile)) {
                try {
                    newSyncFile = new SyncFile(this, newFile, allowedFileExtensionList, disallowedFileExtensionList, ignoreFileList, checkAndCreateChildren);
                    synchronized (inheritUserObjectList) {
                        for (String _key : inheritUserObjectList.keySet()) {
                            Object _object = inheritUserObjectList.get(_key);
                            newSyncFile.setInheritUserObject(_key, _object);
                        }
                    }
                    childSyncFileList.add(newSyncFile);
                    childSyncFileMap.put(newSyncFile.getFileName(), newSyncFile);
                    synchronized (listenerList) {
                        for (SyncFileListener listener : listenerList) {
                            listener.fileCreated(this, newSyncFile, rootPath, name);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SyncFile.class.getName()).log(Level.INFO, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(SyncFile.class.getName()).log(Level.INFO, null, ex);
                }
            }
        }

        return newSyncFile;
    }

    /**
     * Fire file deleted event.
     */
    protected void fireDeleteEvent(String rootPath, String name, boolean deleteFromParent, boolean deleteSyncFileOnly) {
        if (debugMode) {
            System.out.println("fd: " + rootPath + (name.isEmpty() ? "" : "/") + name);
        }

        if (deleteFromParent) {
            SyncFile _parent = getParent();
            if (_parent != null) {
                _parent.removeFile(this, deleteSyncFileOnly);
            }
        }

        if (isDirectory()) {
            for (SyncFile _syncFile : childSyncFileList) {
                _syncFile.fireDeleteEvent(rootPath, name, false, false);
            }
        }

        synchronized (listenerList) {
            for (SyncFileListener listener : listenerList) {
                listener.fileDeleted(this, rootPath, name);
            }
        }
    }

    /**
     * Fire file modified event.
     */
    protected void fireModifyEvent(String rootPath, String name) {
        if (debugMode) {
            System.out.println("fm: " + rootPath + (name.isEmpty() ? "" : "/") + name + " " + isDirectory());
        }

        synchronized (syncFileLock) {
            fileLastModified = new File(rootPath + "/" + name).lastModified();
            synchronized (listenerList) {
                for (SyncFileListener listener : listenerList) {
                    listener.fileModified(this, rootPath, name);
                }
            }
        }
    }

    /**
     * Fire file renamed event.
     */
    protected void fireRenameEvent(String rootPath, String oldName, String newName) {
        if (debugMode) {
            System.out.println("fr: " + rootPath + (oldName.isEmpty() ? "" : "/") + oldName + " > " + newName);
        }

        File oldFile = new File(rootPath + "/" + oldName);
        File newFile = new File(rootPath + "/" + newName);
        if (!newFile.exists()) {
            // should not reach here
            System.out.println("SyncFile:fireRenameEvent(): file not exist: " + newFile.getAbsolutePath());
            return;
        }

        synchronized (syncFileLock) {
            try {
                setFile(newFile);
                if (getParent() != null) {
                    getParent().renameFile(oldFile, newFile, this);
                }
                synchronized (listenerList) {
                    for (SyncFileListener listener : listenerList) {
                        listener.fileRenamed(this, rootPath, oldName, newName);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(SyncFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            if (watchId != -1) {
                JNotify.removeWatch(watchId);
                watchId = -1;
            }
        } catch (JNotifyException ex) {
            Logger.getLogger(SyncFile.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    /**
     * Compare the natural order of the file path. Directory will always return -1 when compared to file.
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof SyncFile) {
            SyncFile syncFile = (SyncFile) o;

            if (isDirectory()) {
                if (!syncFile.isDirectory()) {
                    return -1;
                }
            } else {
                if (syncFile.isDirectory()) {
                    return 1;
                }
            }

            if (this.equals(syncFile)) {
                return 0;
            } else {
                return getAbsolutePath().compareTo(syncFile.getAbsolutePath());
            }
        } else {
            throw new ClassCastException();
        }
    }

    /**
     * Check if the two {@link langfiles.project.SyncFile} has the save file path.
     */
    @Override
    public boolean equals(Object compareTo) {
        if (compareTo == null || !(compareTo instanceof SyncFile)) {
            return false;
        }
        if (compareTo == this) {
            return true;
        }
        SyncFile _object = (SyncFile) compareTo;

        return _object.getAbsolutePath().equals(getAbsolutePath());
    }

    public String toString(String indent) {
        StringBuilder sb = new StringBuilder();

        sb.append(isDirectory() ? "dir" : "file");
        sb.append(": ");
        String parentPath = getParent() != null ? getParent().getAbsolutePath() : "";
        sb.append(getAbsolutePath().replace(parentPath, ""));
        sb.append(", \t\t\t\t\t");
        sb.append("mod: ");
        sb.append(new Date(getLastModified()));
        sb.append(", ");

        if (isDirectory()) {
            sb.append("\tchild file: ");
            sb.append(childFileList.size());
            sb.append(", ");
            sb.append("\tchild SyncFile: ");
            sb.append(childSyncFileList.size());

            if (!childFileList.isEmpty()) {
                sb.append("\n***");
                for (File _file : childFileList) {
                    sb.append(_file.getAbsolutePath().replace(file.getAbsolutePath(), ""));
                    sb.append(", ");
                }
            }
            for (SyncFile _file : childSyncFileList) {
                sb.append("\n");
                sb.append(_file.toString(indent.replaceAll("└", " ") + "└"));
            }
        }

        return indent + sb.toString().replace("\n", "\n" + indent).replaceAll("└(\\s*)└", " $1└");
    }

    @Override
    public String toString() {
        return toString("");
    }

    public static class FileContent {

        protected long lastModified;
        protected String content;

        protected FileContent(long lastModified, String content) {
            this.lastModified = lastModified;
            this.content = content;
        }

        public long getLastModified() {
            return this.lastModified;
        }

        public String getContent() {
            return this.content;
        }
    }

    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // dummy frame to keep program run
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
        SyncFile syncFile = new SyncFile(null, new File("dir"));
        syncFile.addWatch();
    }
}
