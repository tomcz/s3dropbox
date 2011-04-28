package com.tomczarniecki.s3.gui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileDragListener extends DragSourceAdapter implements DragGestureListener {

    private final Component source;
    private final Controller controller;
    private final DownloadWorker worker;

    public FileDragListener(Controller controller, Component source, DownloadWorker worker) {
        this.controller = controller;
        this.source = source;
        this.worker = worker;
    }

    public void dragGestureRecognized(DragGestureEvent evt) {
        if (controller.isObjectSelected()) {

            File target = createFile(controller.getSelectedObjectKey());
            RemoteFileTransferable transferable = new RemoteFileTransferable(target);

            if (DragSource.isDragImageSupported()) {
                Toolkit tk = Toolkit.getDefaultToolkit();
                BufferedImage icon = createIconForFile(target, tk);
                evt.startDrag(DragSource.DefaultMoveDrop, icon, new Point(0, 0), transferable, this);
            } else {
                evt.startDrag(DragSource.DefaultMoveDrop, null, new Point(0, 0), transferable, this);
            }
        }
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent evt) {
        if (evt.getDropSuccess()) {
            System.out.println("Drop was successful");
        }
    }

    private File createFile(String selectedObjectKey) {
        try {
            String name = FilenameUtils.getName(selectedObjectKey);
            File target = new File(SystemUtils.getJavaIoTmpDir(), name);
            FileUtils.touch(target);
            return target;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage createIconForFile(File target, Toolkit tk) {
        Icon icn = FileSystemView.getFileSystemView().getSystemIcon(target);
        Dimension dim = tk.getBestCursorSize(icn.getIconWidth(), icn.getIconHeight());
        BufferedImage buff = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
        icn.paintIcon(source, buff.getGraphics(), 0, 0);
        return buff;
    }
}
