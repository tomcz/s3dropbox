package com.tomczarniecki.s3.gui;

import com.tomczarniecki.s3.ProgressListener;
import com.tomczarniecki.s3.S3Bucket;
import org.joda.time.DateTime;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DualController implements Controller {

    private final Controller tree;
    private final Controller table;
    private final AtomicReference<Controller> active;

    public DualController(Controller tree, Controller table) {
        this.tree = tree;
        this.table = table;
        this.active = new AtomicReference<>(table);
    }

    public void activateTree() {
        active.set(tree);
    }

    public void activateTable() {
        active.set(table);
    }

    @Override
    public void addControllerListener(ControllerListener listener) {
        tree.addControllerListener(listener);
        table.addControllerListener(listener);
    }

    @Override
    public void refreshBuckets() {
        active.get().refreshBuckets();
    }

    @Override
    public List<String> bucketRegions() {
        return active.get().bucketRegions();
    }

    @Override
    public List<S3Bucket> listAllMyBuckets() {
        return active.get().listAllMyBuckets();
    }

    @Override
    public void removeFailedUploads(String bucketName) {
        active.get().removeFailedUploads(bucketName);
    }

    @Override
    public void createBucket(String bucketName, String region) {
        active.get().createBucket(bucketName, region);
    }

    @Override
    public boolean bucketExists(String bucketName) {
        return active.get().bucketExists(bucketName);
    }

    @Override
    public boolean isBucketSelected() {
        return active.get().isBucketSelected();
    }

    @Override
    public String getSelectedBucketName() {
        return active.get().getSelectedBucketName();
    }

    @Override
    public void deleteCurrentBucket() {
        active.get().deleteCurrentBucket();
    }

    @Override
    public void refreshObjects() {
        active.get().refreshObjects();
    }

    @Override
    public boolean isObjectSelected() {
        return active.get().isObjectSelected();
    }

    @Override
    public String getSelectedObjectKey() {
        return active.get().getSelectedObjectKey();
    }

    @Override
    public void getSelectedObject(Callback callback) {
        active.get().getSelectedObject(callback);
    }

    @Override
    public boolean objectExists(String bucketName, String objectKey) {
        return active.get().objectExists(bucketName, objectKey);
    }

    @Override
    public void createObject(String bucketName, String objectKey, File sourceFile, ProgressListener listener) {
        active.get().createObject(bucketName, objectKey, sourceFile, listener);
    }

    @Override
    public void downloadCurrentObject(File targetFile, ProgressListener listener) {
        active.get().downloadCurrentObject(targetFile, listener);
    }

    @Override
    public String getPublicUrlForCurrentObject(DateTime expires) {
        return active.get().getPublicUrlForCurrentObject(expires);
    }

    @Override
    public void deleteCurrentObject() {
        active.get().deleteCurrentObject();
    }
}
