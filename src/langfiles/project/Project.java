package langfiles.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import langfiles.project.DigestedFile.Component;
import langfiles.util.CommonUtil;
import langfiles.util.SortedArrayList;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyAdapter;
import net.contentobjects.jnotify.JNotifyException;

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
    private final Map<DigestedFile, Integer> digestedDataWatchIdList;
    private final Map<DigestedFile, Map<String, DigestedFile>> digestedDataFileList;
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
        digestedData = Collections.synchronizedList(new SortedArrayList<DigestedFile>());
        revalidatingFiles = false;
        digestedDataWatchIdList = Collections.synchronizedMap(new HashMap<DigestedFile, Integer>());
        digestedDataFileList = Collections.synchronizedMap(new HashMap<DigestedFile, Map<String, DigestedFile>>());
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
    public List<DigestedFile> getDigestedData() {
        List<DigestedFile> returnList = new ArrayList<DigestedFile>();
        synchronized (digestedData) {
            returnList = new ArrayList<DigestedFile>(digestedData);
        }
        return returnList;
    }

    /**
     * Add file/folder and all files inside recursively to the project.
     * @param file the file/folder
     */
    public void add(File file) {
        synchronized (digestedData) {
            // compare to existing file list (root level only) to check duplication
            boolean existAlready = false;
            for (DigestedFile digestedFile : digestedData) {
                if (digestedFile.getFile().getAbsolutePath().equals(file.getAbsolutePath())) {
                    existAlready = true;
                    break;
                }
            }
            if (existAlready) {
                return;
            }

            DigestedFile digestedFile = null;
            try {
                digestedFile = getFile(file);
            } catch (IOException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (digestedFile != null) {
                digestedData.add(digestedFile);
                digestedDataFileList.put(digestedFile, getFileList(digestedFile));
                String pathToListen = digestedFile.isDirectory() ? file.getAbsolutePath() : CommonUtil.getFileParentPath(file);
                final DigestedFile finalDigestedFile = digestedFile;
                try {
                    int watchId = JNotify.addWatch(pathToListen, JNotify.FILE_ANY, true, new JNotifyAdapter() {

                        @Override
                        public void fileCreated(int watchId, String rootPath, String name) {
//                            System.out.println("c: " + rootPath + "/" + name);
                            File newFile = new File(rootPath + "/" + name);
                            Map<String, DigestedFile> fileList = digestedDataFileList.get(finalDigestedFile);
                            DigestedFile targetDigestedFile = fileList.get(newFile.getParent());
                            if (targetDigestedFile != null) {
                                DigestedFile newDigestedFile = targetDigestedFile.fireCreateEvent(rootPath, name);
                                if (newDigestedFile != null) {
                                    fileList.put(newFile.getAbsolutePath(), newDigestedFile);
                                }
                            }
                        }

                        @Override
                        public void fileDeleted(int watchId, String rootPath, String name) {
//                            System.out.println("d: " + rootPath + "/" + name);
                            Map<String, DigestedFile> fileList = digestedDataFileList.get(finalDigestedFile);
                            DigestedFile targetDigestedFile = fileList.remove(new File(rootPath + "/" + name).getAbsolutePath());
                            if (targetDigestedFile != null) {
                                targetDigestedFile.fireDeleteEvent(rootPath, name);
                            }
                        }

                        @Override
                        public void fileModified(int watchId, String rootPath, String name) {
//                            System.out.println("m: " + rootPath + "/" + name);
                            Map<String, DigestedFile> fileList = digestedDataFileList.get(finalDigestedFile);
                            DigestedFile targetDigestedFile = fileList.get(new File(rootPath + "/" + name).getAbsolutePath());
                            if (targetDigestedFile != null) {
                                targetDigestedFile.fireModifyEvent(rootPath, name);
                            }
                        }

                        @Override
                        public void fileRenamed(int watchId, String rootPath, String oldName, String newName) {
//                            System.out.println("r: " + rootPath + "/" + oldName + " > " + newName);
                            Map<String, DigestedFile> fileList = digestedDataFileList.get(finalDigestedFile);
                            DigestedFile targetDigestedFile = fileList.get(new File(rootPath + "/" + oldName).getAbsolutePath());
                            if (targetDigestedFile != null) {
                                fileList.remove(targetDigestedFile.getFile().getAbsolutePath());
                                fileList.put(new File(rootPath + "/" + newName).getAbsolutePath(), targetDigestedFile);
                                targetDigestedFile.fireRenameEvent(rootPath, oldName, newName);
                            } else {
                                if (isFileFufilFilter(new File(rootPath + "/" + newName))) {
                                    fileCreated(watchId, rootPath, newName);
                                }
                            }
                        }
                    });
                    digestedDataWatchIdList.put(digestedFile, watchId);
                } catch (JNotifyException ex) {
                    Logger.getLogger(DigestedFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void remove(DigestedFile digestedFile) {
        synchronized (digestedData) {
            digestedData.remove(digestedFile);
            removeWatchId(digestedFile);
            digestedDataFileList.remove(digestedFile);
        }
    }

    protected void removeWatchId(DigestedFile digestedFile) {
        Integer watchId = digestedDataWatchIdList.remove(digestedFile);
        if (watchId != null) {
            try {
                JNotify.removeWatch(watchId);
            } catch (JNotifyException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected Map<String, DigestedFile> getFileList(DigestedFile digestedFile) {
        Map<String, DigestedFile> returnMap = new HashMap<String, DigestedFile>();

        if (digestedFile.isDirectory()) {
            returnMap.put(digestedFile.getFile().getAbsolutePath(), digestedFile);
            List<DigestedFile> _fileList = digestedFile.getFileList();
            for (DigestedFile _digestedFile : _fileList) {
                returnMap.putAll(getFileList(_digestedFile));
            }
        } else {
            returnMap.put(digestedFile.getFile().getAbsolutePath(), digestedFile);
        }

        return returnMap;
    }

    /**
     * Revalidate the {@link #digestedData} list. Add/remove files according to {@link #allowedExtensionList}, {@link #disallowedExtensionList}, {@link #ignoreFileList}.
     * @throws IOException error when reading any files
     */
    protected void revalidateFiles() throws IOException {
        synchronized (revalidateFilesLock) {
            if (revalidatingFiles) {
                return;
            }
            revalidatingFiles = true;
        }
        synchronized (digestedData) {
            Iterator<DigestedFile> iterator = digestedData.iterator();
            while (iterator.hasNext()) {
                DigestedFile digestedFile = iterator.next();
                Map<String, DigestedFile> fileMap = digestedDataFileList.get(digestedFile);
                if (!revalidateFilesRecursively(fileMap, digestedFile)) {
                    removeWatchId(digestedFile);
                    digestedDataFileList.remove(digestedFile);
                    iterator.remove();
                }
            }
        }
        synchronized (revalidateFilesLock) {
            revalidatingFiles = false;
        }
    }

    /**
     * Exclusive use for {@link #revalidateFiles()}.
     * @param digestedFile the file to revalidate
     * @return true if the file fufil the filter, false if not fufil the filter
     * @throws IOException error when reading any files
     */
    protected boolean revalidateFilesRecursively(Map<String, DigestedFile> fileMap, DigestedFile digestedFile) throws IOException {
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
                List<DigestedFile> oldFiles = digestedFile.getFileList();
                Iterator<DigestedFile> iterator = oldFiles.iterator();
                while (iterator.hasNext()) {
                    DigestedFile _digestedFile = iterator.next();
                    if (currentFileAbsolutePath.equals(_digestedFile.getFile().getAbsolutePath())) {
                        // current file match old file
                        if (revalidateFilesRecursively(fileMap, _digestedFile)) {
                            fileList.add(_digestedFile);
                        } else {
                            fileMap.remove(_digestedFile.getFile().getAbsolutePath());
                        }
                        iterator.remove();
                        fileExistInOldFiles = true;
                        break;
                    }
                }

                // check the unexisting file and add it to list if it fufil the filter
                if (!fileExistInOldFiles) {
                    try {
                        DigestedFile _digestedFile = getFile(currentFile);
                        if (_digestedFile != null) {
                            fileMap.put(_digestedFile.getFile().getAbsolutePath(), digestedFile);
                            fileList.add(_digestedFile);
                        }
                    } catch (SecurityException ex) {
                        System.out.println(ex);
                    }
                }
            }

            digestedFile.setFileList(fileList);
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
    protected DigestedFile getFile(File file) throws IOException, SecurityException {
        DigestedFile returnFile = null;
        if (file.isDirectory()) {
            if (!isDirectoryFufilFilter(file)) {
                return null;
            }

            returnFile = new DigestedFile(this, file, new ArrayList<List<Component>>(), new ArrayList<DigestedFile>());

            List<DigestedFile> fileList = new ArrayList<DigestedFile>();

            File[] files = file.listFiles();
            if (files != null) {
                for (File _file : files) {
                    if (_file.isHidden()) {
                        continue;
                    }
                    DigestedFile digestedFile = getFile(_file);
                    if (digestedFile != null) {
                        digestedFile.setParent(returnFile);
                        fileList.add(digestedFile);
                    }
                }
            } else {
                // strange null, IO Exception?
            }

            returnFile.setFileList(fileList);
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

    public DigestedFile getDigestedFileByAbsolutePath(String path) {
        String absolutePath = new File(path).getAbsolutePath();

        DigestedFile returnDigestedFile = null;
        for (Map<String, DigestedFile> fileMap : digestedDataFileList.values()) {
            if ((returnDigestedFile = fileMap.get(absolutePath)) != null) {
                return returnDigestedFile;
            }
        }
        return returnDigestedFile;
//        List<DigestedFile> digestedFileList = getDigestedData();
//        for (DigestedFile digestedFile : digestedFileList) {
//            DigestedFile returnFile = null;
//            if ((returnFile = getDigestedFileByAbsolutePath(digestedFile, absolutePath)) != null) {
//                return returnFile;
//            }
//        }
//
//        return null;
    }

//    protected DigestedFile getDigestedFileByAbsolutePath(DigestedFile digestedFile, String path) {
//        if (digestedFile.isDirectory()) {
//            List<DigestedFile> fileList = digestedFile.getFileList();
//            for (DigestedFile _digestedFile : fileList) {
//                DigestedFile returnFile = null;
//                if ((returnFile = getDigestedFileByAbsolutePath(_digestedFile, path)) != null) {
//                    return returnFile;
//                }
//            }
//        } else {
//            if (digestedFile.getFile().getAbsolutePath().equals(path)) {
//                return digestedFile;
//            }
//        }
//        return null;
//    }
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        for (int watchId : digestedDataWatchIdList.values()) {
            try {
                JNotify.removeWatch(watchId);
            } catch (JNotifyException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        digestedDataWatchIdList.clear();
    }
}