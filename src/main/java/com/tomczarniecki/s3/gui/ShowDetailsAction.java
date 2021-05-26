package com.tomczarniecki.s3.gui;

import com.tomczarniecki.s3.S3Object;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.concurrent.Executor;

public class ShowDetailsAction extends AbstractAction implements Controller.Callback {

    private final Controller controller;
    private final Display display;
    private final Executor executor;
    private final SwingWorker worker;

    public ShowDetailsAction(Controller controller, Display display, Executor executor, SwingWorker worker) {
        super("Show Details");
        this.controller = controller;
        this.display = display;
        this.executor = executor;
        this.worker = worker;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (controller.isObjectSelected()) {
            executor.execute(() -> controller.getSelectedObject(ShowDetailsAction.this));
        }
    }

    @Override
    public void selectedObject(S3Object object) {
        worker.executeOnEventLoop(() -> {
            String msg = String.format(
                    "Key: %s\nSize: %s\nLast Modified: %s",
                    object.getKey(), object.getSize(), object.getLastModified()
            );
            display.showMessage("Details", msg);
        });
    }
}
