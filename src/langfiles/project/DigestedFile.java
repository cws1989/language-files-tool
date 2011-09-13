package langfiles.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is used by {@link DigestedFile}. This class is immutable from outside the package.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class DigestedFile implements Comparable<Object> {

    /**
     * The file represented by this object
     */
    protected File file;
    /**
     * The last modified time of {@link #file}
     */
    protected long lastModified;
    /**
     * If {@link #file} is not a directory, the digested data list. Stored row by row.
     */
    protected List<List<Component>> dataList;
    /**
     * If {@link #file} is a directory, this will store the wanted files contained in this directory.
     */
    protected List<DigestedFile> files;

    /**
     * Constructor.
     * @param file
     * @param dataList
     * @param files 
     */
    protected DigestedFile(File file, List<List<Component>> dataList, List<DigestedFile> files) {
        this.file = file;
        updateDataList(dataList);
        updateFiles(files);
    }

    /**
     * Update the data list of this object.
     * @param dataList the new data list
     */
    protected final void updateDataList(List<List<Component>> dataList) {
        lastModified = file.lastModified();
        this.dataList = dataList;
    }

    /**
     * Update the files in this object.
     * @param files the new file list
     */
    protected final void updateFiles(List<DigestedFile> files) {
        this.files = files;
        Collections.sort(this.files);
    }

    /**
     * Check if this is a directory
     * @return true of it is a directory, false if not
     */
    public boolean isDirectory() {
        return file.isDirectory();
    }

    /**
     * Get the {@link java.io.File}.
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the last modified date of the file.
     * @return the last modified date
     */
    public long lastModified() {
        return lastModified;
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
     * Get the {@link java.io.File} list if it is a directory.
     * @return the file
     */
    public List<DigestedFile> getFiles() {
        return new ArrayList<DigestedFile>(files);
    }

    /**
     * Get the total number of row in this file
     * @return the total number of row
     */
    public int getRowSize() {
        return dataList.size();
    }

    /**
     * Get row data by row number, start from 1.
     * @param rowNumber the row number, start from 1
     * @return the row data, null if that row not exist (row number > total number of rows)
     */
    public List<Component> getRowData(int rowNumber) {
        if (rowNumber > dataList.size()) {
            return null;
        }
        List<Component> returnList = new ArrayList<Component>(dataList.get(rowNumber - 1));
        return returnList;
    }

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

    @Override
    public boolean equals(Object compareTo) {
        if (compareTo == null || !(compareTo instanceof DigestedFile)) {
            return false;
        }
        if (compareTo == this) {
            return true;
        }
        DigestedFile _object = (DigestedFile) compareTo;

        return _object.file.getAbsolutePath().equals(file.getAbsolutePath()) && _object.lastModified == lastModified && _object.dataList.equals(dataList) && _object.files.equals(files);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + (this.file != null ? this.file.hashCode() : 0);
        hash = 31 * hash + (int) (this.lastModified ^ (this.lastModified >>> 32));
        hash = 31 * hash + (this.dataList != null ? this.dataList.hashCode() : 0);
        hash = 31 * hash + (this.files != null ? this.files.hashCode() : 0);
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
            sb.append(files.size());
            sb.append('\n');

            for (int i = 0, iEnd = files.size(); i < iEnd; i++) {
                DigestedFile digestedFile = files.get(i);

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

    @Override
    public Object clone() {
        DigestedFile object = null;
        try {
            object = (DigestedFile) super.clone();
            object.file = file;
            object.lastModified = lastModified;
            object.dataList = dataList;
            object.files = files;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }

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
         * Set the component type.
         * @param type the component type
         */
        public void setType(Type type) {
            this.type = type;
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

        @Override
        public Object clone() {
            Component object = null;
            try {
                object = (Component) super.clone();
                object.type = type;
                object.content = content;
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
            }
            return object;
        }
    }
}