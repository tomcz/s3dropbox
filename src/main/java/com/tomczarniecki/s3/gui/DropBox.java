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

import com.tomczarniecki.s3.PreferenceSetter;
import com.tomczarniecki.s3.Service;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.concurrent.Executor;

import static com.tomczarniecki.s3.gui.Constants.ALL_FOLDERS;
import static com.tomczarniecki.s3.gui.Constants.FOLDER_NAME;
import static com.tomczarniecki.s3.gui.Constants.MAIN_TABLE_NAME;
import static com.tomczarniecki.s3.gui.Constants.MAIN_WINDOW_NAME;

public class DropBox extends JFrame {

    private final UploadWorker uploader;
    private final Controller controller;
    private final Executor executor;
    private final Display display;
    private final Worker worker;
    private final JPanel cards;

    public DropBox(Service service, PreferenceSetter prefs) {
        super(String.format(FOLDER_NAME, ALL_FOLDERS));
        setName(MAIN_WINDOW_NAME);

        display = new Display(this);
        worker = new DropBoxWorker();
        controller = new Controller(service);
        executor = new BusyCursorExecutor(display, worker);
        uploader = new UploadWorker(controller, display, worker);

        JMenu bucketMenu = createBucketMenu();
        JMenu objectMenu = createObjectMenu();

        DropBoxTableModel table = new DropBoxTableModel(worker);
        DropBoxTreeModel tree = new DropBoxTreeModel(service, worker);
        MenuSwitcher switcher = new MenuSwitcher(display, bucketMenu, objectMenu, worker);

        controller.addListener(tree);
        controller.addListener(table);
        controller.addListener(switcher);

        setJMenuBar(createMenuBar(bucketMenu, objectMenu, prefs, tree));

        cards = new JPanel(new CardLayout());
        cards.add("table", createTable(table));
        cards.add("tree", createTree(tree));

        JScrollPane scrollPane = new JScrollPane(cards);
        FileDrop.add(scrollPane, new FileDropListener(controller, table, display, worker, uploader));
        getContentPane().add(scrollPane);

        setSize(600, 300);
        setLocationRelativeTo(null);
    }

    private JTable createTable(DropBoxTableModel model) {
        JTable table = new JTable(model);
        table.setName(MAIN_TABLE_NAME);

        TableColumn iconColumn = table.getColumnModel().getColumn(0);
        iconColumn.setMaxWidth(16);
        iconColumn.setPreferredWidth(16);

        TableColumn sizeColumn = table.getColumnModel().getColumn(2);
        DefaultTableCellRenderer sizeRenderer = new DefaultTableCellRenderer();
        sizeRenderer.setHorizontalAlignment(JLabel.RIGHT);
        sizeColumn.setCellRenderer(sizeRenderer);
        sizeColumn.setMaxWidth(100);
        sizeColumn.setPreferredWidth(100);

        TableColumn dateColumn = table.getColumnModel().getColumn(3);
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer();
        dateRenderer.setHorizontalAlignment(JLabel.RIGHT);
        dateColumn.setCellRenderer(dateRenderer);
        dateColumn.setMaxWidth(150);
        dateColumn.setPreferredWidth(150);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new DropBoxTableListener(controller, model, table));

        table.addMouseListener(new DoubleClickListener(controller, executor));
        table.addMouseListener(createRightClickListener(controller));

        return table;
    }

    private JTree createTree(DropBoxTreeModel model) {
        JTree tree = new JTree(model.getModel());
        tree.addMouseListener(createRightClickListener(model));
        tree.addTreeWillExpandListener(model);
        tree.addTreeSelectionListener(model);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        return tree;
    }

    private JMenuBar createMenuBar(JMenu bucketMenu, JMenu objectMenu, PreferenceSetter prefs, DropBoxTreeModel tree) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(bucketMenu);
        menuBar.add(objectMenu);
        menuBar.add(toolsMenu());
        menuBar.add(viewMenu(prefs, tree));
        return menuBar;
    }

    private JMenu createBucketMenu() {
        JMenu menu = new JMenu("Folders");
        menu.add(new JMenuItem(new CreateBucketAction(controller, display, executor)));
        menu.add(new JMenuItem(new DeleteBucketAction(controller, display, executor, worker)));
        menu.add(new JMenuItem(new RefreshBucketsAction(controller, executor)));
        return menu;
    }

    private JMenu createObjectMenu() {
        JMenu menu = new JMenu("Files");
        menu.add(new JMenuItem(new UploadFileAction(display, worker, uploader)));
        menu.add(new JMenuItem(new CreatePublicLinkAction(controller, display)));
        DownloadWorker downloader = new DownloadWorker(controller, display, worker);
        menu.add(new JMenuItem(new DownloadObjectAction(controller, display, downloader)));
        menu.add(new JMenuItem(new DeleteObjectAction(controller, display, executor)));
        menu.add(new JMenuItem(new RefreshObjectsAction(controller, executor)));
        menu.setVisible(false);
        return menu;
    }

    private JMenu toolsMenu() {
        JMenu menu = new JMenu("Tools");
        menu.add(new JMenuItem(new RemoveFailedUploadsAction(controller, display, worker)));
        return menu;
    }

    private JMenu viewMenu(PreferenceSetter prefs, DropBoxTreeModel tree) {
        JCheckBoxMenuItem darkMode = new JCheckBoxMenuItem("Dark Mode (on restart)", prefs.isDarkMode());
        darkMode.addActionListener(EventHandler.create(ActionListener.class, prefs, "darkMode", "source.selected"));

        JCheckBoxMenuItem showTree = new JCheckBoxMenuItem("Tree View", false);
        showTree.addActionListener(e -> {
            tree.setVisible(showTree.isVisible());
            executor.execute(controller::showBuckets);
            CardLayout layout = (CardLayout) cards.getLayout();
            if (showTree.isSelected()) {
                layout.show(cards, "tree");
            } else {
                layout.show(cards, "table");
            }
        });

        JMenu menu = new JMenu("View");
        menu.add(showTree);
        menu.add(darkMode);
        return menu;
    }

    private RightClickListener createRightClickListener(ObjectController controller) {
        RightClickListener listener = new RightClickListener(controller, display);
        listener.addAction(new ShowDetailsAction(controller, display, executor, worker));
        listener.addAction(new CreatePublicLinkAction(controller, display));
        DownloadWorker downloader = new DownloadWorker(controller, display, worker);
        listener.addAction(new DownloadObjectAction(controller, display, downloader));
        listener.addAction(new DeleteObjectAction(controller, display, executor));
        return listener;
    }

    public void showBuckets() {
        setVisible(true);
        executor.execute(controller::showBuckets);
    }
}
