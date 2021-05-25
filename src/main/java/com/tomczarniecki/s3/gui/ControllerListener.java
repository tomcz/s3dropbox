package com.tomczarniecki.s3.gui;

public interface ControllerListener {

    void showingBuckets();

    void showingObjects(String bucketName);
}
