/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package org.aap.filesearcher.executor.impl;

import org.aap.filesearcher.executor.TaskExecutor;
import org.aap.filesearcher.executor.TaskSupplier;

/**
 * Runner for task.
 * @param <T> Type of Task.
 */
public class TaskRunner<T> implements Runnable {
    private final TaskSupplier<T> taskSupplier;
    private final TaskExecutor<T> taskExecutor;
    private volatile long tasksProcessed;
    private volatile Thread executorThread;

    public TaskRunner(TaskSupplier<T> taskSupplier, TaskExecutor<T> taskExecutor) {
        this.taskSupplier = taskSupplier;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void run() {
        tasksProcessed = 0;
        executorThread = Thread.currentThread();
        while (!executorThread.isInterrupted()) {
            try {
                final T t = taskSupplier.pull();
                if (t != null) {
                    taskExecutor.execute(t);
                    tasksProcessed++;
                }
            } catch (InterruptedException e) {
                System.out.println("Task runner: " + executorThread.getName() + " interrupted.");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Task throws an exception, ignoring...");
                e.printStackTrace(System.err);
            }
        }
    }

    public long getTasksProcessed() {
        return tasksProcessed;
    }

    public Thread getExecutorThread() {
        return executorThread;
    }

}
