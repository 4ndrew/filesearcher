/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher;

import org.aap.filesearcher.executor.TaskAcceptor;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.LinkedList;

/**
 * File listing.
 */
public class FileListing implements Runnable {
    private final Logger logger = Logger.getLogger(FileListing.class);
    private final File rootDirectory;
    private final TaskAcceptor<FileSearchBean> taskQueue;
    private final LinkedList<File> directoryQueue;

    public FileListing(File rootDirectory, TaskAcceptor<FileSearchBean> taskQueue) throws IllegalArgumentException {
        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("ERR: rootDirectory should be directory");
        }

        this.rootDirectory = rootDirectory;
        this.taskQueue = taskQueue;
        directoryQueue = new LinkedList<File>();
    }

    @Override
    public void run() {
        try {
            directoryQueue.addLast(rootDirectory);

            while (directoryQueue.size() > 0) {
                File rootNode = directoryQueue.removeFirst();
                for (File f : rootNode.listFiles()) {
                    if (f.isDirectory()) {
                        directoryQueue.add(f);
                    } else if (f.isFile()) {
                        pushSearchTask(f);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        } finally {
            logger.info("Listing finished.");
            taskQueue.signalEndOfData();
        }
    }

    void pushSearchTask(File f) throws InterruptedException {
        taskQueue.push(new FileSearchBean(f));
    }
}
