/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher.executor.impl;

import org.aap.filesearcher.executor.TaskAcceptor;
import org.aap.filesearcher.executor.TaskExecutor;

/**
 * Single-thread approach
 */
public class SingleTaskQueue<T> implements TaskAcceptor<T> {
    private final TaskExecutor<T> taskExecutor;

    public SingleTaskQueue(TaskExecutor<T> taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void push(T task) throws IllegalArgumentException, InterruptedException {
        try {
            taskExecutor.execute(task);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void signalEndOfData() {
        // Do nothing
    }
}
