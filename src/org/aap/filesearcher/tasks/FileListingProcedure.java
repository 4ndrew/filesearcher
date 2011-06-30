package org.aap.filesearcher.tasks;

import org.aap.filesearcher.FileSearchTask;
import org.aap.filesearcher.TaskAcceptor;

import java.io.File;
import java.sql.SQLOutput;
import java.util.LinkedList;

/**
 * File listing.
 */
public class FileListingProcedure implements Runnable {
    private final File rootDirectory;
    private final TaskAcceptor<FileSearchTask> taskQueue;
    private final LinkedList<File> directoryQueue;

    public FileListingProcedure(File rootDirectory, TaskAcceptor<FileSearchTask> taskQueue) throws IllegalArgumentException {
        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("ERR: rootDirectory should be directory");
        }

        this.rootDirectory = rootDirectory;
        this.taskQueue = taskQueue;
        directoryQueue = new LinkedList<File>();
    }

    @Override
    public void run() {
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

        System.out.println("Listing finished.");
        taskQueue.eof();
    }

    void pushSearchTask(File f) {
        taskQueue.push(new FileSearchTask(f));
    }
}
