package com.tomczarniecki.s3;

import java.util.List;

public class S3ObjectList {

    private final List<S3Object> objects;
    private final String nextMarker;
    private final boolean firstPage;

    public S3ObjectList(List<S3Object> objects, String nextMarker, boolean firstPage) {
        this.objects = objects;
        this.nextMarker = nextMarker;
        this.firstPage = firstPage;
    }

    public List<S3Object> getObjects() {
        return objects;
    }

    public String getNextMarker() {
        return nextMarker;
    }

    public boolean isFirstPage() {
        return firstPage;
    }

    public boolean isTruncated() {
        return !nextMarker.isEmpty();
    }
}
