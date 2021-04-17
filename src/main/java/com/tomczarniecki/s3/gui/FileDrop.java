/*
 * Copyright (c) 2009, Thomas Czarniecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of S3DropBox, Thomas Czarniecki, tomczarniecki.com nor
 *    the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tomczarniecki.s3.gui;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

public class FileDrop {

    private static final Logger logger = LoggerFactory.getLogger(FileDrop.class);

    public static void add(Component component, Listener listener) {
        add(component, listener, true);
    }

    public static void add(Component component, Listener listener, boolean recursive) {
        DropTargetListener dropListener = new FileDropTargetListener(component, listener);
        makeDropTarget(component, dropListener, recursive);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static void remove(Component component) {
        remove(component, true);
    }

    @SuppressWarnings("ConstantConditions")
    public static void remove(Component component, boolean recursive) {
        component.setDropTarget(null);
        if (recursive && (component instanceof Container)) {
            Container container = (Container) component;
            for (Component item : container.getComponents()) {
                remove(item, recursive);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void makeDropTarget(Component component, DropTargetListener listener, boolean recursive) {
        try {
            DropTarget dt = new DropTarget();
            dt.addDropTargetListener(listener);
        } catch (TooManyListenersException e) {
            logger.debug("Unexpected", e);
        }

        // Listen for hierarchy changes and remove the drop target when the parent gets cleared out.
        component.addHierarchyListener(new ComponentHierarchyListener(component, listener));

        if (component.getParent() != null) {
            new DropTarget(component, listener);
        }

        if (recursive && (component instanceof Container)) {
            Container container = (Container) component;
            for (Component item : container.getComponents()) {
                makeDropTarget(item, listener, recursive);
            }
        }
    }

    public interface Listener {
        void filesDropped(File[] files);
    }

    private static class FileDropTargetListener implements DropTargetListener {

        private static final String ZERO_CHAR_STRING = "" + (char) 0;

        private final Border dragBorder;
        private final Listener listener;
        private final Component component;

        private Border normalBorder;

        public FileDropTargetListener(Component component, Listener listener) {
            this.dragBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(0f, 0f, 1f, 0.25f));
            this.component = component;
            this.listener = listener;
        }

        public void dragEnter(DropTargetDragEvent evt) {
            if (isDragOk(evt)) {
                setDragBorder();
                evt.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                evt.rejectDrag();
            }
        }

        public void dragExit(DropTargetEvent evt) {
            unsetDragBorder();
        }

        public void dragOver(DropTargetDragEvent evt) {
            // This is called continually as long as the mouse is over the drag target.
        }

        public void drop(DropTargetDropEvent evt) {
            try {
                Transferable tr = evt.getTransferable();
                if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    handleDefaultDrop(evt, tr);
                } else {
                    handleLinuxDrop(evt, tr);
                }
            } catch (Exception e) {
                logger.info("Bad drop", e);
                evt.rejectDrop();

            } finally {
                unsetDragBorder();
            }
        }

        public void dropActionChanged(DropTargetDragEvent evt) {
            if (isDragOk(evt)) {
                evt.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                evt.rejectDrag();
            }
        }

        private boolean isDragOk(DropTargetDragEvent evt) {
            for (DataFlavor flavor : evt.getCurrentDataFlavors()) {
                if (DataFlavor.javaFileListFlavor.equals(flavor) || flavor.isRepresentationClassReader()) {
                    return true;
                }
            }
            return false;
        }

        private void setDragBorder() {
            if (component instanceof JComponent) {
                JComponent jc = (JComponent) component;
                normalBorder = jc.getBorder();
                jc.setBorder(dragBorder);
            }
        }

        private void unsetDragBorder() {
            if (component instanceof JComponent) {
                JComponent jc = (JComponent) component;
                jc.setBorder(normalBorder);
            }
        }

        private void handleDefaultDrop(DropTargetDropEvent evt, Transferable tr) throws Exception {
            evt.acceptDrop(DnDConstants.ACTION_COPY);
            List<?> fileList = (List<?>) tr.getTransferData(DataFlavor.javaFileListFlavor);
            listener.filesDropped(convertToArray(fileList));
            evt.getDropTargetContext().dropComplete(true);
        }

        private void handleLinuxDrop(DropTargetDropEvent evt, Transferable tr) throws Exception {
            DataFlavor dataFlavor = getLinuxDataFlavor(tr);
            if (dataFlavor != null) {
                evt.acceptDrop(DnDConstants.ACTION_COPY);
                Reader reader = dataFlavor.getReaderForText(tr);
                listener.filesDropped(createFileArray(reader));
                evt.getDropTargetContext().dropComplete(true);
            } else {
                evt.rejectDrop();
            }
        }

        private DataFlavor getLinuxDataFlavor(Transferable tr) {
            for (DataFlavor flavor : tr.getTransferDataFlavors()) {
                if (flavor.isRepresentationClassReader()) {
                    return flavor;
                }
            }
            return null;
        }

        private File[] createFileArray(Reader reader) throws Exception {
            List<File> files = new ArrayList<>();
            for (String line : readLines(reader)) {
                // kde seems to append a 0 char to the end of the reader
                if (!ZERO_CHAR_STRING.equals(line)) {
                    try {
                        files.add(new File(new URI(line)));
                    } catch (URISyntaxException e) {
                        logger.warn("Bad URI", e);
                    }
                }
            }
            return convertToArray(files);
        }

        private List<String> readLines(Reader reader) throws Exception {
            try {
                return IOUtils.readLines(reader);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private File[] convertToArray(List files) {
            return (File[]) files.toArray(new File[0]);
        }
    }

    private static class ComponentHierarchyListener implements HierarchyListener {

        private final Component component;
        private final DropTargetListener listener;

        ComponentHierarchyListener(Component component, DropTargetListener listener) {
            this.component = component;
            this.listener = listener;
        }

        public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
            Component parent = component.getParent();
            if (parent == null) {
                component.setDropTarget(null);
            } else {
                new DropTarget(component, listener);
            }
        }
    }
}
