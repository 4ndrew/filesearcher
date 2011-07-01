/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher;

import org.aap.filesearcher.executor.TaskAcceptor;
import org.aap.filesearcher.executor.TaskExecutor;
import org.aap.filesearcher.executor.impl.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;

/**
 * Main class.
 */
public class FileSearchMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        boolean useNaive = false;
        boolean waitForUserInput = false;
        int threadsCount = 5;
        Charset characterSet = Charset.forName("US-ASCII");

        int argumentsIndex = 0;
        while (args.length > argumentsIndex) {
            final String opts = args[argumentsIndex];
            boolean validArgument = false;
            if (opts.startsWith("-") && opts.length() > 1) {
                switch (opts.charAt(1)) {
                    case 'w':
                        waitForUserInput = validArgument = true;
                        argumentsIndex++;
                        break;
                    case 'n':
                        useNaive = validArgument = true;
                        argumentsIndex++;
                        break;
                    case 't':
                        validArgument = args.length > (argumentsIndex + 1);
                        if (validArgument) {
                            threadsCount = Integer.parseInt(args[argumentsIndex + 1]);
                            argumentsIndex += 2;
                        }
                        break;
                    case 'c':
                        validArgument = args.length > (argumentsIndex + 1);
                        if (validArgument) {
                            characterSet = Charset.forName(args[argumentsIndex + 1]);
                            if (characterSet == null) {
                                throw new IllegalArgumentException("Invalid charset specified: " + args[argumentsIndex + 1]);
                            }
                            argumentsIndex += 2;
                        }
                        break;
                    case '-':
                        argumentsIndex++;
                        break;
                }
            }

            if (!validArgument) {
                break;
            }
        }

        if (args.length - argumentsIndex > 1) {
            final File rootDirectory = new File(args[argumentsIndex]);
            final String stringPattern = args[argumentsIndex + 1];

            if (!rootDirectory.exists()) {
                throw new FileNotFoundException(rootDirectory + " not found");
            }

            if (!rootDirectory.isDirectory()) {
                throw new IllegalArgumentException("Input should be directory");
            }

            if (waitForUserInput) {
                System.out.println("Hit ENTER to start search!");
                //noinspection ResultOfMethodCallIgnored
                System.in.read();
            }

            final TaskAcceptor<FileSearchBean> reporter = new TaskAcceptor<FileSearchBean>() {
                @Override
                public void push(FileSearchBean task) {
                    System.out.println(task.getInputFile().toString());
                }

                @Override
                public void signalEndOfData() {
                }
            };

            final long startTime = System.currentTimeMillis();

            final byte[] patternBytes = stringPattern.getBytes(characterSet);

            TaskExecutor<FileSearchBean> taskExecutor;
            if (useNaive) {
                taskExecutor = new NaiveFileSearchTaskExecutor(patternBytes, reporter);
            } else {
                taskExecutor = new KMPFileSearchTaskExecutor(patternBytes, reporter);
            }

            TaskAcceptor<FileSearchBean> taskAcceptor;
            final LinkedList<TaskRunner<FileSearchBean>> threadPool = new LinkedList<TaskRunner<FileSearchBean>>();
            if (threadsCount > 0) {
                final BlockingTaskQueue<FileSearchBean> taskQueue = new BlockingTaskQueue<FileSearchBean>(4096);
                taskAcceptor = taskQueue;

                for (int i = 0; i < threadsCount; i++) {
                    final TaskRunner<FileSearchBean> taskRunner = new TaskRunner<FileSearchBean>(taskQueue, taskExecutor);
                    final Thread t = new Thread(taskRunner, "Executor #" + i);
                    t.start();

                    threadPool.add(taskRunner);
                }
            } else {
                taskAcceptor = new SingleTaskQueue<FileSearchBean>(taskExecutor);
            }

            FileListing fileListing = new FileListing(rootDirectory, taskAcceptor);
            fileListing.run();

            long totalTaskProcessed = 0;
            for (TaskRunner t : threadPool) {
                t.getExecutorThread().join();
                System.out.printf("Thread: %s stopped, task processed: %d\n",
                        t.getExecutorThread().getName(), t.getTasksProcessed());
                totalTaskProcessed += t.getTasksProcessed();
            }
            final long timeSpend = System.currentTimeMillis() - startTime;
            final long filesPerSecond = timeSpend > 0 ? (int)(totalTaskProcessed*1000/timeSpend) : totalTaskProcessed;
            System.out.printf("Execution time: %d msec, files processed: %d\n" +
                    "Speed: %d files per sec\n", timeSpend, totalTaskProcessed, filesPerSecond);
        } else {
            printHelp();
        }
    }

    public static void printHelp() {
        System.out.println("java FileSearcher [options] [--] <path> <string pattern>");
        System.out.println("    Options:");
        System.out.println("        -w      \tWait for user input before start (Default: no)");
        System.out.println("        -n      \tUse Naive search algorithm (Default: no)");
        System.out.println("        -t <n>  \tSet processing threads count to <n> (Default: 5)");
        System.out.println("        -c <charset>  \tSet character set to <charset> (Default: \"US-ASCII\")");
        System.out.println();
        System.out.println("    <path> - root path");
        System.out.println("    <string pattern> - string pattern for search");
    }
}
