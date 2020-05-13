package com.pepper.sharedocument.document;

import java.io.File;

/**
 * Created by prasanth.mathavan on 16,April,2020
 */
public class DocumentReceived {
    private String fileName;
    private File file;

    public DocumentReceived(File file, String name) {
        this.file = file;
        this.fileName = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
