package langfiles.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Immutable from outside the package.
 */
public class DigestedFile implements Comparable<Object> {

    protected File file;
    protected long lastModified;
    protected List<List<Component>> dataList;
    protected List<DigestedFile> files;

    protected DigestedFile(File file, List<List<Component>> dataList, List<DigestedFile> files) {
        this.file = file;
        updateDataList(dataList);
        updateFiles(files);
    }

    protected final void updateDataList(List<List<Component>> dataList) {
        lastModified = file.lastModified();
        this.dataList = dataList;
    }

    protected final void updateFiles(List<DigestedFile> files) {
        this.files = files;
        Collections.sort(this.files);
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public File getFile() {
        return file;
    }

    public long lastModified() {
        return lastModified;
    }

    public List<List<Component>> getDataList() {
        List<List<Component>> returnList = new ArrayList<List<Component>>();
        for (List<Component> list : dataList) {
            returnList.add(new ArrayList<Component>(list));
        }
        return returnList;
    }

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
        return _object.file.equals(file) && _object.lastModified == lastModified && _object.dataList.equals(dataList) && _object.files.equals(files);
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

                for (int j = 0, jEnd = dataList.size(); j < jEnd; j++) {
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
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }

    /**
     * Immutable.
     */
    public static class Component {

        public static enum Type {

            CODE, TEXT, LANGUAGE
        }
        protected Type type;
        protected String content;

        protected Component(Type type, String content) {
            this.type = type;
            this.content = content;
        }

        public Type getType() {
            return type;
        }

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
                Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return object;
        }
    }
}