package com.tomczarniecki.s3.gui;

public interface SwingWorker {
    void executeOnEventLoop(Runnable command);
}
