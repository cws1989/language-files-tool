package langfiles.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import langfiles.project.DigestedFile.Component;
import langfiles.util.CommonUtil;
import langfiles.util.SortedArrayList;

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
    private final List<DigestedFile> digestedData;

    /**
     * Constructor.
     */
    public Project(String projectName) {
        this.projectName = projectName;
        allowedExtensionList = Collections.synchronizedList(new ArrayList<String>());
        disallowedExtensionList = Collections.synchronizedList(new ArrayList<String>());
        ignoreFileList = Collections.synchronizedList(new ArrayList<String>());
        digestedData = Collections.synchronizedList(new SortedArrayList<DigestedFile>());
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
        try {
            revalidateFiles();
        } catch (IOException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
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
        try {
            revalidateFiles();
        } catch (IOException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
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
        return new ArrayList<String>(ignoreFileList);
    }

    /**
     * Add folder and all files inside recursively to the project.
     * @param folder the directory
     */
    public void addFolder(File folder) {
        // compare to existing file list to check duplication
        boolean existAlready = false;
        for (DigestedFile digestedFile : digestedData) {
            if (digestedFile.getFile().getAbsolutePath().equals(folder.getAbsolutePath())) {
                existAlready = true;
                break;
            }
        }
        if (existAlready) {
            return;
        }

        try {
            DigestedFile digestedFile = getFile(folder);
            if (digestedFile != null) {
                digestedData.add(digestedFile);
            }
        } catch (IOException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Add file to the project.
     * @param file the file to add
     */
    public void addFile(File file) {
        try {
            DigestedFile digestedFile = getFile(file);
            if (digestedFile != null) {
                digestedData.add(digestedFile);
            }
        } catch (IOException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Revalidate the {@link #digestedData} list. Add/remove files according to {@link #allowedExtensionList}, {@link #disallowedExtensionList}, {@link #ignoreFileList}.
     * @throws IOException error when reading any files
     */
    private void revalidateFiles() throws IOException {
        Iterator<DigestedFile> iterator = digestedData.iterator();
        while (iterator.hasNext()) {
            DigestedFile digestedFile = iterator.next();
            if (!revalidateFilesRecursively(digestedFile)) {
                iterator.remove();
            }
        }
    }

    /**
     * Exclusive use for {@link #revalidateFiles()}.
     * @param digestedFile the file to revalidate
     * @return true if the file fufil the filter, false if not fufil the filter
     * @throws IOException error when reading any files
     */
    private boolean revalidateFilesRecursively(DigestedFile digestedFile) throws IOException {
        if (digestedFile.isDirectory()) {
            if (!isDirectoryFufilFilter(digestedFile.getFile())) {
                return false;
            }

            List<DigestedFile> fileList = new ArrayList<DigestedFile>();

            File[] currentFiles = digestedFile.getFile().listFiles();
            for (File currentFile : currentFiles) {
                if (currentFile.isHidden()) {
                    continue;
                }

                String currentFileAbsolutePath = currentFile.getAbsolutePath();

                boolean fileExistInOldFiles = false;

                // compare to existing file and see if there is any match
                List<DigestedFile> oldFiles = digestedFile.getFiles();
                Iterator<DigestedFile> iterator = oldFiles.iterator();
                while (iterator.hasNext()) {
                    DigestedFile _digestedFile = iterator.next();
                    if (currentFileAbsolutePath.equals(_digestedFile.getFile().getAbsolutePath())) {
                        // current file match old file
                        if (revalidateFilesRecursively(_digestedFile)) {
                            fileList.add(_digestedFile);
                        }
                        iterator.remove();
                        fileExistInOldFiles = true;
                        break;
                    }
                }

                // check the unexisting file and add it to list if it fufil the filter
                if (!fileExistInOldFiles) {
                    DigestedFile _digestedFile = getFile(currentFile);
                    if (_digestedFile != null) {
                        fileList.add(_digestedFile);
                    }
                }
            }

            digestedFile.updateFiles(fileList);
        } else {
            if (!isFileFufilFilter(digestedFile.getFile())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parse the file to {@link langfiles.project.DigestedFile}.
     * If it is a directory, it will parse its sub-folder and files recursively.
     * @param file the file to parse
     * @return the DigestedFile or null if filter not fufiled
     * @throws IOException error when reading the file
     */
    private DigestedFile getFile(File file) throws IOException {
        DigestedFile returnFile = null;
        if (file.isDirectory()) {
            if (!isDirectoryFufilFilter(file)) {
                return null;
            }

            List<DigestedFile> fileList = new ArrayList<DigestedFile>();

            File[] files = file.listFiles();
            for (File _file : files) {
                if (_file.isHidden()) {
                    continue;
                }
                DigestedFile digestedFile = getFile(_file);
                if (digestedFile != null) {
                    fileList.add(digestedFile);
                }
            }

            returnFile = new DigestedFile(this, file, new ArrayList<List<Component>>(), fileList);
        } else {
            if (!isFileFufilFilter(file)) {
                return null;
            }
            String fileString = CommonUtil.readFile(file);
            returnFile = new DigestedFile(this, file, parse(fileString), new ArrayList<DigestedFile>());
        }
        return returnFile;
    }

    /**
     * Check whether the directory fufil those filters.
     * @param file the directory to check
     * @return true if fufil, false if not
     */
    private boolean isDirectoryFufilFilter(File file) {
        return !(ignoreFileList.indexOf(file.getAbsolutePath()) != -1);
    }

    /**
     * Check whether the file fufil those filters.
     * @param file the file to check
     * @return true if fufil, false if not
     */
    private boolean isFileFufilFilter(File file) {
        return !((!allowedExtensionList.isEmpty() && allowedExtensionList.indexOf(CommonUtil.getFileExtension(file.getName())) == -1)
                || (!disallowedExtensionList.isEmpty() && disallowedExtensionList.indexOf(CommonUtil.getFileExtension(file.getName())) != -1)
                || ignoreFileList.indexOf(file.getAbsolutePath()) != -1);
    }

    /**
     * Parse the string and return the parsed result. The returned value is stored row by row and each row is divided into components.
     * The returned value is the data list format of {@link langfiles.project.DigestedFile}.
     * @param fileData the string data to parse
     * @return the parsed result
     */
    public List<List<Component>> parse(String fileData) {
        List<List<Component>> dataList = new ArrayList<List<Component>>();

        String[] lines = fileData.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String lineString = lines[i];

            List<Component> rowList = new ArrayList<Component>();
            dataList.add(rowList);

//            Pattern javaPattern = Pattern.compile("\"([^\"]*?(\\\\\")*)+?\"");
            Pattern javaPattern = Pattern.compile("\"(?:[^\\\\\"]{1}|(?:\\\\\"){1})*?\"");
            Matcher matcher = javaPattern.matcher(lineString);
            while (matcher.find()) {
                StringBuffer sb = new StringBuffer();
                matcher.appendReplacement(sb, "");
                rowList.add(new Component(Component.Type.CODE, sb.toString()));
                rowList.add(new Component(Component.Type.TEXT, matcher.group(0)));
            }
            StringBuffer sb = new StringBuffer();
            matcher.appendTail(sb);
            rowList.add(new Component(Component.Type.CODE, sb.toString()));
        }

        return dataList;
    }

    /**
     * Get the list of digested data/files.
     * @return the list of digested data/files
     */
    public List<DigestedFile> getDigestedData() {
        List<DigestedFile> returnList = new ArrayList<DigestedFile>();
        synchronized (digestedData) {
            returnList = new ArrayList<DigestedFile>(digestedData);
        }
        return returnList;
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