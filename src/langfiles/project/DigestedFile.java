package langfiles.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import langfiles.util.SortedArrayList;

/**
 * This class represent/contain a folder or a file.
 * This class is immutable from outside the package.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class DigestedFile implements Comparable<Object> {

    /**
     * The project that this digested file belongs.
     */
    protected Project project;
    /**
     * The parent of this file, null means no parent.
     */
    protected DigestedFile parent;
    /**
     * The file represented by this object.
     */
    protected File file;
    /**
     * The last modified time (in milli second) of {@link #dataList}, not {@link #file}.
     */
    protected long lastModified;
//    /**
//     * If this file is a directory, this is the watch id from JNotify to listen to file change event.
//     * If watch id equals -1, that means the 'watch' has been removed or no watch is added.
//     */
//    protected int watchId;
    /**
     * If {@link #file} is a directory, this will store those non-hidden files contained in this directory.
     * This is sorted file file name in natural order.
     */
    protected List<DigestedFile> fileList;
    /**
     * It store the parsed data of the file.
     * If {@link #file} is not a directory, this should not be empty if the file is not empty. It is stored row by row.
     */
    protected List<List<Component>> dataList;
    /**
     * The list of user object.
     */
    protected final Map<String, Object> userObjectList;
    /**
     * Listener list.
     */
    protected final List<DigestedFileListener> listenerList;

    /**
     * Constructor.
     * @param file {@see #file}
     * @param dataList {@see #dataList}
     * @param fileList  {@see #fileList}
     */
    protected DigestedFile(Project project, File file, List<List<Component>> dataList, List<DigestedFile> fileList) {
//        watchId = -1;
        userObjectList = Collections.synchronizedMap(new HashMap<String, Object>());
        listenerList = Collections.synchronizedList(new ArrayList<DigestedFileListener>());

        this.project = project;
        this.file = file;
        setDataList(dataList);
        setFileList(fileList);

//        if (isDirectory()) {
//            for (int count = 0, trialLimit = 5; count < trialLimit; count++) {
//                try {
//                    watchId = JNotify.addWatch(file.getAbsolutePath(), JNotify.FILE_ANY, false, new JNotifyAdapter() {
//
//                        @Override
//                        public void fileCreated(int watchId, String rootPath, String name) {
//                            System.out.println("c: " + rootPath + "/" + name);
//                            Project project = DigestedFile.this.project;
//                            if (project == null) {
//                                return;
//                            }
//
//                            File file = new File(rootPath + "/" + name);
//                            if (!file.exists()) {
//                                // should not exist
//                                return;
//                            }
//
//                            DigestedFile newDigestedFile = null;
//                            try {
//                                newDigestedFile = project.getFile(file);
//                                if (newDigestedFile == null) {
//                                    return;
//                                }
//                                DigestedFile.this.fileList.add(newDigestedFile);
//                            } catch (IOException ex) {
//                                Logger.getLogger(DigestedFile.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//
//                            synchronized (listenerList) {
//                                for (DigestedFileListener listener : listenerList) {
//                                    listener.fileCreated(DigestedFile.this, newDigestedFile, rootPath, name);
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void fileDeleted(int watchId, String rootPath, String name) {
//                            File file = new File(rootPath + "/" + name);
//
//                            String filePath = file.getAbsolutePath();
//
//                            DigestedFile digestedFileDeleted = null;
//                            Iterator<DigestedFile> iterator = DigestedFile.this.fileList.iterator();
//                            while (iterator.hasNext()) {
//                                DigestedFile digestedFile = iterator.next();
//                                if (digestedFile.getFile().getAbsolutePath().equals(filePath)) {
//                                    digestedFile.setParent(null);
//                                    digestedFile.dispose();
//                                    digestedFileDeleted = digestedFile;
//                                    iterator.remove();
//                                    break;
//                                }
//                            }
//
//                            if (digestedFileDeleted == null) {
//                                return;
//                            }
//
//                            digestedFileDeleted.fireDeleteEvent(rootPath, name);
//                        }
//
//                        @Override
//                        public void fileModified(int watchId, String rootPath, String name) {
//                            File file = new File(rootPath + "/" + name);
//                            if (!file.exists()) {
//                                // should not exist
//                                return;
//                            }
//
//                            String filePath = file.getAbsolutePath();
//
//                            DigestedFile digestedFileModified = null;
//                            Iterator<DigestedFile> iterator = DigestedFile.this.fileList.iterator();
//                            while (iterator.hasNext()) {
//                                DigestedFile digestedFile = iterator.next();
//                                if (digestedFile.isDirectory()) {
//                                    continue;
//                                }
//                                if (digestedFile.getFile().getAbsolutePath().equals(filePath)) {
//                                    digestedFileModified = digestedFile;
//                                    break;
//                                }
//                            }
//
//                            if (digestedFileModified == null) {
//                                return;
//                            }
//
//                            digestedFileModified.fireDeleteEvent(rootPath, name);
//                        }
//
//                        @Override
//                        public void fileRenamed(int watchId, String rootPath, String oldName, String newName) {
//                            File oldFile = new File(rootPath + "/" + oldName);
//                            File newFile = new File(rootPath + "/" + newName);
//                            if (!newFile.exists()) {
//                                // should not exist
//                                return;
//                            }
//
//                            String oldFilePath = oldFile.getAbsolutePath();
//
//                            DigestedFile digestedFileRenamed = null;
//                            Iterator<DigestedFile> iterator = DigestedFile.this.fileList.iterator();
//                            while (iterator.hasNext()) {
//                                DigestedFile digestedFile = iterator.next();
//                                if (digestedFile.getFile().getAbsolutePath().equals(oldFilePath)) {
//                                    digestedFile.file = newFile;
//                                    digestedFileRenamed = digestedFile;
//                                    break;
//                                }
//                            }
//
//                            if (digestedFileRenamed == null) {
//                                return;
//                            }
//
//                            digestedFileRenamed.fireRenameEvent(rootPath, oldName, newName);
//                        }
//                    });
//                    break;
//                } catch (JNotifyException ex) {
//                    if (count == 4) {
//                        Logger.getLogger(DigestedFile.class.getName()).log(Level.SEVERE, null, ex);
//                    } else {
//                        try {
//                            Thread.sleep(50);
//                        } catch (InterruptedException ex1) {
//                            Logger.getLogger(DigestedFile.class.getName()).log(Level.SEVERE, null, ex1);
//                        }
//                    }
//                }
//            }
//        }
    }

//    /**
//     * Dispose this object.
//     */
//    protected void dispose() {
//        if (watchId != -1) {
//            try {
//                JNotify.removeWatch(watchId);
//            } catch (JNotifyException ex) {
//                Logger.getLogger(DigestedFile.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            watchId = -1;
//        }
//    }
    /**
     * Get the project object that this digested file belongs.
     * @return the project
     */
    public Project getProject() {
        return project;
    }

    /**
     * Set the parent {@link langfiles.project.DigestedFile} of this object.
     * @param parent the parent file
     */
    protected void setParent(DigestedFile parent) {
        this.parent = parent;
    }

    /**
     * Get the parent {@link langfiles.project.DigestedFile} of this object.
     * @return the parent
     */
    public DigestedFile getParent() {
        return this.parent;
    }

    /**
     * Check if this is a directory
     * @return true of it is a directory, false if not
     */
    public boolean isDirectory() {
        return file.isDirectory();
    }

    /**
     * Add listener, listen to file change event.
     * @param listener the listener
     */
    public void addListener(DigestedFileListener listener) {
        listenerList.add(listener);
    }

    /**
     * Remove listener.
     * @param listener the listener
     */
    public void removeListener(DigestedFileListener listener) {
        listenerList.remove(listener);
    }

    /**
     * Get the {@link java.io.File}.
     * @return the file {@see #file}
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the last modified date of the {@see #dataList}.
     * @return the last modified date in milli second
     */
    public long lastModified() {
        return lastModified;
    }

    /**
     * Update the data list of this object.
     * @param dataList {@see #dataList}
     */
    protected void setDataList(List<List<Component>> dataList) {
        lastModified = file.lastModified();

        List<List<Component>> newDataList = new ArrayList<List<Component>>();
        synchronized (dataList) {
            for (List<Component> componentList : dataList) {
                newDataList.add(new ArrayList<Component>(componentList));
            }
        }
        this.dataList = newDataList;
    }

    /**
     * Get a copy of the data list.
     * @return the data list
     */
    public List<List<Component>> getDataList() {
        List<List<Component>> returnList = new ArrayList<List<Component>>();
        for (List<Component> list : dataList) {
            returnList.add(new ArrayList<Component>(list));
        }
        return returnList;
    }

    /**
     * Get row data by row number, the row number is start from 1.
     * @param rowNumber the row number, start from 1
     * @return the row data, null if that row not exist (row number <= 0 | row number > total number of rows)
     */
    public List<Component> getRowData(int rowNumber) {
        if (rowNumber <= 0 || rowNumber > dataList.size()) {
            return null;
        }
        return new ArrayList<Component>(dataList.get(rowNumber - 1));
    }

    /**
     * Get the total number of row of {@see #dataList}
     * @return the total number of row
     */
    public int getRowSize() {
        return dataList.size();
    }

    /**
     * Update the {@see #fileList}. This class has a mechanism to listen to the change and do appropriate update (check filter, add/delete file etc.).
     * @param fileList the new file list
     */
    protected void setFileList(List<DigestedFile> fileList) {
        if (!isDirectory()) {
            return;
        }
        this.fileList = new SortedArrayList<DigestedFile>(fileList);
    }

    /**
     * Get the {@link java.io.File} list if it is a directory.
     * @return the file
     */
    public List<DigestedFile> getFileList() {
        return new ArrayList<DigestedFile>(fileList);
    }

    /**
     * Set user object.
     * @param key the key to object
     * @param object the user object
     * @return the previous value associated with key, or null if there was no value for key
     */
    public Object setUserObject(String key, Object object) {
        return userObjectList.put(key, object);
    }

    /**
     * Get the user object by key.
     * @param key the key to object
     * @return the value for the key, or null if contains no value for the key
     */
    public Object getUserObject(String key) {
        return userObjectList.get(key);
    }

    /**
     * Remove the user object.
     * @param key the key to object
     * @return the previous value associated with key, or null if there was no value for key
     */
    public Object removeUserObject(String key) {
        return userObjectList.remove(key);
    }

    /**
     * For file delete event only. {@link #fireDeleteEvent(java.lang.String, java.lang.String)}
     * @param digestedFile the file to remove
     */
    protected void removeFileFromFileList(DigestedFile digestedFile) {
        fileList.remove(digestedFile);
    }

    /**
     * Fire file created event.
     * @return the DigestedFile of the new file
     */
    protected DigestedFile fireCreateEvent(String rootPath, String name) {
//        System.out.println("fc: " + rootPath + "/" + name);
        if (!isDirectory()) {
            return null;
        }
        if (project == null) {
            return null;
        }

        File newFile = new File(rootPath + "/" + name);
        if (!newFile.exists()) {
            // should not exist
            return null;
        }

        DigestedFile newDigestedFile = null;
        try {
            newDigestedFile = project.getFile(newFile);
            if (newDigestedFile == null) {
                return null;
            }
            fileList.add(newDigestedFile);
        } catch (IOException ex) {
            Logger.getLogger(DigestedFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(DigestedFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        synchronized (listenerList) {
            for (DigestedFileListener listener : listenerList) {
                listener.fileCreated(this, newDigestedFile, rootPath, name);
            }
        }

        return newDigestedFile;
    }

    /**
     * Fire file deleted event.
     */
    protected void fireDeleteEvent(String rootPath, String name) {
//        System.out.println("fd: " + rootPath + "/" + name);

        if (getParent() != null) {
            getParent().removeFileFromFileList(this);
            setParent(null);
        }
//        dispose();

        synchronized (listenerList) {
            for (DigestedFileListener listener : listenerList) {
                listener.fileDeleted(this, rootPath, name);
            }
        }
    }

    /**
     * Fire file modified event.
     */
    protected void fireModifyEvent(String rootPath, String name) {
//        System.out.println("fm: " + rootPath + "/" + name + " " + isDirectory());
        synchronized (listenerList) {
            for (DigestedFileListener listener : listenerList) {
                listener.fileModified(this, rootPath, name);
            }
        }
    }

    /**
     * Fire file renamed event.
     */
    protected void fireRenameEvent(String rootPath, String oldName, String newName) {
//        System.out.println("fr: " + rootPath + "/" + oldName + " > " + newName);

        File newFile = new File(rootPath + "/" + newName);
        if (!newFile.exists()) {
            // should not exist
            return;
        }

        file = newFile;

        synchronized (listenerList) {
            for (DigestedFileListener listener : listenerList) {
                listener.fileRenamed(this, rootPath, oldName, newName);
            }
        }
    }

    /**
     * Compare the natural order of the file path. Directory will always return -1 when compared to file.
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof DigestedFile) {
            DigestedFile digestedFile = (DigestedFile) o;

            if (isDirectory()) {
                if (!digestedFile.isDirectory()) {
                    return -1;
                }
            } else {
                if (digestedFile.isDirectory()) {
                    return 1;
                }
            }

            if (this.equals(digestedFile)) {
                return 0;
            } else {
                return file.getAbsolutePath().compareTo(digestedFile.getFile().getAbsolutePath());
            }
        } else {
            throw new ClassCastException();
        }
    }

    /**
     * Check if the two {@link langfiles.project.DigestedFile} has the save file path.
     */
    @Override
    public boolean equals(Object compareTo) {
        if (compareTo == null || !(compareTo instanceof DigestedFile)) {
            return false;
        }
        if (compareTo == this) {
            return true;
        }
        DigestedFile _object = (DigestedFile) compareTo;

        return _object.file.getAbsolutePath().equals(file.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.file != null ? this.file.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(isDirectory() ? "directory" : "file");
        sb.append(" path: ");
        sb.append(file.getAbsolutePath());
        sb.append(", ");
        sb.append("last modified: ");
        sb.append(new Date(lastModified));
        sb.append(", ");

        if (isDirectory()) {
            sb.append("file count: ");
            sb.append(fileList.size());
            sb.append('\n');

            for (int i = 0, iEnd = fileList.size(); i < iEnd; i++) {
                DigestedFile digestedFile = fileList.get(i);

                sb.append("file ");
                sb.append((i + 1));
                sb.append(": ");

                sb.append(digestedFile.toString());

                sb.append('\n');
            }
        } else {
            sb.append("row count: ");
            sb.append(dataList.size());
            sb.append('\n');

            for (int i = 0, iEnd = dataList.size(); i < iEnd; i++) {
                List<Component> list = dataList.get(i);

                sb.append("Row ");
                sb.append((i + 1));
                sb.append(": ");

                for (int j = 0, jEnd = list.size(); j < jEnd; j++) {
                    if (j != 0) {
                        sb.append(", ");
                    }
                    sb.append(list.get(j));
                }

                sb.append('\n');
            }
        }

        return sb.toString();
    }

//    @Override
//    protected void finalize() throws Throwable {
//        super.finalize();
//        if (watchId != -1) {
//            JNotify.removeWatch(watchId);
//            watchId = -1;
//        }
//    }
//    @Override
//    public Object clone() {
//        DigestedFile object = null;
//        try {
//            object = (DigestedFile) super.clone();
//            object.project = project;
//            object.parent = parent;
//            object.file = file;
//            object.lastModified = lastModified;
//            object.fileList = getFileList();
//            object.dataList = getDataList();
//            // cannot copy final fields
//        } catch (CloneNotSupportedException ex) {
//            Logger.getLogger(DigestedFile.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return object;
//    }
    /**
     * Component used by DigestedFile after digesting the text.
     */
    public static class Component {

        /**
         * Component type.
         * <p>
         * CODE: normal code<br />
         * TEXT: string text in code, possibly take into the language file
         * LANGUAGE: language existed in the language file
         * </p>
         */
        public static enum Type {

            CODE, TEXT, LANGUAGE
        }
        /**
         * Component type.
         */
        protected Type type;
        /**
         * The string content of this component.
         */
        protected String content;

        /**
         * Constructor.
         * @param type component type
         * @param content string content of this component
         */
        protected Component(Type type, String content) {
            this.type = type;
            this.content = content;
        }

        /**
         * Get the component type.
         * @return the component type
         */
        public Type getType() {
            return type;
        }

        /**
         * Get the string content
         * @return the string content
         */
        public String getContent() {
            return content;
        }

        @Override
        public boolean equals(Object compareTo) {
            if (compareTo == null || !(compareTo instanceof Component)) {
                return false;
            }
            if (compareTo == this) {
                return true;
            }
            Component _object = (Component) compareTo;
            return _object.type == type && _object.content.equals(content);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.type != null ? this.type.hashCode() : 0);
            hash = 79 * hash + (this.content != null ? this.content.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            switch (type) {
                case CODE:
                    sb.append("code");
                    break;
                case TEXT:
                    sb.append("text");
                    break;
                case LANGUAGE:
                    sb.append("language");
                    break;
            }
            sb.append(" : ");
            sb.append(content);

            return sb.toString();
        }
//        @Override
//        public Object clone() {
//            Component object = null;
//            try {
//                object = (Component) super.clone();
//                object.type = type;
//                object.content = content;
//            } catch (CloneNotSupportedException ex) {
//                Logger.getLogger(Component.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            return object;
//        }
    }
}
