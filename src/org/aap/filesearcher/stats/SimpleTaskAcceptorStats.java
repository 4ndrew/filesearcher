/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher.stats;

import org.aap.filesearcher.executor.TaskAcceptor;

/**
 * Utility decorator class to calculate simple statistics.
 * Not thread-safe.
 */
public class SimpleTaskAcceptorStats<T> implements TaskAcceptor<T> {
    private final TaskAcceptor<T> taskAcceptor;
    private long taskCount;
    private long constructionTime;
    private long totalWaitTime;

    public SimpleTaskAcceptorStats(TaskAcceptor<T> taskAcceptor) {
        this.taskAcceptor = taskAcceptor;
        taskCount = 0;
        totalWaitTime = 0;
        constructionTime = System.currentTimeMillis();
    }

    @Override
    public void push(T task) throws IllegalArgumentException, InterruptedException {
        taskCount++;

        // final long startTime = System.currentTimeMillis();
        taskAcceptor.push(task);
        // totalWaitTime += System.currentTimeMillis() - startTime;
    }

    @Override
    public void signalEndOfData() {
        taskAcceptor.signalEndOfData();
    }

    public long getTaskCount() {
        return taskCount;
    }

    public long getConstructionTime() {
        return constructionTime;
    }

    public long getTotalWaitTime() {
        return totalWaitTime;
    }
}
