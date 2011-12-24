/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher.executor;

/**
 * Executor for task.
 *
 * @param <T> Type of tasks for this executor.
 */
public interface TaskExecutor<T> {
    /**
     * Just initialize buffer for internal algorithm
     *
     * @return Buffer or null if no buffer required
     */
    Object initializeBuffer();

    /**
     * Execute task of specified type.
     *
     * @param task           Task
     * @param executorBuffer Buffer which is created by {@link #initializeBuffer()}, helpful with multi-threaded
     *                       approach. Each thread creates own buffer.
     * @throws Exception if any error occurs
     */
    void execute(T task, Object executorBuffer) throws Exception;
}
