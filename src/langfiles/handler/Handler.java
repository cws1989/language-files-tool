package langfiles.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import langfiles.CommonUtil;
import langfiles.handler.DigestedFile.Component;

public class Handler {

    private final List<String> allowedExtensionList = Collections.synchronizedList(new ArrayList<String>());
    private final List<DigestedFile> digestedData = Collections.synchronizedList(new ArrayList<DigestedFile>());

    public Handler() {
    }

    public List<String> getAllowedExtensions() {
        List<String> returnList = null;
        synchronized (allowedExtensionList) {
            returnList = new ArrayList<String>(allowedExtensionList);
        }
        return returnList;
    }

    public void setAllowedExtensions(List<String> extensionList) {
        synchronized (allowedExtensionList) {
            allowedExtensionList.clear();
            allowedExtensionList.addAll(extensionList);
        }
    }

    public void addDirectory(File directory) {
        List<File> fileList = CommonUtil.getFiles(directory, allowedExtensionList);
        for (File file : fileList) {
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

    public void addFile(File file) {
        if (!allowedExtensionList.isEmpty() && allowedExtensionList.indexOf(CommonUtil.getFileExtension(file.getName())) == -1) {
            return;
        }
        try {
            String fileString = CommonUtil.readFile(new File("IncidentLog.java"));
            digestedData.add(new DigestedFile(file, parse(fileString), new ArrayList<DigestedFile>()));
            synchronized (digestedData) {
                Collections.sort(digestedData);
            }
        } catch (IOException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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

    public List<DigestedFile> getDigestedData() {
        List<DigestedFile> returnList = new ArrayList<DigestedFile>();
        synchronized (digestedData) {
            returnList = new ArrayList<DigestedFile>(digestedData);
        }
        return returnList;
    }

    public void commit() {
    }

    public static void main(String[] args) {
        long time = 0xfffff0ffL;
        int a = (int) time;
        time = a & 0xffffffffL;
        System.out.println(a);
        System.out.println(time);
    }
}
