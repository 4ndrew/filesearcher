/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher;

import org.aap.filesearcher.executor.TaskAcceptor;
import org.aap.filesearcher.executor.TaskExecutor;
import org.aap.filesearcher.executor.impl.*;
import org.aap.filesearcher.stats.SimpleTaskAcceptorStats;
import org.aap.filesearcher.util.ExecutorThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;

/**
 * Main class.
 * How it works:
 * <ul>
 *  <li> On start pool of threads will be created (or single-threaded queue if 0 threads specified). Each
 *  thread pulling queue for new file and begin processing.
 *  <li> {@link FileListing} performs recursive file listing and pushes file to the queue.
 *</ul>
 *
 */
public class FileSearchMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        int bufferSize = 8192;
        boolean printStats = false;
        boolean useNaive = false;
        boolean useFastNIO = false;
        boolean waitForUserInput = false;
        int threadsCount = 5;
        Charset characterSet = Charset.forName("US-ASCII");

        // Options parsing
        // TODO: migrate to gnuopts for Java if time permit
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
                    case 's':
                        printStats = validArgument = true;
                        argumentsIndex++;
                        break;
                    case 'n':
                        useNaive = validArgument = true;
                        argumentsIndex++;
                        break;
                    case 'f':
                        useFastNIO = validArgument = true;
                        argumentsIndex++;
                        break;
                    case 'b':
                        if (!(validArgument = args.length > (argumentsIndex + 1))) {
                            throw new IllegalArgumentException("Argument required for " + opts.charAt(1));
                        }
                        bufferSize = Integer.parseInt(args[argumentsIndex + 1]);
                        argumentsIndex += 2;
                        break;
                    case 't':
                        if (!(validArgument = args.length > (argumentsIndex + 1))) {
                            throw new IllegalArgumentException("Argument required for " + opts.charAt(1));
                        }
                        threadsCount = Integer.parseInt(args[argumentsIndex + 1]);
                        argumentsIndex += 2;
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

            // Algorithm selection
            TaskExecutor<FileSearchBean> taskExecutor;
            if (useNaive) {
                taskExecutor = new NaiveFileSearchTaskExecutor(patternBytes, reporter, bufferSize);
            } else if (useFastNIO) {
                taskExecutor = new KMPFileSearchTaskExecutorNIO(patternBytes, reporter, bufferSize);
            } else {
                taskExecutor = new KMPFileSearchTaskExecutor(patternBytes, reporter, bufferSize);
            }

            // Threading
            TaskAcceptor<FileSearchBean> taskAcceptor;
            final LinkedList<ExecutorThread<FileSearchBean>> threadPool = new LinkedList<ExecutorThread<FileSearchBean>>();
            if (threadsCount > 0) {
                final BlockingTaskQueue<FileSearchBean> taskQueue = new BlockingTaskQueue<FileSearchBean>(4096);
                taskAcceptor = taskQueue;

                for (int i = 0; i < threadsCount; i++) {
                    final TaskRunner<FileSearchBean> taskRunner = new TaskRunner<FileSearchBean>(taskQueue, taskExecutor);
                    final ExecutorThread<FileSearchBean> t = new ExecutorThread<FileSearchBean>(taskRunner, "Executor #" + i);
                    t.start();

                    threadPool.add(t);
                }
            } else {
                // single-thread approach
                taskAcceptor = new SingleTaskQueue<FileSearchBean>(taskExecutor);
            }

            final SimpleTaskAcceptorStats<FileSearchBean> taskCounter = new SimpleTaskAcceptorStats<FileSearchBean>(taskAcceptor);

            final FileListing fileListing = new FileListing(rootDirectory, taskCounter);
            fileListing.run();

            // Wait for all threads...
            for (ExecutorThread<FileSearchBean> t : threadPool) {
                t.join();
            }

            // Wait for threads...
            final long totalTaskProcessed = taskCounter.getTaskCount();
            final long timeSpend = System.currentTimeMillis() - startTime;
            long threadTimeTotal = 0;

            if (printStats) {
                for (ExecutorThread<FileSearchBean> t : threadPool) {
                    TaskRunner tr = t.getTaskRunner();
                    System.out.printf("Thread '%s' stats: task processed: %d, time: %d msec\n",
                            t.getName(), tr.getTasksProcessed(), tr.getThreadUptime());
                    threadTimeTotal += tr.getThreadUptime();
                }
                final long filesPerSecond = timeSpend > 0 ? (int)(totalTaskProcessed*1000/timeSpend) : totalTaskProcessed;
                System.out.printf("Execution time: %d msec (threads uptime: %d msec), files processed: %d\n" +
                        "Speed: %d files per sec\n", timeSpend, threadTimeTotal, totalTaskProcessed, filesPerSecond);
            }
        } else {
            printHelp();
        }
    }

    /**
     * Just print help to standard output.
     */
    public static void printHelp() {
        System.out.println("java FileSearcher [options] [--] <path> <string pattern>");
        System.out.println("    Options:");
        System.out.println("        -t <n>  \tSet processing threads count to <n> (Default: 5)");
        System.out.println("        -b <n>  \tSet file-input buffer to <n> (Default: 8192)");
        System.out.println("        -c <charset>  \tSet character set to <charset> (Default: \"US-ASCII\")");
        System.out.println("        -s      \tPrint stats after processing (Default: no)");
        System.out.println("        -w      \tWait for user input before start (Default: no)");
        System.out.println("        -n      \tUse Naive search algorithm (Default: no)");
        System.out.println();
        System.out.println("    <path> - root path");
        System.out.println("    <string pattern> - string pattern for search");
    }
}
