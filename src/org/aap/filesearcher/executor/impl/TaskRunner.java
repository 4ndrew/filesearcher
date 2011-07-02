/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package org.aap.filesearcher.executor.impl;

import org.aap.filesearcher.executor.TaskExecutor;
import org.aap.filesearcher.executor.TaskSupplier;
import org.apache.log4j.Logger;

/**
 * Runner for task.
 * @param <T> Type of Task.
 */
public class TaskRunner<T> implements Runnable {
    private final Logger logger = Logger.getLogger(TaskRunner.class);
    private final TaskSupplier<T> taskSupplier;
    private final TaskExecutor<T> taskExecutor;
    private volatile long tasksProcessed;

    public TaskRunner(TaskSupplier<T> taskSupplier, TaskExecutor<T> taskExecutor) {
        this.taskSupplier = taskSupplier;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void run() {
        tasksProcessed = 0;
        Thread executorThread = Thread.currentThread();
        while (!executorThread.isInterrupted()) {
            try {
                final T t = taskSupplier.pull();
                if (t != null) {
                    taskExecutor.execute(t);
                    tasksProcessed++;
                }
            } catch (InterruptedException e) {
                logger.info(String.format("Task runner: %s interrupted.", executorThread.getName()));
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("Task throws an exception, ignoring...", e);
            }
        }

        logger.debug(String.format("Thread %s execution completed.", executorThread.getName()));
    }

    public long getTasksProcessed() {
        return tasksProcessed;
    }

}
