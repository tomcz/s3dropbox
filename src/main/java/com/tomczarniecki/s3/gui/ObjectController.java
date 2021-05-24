package com.tomczarniecki.s3.gui;

import com.tomczarniecki.s3.ProgressListener;
import com.tomczarniecki.s3.S3Object;
import org.joda.time.DateTime;

import java.io.File;

public interface ObjectController {

    interface Callback {
        void selectedObject(S3Object object);
    }

    boolean isObjectSelected();

    String getSelectedObjectKey();

    String getSelectedBucketName();

    String getPublicUrlForCurrentObject(DateTime expires);

    void deleteCurrentObject();

    void downloadCurrentObject(File target, ProgressListener listener);

    void getSelectedObject(Callback callback);
}
