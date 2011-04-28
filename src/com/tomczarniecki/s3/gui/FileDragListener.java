package com.tomczarniecki.s3.gui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileDragListener extends DragSourceAdapter implements DragGestureListener {

    private final Controller controller;
    private final Component source;

    private Cursor cursor;

    public FileDragListener(Controller controller, Component source) {
        this.controller = controller;
        this.source = source;
    }

    public void dragGestureRecognized(DragGestureEvent evt) {
        if (controller.isObjectSelected()) {
            File target = createFile();

            Toolkit tk = Toolkit.getDefaultToolkit();
            BufferedImage icon = createIconForFile(target, tk);
            RemoteFileTransferable transferable = new RemoteFileTransferable(target);

            if (DragSource.isDragImageSupported()) {
                evt.startDrag(DragSource.DefaultMoveDrop, icon, new Point(0, 0), transferable, this);
            } else {
                cursor = tk.createCustomCursor(icon, new Point(0, 0), target.getName());
                evt.startDrag(cursor, null, new Point(0, 0), transferable, this);
            }
        }
    }

    public void dragEnter(DragSourceDragEvent evt) {
        DragSourceContext ctx = evt.getDragSourceContext();
        ctx.setCursor(cursor);
    }

    public void dragExit(DragSourceEvent evt) {
        DragSourceContext ctx = evt.getDragSourceContext();
        ctx.setCursor(DragSource.DefaultMoveDrop);
    }

    private File createFile() {
        try {
            String name = FilenameUtils.getName(controller.getSelectedObjectKey());
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
