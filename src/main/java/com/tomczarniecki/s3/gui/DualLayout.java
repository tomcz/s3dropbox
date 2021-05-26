package com.tomczarniecki.s3.gui;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import java.awt.CardLayout;

public class DualLayout {

    private final JPanel panel;
    private final CardLayout layout;
    private final DualController controller;

    public DualLayout(DualController ctrl, JTable table, JTree tree) {
        layout = new CardLayout();
        panel = new JPanel(layout);
        panel.add("table", table);
        panel.add("tree", tree);
        controller = ctrl;
    }

    public JPanel getPanel() {
        return panel;
    }

    public void show(boolean treeView) {
        if (treeView) {
            controller.activateTree();
            layout.show(panel, "tree");
        } else {
            controller.activateTable();
            layout.show(panel, "table");
        }
    }
}
