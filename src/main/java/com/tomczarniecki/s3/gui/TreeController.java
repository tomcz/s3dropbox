package com.tomczarniecki.s3.gui;

import com.tomczarniecki.s3.Pair;
import com.tomczarniecki.s3.ProgressListener;
import com.tomczarniecki.s3.S3Bucket;
import com.tomczarniecki.s3.S3List;
import com.tomczarniecki.s3.S3Object;
import com.tomczarniecki.s3.Service;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.Enumeration;
import java.util.List;

public class TreeController implements TreeSelectionListener, TreeWillExpandListener, Controller {

    public static final String LOADING = "loading ...";
    public static final String EMPTY = "<empty>";

    private final Announcer<ControllerListener> announcer;
    private final DefaultMutableTreeNode root;
    private final DefaultTreeModel model;
    private final Service service;
    private final Worker worker;

    private TreePath selectedPath;

    public TreeController(Service service, Worker worker) {
        this.root = loadingNode("root");
        this.model = new DefaultTreeModel(root);
        this.announcer = Announcer.createFor(ControllerListener.class);
        this.service = service;
        this.worker = worker;
    }

    public DefaultTreeModel getModel() {
        return model;
    }

    public void addControllerListener(ControllerListener listener) {
        announcer.add(listener);
    }

    public List<String> bucketRegions() {
        return service.bucketRegions();
    }

    public List<S3Bucket> listAllMyBuckets() {
        return service.listAllMyBuckets();
    }

    public void removeFailedUploads(String bucketName) {
        service.removeFailedUploads(bucketName);
    }

    public void createBucket(String bucketName, String region) {
        service.createBucket(bucketName, region);
        refreshBuckets();
    }

    public boolean bucketExists(String bucketName) {
        return service.bucketExists(bucketName);
    }

    public void deleteCurrentBucket() {
        service.deleteBucket(getSelectedBucketName());
        refreshBuckets();
    }

    public boolean objectExists(String bucketName, String objectKey) {
        return service.objectExists(bucketName, objectKey);
    }

    public void createObject(String bucketName, String objectKey, File sourceFile, ProgressListener listener) {
        service.createObject(bucketName, objectKey, sourceFile, listener);
        refreshObjects(bucketName);
    }

    public boolean isBucketSelected() {
        if (selectedPath == null) {
            return false;
        }
        Pair<String, String> pair = bucketAndObjectKey(selectedPath);
        return !pair.getLeft().isEmpty();
    }

    public boolean isObjectSelected() {
        if (selectedPath == null) {
            return false;
        }
        Pair<String, String> pair = bucketAndObjectKey(selectedPath);
        String bucket = pair.getLeft();
        String key = pair.getRight();
        return !bucket.isEmpty()
                && !key.isEmpty()
                && !key.endsWith(EMPTY)
                && !key.endsWith(LOADING)
                && !key.endsWith(Service.DELIMITER);
    }

    public String getSelectedBucketName() {
        return bucketAndObjectKey(selectedPath).getLeft();
    }

    public String getSelectedObjectKey() {
        return bucketAndObjectKey(selectedPath).getRight();
    }

    public String getPublicUrlForCurrentObject(DateTime expires) {
        Pair<String, String> pair = bucketAndObjectKey(selectedPath);
        return service.getPublicUrl(pair.getLeft(), pair.getRight(), expires);
    }

    public void downloadCurrentObject(File target, ProgressListener listener) {
        Pair<String, String> pair = bucketAndObjectKey(selectedPath);
        service.downloadObject(pair.getLeft(), pair.getRight(), target, listener);
    }

    public void getSelectedObject(Callback callback) {
        Pair<String, String> pair = bucketAndObjectKey(selectedPath);
        S3Object object = service.getObject(pair.getLeft(), pair.getRight());
        callback.selectedObject(object);
    }

    public void deleteCurrentObject() {
        Pair<String, String> pair = bucketAndObjectKey(selectedPath);
        service.deleteObject(pair.getLeft(), pair.getRight());
        worker.executeOnEventLoop(() -> {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) child.getParent();
            parent.remove(child);
            mustBeParent(parent);
            model.reload(parent);
            selectedPath = null;
        });
    }

    public void refreshBuckets() {
        worker.executeInBackground(() -> {
            List<S3Bucket> buckets = service.listAllMyBuckets();
            worker.executeOnEventLoop(() -> {
                root.removeAllChildren();
                for (S3Bucket bucket : buckets) {
                    root.add(loadingNode(bucket.getName()));
                }
                model.reload(root);
                selectedPath = null;
            });
        });
    }

    public void refreshObjects() {
        if (selectedPath == null) {
            refreshBuckets();
        } else if (isBucketSelected()) {
            refreshObjects(getSelectedBucketName());
        }
    }

    public void refreshObjects(String bucketName) {
        Enumeration<TreeNode> children = root.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            if (child.toString().equals(bucketName)) {
                loadChildren(Pair.pair(bucketName, ""), child);
                return; // no need to look further
            }
        }
    }

    public void valueChanged(TreeSelectionEvent event) {
        selectedPath = event.getPath();
        Pair<String, String> pair = bucketAndObjectKey(selectedPath);
        String bucketName = pair.getLeft();
        String objectKey = pair.getRight();
        if (bucketName.isEmpty() || objectKey.isEmpty()) {
            announcer.announce().showingBuckets();
        } else {
            announcer.announce().showingObjects(bucketName);
        }
    }

    public void treeWillExpand(TreeExpansionEvent event) {
        TreePath path = event.getPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        String childName = node.getFirstChild().toString();
        if (childName.equals(LOADING) || childName.equals(EMPTY)) {
            loadChildren(bucketAndObjectKey(path), node);
        }
    }

    public void treeWillCollapse(TreeExpansionEvent event) {
        // don't need to clear out collapsed tree for now
    }

    private void loadChildren(Pair<String, String> bucketAndPrefix, DefaultMutableTreeNode node) {
        worker.executeInBackground(() -> {
            String bucketName = bucketAndPrefix.getLeft();
            String prefix = bucketAndPrefix.getRight();
            S3List res = service.listItemsInBucket(bucketName, prefix);
            worker.executeOnEventLoop(() -> {
                node.removeAllChildren();
                for (String folder : res.getFolders()) {
                    node.add(loadingNode(StringUtils.removeStart(folder, prefix)));
                }
                for (String file : res.getFiles()) {
                    node.add(leafNode(StringUtils.removeStart(file, prefix)));
                }
                mustBeParent(node);
                model.reload(node);
                selectedPath = null;
            });
        });
    }

    private static Pair<String, String> bucketAndObjectKey(TreePath path) {
        // = 0 root
        // = 1 bucket
        // > 1 path within bucket
        Object[] elements = path.getPath();
        String bucketName = "";
        if (elements.length > 1) {
            bucketName = elements[1].toString();
        }
        StringBuilder objectKey = new StringBuilder();
        if (elements.length > 2) {
            for (int i = 2; i < elements.length; i++) {
                objectKey.append(elements[i]);
            }
        }
        return Pair.pair(bucketName, objectKey.toString());
    }

    private static DefaultMutableTreeNode leafNode(String name) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
        node.setAllowsChildren(false);
        return node;
    }

    private static DefaultMutableTreeNode loadingNode(String name) {
        DefaultMutableTreeNode parent = new DefaultMutableTreeNode(name);
        parent.add(leafNode(LOADING));
        return parent;
    }

    private static void mustBeParent(DefaultMutableTreeNode node) {
        if (node.getChildCount() == 0) {
            node.add(leafNode(EMPTY));
        }
    }
}
