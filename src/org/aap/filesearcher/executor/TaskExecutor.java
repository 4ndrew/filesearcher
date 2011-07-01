/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher.executor;

/**
 * Executor for task.
 * @param <T> Type of tasks for this executor.
 */
public interface TaskExecutor<T> {
    /**
     * Execute task of specified type.
     * @param task Task
     * @throws Exception if any error occurs
     */
    void execute(T task) throws Exception;
}
