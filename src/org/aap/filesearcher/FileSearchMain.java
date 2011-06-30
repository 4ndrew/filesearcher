/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher;

import org.aap.filesearcher.tasks.FileListingProcedure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;

/**
 * Main class.
 */
public class FileSearchMain {
    final static int THREADS_COUNT = 5;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 1) {
            final File rootDirectory = new File(args[0]);
            final String stringPattern = args[1];

            if (!rootDirectory.exists()) {
                throw new FileNotFoundException(rootDirectory + " not found");
            }

            if (!rootDirectory.isDirectory()) {
                throw new IllegalArgumentException("Input should be directory");
            }

            // VisualVM on Mac failed to connect to jvm...
            System.out.println("Hit ENTER to start search!");
            System.in.read();

            final TaskQueue<FileSearchTask> taskQueue = new TaskQueue<FileSearchTask>();
            final NaiveFileSearchTaskExecutor taskExecutor = new NaiveFileSearchTaskExecutor(stringPattern,
                    Charset.forName("US-ASCII"), new TaskAcceptor<FileSearchTask>() {
                @Override
                public void push(FileSearchTask task) {
                    System.out.println(task.getInputFile().toString());
                }

                @Override
                public void eof() {
                }
            });
            final LinkedList<TaskRunner<FileSearchTask>> threadPool = new LinkedList<TaskRunner<FileSearchTask>>();

            for (int i = 0; i < THREADS_COUNT; i++) {
                final TaskRunner<FileSearchTask> taskRunner = new TaskRunner<FileSearchTask>(taskQueue, taskExecutor);
                final Thread t = new Thread(taskRunner, "Executor #" + i);
                t.start();

                threadPool.add(taskRunner);
            }

            FileListingProcedure fileListingProcedure = new FileListingProcedure(rootDirectory, taskQueue);
            fileListingProcedure.run();

            for (TaskRunner t : threadPool) {
                t.getExecutorThread().join();
                System.out.printf("Thread: %s stopped, task processed: %d\n",
                        t.getExecutorThread().getName(), t.getTasksProcessed());
            }
        } else {
            printHelp();
        }
    }

    public static void printHelp() {
        System.out.println("FileSearcher <path> <string pattern>");
        System.out.println("    <path> - root path");
        System.out.println("    <string pattern> - string pattern for search");
    }
}
