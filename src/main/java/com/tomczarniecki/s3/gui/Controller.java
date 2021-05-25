package com.tomczarniecki.s3.gui;

import com.tomczarniecki.s3.ProgressListener;
import com.tomczarniecki.s3.S3Bucket;
import com.tomczarniecki.s3.S3Object;
import org.joda.time.DateTime;

import java.io.File;
import java.util.List;

public interface Controller {

    void addControllerListener(ControllerListener listener);

    void refreshBuckets();

    List<String> bucketRegions();

    List<S3Bucket> listAllMyBuckets();

    void removeFailedUploads(String bucketName);

    void createBucket(String bucketName, String region);

    boolean bucketExists(String bucketName);

    boolean isBucketSelected();

    String getSelectedBucketName();

    void deleteCurrentBucket();

    void refreshObjects();

    boolean isObjectSelected();

    String getSelectedObjectKey();

    void getSelectedObject(Callback callback);

    boolean objectExists(String bucketName, String objectKey);

    void createObject(String bucketName, String objectKey, File sourceFile, ProgressListener listener);

    void downloadCurrentObject(File targetFile, ProgressListener listener);

    String getPublicUrlForCurrentObject(DateTime expires);

    void deleteCurrentObject();

    interface Callback {
        void selectedObject(S3Object object);
    }
}

