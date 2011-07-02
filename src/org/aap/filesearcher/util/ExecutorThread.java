/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher.util;

import org.aap.filesearcher.executor.impl.TaskRunner;

/**
 * Simple thread executor class which provides its task runner.
 */
public class ExecutorThread<T> extends Thread {
    private final TaskRunner<T> taskRunner;
    public ExecutorThread(TaskRunner<T> taskRunner, String s) {
        super(taskRunner, s);
        this.taskRunner = taskRunner;
    }

    public TaskRunner<T> getTaskRunner() {
        return taskRunner;
    }
}
