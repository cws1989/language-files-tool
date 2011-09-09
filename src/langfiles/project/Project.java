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

/**
 * The project handler.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class Project {

    /**
     * The list of allowed file extensions.
     */
    private final List<String> allowedExtensionList = Collections.synchronizedList(new ArrayList<String>());
    /**
     * The list of digested data/files.
     */
    private final List<DigestedFile> digestedData = Collections.synchronizedList(new ArrayList<DigestedFile>());

    /**
     * Constructor.
     */
    public Project() {
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
     * Set the list of allowed file extensions. See also {@link #validateFilesFileExtension}.
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
    }

    /**
     * Remove files whose file extension did not exist in allowed file extension list.
     */
    public void validateFilesFileExtension() {
        synchronized (digestedData) {
            synchronized (allowedExtensionList) {
                if (allowedExtensionList.isEmpty()) {
                    return;
                }
                Iterator<DigestedFile> iterator = digestedData.iterator();
                while (iterator.hasNext()) {
                    DigestedFile digestedFile = iterator.next();
                    if (!recursiveValidateFilesFileExtension(digestedFile)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Remove files whose file extension did not exist in allowed file extension list recursively.
     * @param digestedFile the file to check
     * @return true if (file extension exist in the allowed file extension list | directory is not empty after checking)
     */
    private boolean recursiveValidateFilesFileExtension(DigestedFile digestedFile) {
        if (digestedFile.isDirectory()) {
            List<DigestedFile> files = digestedFile.getFiles();

            Iterator<DigestedFile> iterator = files.iterator();
            while (iterator.hasNext()) {
                DigestedFile _digestedFile = iterator.next();
                if (!recursiveValidateFilesFileExtension(_digestedFile)) {
                    iterator.remove();
                }
            }

            if (files.isEmpty()) {
                return false;
            }
        } else {
            if (allowedExtensionList.indexOf(CommonUtil.getFileExtension(digestedFile.getFile().getName())) == -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add folder and all files inside recursively to the handler.
     * @param folder the directory
     */
    public void addFolder(File folder) {
        List<File> fileList = CommonUtil.getFiles(folder, allowedExtensionList);
        for (File file : fileList) {
            // compare to existing file list to check duplication
            boolean existAlready = false;
            for (DigestedFile digestedFile : digestedData) {
                if (digestedFile.getFile().equals(file)) {
                    existAlready = true;
                    break;
                }
            }
            if (!existAlready) {
                continue;
            }

            addFile(file);
        }
    }

    /**
     * Add file to the handler.
     * @param file the file to add
     */
    public void addFile(File file) {
        if (!allowedExtensionList.isEmpty() && allowedExtensionList.indexOf(CommonUtil.getFileExtension(file.getName())) == -1) {
            return;
        }
        try {
            String fileString = CommonUtil.readFile(file);
            digestedData.add(new DigestedFile(file, parse(fileString), new ArrayList<DigestedFile>()));
            synchronized (digestedData) {
                Collections.sort(digestedData);
            }
        } catch (IOException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Parse the string and return the parsed result. The returned value is stored row by row and each row is divided into components.
     * The returned value is the data list format of {@link langfiles.handler.DigestedFile}.
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

            Pattern javaPattern = Pattern.compile("\"([^\"]*?(\\\\\")*)*?\"");
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
}
