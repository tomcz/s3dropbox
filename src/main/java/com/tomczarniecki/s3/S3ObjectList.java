package com.tomczarniecki.s3;

import java.util.List;
import java.util.Optional;

public class S3ObjectList {

    private final List<S3Object> objects;
    private final Optional<String> nextMarker;
    private final boolean firstPage;

    public S3ObjectList(List<S3Object> objects, Optional<String> nextMarker, boolean firstPage) {
        this.objects = objects;
        this.nextMarker = nextMarker;
        this.firstPage = firstPage;
    }

    public List<S3Object> getObjects() {
        return objects;
    }

    public Optional<String> getNextMarker() {
        return nextMarker;
    }

    public boolean isFirstPage() {
        return firstPage;
    }

    public boolean isTruncated() {
        return nextMarker.isPresent();
    }
}
