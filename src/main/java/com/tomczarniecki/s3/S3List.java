package com.tomczarniecki.s3;

import java.util.List;

public class S3List {

    private final List<String> folders;
    private final List<String> files;

    public S3List(List<String> folders, List<String> files) {
        this.folders = folders;
        this.files = files;
    }

    public List<String> getFolders() {
        return folders;
    }

    public List<String> getFiles() {
        return files;
    }
}
